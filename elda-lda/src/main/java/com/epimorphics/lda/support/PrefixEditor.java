package com.epimorphics.lda.support;

import java.util.*;

/**
    Edits the prefixes of strings (in the motivating case, URIs)
    by replacing them with different strings as specified by a
    collection of (from, to) pairs.

*/
public class PrefixEditor {

	boolean sorted = false;
	final List<PrefixEditor.FromTo> renamings = new ArrayList<PrefixEditor.FromTo>();
	
	public PrefixEditor() {
		// no body at present
	}
	
	public PrefixEditor set(String from, String to) {
		renamings.add( new FromTo( from, to ) );
		sorted = false;
		return this;
	}
	
	public boolean isEmpty() {
		return renamings.isEmpty();
	}
	
	/**
	    rename returns the renamed subject, unchanged if no FromTo's apply.
	*/
	public String rename( String subject ) {
		sortRenamingsIfNecessary();
		for (PrefixEditor.FromTo ft: renamings) {
			String renamed = ft.rename( subject );
			if (renamed != null) return renamed;
		}
		return subject;
	}
	
	@Override public boolean equals(Object other) {
		return other instanceof PrefixEditor && same( (PrefixEditor) other );
	}
	
	@Override public int hashCode() {
		return renamings.hashCode();
	}
	
	@Override public String toString() {
		return "<PrefixEditor " + renamings + ">";
	}
	
	private boolean same(PrefixEditor other) {
		sortRenamingsIfNecessary();
		return renamings.equals(other.renamings);
	}

	private void sortRenamingsIfNecessary() {
		if (sorted == false) {
			Collections.sort( renamings, compareFromTo );
			sorted = true;
		}
	}

	private static final Comparator<PrefixEditor.FromTo> compareFromTo = new Comparator<PrefixEditor.FromTo>() {

		@Override public int compare(PrefixEditor.FromTo a, PrefixEditor.FromTo b) {
			return -a.from.compareTo( b.from );
		}
	};
	
	static class FromTo {
		String from;
		String to;
		
		FromTo( String from, String to ) {
			this.from = from;
			this.to = to;
		}

		@Override public String toString() {
			return "<From: " + from + " To: " + to + ">";
		}
		
		@Override public boolean equals(Object other) {
			return other instanceof FromTo && same( (FromTo) other );
		}
		
		private boolean same(FromTo other) {
			return from.equals(other.from) && to.equals(other.to);
		}

		@Override public int hashCode() {
			return from.hashCode() + to.hashCode();
		}
		
		public String rename(String subject) {
			if (subject.startsWith( from )) {
				return to + subject.substring( from.length() );
			}
			return null;
		}
	}

	
}