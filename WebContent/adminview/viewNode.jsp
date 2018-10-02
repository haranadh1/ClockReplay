<%@page import="com.crp.common.*"%>
<%@page import="com.crp.sqldb.*"%>
<%@page import="java.util.*"%>
<html>
<head>
<link rel="stylesheet" type="text/css" href="../tab.css">
<style type="text/css">
div#top_menu span{
padding:2px;
}

div#top_menu{
align:center;
width:250;
display:block;
height:50px;
text-align:center;
font-size:15px;
background-color:#FFFAF1;
margin:15px;
}

div#top_menu a.linker{
padding: 2px;
display:block;
float:left;
background-color:#dddddd;
border:3px solid #c1c1c1;
}
</style>
<script type="text/javascript">
$(document).ready(function(){
 $('.tab> tbody > tr:odd').css("background-color", "#FCF6CF");
 $('.tab> tbody > tr:even').css("background-color", "#CCF6CC");
$("a#create").click(function() {	
	$("div#thebox").css("display","block");
	});
});
</script>
<script type="text/javascript">
	function show_prompt()
	{
	var name=prompt("Enter New Node Name","");
	if (name!=null && name!="")
	  {
	   document.location.href="createLikeNode.jsp?host="+name;
	  }
	}

	function show_confirm()
	{
	var r=confirm("Are you sure!");
	if (r==true)
 	 {
	  //document.getElementById('botright').style.display= 'block';
	  document.location.href="delNodeInd.jsp?host=<%=request.getParameter("host")%>";
	  }
	}
</script>

</head>
<body>
<h1 align="center"><b><u><%=request.getParameter("host")%></u></b></h1>
<div align="center">
<div id="top_menu">
<!--onclick="show_prompt()"-->
<a   id="create"><img src="../images/like_ic.png"/><span style="cursor:pointer">Create Like</span></a>
<a   id="del" onclick="show_confirm()"><img src="../images/minus_ic.gif"/><span style="cursor:pointer">Delete Node</span></a>
</div>
<div id="thebox" style="display:none;">
<form action="createLikeNode.jsp">
<input type="hidden" name="host1" value="<%=request.getParameter("host")%>"/>
Node:&nbsp;&nbsp;&nbsp;
<input type="text" name="host2" value=""/><br><br>
<input type="submit" name="ok" value="OK"/>
<input type="reset" name="cancel" value="cancel">
</form>
</div>
<%
		String host=request.getParameter("host");
		DBStore db=new DBStore();
		db.connect();
		String doc =db.getJsonDoc();
		db.disconnect();
		CRPConfig cc=new CRPConfig();		
		HashMap<String,String> hm1=cc.getHostCaptureData(doc, host);
		HashMap<String,String> hm2=cc.getHostReplayData(doc, host);
		
		out.print("<table class=\"tab\" width=300><caption><h2>Capture Services</h2></caption>");
		out.print("<tr><th>Port<th>Capture Service  </tr>");
		Iterator<String> key1=hm1.keySet().iterator();
		while(key1.hasNext()){
		String s=(String)key1.next();
		if(hm1.get(s)!=null){
		out.print("<tr>");
		out.print("<td> "+s+" <td> "+hm1.get(s));
		out.print("</tr>");	
		}	
		}
		
		out.print("</table>");
		out.print("<hr>");
		out.print("<table class=\"tab\" width=300><caption><h2>Replay Services</h2></caption>");
		out.print("<tr><th>Port<th>Replay Service  </tr>");
		Iterator<String> key2=hm2.keySet().iterator();
		while(key2.hasNext()){
		String s=(String)key2.next();
		if(hm2.get(s)!=null){
		out.print("<tr>");
		out.print("<td> "+s+" <td> "+hm2.get(s));
		out.print("</tr>");	
		}	
		}
		System.out.print("</table>");
%>
</div>
</body>
</html>