package com.epimorphics.lda.log;

import org.slf4j.Logger;

import com.epimorphics.lda.restlets.RouterRestlet;

public class ELog {

	public static void warn(Logger log, String format, Object ... args) {
		if (log.isWarnEnabled()) 
			log.warn(String.format("[%s]: " + format, withSeqId(args)));
	}

	public static void debug(Logger log, String format, Object ... args) {
		if (log.isDebugEnabled()) 
			log.debug(String.format("[%s]: " + format, withSeqId(args)));
	}
	
	public static void info(Logger log, String format, Object ... args) {
		if (log.isInfoEnabled()) {	
			log.info(String.format("[%s]: " + format, withSeqId(args)));
		}
	}
	
	public static void error(Logger log, String format, Object ... args) {
		if (log.isErrorEnabled()) 
			log.error(String.format("[%s]: " + format, withSeqId(args)));
	}
	
	private static Object[] withSeqId(Object[] args) {
		Object [] extended = new Object[args.length + 1];
		System.arraycopy(args, 0, extended, 1, args.length);
		extended[0] = RouterRestlet.getSeqID(); 
		return extended;
	}

}
