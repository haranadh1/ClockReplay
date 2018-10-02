<%@page import="com.crp.common.*"%>
<%@page import="com.crp.sqldb.*"%>
<%@page import="java.util.*"%>
<%@page import="java.sql.*"%>
<%
		DBStore dbc=new DBStore();
		dbc.connect();
		try {
			ResultSet rs=dbc.select("nodes");
			while(rs.next()){
			        out.print("<option value=\""+rs.getString(1)+"\">"+rs.getString(1)+"</option>");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		dbc.disconnect();
%>