package com.epimorphics.lda.shortnames;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.epimorphics.jsonrdf.*;
import com.epimorphics.lda.renderers.JSONRenderer;
import com.hp.hpl.jena.rdf.model.Property;

public class CompleteReadContext {

    static Logger log = LoggerFactory.getLogger(JSONRenderer.class);

	public static ReadContext create( final Context context, final Map<String, String> uriToName ) {
		return new ReadContext() {
			
			Map<Property, ContextPropertyInfo> buffer = new HashMap<Property, ContextPropertyInfo>();
			
			@Override public boolean isSortProperties() {
				return true;
			}
			
			@Override public String getURIfromName(String code) {
				log.debug( "readContext: getURIfromName unexpectedly called." );
				return context.getURIfromName(code);
			}
			
			@Override public ContextPropertyInfo getPropertyByName(String name) {
				log.debug( "readContext: getpropertyByName unexpectedly called." );
				return context.getPropertyByName(name);
			}
			
			@Override public String getNameForURI(String uri) {
//				log.debug( "readContext: unusually, getNameForURI called." );
				return uriToName.get(uri);
			}
			
			@Override public String getBase() {
				return context.getBase();
			}
			
			@Override public String forceShorten(String uri) {
//				log.debug( "readContext: unusually, forceShorten called." );
				return context.forceShorten(uri);
			}
			
			/**
			    Changes to ContextPropertyInfos don't get pushed into the
			    "real" map, just into a clone in this local map. 
			*/
			@Override public ContextPropertyInfo findProperty(Property p) {
				ContextPropertyInfo result = buffer.get(p);
				if (result == null) {				
					String shortName = uriToName.get(p.getURI());
					ContextPropertyInfo original = context.findProperty( p, shortName );
					buffer.put( p, result = original.clone() );
				}
				return result;
			}
		};
	}
}
