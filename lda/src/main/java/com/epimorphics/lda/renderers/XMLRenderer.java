package com.epimorphics.lda.renderers;

import com.epimorphics.lda.core.APIResultSet;

public class XMLRenderer implements Renderer {

	public static final String XML_MIME = "text/xml";
	
	@Override public String getMimeType() {
		return XML_MIME;
	}

	@Override public String render( APIResultSet results ) {
		return "<oops>Not Implemented Yet</oops>\n";
	}
}
