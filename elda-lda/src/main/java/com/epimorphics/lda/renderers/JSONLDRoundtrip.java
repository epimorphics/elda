package com.epimorphics.lda.renderers;

import java.io.ByteArrayInputStream;

import com.epimorphics.lda.vocabularies.API;
import com.epimorphics.lda.vocabularies.ELDA_API;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.vocabulary.DCTerms;
import com.hp.hpl.jena.vocabulary.XSD;

public class JSONLDRoundtrip {
	
	static final Property ANY = null;
	
	static final Property others = ResourceFactory.createProperty(ELDA_API.NS + "others");
	static final Property version = ResourceFactory.createProperty(ELDA_API.NS + "version");
	static final Property meta = ResourceFactory.createProperty("eh:/vocab/fixup/meta");

	public JSONLDRoundtrip() {
		
	}
	
	public void check(final Model model, final Model objectModel, byte[] bytes) {
		Model reconstituted = ModelFactory
			.createDefaultModel()
			.read(new ByteArrayInputStream(bytes), "", "JSON-LD")
			;
			
		reconstituted.removeAll(ANY, DCTerms.format, ANY);
		reconstituted.removeAll(ANY, version, ANY);
		reconstituted.removeAll(ANY, meta, ANY);	
		
		Model given = normaliseLiterals(model), recon = normaliseLiterals(reconstituted);
		restitchItemLists(given, recon);
		
		if (recon.isIsomorphicWith(given)) {
			
			// System.err.println(">> Hooray, edited reconstitution isomorphic with original [after canonisation]");
			
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
	}
	
	private void restitchItemLists(Model given, Model recon) {
		Statement G = given.listStatements(ANY, API.items, ANY).toList().get(0);
		Resource page = G.getSubject();
	//
		Statement S = recon.listStatements(ANY, API.items, ANY).toList().get(0);
		Resource items = S.getObject().asResource();
		S.remove();
		recon.add(page, API.items, items);
		recon.removeAll(ANY, others, ANY);
	}

	private Model normaliseLiterals(Model m) {
		Model result = ModelFactory.createDefaultModel();
		for (StmtIterator it = m.listStatements(); it.hasNext();) {
			result.add(normaliseLiterals(it.next()));
		}
		return result;
	}

	private Statement normaliseLiterals(Statement s) {
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
}
