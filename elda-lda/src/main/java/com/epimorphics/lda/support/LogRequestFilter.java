/*
    See lda-top/LICENCE (or https://raw.github.com/epimorphics/elda/master/LICENCE)
    for the licence for this software.
  
    Created by Dave Reynolds, 14 Dec 2014, as part of SAPI
    (c) Copyright 2014 Epimorphics Limited
    $Id$
*/

package com.epimorphics.lda.support;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicLong;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.epimorphics.lda.log.ELog;
import com.epimorphics.lda.query.QueryParameter;
import com.epimorphics.util.NameUtils;

/**
 * A Filter that can be added to filter chain to log all incoming requests and
 * the corresponding response (with response code and execution time). Assigns a 
 * simple request number to each request and includes that in the response headers
 * for diagnosis. Not robust against restarts but easier to work with than UUIDs.
 */
public class LogRequestFilter implements Filter {
    
    /**
    	The response header used for the ID of this request/response.
    */
    public static final String X_RESPONSE_ID  = "X-Response-Id";
        
    public static final String X_REQUEST_ID  = "X-Request-Id";
    
    static final Logger log = LoggerFactory.getLogger( LogRequestFilter.class );
    
    protected static AtomicLong queryCount = new AtomicLong(0);

    protected String ignoreIfMatches = null;
    
    @Override public void init(FilterConfig filterConfig) throws ServletException {
    	ignoreIfMatches = filterConfig.getInitParameter("com.epimorphics.lda.logging.ignoreIfMatches");
    }

    @Override
    public void doFilter
    	( ServletRequest request
    	, ServletResponse response
    	, FilterChain chain
    	) throws IOException, ServletException 
    {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        
        String query = httpRequest.getQueryString();
        String path = httpRequest.getRequestURI();
        boolean logThis = ignoreIfMatches == null || !path.matches(ignoreIfMatches);
    //
        if (logThis) {
        	
        	String ID = null;
        	
        	String headerID = httpRequest.getHeader(X_REQUEST_ID);
        	String paramID = httpRequest.getParameter(QueryParameter._QUERY_ID);
        	
        	if (ID == null) ID = paramID;
        	if (ID == null) ID = headerID;
        	if (ID == null) ID = generateID(httpRequest);
        	
        	ELog.setQueryId(ID);
        	
	        long requestCount = queryCount.incrementAndGet();	        
	        String seqId = Long.toString(requestCount);
	        
	        ELog.setSeqID(seqId);
	        
	        log.info(String.format
	        	( "Request  [%s, %s] : %s"
	        	, seqId
	        	, ID
	        	, path) + (query == null ? "" : ("?" + query))
	        	);
	        
			httpResponse.addHeader(X_RESPONSE_ID, ID);			
	        
			long startTime = System.currentTimeMillis();
	        chain.doFilter(request, response);
	        long endTime = System.currentTimeMillis();
	        
	        int status = getStatus(httpResponse);
	        String statusString = status < 0 ? "(status unknown)" : "" + status;
	        
			log.info(String.format
				( "Response [%s, %s] : %s (%s)"
				, seqId
				, ID
				, statusString
	            , NameUtils.formatDuration(endTime - startTime) ) 
	            );
			
	    } else {
	        chain.doFilter(request, response);
	    }
    }

    private String generateID(HttpServletRequest req) {
		// return UUID_V1.generate().toString();
		return req.getLocalAddr();
	}

	// The check for NoSuchMethodError is because Tomcat6 doesn't have a
    // getStatus() in its HttpServletResponse implementation, and we have
    // users still on Tomcat 6. -1 is a "no not really" value.
	private int getStatus(HttpServletResponse httpResponse) {
		try { return httpResponse.getStatus(); }
		catch (NoSuchMethodError e) { return -1; }
	}

    @Override
    public void destroy() {
    }

}