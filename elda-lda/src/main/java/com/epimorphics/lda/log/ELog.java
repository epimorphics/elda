/*
    See lda-top/LICENCE (or https://raw.github.com/epimorphics/elda/master/LICENCE)
    for the licence for this software.
    
    (c) Copyright 2015 Epimorphics Limited
*/

package com.epimorphics.lda.log;

import org.slf4j.Logger;

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

	public static String getSeqID() {
		String id = seqID.get();
		return id == null ? "" : id;
	}

	public static void setSeqID(String id) {
		seqID.set(id);
	}

//	public static void warn(Logger log, String format, Object ... args) {
//		if (log.isWarnEnabled()) 
//			log.warn(String.format("[%s]: " + format, withSeqId(args)));
//	}

//	public static void debug(Logger log, String format, Object ... args) {
//		if (log.isDebugEnabled()) 
//			log.debug(String.format("[%s]: " + format, withSeqId(args)));
//	}
	
//	public static void info(Logger log, String format, Object ... args) {
//		if (log.isInfoEnabled()) {	
//			log.info(String.format("[%s]: " + format, withSeqId(args)));
//		}
//	}
	
//	public static void error(Logger log, String format, Object ... args) {
//		if (log.isErrorEnabled()) 
//			log.error(String.format("[%s]: " + format, withSeqId(args)));
//	}
	
	private static Object[] withSeqId(Object[] args) {
		Object [] extended = new Object[args.length + 1];
		System.arraycopy(args, 0, extended, 1, args.length);
		extended[0] = ELog.getSeqID(); 
		return extended;
	}

	public static String message(String message, Object... args) {
		return String.format("[%s]: " + message, withSeqId(args));
	}

}
