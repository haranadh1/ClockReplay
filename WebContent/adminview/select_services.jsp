<%@page import="com.crp.common.*"%>
<%@page import="com.crp.sqldb.*"%>
<%@page import="java.util.*"%>
<%@page import="java.sql.*"%>
<%@page import="net.sf.json.*"%>
<%
DBStore ds = new DBStore();
ds.connect();
String json=ds.getJsonDoc();
ResultSet rs = ds.select("nodes");
ds.disconnect();
CRPConfig crp = new CRPConfig();
JSONArray cap = crp.getCaptureInfo(json);
JSONArray rep = crp.getReplayInfo(json);
while (rs.next()) {
String nd=rs.getString(1);
out.println("<p class=\"node\">");
out.println("<h3 class=\"heading\">"+nd+"<!--<input type=\"checkbox\" name=\"node\" value=\""+nd+"\"/>--></h3>");
out.println("<table class=\"tab\"><tr>");
out.println("<td valign=\"top\" style=\"border:1px solid grey;\">");
out.println("<table>");
out.println("<tr><td class=\"serv_head\">Captures</td></tr>");
for (int i = 0; i < cap.size(); i++) {					
JSONObject ja=(JSONObject)cap.get(i);
if(ja.get("host").equals(nd)){
out.println("<tr><td><input type=\"checkbox\" name=\"capture\" value='{\"host\":\""+nd+"\",\"name\":\""+ja.get("name")+"\",\"port\":\""+ja.get("port")+"\"}' />"+ja.get("name")+"</td></tr>");
}				
}
out.println("</table>");
out.println("</td>");
out.println("<td valign=\"top\" style=\"border:1px solid grey;\">");			
out.println("<table>");
out.println("<tr><td class=\"serv_head\">Replays</td></tr>");
for (int i = 0; i < rep.size(); i++) {				
JSONObject ja=(JSONObject)rep.get(i);
if(ja.get("host").equals(nd)){
out.println("<tr><td><input type=\"checkbox\" name=\"replay\" value='{\"host\":\""+nd+"\",\"name\":\""+ja.get("name")+"\",\"port\":\""+ja.get("port")+"\"}' />"+ja.get("name")+"</td></tr>");
}				
}
out.println("</table>");
out.println("</td>");
out.println("</tr></table>");
out.println("</p>");
}
%>