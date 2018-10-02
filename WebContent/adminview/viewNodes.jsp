<!DOCTYPE html>
<%@page import="com.crp.common.*"%>
<%@page import="com.crp.sqldb.*"%>
<%@page import="java.util.*"%>
<%@page import="java.sql.*"%>
<html lang="en">
	<head>
		<meta charset="utf-8">
		<title>jQuery UI Dialog - Animation</title>
		<link href="../css/imgbubbles.css" rel="stylesheet" type="text/css" />
		<link href="../css/bubble.css" rel="stylesheet" type="text/css" />
		<script type="text/javascript" src="../js/jquery-1.6.2.js"></script>
		<script type="text/javascript" src="../js/imgbubbles.js"></script>
		<style type="text/css">
			#main{
			display:block;
  			  /*set the div in the top right corner*/
  			margin-top:50px;
			}
			.toolbox{
			display:block;
  			  /*set the div in the top right corner*/
    			position:absolute;
  			top:20px;
   			right:40px;
   			width:200px;    
   			 /*give it some background and border
  			background:#eee;
 			border:1px solid #ddd;*/
			}
			div.close {
				display: block;
				/*set the div in the bottom right corner*/
				position: absolute;
				border-color: red;
				border-width: 2px;
				top: 0px;
				right: 0px;
				width: 25px;
			}
			
			div.img_div {

				image-align: center;
				width: 80px;
				float: obsolute;
			}
			div.text_div {

				text-align: center;
				font-size: 10px;
				width: 80px;
				float: obsolute;
			}
			div.host_div {

				width: 80px;
				float: left;
			}
			body {
				background-color: #fffaf1;
				font-size: 75.5%;
			}
			div#dialog {
				background-color: #FFFAF1;
			}
			div#top_menu span {
				padding: 5px;
			}
			div#top_menu {
				height: 50px;
				text-align: center;
				font-size: 20px;
				background-color: #FFFAF1;
				margin: 15px;
			}
			div#top_menu a.opener {
				padding: 2px;
				display: block;
				float: left;
				background-color: #dddddd;
				border: 3px solid #c1c1c1;
			}
			.blockbox{
			top:50px;
			left:25%;
			position:absolute;
			text-align:center;
			background-color:#ffffcc;
			padding:5px;
			border:2px solid #ffdd66;
			width:50%;
			height:30px;
			font-size:15px;
			font-weight:bold;
			border-radius:10px;
			}

		</style>	
		<script type="text/javascript">
		function getDetails(did){
		if(did=='addnode'){
		document.getElementById('delnode').style.display='none';
		document.getElementById(did).style.display='block';
		}
		if(did=='delnode'){
		document.getElementById('addnode').style.display='none';
		document.getElementById(did).style.display='block';
		}
		}
		
		</script>	
		<script type="text/javascript">
			jQuery(document).ready(function($) {
				$('ul#orbs').imgbubbles({
					factor : 1.75
				}) //add bubbles effect to UL id="orbs"
				$('ul#squares').imgbubbles({
					factor : 2.5
				}) //add bubbles effect to UL id="squares"
			})
		</script>
		<link rel="stylesheet" href="../Dialog/themes/base/jquery.ui.all.css">
		<script src="../Dialog/jquery-1.5.1.js"></script>
		<script src="../Dialog/ui/jquery.ui.core.js"></script>
		<script src="../Dialog/ui/jquery.ui.widget.js"></script>
		<script src="../Dialog/ui/jquery.ui.mouse.js"></script>
		<script src="../Dialog/ui/jquery.ui.button.js"></script>
		<script src="../Dialog/ui/jquery.ui.draggable.js"></script>
		<script src="../Dialog/ui/jquery.ui.position.js"></script>
		<script src="../Dialog/ui/jquery.ui.resizable.js"></script>
		<script src="../Dialog/external/jquery.bgiframe-2.1.2.js"></script>
		<script src="../Dialog/ui/jquery.ui.dialog.js"></script>
		<script src="../Dialog/ui/jquery.effects.core.js"></script>
		<script src="../Dialog/ui/jquery.effects.blind.js"></script>
		<script src="..Dialog/ui/jquery.effects.explode.js"></script>
		<link rel="stylesheet" href="../Dialog/demos.css">
		<script>
			// increase the default animation speed to exaggerate the effect
			$.fx.speeds._default = 1000;
			$(function() {

				$( "#dialog" ).dialog({
					autoOpen : false,
					show : "blind",
					hide : "explode",
					height : 400,
					position : 'center',
					width : 500,
					modal : true
				});
				$( ".opener" ).click(function() {
					var a = $(this).attr('id');					
					$('#dialog').load('viewNode.jsp?host=' + a, function() {});
					$( "#dialog" ).dialog("open");
					return false;
				});
			});

		</script>
	</head>
	<body>
		<div class="demo" style="display:block;border:2px solid #cccccc;margin:17px;overflow:auto;">			
		<div id="dialog" title="Host Details"></div>
		<div class="toolbox">
		<img src="../images/plus_ic.gif"  width=40 height: 40 
			onclick="javascript:getDetails('addnode');"
			style=" opacity:1;filter:alpha(opacity=100)" title = "add node" 
			onmouseover="this.style.opacity=0.4;this.filters.alpha.opacity=40" 
			onmouseout="this.style.opacity=1;this.filters.alpha.opacity=100"        >
		<img src="../images/minus_ic.gif"   width=40 height: 40
			onclick="javascript:getDetails('delnode');"
			style="opacity:1;filter:alpha(opacity=100)"  title="delete node" 
			onmouseover="this.style.opacity=0.4;this.filters.alpha.opacity=40" 
			onmouseout="this.style.opacity=1;this.filters.alpha.opacity=100"        >

		</div> <!-- Tool Box Div end --><br>
		<div id="addnode"  class="blockbox" style="display:none;">
		<form action="add_node.jsp" name="f1">
		Node:&nbsp;
		<input type="text" name="host1">
		<input type="submit" name="submit" value="Add Node"/>
		</form></div>
		<div id="delnode" class="blockbox" style="display:none;">
		<form action="del_node.jsp" name="f2">
		Node:&nbsp;<select name="host2">
		<option>--Select Host--</option>
		<jsp:include page="select_nodes.jsp" flush="true" />
		</select>
		<input type="submit" name="submit" value="Delete Node"/>
		</form></div>
		<div id="main">
				<ul id="orbs" class="bubblewrap">
				<%
				DBStore dbc=new DBStore();
				dbc.connect();
				ResultSet rs=dbc.select("nodes");
				try {
					while(rs.next()){
					      String t=rs.getString(1);
					      String t2=t;
						if(t.length()>10){
						t=t.substring(0,10)+"...";
						}
						out.print("<div class=\"host_div\"><div class=\"img_div\" align=\"center\"><li><a class=\"opener\" id=\""+t2+"\"><img src=\"../images/host.gif\" alt=\""+t+"\" /></a></li></div><div class=\"text_div\" align=\"center\"><b>"+t+"</b></div></div>");
					}
				} catch (SQLException e) {
					e.printStackTrace();
				}
				dbc.disconnect();
				%>
			</ul>
			<br>
		</div>
		</div><!-- End demo -->
	</body>
</html>
