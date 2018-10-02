<%@page import="com.crp.common.*"%>
<%@page import="com.crp.sqldb.*"%>
<%@page import="java.util.*"%>
<%@page import="java.sql.*"%>
<%
	String json="";
	DBStore ds=new DBStore();
	ds.connect();
	ResultSet rs=ds.select("config_tab1");		
	try {
		while(rs.next()){
			json=rs.getString(2);
		}
	} catch (SQLException e) {
		e.printStackTrace();
	}
	CRPConfig cc=new CRPConfig();
	String name=(String)request.getParameter("name");
	String host=(String)request.getParameter("host");
	String port=(String)request.getParameter("port");
	if(name.equals("")||name.length()==0||host.equals("--Select Host--")||port.equals("")||port.length()==0){
%><jsp:forward page="closebox.jsp?msg=Provide valid data"/><%
	}else{
	String nwJson=cc.addCaptureService(json, name, host, port);
	ds.updateJsonDoc(nwJson);
	}
	ds.disconnect();
%>
<jsp:include page="captures2.jsp"/>
<jsp:include page="closebox.jsp?msg=Capture Service has been added."/>
