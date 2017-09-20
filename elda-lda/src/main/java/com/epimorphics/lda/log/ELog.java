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
		if (queryId != null) queryID.set(queryId);
	}
	
	public static String getQueryId() {
		return queryID.get();
	}

}
