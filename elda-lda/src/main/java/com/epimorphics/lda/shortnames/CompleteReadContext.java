package com.epimorphics.lda.shortnames;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.epimorphics.jsonrdf.Context;
import com.epimorphics.jsonrdf.ContextPropertyInfo;
import com.epimorphics.jsonrdf.ReadContext;
import com.epimorphics.lda.renderers.JSONRenderer;
import com.hp.hpl.jena.rdf.model.Property;

public class CompleteReadContext {

    static Logger log = LoggerFactory.getLogger(JSONRenderer.class);

	public static ReadContext create( final Context context, final Map<String, String> uriToName ) {
		return new ReadContext() {
			
			@Override public boolean isSortProperties() {
				return true;
			}
			
			@Override public String getURIfromName(String code) {
				log.warn( "readContext: getURIfromName unexpectedly called." );
				return context.getURIfromName(code);
			}
			
			@Override public ContextPropertyInfo getPropertyByName(String name) {
				log.warn( "readContext: getpropertyByName unexpectedly called." );
				return context.getPropertyByName(name);
			}
			
			@Override public String getNameForURI(String uri) {
				log.warn( "readContext: getNameForURI unexpectedly called." );
				return uriToName.get(uri);
				// return context.getNameForURI(uri);
			}
			
			@Override public String getBase() {
				return context.getBase();
			}
			
			@Override public String forceShorten(String uri) {
				log.warn( "readContext: forceShorten unexpectedly called." );
				return context.forceShorten(uri);
			}
			
			@Override public ContextPropertyInfo findProperty(Property p) {
				String shortName = uriToName.get(p.getURI());
				return context.findProperty( p, shortName );
			}
		};
	}
}
