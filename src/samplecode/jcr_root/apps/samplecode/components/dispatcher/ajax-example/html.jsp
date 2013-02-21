<%--
This script retrieves {resource-path}.ssi.html via ajax call.  This script
demonstrates how ajax could be leveraged to help create a personalized site
that is cacheable.
--%><%@include file="/libs/foundation/global.jsp"%><%
%><%@ page import="org.apache.commons.codec.digest.DigestUtils" %><%
String resourcePath = resource.getPath();
String divId = DigestUtils.md5Hex(resourcePath);
%><cq:includeClientLib categories="granite.jquery"/>
<div id="<%= divId %>"></div>
<script>
        jQuery(document).ready(function(){
                jQuery("#<%= divId %>").load("<%= resourcePath %>.ssi.html");
        });
</script>
