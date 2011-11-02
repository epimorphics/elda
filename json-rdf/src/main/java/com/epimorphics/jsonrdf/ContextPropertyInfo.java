package com.epimorphics.jsonrdf;

import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.vocabulary.OWL;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

/** 
 	Sub interface used to describe a mapped property 
*/
public class ContextPropertyInfo implements Comparable<ContextPropertyInfo>, Cloneable {
	
    protected final String uri;
    
    protected String name;
    protected boolean multivalued = false;
    protected boolean hidden = false;
    protected boolean structured = false;
    
    /**
        Clone this Prop -- used by Context.clone() to avoid updating
        a shared Context object.
    */
    @Override public ContextPropertyInfo clone() {
    	try { 
    		return (ContextPropertyInfo) super.clone();
		} catch (CloneNotSupportedException e) {
			throw new RuntimeException( "Cannot happen." );
		}
    }
    
    public boolean isHidden() {
        return hidden;
    }
	public void setHidden(boolean hidden) {
        this.hidden = hidden;
    }

    public void setStructured(boolean b) {
		this.structured = b;			
	}
    
	public boolean isStructured() {
		return structured;
	}

    protected String type = null;
    protected Property p;
    
    public ContextPropertyInfo(String uri, String name) {
        this.uri = uri;
        this.name = name;
    }
    
    /** The absolute URI of the property */
    public String getURI() {
        return uri;
    }
    
    /** The shortened name to use in serialization */
    public String getName() {
        return name;
    }
    
    /** True if the property should be treated as multi-valued */
    public boolean isMultivalued() {
        return multivalued;
    }
    
    public void setName(String name) {
        this.name = name;
    }

    public void setMultivalued(boolean multivalued) {
        this.multivalued = multivalued;
    }

    public void setType(String type) {
        this.type = type;
    }
    
    /** Record the type of a sample value, if there is a clash with prior type then default to rdfs:Resource */
    public void addType(RDFNode value) {
        if (type == null) {
            type = decideType(value);
        } else {
            String ty = decideType(value);
            if ( ! this.type.equals(ty) ) {
                if (TypeUtil.isSubTypeOf(ty, type)) {
                    // Current type is OK
                } if (TypeUtil.isSubTypeOf(type, ty)) {
                    type = ty;
                } else {
                    // Generalize type
                    type = RDFS.Resource.getURI();
                }
            }
        }
    }
    
    private String decideType(RDFNode value) {
        if (value instanceof Resource) {
            if (RDFUtil.isList(value) || value.equals(RDF.nil)) {
                return RDF.List.getURI();
            } else {
                return OWL.Thing.getURI();
            }
        } else {
            Literal l = (Literal)value;
            String ty = l.getDatatypeURI();
            if (ty == null || ty.isEmpty()) {
                return RDFUtil.RDFPlainLiteral;
            } else {
                return ty;
            }
        }
    }

    /** Returns the assumed range of the property as a URI. Values with particular
     * significance for the serialization are rdfs:Resource, rdfs:List and xsd:* */
    public String getType() {
        return type; 
    }
    
    /** Get the corresponding RDF property, may cache */
    public Property getProperty(Model m) {
        if (p == null) {
            p = m.getProperty(uri);
        }
        return p;
    }

    /**
     * Compare on names to permit sorting.
     */
    @Override public int compareTo(ContextPropertyInfo o) {
       return name.compareTo(o.name);     
    }
}