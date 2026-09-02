/*
    See lda-top/LICENCE (or https://raw.github.com/epimorphics/elda/master/LICENCE)
    for the licence for this software.
  
    Created by Dave Reynolds, 14 Dec 2014, as part of SAPI
    (c) Copyright 2014 Epimorphics Limited
    $Id$
*/

package com.epimorphics.lda.support;

import com.epimorphics.lda.query.QueryParameter;
import com.epimorphics.util.NameUtils;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicLong;

/**
 * A Filter that can be added to filter chain to log all incoming requests and
 * the corresponding response (with response code and execution time). Assigns a
 * simple request number to each request and includes that in the response headers
 * for diagnosis. Not robust against restarts but easier to work with than UUIDs.
 */
public class LogRequestFilter implements Filter {

    /**
     * The response header used for the ID of this request/response.
     */
    public static final String X_RESPONSE_ID = "X-Response-Id";

    public static final String X_REQUEST_ID = "X-Request-Id";

    static final Logger log = LoggerFactory.getLogger(LogRequestFilter.class);

    protected static AtomicLong queryCount = new AtomicLong(0);

    protected String ignoreIfMatches = null;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        ignoreIfMatches = filterConfig.getInitParameter("com.epimorphics.lda.logging.ignoreIfMatches");
    }

    static boolean useID = "true".equals(System.getenv("ELDA_USE_ID"));

    @Override
    public void doFilter
            (ServletRequest request
                    , ServletResponse response
                    , FilterChain chain
            ) throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String query = httpRequest.getQueryString();
        String path = httpRequest.getRequestURI();
        String fullPath = path + (query == null ? "" : "?" + query);

        boolean logThis = ignoreIfMatches == null || !path.matches(ignoreIfMatches);

        // log.info(">> path " + path + " matches " + ignoreIfMatches + ": " + path.matches(ignoreIfMatches));
        // log.info(">> does " + fullPath + " log -- " + (logThis ? "yes" : "no"));

        if (logThis == false) {
            chain.doFilter(request, response);
        } else {

            String ID = null;

            String headerID = httpRequest.getHeader(X_REQUEST_ID);
            String paramID = httpRequest.getParameter(QueryParameter._QUERY_ID);

            if (useID) {
                if (ID == null) ID = paramID;
                if (ID == null) ID = headerID;
            }

            if (ID != null) {
                MDC.put(MDCParam.RequestId.name, ID);
                httpResponse.addHeader(X_RESPONSE_ID, ID);
            }
            MDC.put(MDCParam.RequestUri.name, fullPath);
            MDC.put(MDCParam.RequestStatus.name, RequestStatus.Received.name);

            log.info("Request {}", fullPath);
            MDC.put(MDCParam.RequestStatus.name, RequestStatus.Processing.name);
            long startTime = System.currentTimeMillis();
            chain.doFilter(request, response);
            long endTime = System.currentTimeMillis();
            int status = getStatus(httpResponse);
            long requestTimeMs = endTime - startTime;
            MDC.put(MDCParam.StatusCode.name, "" + status);
            MDC.put(MDCParam.RequestTime.name, "" + requestTimeMs);
            MDC.put(MDCParam.RequestStatus.name, RequestStatus.Completed.name);

            log.info("Response {} {} {}"
                    , fullPath
                    , status < 0 ? "(status unknown)" : "" + status
                    , NameUtils.formatDuration(requestTimeMs)
            );

            for (MDCParam param: MDCParam.values()) {
                MDC.remove(param.name);
            }
        }
    }

    // The check for NoSuchMethodError is because Tomcat6 doesn't have a
    // getStatus() in its HttpServletResponse implementation, and we have
    // users still on Tomcat 6. -1 is a "no not really" value.
    private int getStatus(HttpServletResponse httpResponse) {
        try {
            return httpResponse.getStatus();
        } catch (NoSuchMethodError e) {
            return -1;
        }
    }

    @Override
    public void destroy() {
    }
}