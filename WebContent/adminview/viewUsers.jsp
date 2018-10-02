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
			div.user_div {

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

		</style>		
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
					if(a=='add'){
					$( "#dialog" ).dialog("option", "title", 'Add User');
					$('#dialog').load('addUser.html', function() {});
					$( '#dialog' ).dialog("open");
					}else if(a=='up'){
					$( "#dialog" ).dialog("option", "title", 'Update User');
					$('#dialog').load('updateUser.html', function() {});
					$( '#dialog' ).dialog("open");
					}else if(a=='del'){
					$( "#dialog" ).dialog("option", "title", 'Delete User');
					$('#dialog').load('delUser.html', function() {});
					$( '#dialog' ).dialog("open");
					}else {
					$( "#dialog" ).dialog("option", "title", 'User Details');
					$('#dialog').load('viewUser.jsp?user=' + a, function() {});
					$( '#dialog' ).dialog("open");
					}
					return false;
				});
			});

		</script>
	</head>
	<body>
		<div class="demo" style="display:block;border:2px solid #cccccc;margin:17px;overflow:auto;">
			<div id="dialog" title="Host Details"></div>
			<div class="toolbox">
		<img class="opener" id="add" src="../images/plus_ic.gif"  width=40 height: 40 
			onclick="javascript:getDetails('addUser.html');"
			style=" opacity:1;filter:alpha(opacity=100)" title = "add project" 
			onmouseover="this.style.opacity=0.4;this.filters.alpha.opacity=40" 
			onmouseout="this.style.opacity=1;this.filters.alpha.opacity=100"        >
		<img class="opener" id="up" src="../images/plus_ic.gif"   width=40 height: 40
			onclick="javascript:getDetails('updateUser.html');"
			style=" opacity:1;filter:alpha(opacity=100)"  title = "update project"
			onmouseover="this.style.opacity=0.4;this.filters.alpha.opacity=40" 
			onmouseout="this.style.opacity=1;this.filters.alpha.opacity=100"        >
		<img class="opener" id="del" src="../images/minus_ic.gif"   width=40 height: 40
			onclick="javascript:getDetails('delUser.html');"
			style="opacity:1;filter:alpha(opacity=100)"  title="delete project" 
			onmouseover="this.style.opacity=0.4;this.filters.alpha.opacity=40" 
			onmouseout="this.style.opacity=1;this.filters.alpha.opacity=100"        >

		</div> <!-- Tool Box Div end -->
			
			<div id="main">
			<ul id="orbs" class="bubblewrap">
				<div class="user_div"><div class="img_div" align="center"><li><a class="opener" id="Satyam"><img src="../images/person_icon.png" alt="Satyam" /></a></li></div><div class="text_div" align="center"><b>Satyam</b></div></div>
			</ul>
			<ul id="orbs" class="bubblewrap">
				<div class="user_div"><div class="img_div" align="center"><li><a class="opener" id="Vamsi"><img src="../images/person_icon.png" alt="Vamsi" /></a></li></div><div class="text_div" align="center"><b>Vamsi</b></div></div>
			</ul>
			<ul id="orbs" class="bubblewrap">
				<div class="user_div"><div class="img_div" align="center"><li><a class="opener" id="Krishna"><img src="../images/person_icon.png" alt="Krishna" /></a></li></div><div class="text_div" align="center"><b>Krishna</b></div></div>
			</ul>
			<ul id="orbs" class="bubblewrap">
				<div class="user_div"><div class="img_div" align="center"><li><a class="opener" id="Santro"><img src="../images/person_icon.png" alt="Santro" /></a></li></div><div class="text_div" align="center"><b>Santro</b></div></div>
			</ul>			
			</div>
		</div>
		<!-- End demo -->
	</body>
</html>
