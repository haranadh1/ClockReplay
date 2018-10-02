<%@page import="com.crp.common.*"%>
<%@page import="com.crp.sqldb.*"%>
<%@page import="java.util.*"%>
<%@page import="java.sql.*"%>
<%@page import="net.sf.json.*"%>
<%
DBStore ds = new DBStore();
ds.connect();
String json=ds.getJsonDoc();
ds.disconnect();
CRPConfig crp = new CRPConfig();
JSONArray rep = crp.getReplayInfo(json);
ArrayList<String> srvs=crp.getReplayNodes(json,"name");
HashSet hs = new HashSet();
hs.addAll(srvs);
srvs.clear();
srvs.addAll(hs);
if(srvs!=null){
for(String s:srvs)  {			
out.println("<p class=\"node\">");
out.println("<h3 class=\"heading\">"+s+"</h3>");
out.println("<table class=\"tab\">");
out.println("<tr>");
out.println("<td class=\"serv_head\">Host</td>");
out.println("<td class=\"serv_head\">Port</td>");
out.println("<td class=\"serv_head\"></td>");
out.println("</tr>");			
for (int i = 0; i < rep.size(); i++) {				
JSONObject ja=(JSONObject)rep.get(i);
if(ja.get("name").equals(s)){
out.println("<tr>");
out.println("<td class=\"serv_cont hst\">"+ja.get("host")+"</td>");
out.println("<td class=\"serv_cont prt\">"+ja.get("port")+"</td>");
out.println("<td><img src=\"../images/minus_ic.gif\" class=\"rmv\" id=\""+s+"\"></td>");
out.println("</tr>");	
}
}
out.println("</table>");
out.println("</p>");
}
}else{
out.print("No Replay Services..");
}
%>