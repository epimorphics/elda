package com.epimorphics.lda.exceptions;

import java.util.List;
import java.util.Set;

import com.epimorphics.util.CollectionUtils;

public class ReusedShortnameException extends RuntimeException {

	private static final long serialVersionUID = 1L;
	
	public final List<One> problems;

	public ReusedShortnameException( String shortName, Set<String> uris ) {
		this( new One( shortName, uris ) );
	}

	public ReusedShortnameException(One one) {
		this( CollectionUtils.list( one ) );
	}
	
	public ReusedShortnameException( List<One> problems ) {
		super( message( problems ) );
		this.problems = problems;
	}

	private static String message( List<One> problems ) {
		StringBuilder sb = new StringBuilder();
		sb.append( "there are " ).append( problems.size() ).append( " shortname conflicts:\n" );
		for (One p: problems) {
			sb.append( "\n  " ).append( p.shortName ).append( " is bound to: " ).append( p.uris ).append( "\n" );
		}
		return sb.toString();
	}
	
	public static class One {
		
		protected String shortName;
		protected Set<String> uris;
		
		public One( String shortName, Set<String> uris ) {
			this.shortName = shortName;
			this.uris = uris;
		}
	}
}
