<%--
  Copyright 1997-2008 Day Management AG
  Barfuesserplatz 6, 4001 Basel, Switzerland
  All Rights Reserved.

  This software is the confidential and proprietary information of
  Day Management AG, ("Confidential Information"). You shall not
  disclose such Confidential Information and shall use it only in
  accordance with the terms of the license agreement you entered into
  with Day.

  ==============================================================================

  Generic 404 error handler

  Important note:  
  Since Sling uses the user from the request (depending on the authentication
  handler but typically HTTP basic auth) to login to the repository and JCR/CRX
  will simply say "resource not found" if the user does not have a right to
  access a certain node, everything ends up in this 404 handler, both access
  denied ("401", eg. for non-logged in, anonymous users) and really-not-existing
  scenarios ("404", eg. logged in, but does not exist in repository).
  
--%><%
%><%@ page session="false" %><%
%><%@ page import="
    java.net.URLEncoder,
    org.apache.sling.api.scripting.SlingBindings,
    org.apache.sling.engine.auth.Authenticator,
    org.apache.sling.engine.auth.NoAuthenticationHandlerException,
    com.day.cq.wcm.api.WCMMode,
    org.apache.sling.runmode.RunMode,
    org.apache.sling.api.scripting.SlingScriptHelper" %><%!

    private boolean isAnonymousUser(HttpServletRequest request) {
        return request.getAuthType() == null
            || request.getRemoteUser() == null;
    }

    private boolean isBrowserRequest(HttpServletRequest request) {
        // check if user agent contains "Mozilla" or "Opera"
        final String userAgent = request.getHeader("User-Agent");
        return userAgent != null
            && (userAgent.indexOf("Mozilla") > -1
                || userAgent.indexOf("Opera") > -1);
    }
    
%><%@taglib prefix="sling" uri="http://sling.apache.org/taglibs/sling/1.0" %><%
%><sling:defineObjects /><%
//SlingBindings bindings = (SlingBindings) request.getAttribute("org.apache.sling.api.scripting.SlingBindings");
//SlingScriptHelper sling = bindings.getSling();

RunMode runmode = sling.getService(org.apache.sling.runmode.RunMode.class);
    String[] runmodes = runmode.getCurrentRunModes();
    boolean isPublish = false;
    String [] expectedRunModes = {"publish"};
    if(runmode.isActive(expectedRunModes)) {
    isPublish = true;
}

if(!isPublish) {
    // decide whether to redirect to the (wcm) login page, or to send a plain 404
    if (isAnonymousUser(request)
            && isBrowserRequest(request)) {
        
        Authenticator auth = sling.getService(Authenticator.class);
        if (auth != null) {
            try {
                auth.login(request, response);
                
                // login has been requested, nothing more to do
                return;
            } catch (NoAuthenticationHandlerException nahe) {
                bindings.getLog().warn("Cannot login: No Authentication Handler is willing to authenticate");
            }
        } else {
            bindings.getLog().warn("Cannot login: Missing Authenticator service");
        }
        
    }
}

    // get here if authentication should not take place or if
    // no Authenticator service is available or if no
    // AuthenticationHandler is willing to authenticate
    // So we fall back to plain old 404/NOT FOUND    
%><%@include file="default.jsp"%>