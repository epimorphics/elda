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
}
