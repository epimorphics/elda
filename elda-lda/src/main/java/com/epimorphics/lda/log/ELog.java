/*
    See lda-top/LICENCE (or https://raw.github.com/epimorphics/elda/master/LICENCE)
    for the licence for this software.
    
    (c) Copyright 2015 Epimorphics Limited
*/

package com.epimorphics.lda.log;

/**
	Elog is a wrapper for (part of) the log interface. It injects the
	current thread's seqID at the beginning of the log message.
*/
public class ELog {

	/**
		Each thread is given a seqID to hold the request ID allocated
		by the log filter.
	*/
	public static ThreadLocal<String> seqID = new ThreadLocal<String>();
	public static ThreadLocal<String> queryID = new ThreadLocal<String>();

	public static String getSeqID() {
		String id = seqID.get();
		return id == null ? "" : id;
	}

	public static void setSeqID(String seqId) {
		seqID.set(seqId);
	}
	
	public static void setQueryId(String queryId) {
		queryID.set(queryId);
	}
	
	public static String getQueryId() {
		return queryID.get();
	}
	
	private static Object[] withSeqId(Object[] args) {
		Object [] extended = new Object[args.length + 1];
		System.arraycopy(args, 0, extended, 1, args.length);
		extended[0] = ELog.getSeqID(); 
		return extended;
	}
	
	private static Object[] withQueryId(String queryID, Object[] args) {
		Object [] extended = new Object[args.length + 2];
		System.arraycopy(args, 0, extended, 2, args.length);
		extended[0] = getSeqID(); 
		extended[1] = queryID;
		return extended;
	}

	public static String message(String message, Object... args) {
		String q = queryID.get();
//		System.err.println(">> message: query ID = '" + q + "'");
		if (q == null) {
			return String.format("[%s]: " + message, withSeqId(args));		
		} else {
			return String.format("[%s.%s]: " + message,  withQueryId(q, args));
		}
	}

}
