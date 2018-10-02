<%@page import="com.crp.common.*"%>
<%@page import="com.crp.sqldb.*"%>
<%@page import="java.util.*"%>
<%@page import="java.sql.*"%>
<%
		String host=(String)request.getParameter("host1");
		if(host.equals("")||host.length()==0||host==null){
%><jsp:forward page="closebox.jsp?msg=Node should not be empty or null"/><%
		}
		DBStore db=new DBStore();
		db.connect();
		ResultSet rs=db.select("nodes");
		try {
			boolean t=true;
			while(rs.next()){
				if(rs.getString(1).equals(host)){
					t=false;
				db.disconnect();
%><jsp:forward page="closebox.jsp?msg=Node already exists"/><%
				}
			}
			if(t){
				ArrayList<String> a=new ArrayList<String>();
				a.add("node");
				ArrayList<String> b=new ArrayList<String>();
				b.add(host);
				db.insert("nodes", a, b);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		db.disconnect();
%>
<jsp:include page="viewNodes.jsp"/>
<jsp:include page="closebox.jsp?msg=Node has been added"/>

