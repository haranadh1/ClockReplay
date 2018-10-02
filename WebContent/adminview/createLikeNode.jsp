<%@page import="com.crp.common.*"%>
<%@page import="com.crp.sqldb.*"%>
<%@page import="java.util.*"%>
<%@page import="java.sql.*"%>
<%
		String host1=(String)request.getParameter("host1");
		String host2=(String)request.getParameter("host");
		if(host2.equals("--Select Node--")||host2.length()==0){
		%><jsp:forward page="closebox.jsp?msg=Node should not be empty or null"/><%
		}else{
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
				System.out.println("JSON: "+json);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		CRPConfig cc=new CRPConfig();
		HashMap<String,String> hm1=cc.getHostCaptureData(json, host1);
		HashMap<String,String> hm2=cc.getHostReplayData(json, host1);
	
		Object[] str1=hm1.keySet().toArray();
		for(Object o1:str1){
			json=cc.addCaptureService(json, hm1.get(o1), host2, (String)o1);
		}

		Object[] str2=hm2.keySet().toArray();
		for(Object o2:str2){
			json=cc.addReplayService(json, hm2.get(o2), host2, (String)o2);
		}
		ds.updateJsonDoc(json);
		ds.disconnect();
		}
%>
<jsp:include page="viewNodes.jsp"/>
<jsp:include page="closebox.jsp?msg=Node has been created"/>
