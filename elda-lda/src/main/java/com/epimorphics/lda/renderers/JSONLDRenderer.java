package com.epimorphics.lda.renderers;

import java.io.*;
import java.util.*;

import com.epimorphics.jsonrdf.ContextPropertyInfo;
import com.epimorphics.jsonrdf.ReadContext;
import com.epimorphics.lda.bindings.Bindings;
import com.epimorphics.lda.core.APIEndpoint;
import com.epimorphics.lda.core.APIResultSet;
import com.epimorphics.lda.shortnames.CompleteContext.Mode;
import com.epimorphics.lda.shortnames.*;
import com.epimorphics.lda.support.Times;
import com.epimorphics.util.MediaType;
import com.github.jsonldjava.core.*;
import com.github.jsonldjava.impl.TurtleRDFParser;
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
public class JSONLDRenderer implements Renderer {

	final MediaType mt;
    final APIEndpoint ep;
    
	public JSONLDRenderer(Resource config, MediaType mt, APIEndpoint ep, ShortnameService sns) {
		this.ep = ep;
		this.mt = mt;
	}

	@Override public MediaType getMediaType(Bindings ignored) {
		return mt;
	}

	@Override public Mode getMode() {
		return Mode.PreferLocalnames;
	}

	@Override public BytesOut render(Times t, Bindings rc, final Map<String, String> termBindings, APIResultSet results) {
        final Model model = results.getMergedModel();
		ShortnameService sns = ep.getSpec().getAPISpec().getShortnameService();
        final ReadContext context = CompleteReadContext.create(sns.asContext(), termBindings );        

        final RDFParser parser = new TurtleRDFParser();
        ByteArrayOutputStream it = new ByteArrayOutputStream();
        model.write(it, "TTL");
        final String modelString = it.toString();

        return new BytesOutTimed() {

			@Override protected void writeAll(OutputStream os) {
				try {
					Writer w = new OutputStreamWriter(os, "UTF-8");
					Object json = JsonLdProcessor.fromRDF(modelString, parser);
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
