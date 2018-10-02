<%=request.getParameter("project_name")%><br>
<%=request.getParameter("data_size")%><br>
<%=request.getParameter("admin")%><hr>
<%
try{
String s1[]=request.getParameterValues("users");
for(String x1:s1)
out.print(x1+"<br>");
out.print("<hr>");
String s2[]=request.getParameterValues("capture");
for(String x2:s2)
out.print(x2+"<br>");
out.print("<hr>");
String s3[]=request.getParameterValues("replay");
for(String x3:s3)
out.print(x3+"<br>");
out.print("<hr>");
}catch(Exception e){out.print("Validate data..");}
%>