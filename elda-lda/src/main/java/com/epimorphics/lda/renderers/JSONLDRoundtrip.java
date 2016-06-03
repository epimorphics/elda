package com.epimorphics.lda.renderers;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import com.epimorphics.lda.log.ELog;
import com.epimorphics.lda.vocabularies.API;
import com.epimorphics.lda.vocabularies.ELDA_API;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.vocabulary.DCTerms;
import com.hp.hpl.jena.vocabulary.XSD;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JSONLDRoundtrip {

	static Logger log = LoggerFactory.getLogger(JSONLDRenderer.class);
	
	static final Property ANY = null;
	
	static final Property others = ResourceFactory.createProperty(ELDA_API.NS + "others");
	static final Property version = ResourceFactory.createProperty(ELDA_API.NS + "version");

	public JSONLDRoundtrip() {
	}
	
	public boolean check(final Model model, final Model objectModel, byte[] bytes) {
		Model reconstituted = ModelFactory
			.createDefaultModel()
			.read(new ByteArrayInputStream(bytes), "", "JSON-LD")
			;
			
		reconstituted.removeAll(ANY, DCTerms.format, ANY);
		reconstituted.removeAll(ANY, version, ANY);
		reconstituted.removeAll(ANY, ELDA_API.meta, ANY);	
		
		Model given = normaliseLiterals(model), recon = normaliseLiterals(reconstituted);
		restitchItemLists(given, recon);
		
		if (recon.isIsomorphicWith(given)) {
			
			return true;
			
		} else {
			
			System.err.println(">> YOUO");
			Say("ALAS, canonised models are not isomorphic");
			Say("original has " + given.size() + " statements,");
			Say("reconstituted has " + recon.size() + " statements.");
			
			Model common = recon.intersection(given);
			
			Say("there are " + common.size() + " common statements.");
			
			Model G = given.difference(common), R = recon.difference(common);
			
			SayGraph(G, "given");
			SayGraph(R, "reconstituted");
			return false;			
		}		
	}
	
	protected void Say(String what) {
		log.warn(ELog.message(what));
	}
	
	protected void SayGraph(Model m, String title) {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		log.warn(ELog.message("%s -----------------", title));
		log.warn(ELog.message(bos.toString()));
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
