<%@page import="com.crp.common.*"%>
<%@page import="com.crp.sqldb.*"%>
<%@page import="java.util.*"%>
<%@page import="java.sql.*"%>
<%
		String host=(String)request.getParameter("host");
		String json="";
		DBStore ds=new DBStore();
		ds.connect();
		/*ArrayList<String> a=new ArrayList<String>();
		a.add("node");
		ArrayList<String> b=new ArrayList<String>();
		b.add(host2);
		ds.insert("nodes", a, b);*/
		ResultSet rs=ds.select("config_tab1");		
		try {
			while(rs.next()){
				json=rs.getString(2);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		CRPConfig cc=new CRPConfig();
		HashMap<String,String> hm1=cc.getHostCaptureData(json, host);
		System.out.println("JSON CAP:"+host+" : "+hm1);
		HashMap<String,String> hm2=cc.getHostReplayData(json, host);
		System.out.println("JSON Rep:"+host+" : "+hm2);
		Object[] str1=hm1.keySet().toArray();
		for(Object o1:str1){
			json=cc.removeCaptureService(json, hm1.get(o1), host, (String)o1);
		}

		Object[] str2=hm2.keySet().toArray();
		for(Object o2:str2){
			json=cc.removeReplayService(json, hm2.get(o2), host, (String)o2);
		}
		ds.updateJsonDoc(json);
		ds.disconnect();
%>
<jsp:include page="viewNodes.jsp"/>
<jsp:include page="closebox.jsp?msg=Node has been deleted."/>
