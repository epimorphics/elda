package com.epimorphics.lda.renderers;

import java.io.*;
import java.util.*;

import com.epimorphics.jsonrdf.*;
import com.epimorphics.lda.bindings.Bindings;
import com.epimorphics.lda.core.APIEndpoint;
import com.epimorphics.lda.core.APIResultSet;
import com.epimorphics.lda.shortnames.CompleteContext.Mode;
import com.epimorphics.lda.shortnames.*;
import com.epimorphics.lda.support.Times;
import com.epimorphics.lda.vocabularies.ELDA_API;
import com.epimorphics.util.MediaType;
import com.github.jsonldjava.core.*;
import com.github.jsonldjava.impl.TurtleTripleCallback;
import com.github.jsonldjava.jena.JenaJSONLD;
import com.github.jsonldjava.jena.JenaRDFParser;
import com.github.jsonldjava.utils.JsonUtils;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.shared.WrappedException;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;

/**
	A renderer into JSON-LD.

	The renderer is a wrapping of com.github.jsonld-java. It supplies
	the LDA shortnames and property types to json-ld and generates
	"compact" JSON. It is not particularly human-readable as JSON goes
	and has little in common with the LDA JSON layout. It is expected
	that consumers of JSON-LD will use libraries to make traversal and
	inspection straightforward.
	
*/
public class JSONLD_Renderer implements Renderer {

	public static final String RESULTS = ELDA_API.NS + "results";
	
	public static final Property pRESULTS = ResourceFactory.createProperty(RESULTS);
	
	final MediaType mt;
    final APIEndpoint ep;
    final boolean jsonUsesISOdate; // not currently used
    
	public JSONLD_Renderer(Resource config, MediaType mt, APIEndpoint ep, ShortnameService sns, boolean jsonUsesISOdate) {
		this.ep = ep;
		this.mt = mt;
		this.jsonUsesISOdate = jsonUsesISOdate;
	}

	@Override public MediaType getMediaType(Bindings ignored) {
		return mt;
	}

	@Override public Mode getMode() {
		return Mode.PreferLocalnames;
	}
	
	static boolean byHand = true;
	
	static { 
		
		JenaJSONLD.init();
		
	}
	
	static final String someBytes = "{\n\n '@context': {'isPartOf': 'http://purl.org/dc/terms/isPartOf', 'rest': 'http://www.w3.org/1999/02/22-rdf-syntax-ns#rest', 'playTimeMinutes': {";

	static final String someNiceBytes = "{}";
	
	static final String mtc = "{ \"@context\" : {\"isPartOf\" : \"http://purl.org/dc/terms/isPartOf\"}, \"format\": \"linked-data-api\"}";
	
	static final String twople = "\"meta\": {\"@id\": \"http://localhost:8080/standalone/again/games.json-ld\", \"startIndex\": ​1, \"type\":" +
			"\n	[{\"@id\": \"http://purl.org/linked-data/api/vocab#Page\"}]"
			;
	
	static final String example =
			joinup
				( "\"@context\": {\"name\": \"eh:/name\"}"
				, "\"@id\": \"http://dbpedia.org/resource/John_Lennon\""
				, "\"name\": \"John Lennon\""
				, "\"born\": \"1940-10-09\""
				, "\"spouse\": \"http://dbpedia.org/resource/Cynthia_Lennon\""		
				);
	
	static String joinup(String ...elements ) {
		StringBuilder sb = new StringBuilder();
		sb.append("{\n");
		String comma = ",";
		for (String x: elements) {
			sb.append(x).append(comma).append("\n");
		}
		sb.append("\"hack\": \"for parsing\"}\n");
		return sb.toString();
	}
		
	@Override public BytesOut render(Times t, Bindings rc, final Map<String, String> termBindings, final APIResultSet results) {
		final Model model = results.getMergedModel();
		final Model objectModel = results.getModels().getObjectModel();
		ShortnameService sns = ep.getSpec().getAPISpec().getShortnameService();
		final ReadContext context = CompleteReadContext.create(sns.asContext(), termBindings );        
        final Resource root = results.getRoot().inModel(model);

		if (byHand) {
			return new BytesOutTimed() {
				
				@Override protected void writeAll(OutputStream os) {
					ByteArrayOutputStream keepos = new ByteArrayOutputStream();
					try {
						System.err.println(">> writing model with " + model.size() + " triples.");
						Writer w = new OutputStreamWriter(keepos, "UTF-8");
						JSONWriterFacade jw = new JSONWriterWrapper(w, true);						
						JSONLD_Composer c = new JSONLD_Composer(model, root, context, termBindings, jw);
						c.renderItems(results.getResultList());
						w.flush();						
					//
						byte[] bytes = keepos.toByteArray();
						os.write(bytes);
						
						ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
						
						Model reconstituted = ModelFactory.createDefaultModel();
												
						reconstituted.read(new ByteArrayInputStream(bytes), "", "JSON-LD");
						
						if (!objectModel.containsAll(reconstituted)) {
							System.err.println(">> ARGH");
							System.err.println("reconstituted model contains statements not in object model");
							for (StmtIterator it = reconstituted.listStatements(); it.hasNext();) {
								Statement s = it.nextStatement(); 
								if (!s.getPredicate().equals(pRESULTS))
									if (!model.contains(s)) {
										System.err.println(">> eg: " + s);
								}
							}
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
		

        final RDFParser parser = new JenaRDFParser(); 
        
//        System.err.println(">> model as Turtle");
//        String [] lines = modelString.split("\n");
//        int n = 1;
//        for (String line: lines) {
//        	String number = ("" + (10000 + n++)).substring(1);
//        	System.err.println(number + " " + line);
//        }

        return new BytesOutTimed() {

			@Override protected void writeAll(OutputStream os) {
				try {
					Writer w = new OutputStreamWriter(os, "UTF-8");
					Object json = JsonLdProcessor.fromRDF(model, parser);
		            Object ldContext = makeContext(context, model, termBindings);
					Object compacted = JsonLdProcessor.compact(json, ldContext, new JsonLdOptions());					
					JsonUtils.writePrettyPrint(w, compacted);
					w.flush();
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
	
	static private Object makeContext(ReadContext cx, Model model, Map<String, String> termBindings) {
		Set<String> present = new HashSet<String>();
		ExtendedIterator<Triple> triples = model.getGraph().find(Node.ANY, Node.ANY, Node.ANY);
		while (triples.hasNext()) {
			Triple t = triples.next();
			if (t.getSubject().isURI()) present.add(t.getSubject().getURI());
			if (t.getPredicate().isURI()) present.add(t.getPredicate().getURI());
			if (t.getObject().isURI()) present.add(t.getObject().getURI());
		}
	//
		Map<String, Object> result = new HashMap<String, Object>();
		for (Map.Entry<String, String> e: termBindings.entrySet()) {
			String URI = e.getKey(), shortName = e.getValue();
			if (present.contains(URI)) {
				ContextPropertyInfo cp = cx.findProperty(ResourceFactory.createProperty(URI));
				String type = cp.getType();
				if (type == null) {
					result.put(shortName, URI);
				} else {			
					Map<String, String> struct = new HashMap<String, String>();
					struct.put("@id", URI);
					struct.put("@type", type);
					result.put(shortName, struct);
				}
			}
		}
		return result;
	}

	@Override public String getPreferredSuffix() {
		return "json-ld";
	}
}
