package com.epimorphics.jsonrdf;

import com.epimorphics.vocabs.NsUtils;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.vocabulary.*;

/** 
 	Sub interface used to describe a mapped property 
*/
public class ContextPropertyInfo implements Comparable<ContextPropertyInfo>, Cloneable {
	    
	protected final String uri;
    protected boolean multivalued = false;
    protected String name;
    protected boolean hidden = false;
    protected boolean structured = false;    
    protected String type = null;
    protected Property p;

    @Override public boolean equals( Object other) {
    	return other instanceof ContextPropertyInfo && same( (ContextPropertyInfo) other );
    }
    
    private boolean same(ContextPropertyInfo other) {
    	return
    		uri.equals( other.uri )
    		&& multivalued == other.multivalued
    		&& name.equals( other.name )
    		&& hidden == other.hidden
    		&& structured == other.structured
    		&& eq(p, other.p)
    		&& eq( type, other.type )
    		;
    }
    
    @Override public String toString() {
    	return 
    		"<CPI short: " + name
    		+ ", multivalued: " + multivalued
    		+ ", hidden: " + hidden
    		+ ", structured: " + structured
    		+ ", type: " + type
    		+ ", property: " + p
    		+ ", uri: " + uri
    		+ ">"
    		;
    }

	public String diff(ContextPropertyInfo b) {
		StringBuffer result = new StringBuffer();
		result.append( "diff[" + name + "] " );
		if (!name.equals(b.name)) result.append( " name: " ).append(name).append(" vs ").append(b.name);  
		if (!eq(type, b.type)) result.append( " type: " ).append(type).append(" vs ").append(b.type);  
		if (multivalued != b.multivalued) result.append( " multivalued: " ).append(multivalued).append(" vs ").append( b.multivalued );  
		if (!uri.equals(b.uri)) result.append( " uri: " ).append(uri).append(" vs ").append(b.uri);  
		if (hidden != b.hidden) result.append( " hidden: " ).append(hidden).append(" vs ").append(b.hidden);  
		if (structured != b.structured) result.append( " structured: " ).append(structured).append(" vs ").append(b.structured);  
		if (!eq(p, b.p)) result.append( " p: " ).append(p).append(" vs ").append(b.p);  
		return result.toString();
	}
    
    @Override public int hashCode() {
    	return uri.hashCode();
    }
    
    private boolean eq(Object a, Object b) {
		return a == null ? b == null : a.equals(b);
	}

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
    
    public String getSerialisationName() {
    	if (NsUtils.isMagic( NsUtils.getNameSpace(uri ) )) return NsUtils.getLocalName( uri );
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
    // Only called from JSON renderer, would like to eliminate its use if possible ...
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
