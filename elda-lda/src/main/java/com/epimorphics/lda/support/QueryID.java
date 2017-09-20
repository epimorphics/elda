package com.epimorphics.lda.support;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.NDC;

public class QueryID implements Filter {

	@Override public void init(FilterConfig filterConfig) throws ServletException {
		// No init needed yet.
	}

	public static ThreadLocal<String> queryID = new ThreadLocal<String>();
	
	public static final String X_REQUEST_ID  = "X-Request-Id";

    public static final String X_RESPONSE_ID  = "X-Response-Id";
    
    public static final String QUERY_ID_PARAM  = "_query-id";
    
    public static final String DSAPI_INSTANCE = "DSAPI_INSTANCE";
	
	@Override public void doFilter(
		ServletRequest request
		, ServletResponse response
		, FilterChain chain
		)
		throws IOException, ServletException {
		HttpServletResponse httpResponse = (HttpServletResponse)response;
		HttpServletRequest httpRequest = (HttpServletRequest)request;
//		
		String ID = null;
		String headerID = httpRequest.getHeader(X_REQUEST_ID);
		String paramID = httpRequest.getParameter(QUERY_ID_PARAM);
//
		if (ID == null) ID = paramID;
		if (ID == null) ID = headerID;
		if (ID == null) ID = getDefaultId();
//
		httpResponse.setHeader(X_RESPONSE_ID, ID);
		setQueryId(ID);
		
		NDC.push(ID);
		chain.doFilter(request, response);
		NDC.pop();
	}
	
	public String getDefaultId() {
		String result = System.getProperty(DSAPI_INSTANCE);
		if (result == null) result = System.getenv(DSAPI_INSTANCE);
		if (result == null) result = "ANON." + System.currentTimeMillis();
		return result;
	}

	public static void setQueryId(String id) {
		queryID.set(id);
	}

	public static String getQueryId() {
		return queryID.get();
	}

	@Override public void destroy() {	
		// No action needed.
	}
}
