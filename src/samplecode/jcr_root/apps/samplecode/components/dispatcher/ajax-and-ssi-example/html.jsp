<%@include file="/libs/foundation/global.jsp"%><%
%><%@ page import="org.apache.commons.codec.digest.DigestUtils" %><%
%><%
String resourcePath = resource.getPath();

if(request.getHeader("X-Forwarded-For") != null) {

    %><!--#include virtual="<%= resourcePath %>.ssi.html" --><%

} else {

String divId = DigestUtils.md5Hex(resourcePath);
%><cq:includeClientLib categories="granite.jquery"/>
<div id="<%= divId %>"></div>
<script>
    jQuery(document).ready(function(){           
        jQuery("#<%= divId %>").load("<%= resourcePath %>.ssi.html");       
    });
</script>
<% } %>
