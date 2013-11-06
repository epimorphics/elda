package com.epimorphics.lda.renderers.velocity;

public class WrappedString {
	
	final String content;
	
	public WrappedString(String content) {
		this.content = content;
	}

	public String toString() {
		StringBuilder sb = new StringBuilder( content.length() * 10 / 9 );
		for (int i = 0; i < content.length(); i += 1) {
			char ch = content.charAt(i);
			if (ch == '&') sb.append( "&amp;" );
			else if (ch == '<') sb.append( "&lt;" );
			else sb.append(ch);
		}
		return sb.toString();
	}
	
	public String raw() {
		return content;
	}
	
	public String quotedURI() {
		StringBuilder sb = new StringBuilder(content.length() + 10);
		for (int i = 0; i < content.length(); i += 1) {
			char ch = content.charAt(i);
			if (ch == '#') sb.append( "%23" );
			else if (ch == '?') sb.append("%3F");
			else if (ch == '&') sb.append("%26");
			else sb.append(ch);
		}
		return sb.toString();
	}

	/**
	    The content with spaces inserted (a) in place of any run of '_'
	    characters (b) between a lower-case letter and an upper-case one.
	*/
	public WrappedString cut() {
		StringBuilder sb = new StringBuilder();
		char prev = 0;
		int startBig = 0;
		for (int i = 0; i < content.length(); i += 1) {
			char ch = content.charAt(i);
			boolean onUpper = Character.isUpperCase( ch );
			if (startBig > 0) {
				if (!onUpper) {
					if (i == startBig + 1) {
						sb.append( Character.toLowerCase( content.charAt(startBig) ) );
					} else {
						for (int j = startBig; j < i; j += 1) sb.append( content.charAt(j) );
						sb.append(' ');
					}
					sb.append( ch );
					startBig = 0;
				}
			} else if (ch == '_') {
				if (prev != '_') sb.append( ' ' );
			} else if (onUpper && Character.isLowerCase( prev )) {
				startBig = i;
				sb.append(' ');
			
			} else {
				sb.append( ch );
			}
			prev = ch;
		}
		return new WrappedString( sb.toString() );
	}
}
