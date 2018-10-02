<%@page import="com.crp.common.*"%>
<%@page import="com.crp.sqldb.*"%>
<%@page import="java.util.*"%>
<%@page import="java.sql.*"%>
<%
		String host=(String)request.getParameter("host2");
		if(host.equals("--Select Host--")){
%><jsp:forward page="closebox.jsp?msg=Choose a node"/><%		
		}else{
		DBStore db=new DBStore();
		db.connect();
		db.delete("nodes","node='"+host+"'");
		db.disconnect();
		}
%>
<jsp:include page="viewNodes.jsp"/>
<jsp:include page="closebox.jsp?msg=Node has been deleted"/>

