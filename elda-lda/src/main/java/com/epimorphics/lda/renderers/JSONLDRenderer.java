package com.epimorphics.lda.renderers;

import java.io.*;
import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.epimorphics.jsonrdf.*;
import com.epimorphics.lda.bindings.Bindings;
import com.epimorphics.lda.core.APIEndpoint;
import com.epimorphics.lda.core.APIResultSet;
import com.epimorphics.lda.log.ELog;
import com.epimorphics.lda.shortnames.CompleteContext.Mode;
import com.epimorphics.lda.shortnames.*;
import com.epimorphics.lda.support.Times;
import com.epimorphics.lda.vocabularies.API;
import com.epimorphics.lda.vocabularies.ELDA_API;
import com.epimorphics.util.MediaType;
import com.epimorphics.vocabs.OpenSearch;
import com.epimorphics.vocabs.XHV;
import com.github.jsonldjava.jena.JenaJSONLD;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.shared.WrappedException;
import com.hp.hpl.jena.vocabulary.*;

import static com.epimorphics.lda.renderers.JSONLDComposer.*;

/**
	A renderer into JSON-LD.

	The renderer is a wrapping of com.github.jsonld-java. It supplies
	the LDA shortnames and property types to json-ld and generates
	"compact" JSON. It is not particularly human-readable as JSON goes
	and has little in common with the LDA JSON layout. It is expected
	that consumers of JSON-LD will use libraries to make traversal and
	inspection straightforward.
	
*/
public class JSONLDRenderer implements Renderer {
	
	static { JenaJSONLD.init(); }

	static Logger log = LoggerFactory.getLogger(JSONLDRenderer.class);
	
	final MediaType mt;
    final APIEndpoint ep;
    final boolean jsonUsesISOdate; // not currently used
    final boolean checkRoundTrip;
    
	public JSONLDRenderer(Resource config, MediaType mt, APIEndpoint ep, ShortnameService sns, boolean jsonUsesISOdate) {
		this.ep = ep;
		this.mt = mt;
		this.jsonUsesISOdate = jsonUsesISOdate;
		this.checkRoundTrip = isCheckingRoundTrip(config);
	}

	@Override public MediaType getMediaType(Bindings ignored) {
		return mt;
	}

	@Override public Mode getMode() {
		return Mode.PreferLocalnames;
	}
	
	private boolean isCheckingRoundTrip(Resource config) {
		Statement check = config.getProperty(ELDA_API.checkJSONLDRoundTrip);
		return check == null ? true : check.getBoolean();
	}
		
	@Override public BytesOut render(Times t, Bindings rc, final Map<String, String> termBindings, final APIResultSet results) {
		final Model model = results.getMergedModel();
		final Model objectModel = results.getModels().getObjectModel();
		ShortnameService sns = ep.getSpec().getAPISpec().getShortnameService();
		final ReadContext context = CompleteReadContext.create(sns.asContext(), termBindings );        
        final Resource root = results.getRoot().inModel(model);

		return new BytesOutTimed() {
			
			@Override protected void writeAll(OutputStream os) {
				try {
					ByteArrayOutputStream bytesOut = new ByteArrayOutputStream();
				
					OutputStream s = (checkRoundTrip ? bytesOut : os);
					Writer w = new OutputStreamWriter(s, "UTF-8");
					JSONWriterFacade jw = new JSONWriterWrapper(w, true);						
					JSONLDComposer c = new JSONLDComposer(model, root, context, termBindings, jw);
					c.renderItems(results.getResultList());
					w.flush();	
					if (checkRoundTrip) {
						byte[] bytes = bytesOut.toByteArray();
						os.write(bytes);
						log.info(ELog.message("checking that JSON LD result round-trips"));
						checkRoundTripping(model, objectModel, bytes);
					}
										
				} catch (Throwable e) {
					throw new WrappedException(e);
				}
			}


			
			@Override protected String getFormat() {
				return getPreferredSuffix();
			}

			@Override public String getPoison() {
				return JSONRenderer.JSON_POISON;
			}
		};
		
	}

	@Override public String getPreferredSuffix() {
		return "json-ld";
	}
	
	static final Property ANY = null;


	private void checkObjectStatementsAppearInReconstitution(Model objectModel, Model reconstituted) {
		Model inCommon = objectModel.intersection(reconstituted);

		Model om = objectModel.difference(inCommon);
		Model rm = reconstituted.difference(inCommon);
		
		System.err.println(">> om size " + om.size());
		System.err.println(">> rm size " + rm.size());

		if (om.size() > 0) System.err.println(">> OOPS: missing object model statements.");
		
	}
	
	private void checkTermBindings(Model model, Model reconstituted) {
		Model A = getTermBindings(model), B = getTermBindings(reconstituted);
		if (A.isIsomorphicWith(B)) return;
		log.warn(ELog.message("JSON LD round-trip failed, term bindings not isomorphic."));
	}
	
	static final Property version = ResourceFactory.createProperty(ELDA_API.NS + "version");
	static final Property meta = ResourceFactory.createProperty("eh:/vocab/fixup/meta");

	private void checkRoundTripping(final Model model, final Model objectModel, byte[] bytes) {
		Model reconstituted = ModelFactory
			.createDefaultModel()
			.read(new ByteArrayInputStream(bytes), "", "JSON-LD")
			;
			
		reconstituted.removeAll(ANY, DCTerms.format, ANY);
		reconstituted.removeAll(ANY, version, ANY);
		reconstituted.removeAll(ANY, meta, ANY);
		
//		reconstituted.removeAll(ANY, JSONLDComposer.pMETA, ANY);
//		reconstituted.removeAll(ANY, JSONLDComposer.pOTHERS, ANY);
//		reconstituted.removeAll(ANY, JSONLDComposer.pRESULTS, ANY);
//
//		reconstituted.removeAll(ANY, DCTerms.hasPart, ANY);
//		reconstituted.removeAll(ANY, DCTerms.isPartOf, ANY);
//		
//		reconstituted.removeAll(ANY, API.definition, ANY);
//		reconstituted.removeAll(ANY, XHV.first, ANY);
//		reconstituted.removeAll(ANY, XHV.next, ANY);
//		reconstituted.removeAll(ANY, API.page, ANY);
//		reconstituted.removeAll(ANY, RDF.type, API.Page);
//		reconstituted.removeAll(ANY, RDF.type, API.ListEndpoint);
//		reconstituted.removeAll(ANY, RDF.type, API.ItemEndpoint);
//		reconstituted.removeAll(ANY, API.extendedMetadataVersion, ANY);
//		
//		reconstituted.removeAll(ANY, OpenSearch.itemsPerPage, ANY);
//		reconstituted.removeAll(ANY, OpenSearch.startIndex, ANY);		
		
		Model given = canonise(model), recon = canonise(reconstituted);
		
		if (recon.isIsomorphicWith(given)) {
			
			System.err.println(">> Hooray, edited reconstitution isomorphic with original [after canonisation]");
			
		} else {
			System.err.println(">> ALAS, canonised models are not isomorphic");
			System.err.println(">> original has " + given.size() + " statements,");
			System.err.println(">> reconstituted has " + recon.size() + " statements.");
			
			Model common = recon.intersection(given);
			
			System.err.println(">> there are " + common.size() + " common statements.");
			
			Model G = given.difference(common), R = recon.difference(common);
			
			System.err.println(">> G ===================================");
			G.write(System.err, "TTL");
			System.err.println(">> R ===================================");
			R.write(System.err, "TTL");
						
		}
		
		if (true) return;
		
		checkObjectStatementsAppearInReconstitution(objectModel, reconstituted);
		checkTermBindings(model, reconstituted);
		
		// if (true) return;
		
		if (!model.isIsomorphicWith(reconstituted)) {
			System.err.println(">> ALAS original and reconstituted models are not isomorphic:");
			System.err.println(">> original has " + model.size() + " statements,");
			System.err.println(">> reconstituted has " + reconstituted.size() + " statements.");
			
			Model inCommon = model.intersection(reconstituted);
			Model onlyOriginal = model.difference(inCommon);
			Model onlyReconstituted = reconstituted.difference(inCommon);
			
			List<Statement> removeTheseOriginals = new ArrayList<Statement>();
			List<Statement> removeTheseReconstitutions = new ArrayList<Statement>();
			
			Statement itemListStatement = onlyOriginal.listStatements(ANY, API.items, ANY).nextStatement();
			Resource items = itemListStatement.getSubject();
			Resource itemList = itemListStatement.getObject().asResource();
			Resource X = itemList;
			
			while (!itemList.equals(RDF.nil)) {
				Resource f = itemList.getPropertyResourceValue(RDF.first);
				Resource next = itemList.getPropertyResourceValue(RDF.rest);
				removeTheseOriginals.add(model.createStatement(itemList, RDF.first, f));
				removeTheseOriginals.add(model.createStatement(itemList, RDF.rest, next));
				itemList = next;
			}
			removeTheseOriginals.add(model.createStatement(items, API.items, X));
			
			for (StmtIterator it = onlyReconstituted.listStatements(); it.hasNext();) {
				Statement s = it.nextStatement();
				Property p = s.getPredicate();
				if (p.equals(JSONLDComposer.pOTHERS) || p.equals(JSONLDComposer.pRESULTS))
					removeTheseReconstitutions.add(s);
			}
			
//			for (StmtIterator it = onlyReconstituted.listStatements(ANY, API.termBinding, ANY); it.hasNext();) {
//				Statement tb = it.nextStatement();
//				Resource lap = tb.getObject().asResource();
//				removeTheseReconstitutions.add(tb);
//				for (StmtIterator lapit = lap.listProperties(); lapit.hasNext();) {
//					removeTheseReconstitutions.add(lapit.nextStatement());
//				}
//			}
			
			Model A = getTermBindings(model), B = getTermBindings(reconstituted);
			if (!A.isIsomorphicWith(B)) {
				System.err.println(">> OH BOTHER.");
				System.err.println(">> TermBindings: " + A.size() + " vs " + B.size() + " statements.");
				
				Set<String> namesForA = namesFor(A);
				Set<String> namesForB = namesFor(B);
				if (namesForA.equals(namesForB)) {
					System.err.println(">> names are the same");
				} else {
					System.err.println("A: " + namesForA);
					System.err.println("B: " + namesForB);
				}
				
				
				System.err.println(">> A ------------------------------------------------------");
				A.write(System.err, "TTL");
				System.err.println(">> B ------------------------------------------------------");
				B.write(System.err, "TTL");
				System.err.println(">> @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");
				System.err.println(">> ISO: " + A.isIsomorphicWith(B));
			}
						
			
			onlyReconstituted.remove(removeTheseReconstitutions);
			onlyReconstituted.removeAll(ANY, DCTerms.format, ANY);
			onlyReconstituted.removeAll(ANY, meta, ANY);
			onlyReconstituted.removeAll(ANY, version, ANY);
			onlyReconstituted.removeAll(ANY, JSONLDComposer.pMETA, ANY);
			onlyReconstituted.removeAll(ANY, JSONLDComposer.pOTHERS, ANY);
			onlyReconstituted.removeAll(ANY, JSONLDComposer.pRESULTS, ANY);
			

			onlyOriginal.remove(removeTheseOriginals);
			
			System.err.println(">> they have " + inCommon.size() + " statements in common:");
			System.err.println(">> so unique original statements number " + onlyOriginal.size() + ",");
			System.err.println(">> and unique reconstituted statements number " + onlyReconstituted.size() + ".");
			
			onlyReconstituted.write(System.err, "TTL");		
			
//			System.err.println(">> ONLY ORIGINAL");
//			onlyOriginal.write(System.err, "TTL");
//			
//			System.err.println(">> ONLY RECONSTITUTED");
//			onlyReconstituted.write(System.err, "TTL");
						
		}
		
		
//		boolean needsHeaderA = true;
//		for (StmtIterator it = reconstituted.listStatements(); it.hasNext();) {
//			Statement s = it.nextStatement();
////			System.err.println(">> reconstituted " + s);
//			if (isContentStatement(s))
//				if (!model.contains(s)) {
//					if (needsHeaderA) {
//						needsHeaderA = false;
//						log.warn(ELog.message("reconstituted model contains statements not in object model"));
//					}
//					log.warn(ELog.message("viz: %s", s));
//				}
//		}
//		
//		boolean needsHeaderB = true;
//		for (StmtIterator it = objectModel.listStatements(); it.hasNext();) {
//			Statement s = it.nextStatement(); 
//			if (isContentStatement(s))
//				if (!reconstituted.contains(s)) {
//					if (needsHeaderB) {
//						needsHeaderB = false;
//						log.warn(ELog.message("object model contains statements not reconstituted"));
//					}
//					log.warn(ELog.message("viz: %s", s));
//				}
//		}
	}


	private Set<String> namesFor(Model b) {
		Set<String> results = new HashSet<String>();
		for (StmtIterator it = b.listStatements(ANY, API.label, ANY); it.hasNext();) {
			results.add(it.nextStatement().getString());
		}
		return results;
	}

	private Model getTermBindings(Model m) {
		Model result = ModelFactory.createDefaultModel();
		for (StmtIterator it = m.listStatements(ANY, API.termBinding, ANY); it.hasNext();) {
			Statement tb = it.nextStatement();
			result.add(changeObjectLiteral(tb));
			Resource lap = tb.getObject().asResource();
			for (StmtIterator lapit = lap.listProperties(); lapit.hasNext();) {
				Statement s = lapit.nextStatement();
				result.add(changeObjectLiteral(s));
			}
		}
		return result;
	}

	private Model canonise(Model m) {
		Model result = ModelFactory.createDefaultModel();
		for (StmtIterator it = m.listStatements(); it.hasNext();) {
			result.add(canonise(it.next()));
		}
		return result;
	}

	private Statement canonise(Statement s) {
		RDFNode O = s.getObject();
		if (O.isLiteral()) 
			if (XSD.xstring.getURI().equals(O.asNode().getLiteralDatatypeURI())) {
				Model M = s.getModel();
				Resource S = s.getSubject();
				Property P = s.getPredicate();
				return M.createStatement(S,  P,  O.asNode().getLiteralLexicalForm());
			}
		return s;
	}
	
	private Statement changeObjectLiteral(Statement s) {
		Resource S = s.getSubject();
		Property P = s.getPredicate();
		Model M = s.getModel();
		RDFNode O = s.getObject();
		RDFNode L = changeLiteral(O, M);
		return M.createStatement(S,  P, L);
	}

	private RDFNode changeLiteral(RDFNode o, Model M) {
		if (o.isLiteral()) {
			Node ln = o.asNode();
			String dt = ln.getLiteralDatatypeURI();
			if (dt == null) return o;
			if (dt.equals(XSD.xstring.getURI())) {
				return M.createLiteral(ln.getLiteralLexicalForm());
			}
		} 
		return o;
	}
}
