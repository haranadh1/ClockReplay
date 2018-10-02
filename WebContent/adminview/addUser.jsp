<%@page import="nfolks.db.*"%>
<%@page import="java.util.*"%>
<%@page import="java.sql.*"%>
<%@page import="nfolks.security.*"%>
<%
			/*
			try {
			DBConnect dbc = new DBConnect();
			Connection con=dbc.getConnection();
			ArrayList<String> a1=new ArrayList<String>();
			a1.add("user_name");
			a1.add("pwd");
			ArrayList<String> a2=new ArrayList<String>();
			a2.add(request.getParameter("user_name"));
			a2.add(PasswordCreation.getHash(request.getParameter("pwd")));
			int k=dbc.insert("crp_users_db", a1, a2);
			}catch(Exception e){}
			*/
			
%>
<jsp:include page="viewUsers.jsp"/>
<jsp:include page="closebox.jsp?msg=User has been added."/>

