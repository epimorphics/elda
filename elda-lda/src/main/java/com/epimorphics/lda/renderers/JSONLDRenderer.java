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
import com.epimorphics.lda.vocabularies.ELDA_API;
import com.epimorphics.util.MediaType;
import com.github.jsonldjava.jena.JenaJSONLD;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.shared.WrappedException;
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
	
	private void checkRoundTripping(final Model model, final Model objectModel, byte[] bytes) {
		Model reconstituted = ModelFactory
				.createDefaultModel()
				.read(new ByteArrayInputStream(bytes), "", "JSON-LD")
				;
		
		if (!model.isIsomorphicWith(reconstituted)) {
			System.err.println(">> ALAS original and reconstituted models are not isomorphic:");
			System.err.println(">> original has " + model.size() + " statements,");
			System.err.println(">> reconstituted has " + reconstituted.size() + " statements.");
			
			Model inCommon = model.intersection(reconstituted);
			Model onlyOriginal = model.difference(inCommon);
			Model onlyReconstituted = reconstituted.difference(inCommon);
			
			System.err.println(">> they have " + inCommon.size() + " statements in common:");
			System.err.println(">> so unique original statements number " + onlyOriginal.size() + ",");
			System.err.println(">> and unique reconstituted statements number " + onlyReconstituted.size() + ".");
			
			System.err.println(">> ONLY ORIGINAL");
			onlyOriginal.write(System.err, "TTL");
			
			System.err.println(">> ONLY RECONSTITUTED");
			onlyReconstituted.write(System.err, "TTL");
						
		}
		
		
		boolean needsHeaderA = true;
		for (StmtIterator it = reconstituted.listStatements(); it.hasNext();) {
			Statement s = it.nextStatement();
//			System.err.println(">> reconstituted " + s);
			if (isContentStatement(s))
				if (!model.contains(s)) {
					if (needsHeaderA) {
						needsHeaderA = false;
						log.warn(ELog.message("reconstituted model contains statements not in object model"));
					}
					log.warn(ELog.message("viz: %s", s));
				}
		}
		
		boolean needsHeaderB = true;
		for (StmtIterator it = objectModel.listStatements(); it.hasNext();) {
			Statement s = it.nextStatement(); 
			if (isContentStatement(s))
				if (!reconstituted.contains(s)) {
					if (needsHeaderB) {
						needsHeaderB = false;
						log.warn(ELog.message("object model contains statements not reconstituted"));
					}
					log.warn(ELog.message("viz: %s", s));
				}
		}
	}
}
