/*
    See lda-top/LICENCE (or https://raw.github.com/epimorphics/elda/master/LICENCE)
    for the licence for this software.
    
    (c) Copyright 2011 Epimorphics Limited
    $Id$

    File:        Encoder.java
    Created by:  Dave Reynolds
    Created on:  21 Dec 2009
*/

package com.epimorphics.jsonrdf;

import static com.epimorphics.jsonrdf.RDFUtil.getLexicalForm;

import java.io.IOException;
import java.io.Writer;
import java.util.*;

import org.apache.jena.atlas.json.JsonObject;
import org.codehaus.jettison.json.JSONException;

import com.epimorphics.jsonrdf.impl.EncoderDefault;
import com.epimorphics.lda.vocabularies.API;
import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.util.OneToManyMap;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;
import com.hp.hpl.jena.util.iterator.Map1;
import com.hp.hpl.jena.vocabulary.RDF;

/**
 * Driver for encoding a set of RDF resources into JSON.
 * The serialization decisions are made (relative to a Context spec)
 * by an EncoderPlugin for which there is a default implementation.
 * 
 * @author <a href="mailto:dave@epimorphics.com">Dave Reynolds</a>
 * @version $Revision: $
 */
public class Encoder {
    
    protected static EncoderPlugin defaultPlugin = new EncoderDefault();
    
    public static Encoder getForOneResult( ReadContext c ) {
    	return new Encoder( defaultPlugin, c, true );
    }
    
    public static Encoder getForOneResult() {
    	return getForOneResult( new Context() );    	
    }
    
    /**
     * Return an encoder using the default rules and no Context, any
     * Context information will be generated on-the-fly.
     */
    public static Encoder get( ReadContext context ) {
        return get(defaultPlugin, context );
    }
    
    /**
     * Return an encoder using the specified rules and no Context, any
     * Context information will be generated on-the-fly.
     */
    public static Encoder get(EncoderPlugin rules) {
        return get(rules, new Context());
    }
    
    /**
     * Return an encoder using the specified rules and the specified 
     * base ontology.
     */
    public static Encoder get(EncoderPlugin rules, ReadContext fromOntology) {
        return new Encoder(rules, fromOntology, false );
    }

    /**
     * @param rules
     * @param context
     */
    private Encoder(EncoderPlugin rules, ReadContext context) {
    	this( rules, context, false );
    }

    /**
     * @param rules
     * @param context
     * @param oneResult true iff the LDA "result: object" style is required
     */
    private Encoder(EncoderPlugin rules, ReadContext context, boolean oneResult ) {
        this.rules = rules;
        this.context = context;
        this.oneResult = oneResult;
    }

    // Instance data
    protected final EncoderPlugin rules;
    protected final ReadContext context;
    protected final boolean oneResult;
    
    /**
     * Encode the whole of the given RDF model into the writer 
     * @param model The RDF to be encoded
     * @param writer The output writer, ideally platform neutral charset like UTF-8
     * @throws IOException 
     */
    public void encode(Model model, Writer writer) throws IOException {
        encode(model, null, writer);
    }
    
    /**
     * Encode the whole of the given RDF model into the writer 
     * @param model The RDF to be encoded
     * @return encoding as a JSON object
     */
    public JsonObject encode(Model model) {
        JSONWriterObject jwo = new JSONWriterObject();
        encode(model, null, jwo); 
        return jwo.getTopObject();
    }
    
    /**
     * Encode a list of resources from the given RDF model, only top level assertions and
     * bNode closures connected to them will be output.
     * @param model The RDF to be encoded
     * @param writer The output writer, ideally platform neutral charset like UTF-8
     * @param pretty set to true to pretty-print the json
     * @throws IOException 
     */
    public void encode(Model model, Writer writer, boolean pretty) throws IOException {
        encode(model, null, writer, pretty);
    }
    
    /**
     * Encode a list of resources from the given RDF model, only top level assertions and
     * bNode closures connected to them will be output.
     * @param model The RDF to be encoded
     * @param roots the root resources to be encoded
     * @param writer The output writer, ideally platform neutral charset like UTF-8
     * @throws IOException 
     */
    public void encode(Model model, List<Resource> roots, Writer writer) throws IOException {
        encode(model, roots, new JSONWriterWrapper(writer));
    }
    
    /**
     * Encode a list of resources from the given RDF model, only top level assertions and
     * bNode closures connected to them will be output.
     * @param model The RDF to be encoded
     * @param roots the root resources to be encoded
     * @return the JSONObject containing the encoding
     */
    public JsonObject encode(Model model, List<Resource> roots) {
        JSONWriterObject jwo = new JSONWriterObject();
        encode(model, roots, jwo);
        return jwo.getTopObject();
    }
    
    /**
     * Encode a list of resources from the given RDF model, only top level assertions and
     * bNode closures connected to them will be output.
     * @param model The RDF to be encoded
     * @param roots the root resources to be encoded
     * @param writer The output writer, ideally platform neutral charset like UTF-8
     * @param pretty set to true to pretty-print the json
     * @throws IOException 
     */
    public void encode(Model model, List<Resource> roots, Writer writer, boolean pretty) {
        encode(model, roots, new JSONWriterWrapper(writer, pretty));
    }

    protected void encode(Model model, List<Resource> roots, JSONWriterFacade jw) {
        EncoderInstance ei = new EncoderInstance(model, jw);
        ei.encodeSingleModelRoots(roots, false);
    }
    
    /**
     * Encode the given list of resources from the given model, plus any resources that those
     * roots reference - whether bNodes or URIs.
     * @param model The RDF to be encoded
     * @param roots the root resources from which encoding should start
     * @param writer The output writer, ideally platform neutral charset like UTF-8
     * @param pretty set to true to pretty-print the json
     */
    public void encodeRecursive(Model model, List<Resource> roots, Writer writer, boolean pretty) {
        encodeRecursive(model, roots, new JSONWriterWrapper( writer, pretty ));
    }
    
    /**
     * Encode the given list of resources from the given model, plus any resources that those
     * roots reference - whether bNodes or URIs.
     * @param model The RDF to be encoded
     * @param roots the root resources from which encoding should start
     * @param writer The output writer, ideally platform neutral charset like UTF-8
     * @throws IOException 
     */
    public void encodeRecursive(Model model, List<Resource> roots, Writer writer) {
        encodeRecursive(model, roots, new JSONWriterWrapper( writer ));
    }
    
    /**
     * Encode the given list of resources from the given model, plus any resources that those
     * roots reference - whether bNodes or URIs.
     * @param model The RDF to be encoded
     * @param roots the root resources from which encoding should start
     * @return JSONObject containing the encoding
     * @throws IOException 
     */
    public JsonObject encodeRecursive(Model model, List<Resource> roots) {
        JSONWriterObject jwo = new JSONWriterObject();
        encodeRecursive(model, roots, jwo);
        return jwo.getTopObject();
    }
    
    protected void encodeRecursive(Model model, List<Resource>roots, JSONWriterFacade jw) {
        EncoderInstance ei = new EncoderInstance(model, jw);
        ei.encodeSingleModelRoots(roots, true);
    }
    
    /**
     * Write out a collection of named graphs plus a default graph
     * @param dataset the collection of models
     * @param writer writer to output to
     * @throws IOException
     */
    public void encode(Dataset dataset, Writer writer) throws IOException {
        encode(dataset, new JSONWriterWrapper(writer));
    }
    
    /**
     * Write out a collection of named graphs plus a default graph
     * @param dataset the collection of models
     * @return ecoded dataset as a JSON object
     */
    public JsonObject encode(Dataset dataset) {
        JSONWriterObject jwo = new JSONWriterObject();
        try {
            encode(dataset, jwo);
        } catch (IOException e) {
            throw new EncodingException("Impossible IOException", e);
        }
        return jwo.getTopObject();
    }
    
    /**
     * Write out a collection of named graphs plus a default graph
     * @param dataset the collection of models
     * @param writer writer to output to
     * @param pretty set to true to pretty-print the json
     * @throws IOException
     */
    public void encode(Dataset dataset, Writer writer, boolean pretty) throws IOException {
        encode(dataset, new JSONWriterWrapper(writer, pretty));
    }
    
    /**
     * Write out a collection of named graphs plus a default graph
     * @param dataset the collection of models
     * @param jw JSON writer to output to
     * @throws IOException
     */
    protected void encode(Dataset dataset, JSONWriterFacade jw) throws IOException {
        EncoderInstance ei = new EncoderInstance(dataset.getDefaultModel(), jw);
        ei.startEncode();
        ei.encodeAll();
        ei.finishModelEncode();
        for (Iterator<String> i = dataset.listNames(); i.hasNext(); ) {
            String name = i.next();
            Model model = dataset.getNamedModel(name);
            ei.encodeNamedModel(name, model);
        }
        ei.finishEncode();
    }
    
    class EncoderInstance {
        protected Model model;
        protected JSONWriterFacade jw;
        protected List<Resource> roots = new ArrayList<Resource>();
        protected int bnodeCount = 1;
        protected Map<AnonId, Integer> bNodes = new HashMap<AnonId, Integer>();
        protected boolean startedGraphs = false;
        
        protected boolean recurseOverResources = false;
        protected static final boolean nestResources = true;     // Could be another config option
		protected static final boolean deferSharedBNodes = false;  // Could be another another config option
        
        // When nesting we need cycle detection
        protected Set<Resource> cycles;
        protected Set<Resource> noCycles;
        protected Map<Resource, Set<Resource>> visitedFrom;
        
        protected ArrayList<RDFList> topItemLists=null;
        protected HashSet<Resource>  topItems=null;
        protected long depth = 0;
        
        
        // When not nesting just track visits
        protected Set<Resource> encoded;
        
        public EncoderInstance(Model model, Writer w) {
            this.model = model;
            this.jw = new JSONWriterWrapper( w );
        }
        
        public EncoderInstance(Model model, JSONWriterFacade w) {
            this.model = model;
            this.jw = w;
        }

        /**
         * Encode a single model starting from the given roots.
         * @param roots The list of resources to write out (or at least start from), if this is null
         * then the whole model is written out.
         * @param recurse If true then all resources referenced from the roots will be written. 
         * @throws JSONException
         */
        void encodeSingleModelRoots(List<Resource> roots, boolean recurse) {
            if (recurse) {
                recurseOverResources = true;
                if (nestResources) {
                    cycles = new HashSet<Resource>();
                    noCycles = new HashSet<Resource>();
                    visitedFrom = new HashMap<Resource, Set<Resource>>();
                } else {
                    encoded = new HashSet<Resource>();
                }
            }
            setTopItems(roots);
            startEncode();
            encode(roots);
            finishModelEncode();
            finishEncode();
        }
        
		void setTopItems(List<Resource> roots) {
			ArrayList<RDFList> res = new ArrayList<RDFList>(10);
			HashSet<Resource> res2 = new HashSet<Resource>(10);
			
			if(roots!=null) for (Resource root : roots) {
				ExtendedIterator<Resource> i = root.listProperties(API.items)
						.mapWith(new Map1<Statement, Resource>() {
							@Override
							public Resource map1(Statement o) {
								return o.getResource();
							}
						});
				while (i.hasNext()) {
					Resource list = i.next();
					if (RDFUtil.isList(list))
						res.add(list.as(RDFList.class));
				}
				topItemLists = res;
				for (RDFList list : topItemLists) {
					ExtendedIterator<Resource> iter = list.iterator().mapWith(new Map1<RDFNode, Resource>(){

						@Override
						public Resource map1(RDFNode o) {
							return o.asResource();
						}
					});
					res2.addAll(iter.toList());
				}
			}
			topItems = res2;
		}
		
        /**
         * Test if the resource needs encoding (has any values, hasn't already been
         * encoded) and optionally mark as now-encoded
         */
        private boolean needEncodeResource(Resource r, boolean markAsEncoded) {
            if ( r.getModel().contains(r, null, (RDFNode)null) ) {
                if (recurseOverResources) {
                    if (nestResources) {
                        if (cycles.contains(r)) {
                            return false;
                        } else if(topItems!=null && topItems.contains(r) && depth!=2 ) {
                        	// Don't encode top level api:items except when encoding items
                        	// depth = 1 => root
                        	// depth = 2 => root.items (if present - if not topItems will be empty)
                        	return false;
                        } else if (noCycles.contains(r)) {
                            return true;
                        }
                        if (markAsEncoded) {
                            if (visitedFrom.containsKey(r)) {
                                cycles.add(r);
                                visitedFrom.remove(r);
                                return false;
                            } else {
                                // Update visit records for the cycles we are tracking
                                for (Set<Resource> s : visitedFrom.values())
                                    s.add(r);
                                visitedFrom.put(r, new HashSet<Resource>());
                            }
                        }
                        return true;
                    } else {
                        if (encoded.contains(r)) return false;
                        if (markAsEncoded) encoded.add(r);
                        return true;
                    }
                } else {
                    return true;
                }
            } else {
                // No values to encode
                return false;
            }
        }
        
        private void markVisitcompleted(Resource r) {
            if (recurseOverResources && nestResources) {
                visitedFrom.remove(r);
                noCycles.add(r);
            }
        }
        
        private void visitResource(Resource r) {
            if (recurseOverResources && !nestResources) {
                if (!encoded.contains(r)) {
                    roots.add(r);
                }
            }
        }
        
        private List<Resource> findRoots() {
            List<Resource> roots = new ArrayList<Resource>();
            for (ResIterator i = model.listSubjects(); i.hasNext(); ) {
                Resource r = i.next();
                if (r.isAnon() && model.contains(null, null, r))
                    continue;
                roots.add(r);
            }
            return roots;
        }
        
        private void encode( List<Resource> seedRoots ) {
            for ( Resource r : (seedRoots == null ? findRoots() : seedRoots) ) {
                encode(r);
            }
            while (!roots.isEmpty()) {
                List<Resource> newRoots = new ArrayList<Resource>( roots );
                // System.err.println( ">> " + roots + " :: " + oneResult );
                roots.clear();
                for (Resource r : newRoots) {
                	if (oneResult) jw.key( rules.encodebNodeId(bNodeIdFor(r)) ); 
                    encode(r);
                }                
            }
        }
        
        private void encodeAll()  {
            encode( findRoots() );
        }
        
        void encodeNamedModel(String name, Model model) {
            this.model = model;     // side effect but only used for checking roots
            if (!startedGraphs) {
                startedGraphs = true;
                rules.startNamedGraphs(jw);
            }
            rules.startNamedGraph(jw, name);
            rules.startResults(jw, oneResult );
            encodeAll();
            rules.endResults( jw, oneResult );
            rules.finishNamedGraph(jw);
        }
        
        private void startEncode() {
            rules.writeHeader(jw);
            rules.startResults(jw, oneResult );
        }
        
        private void finishModelEncode() {
            rules.endResults( jw, oneResult );
        }
        
        private void finishEncode() {
            if (startedGraphs) rules.finishNamedGraphs(jw);
            jw.endObject();
        }
        
        private void encode(Resource r) {
        	depth++;
            if (! needEncodeResource(r, true)) {
                if (r.isAnon()) {
                    // Case of an empty bNode, for URI nodes we will already have output URI reference
                    jw.object(); 
                    jw.endObject();
                } else {
                    jw.value( rules.encodeResourceURI(r.getURI(), context, false) );
                }
            } else {
            	jw.object();
	            encodeResource(r);
	            jw.endObject();
	            markVisitcompleted(r);
            }
            depth--;
        }

		private void encodeResource(Resource r) {
			if (r.isAnon()) {
                // Test if this is a root or single referenced bNode
                if ( isMultiplyReferencedbNode(r) ) {
                    int id = bNodeIdFor(r);
                    jw.key(rules.getPNResourceID()).value(rules.encodebNodeId(id));
                }
            } else {
                jw
                	.key(rules.getPNResourceID())
                	.value( rules.encodeResourceURI(r.getURI(), context, false))
                	;
            }
            
            OneToManyMap<Property, RDFNode> vals = buildPropertyValueMap(r);
            List<Property> props = getSortedProperties(vals);

            for (Property p : props) {
                ContextPropertyInfo prop = context.findProperty(p);
                if (!prop.isHidden()) writePropertyValues( vals, p, prop );
            }
		}

		private OneToManyMap<Property, RDFNode> buildPropertyValueMap(Resource r) {
			OneToManyMap<Property, RDFNode> vals = new OneToManyMap<Property, RDFNode>();
            for (StmtIterator si = r.listProperties(); si.hasNext(); ) {
                Statement s = si.next();
                vals.put(s.getPredicate(), s.getObject());
            }
			return vals;
		}

		private List<Property> getSortedProperties(OneToManyMap<Property, RDFNode> vals) {
			List<Property> props = new ArrayList<Property>(vals.keySet());
            if (context.isSortProperties()) {
                Collections.sort(props, new Comparator<Property>() {
                    @Override
                    public int compare(Property p1, Property p2) {
                        return context.findProperty(p1).compareTo(context.findProperty(p2));
                    }
                });
            }
			return props;
		}
		
        private void writePropertyValues( OneToManyMap<Property, RDFNode> vals, Property p, ContextPropertyInfo prop ) {                
        	Iterator<RDFNode> i = vals.getAll(p);
            boolean multi = prop.isMultivalued();
            boolean isStructured = prop.isStructured();
            RDFNode first = i.next();
            prop.addType(first);
            if (!i.hasNext() && !multi) {
                // just emit single value
            	jw.key(prop.getSerialisationName());
                emitNode(first, isStructured);
            } else {
                // Emit as array, do so with sorting
                List<RDFNode> nvals = new ArrayList<RDFNode>();
                nvals.add(first);
                while (i.hasNext()) nvals.add(i.next());
                Collections.sort(nvals, new Comparator<RDFNode>() {
                    @Override
                    public int compare(RDFNode arg0, RDFNode arg1) {
                        return getLexicalForm(arg0).compareTo(getLexicalForm(arg1));
                    }
                });
            	jw.key(prop.getSerialisationName());
                jw.array();
                for (RDFNode node : nvals) {
					emitNode(node, isStructured);
				}
                jw.endArray();
            }
        }
    
        private void emitNode(RDFNode valNode, boolean isStructured) {
            if (valNode.isLiteral()) {
            	rules.encodeLiteral( jw, isStructured, (Literal) valNode, context );
            } else {
                Resource r = (Resource)valNode;
                if (RDFUtil.isList(r)) {
                	jw.array();
                	while (!r.equals(RDF.nil)) {
            			Statement first = r.getProperty( RDF.first );
            			Statement rest = r.getProperty( RDF.rest );
            			if (first != null) emitNode( first.getObject(), isStructured );
            			if (rest == null) break;
            			r = rest.getResource();
                	}
                    jw.endArray();
                } else if (r.isAnon()) {
                    if (isMultiplyReferencedbNode(r) && deferSharedBNodes) {
                        if ( ! seenbNode(r)) {
                            roots.add(r);
                        }
                        jw.value( rules.encodebNodeId(bNodeIdFor(r)) );
                    } else {
                        encode(r);
                    }
                } else if (r.equals(RDF.nil)) {
                    jw.array(); jw.endArray();
                } else {
                    if (recurseOverResources && nestResources) {
                        encode(r);
                    } else {
                        visitResource(r);
                        jw.value( rules.encodeResourceURI(r.getURI(), context, false) );
                    }
                }
            }
        }

        private int bNodeIdFor(Resource r) {
            AnonId id = r.getId();
            Integer shortId = bNodes.get( id );
            if (shortId == null) {
                shortId = bnodeCount++;
                bNodes.put(id, shortId);
            }
            return shortId;
        }
        
        private boolean seenbNode(Resource r) {
            return bNodes.containsKey( r.getId() );
        }

        private boolean isMultiplyReferencedbNode(Resource r) {
            ResIterator ri = model.listSubjectsWithProperty(null, r);
            boolean multiRef = false;
            if (ri.hasNext()) {
                ri.next();
                multiRef = ri.hasNext();
            }
            ri.close();
            return multiRef;
        }
    }
}

