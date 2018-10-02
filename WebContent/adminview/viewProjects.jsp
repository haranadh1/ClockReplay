<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN"
"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
		<title>project Demo</title>
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
		<script src="../Dialog/ui/jquery.effects.explode.js"></script>
		<link rel="stylesheet" href="../Dialog/demos.css">
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
		top: -10px;
		right: -10px;
		width: 25px;
		}
		
		body {
		background-color: #fffaf1;
		font-size: 75.5%;
		}
		div#dialog{
		background-color:#FFFAF1;
		}
		a:link    {
		/* Applies to all unvisited links */
		text-decoration:  none;
		}
		.img_tab {
		padding-top: 5px;
		padding-bottom: 5px;
		padding-left: 25px;
		padding-right: 25px;
		//background-color: #DDD;
		height: 120px;
		width : 105px;
		float: left;
		}

		</style>
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
				$(".opener").click(function() {
					
					var a = $(this).attr('id');
					if(a=='add'){
					$( "#dialog" ).dialog("option", "title", 'Add Project');
					$('#dialog').load('createProject.jsp', function() {});
					$( '#dialog' ).dialog("open");
					}
					if(a=='up'){
					$( "#dialog" ).dialog("option", "title", 'Update Project');
					$('#dialog').load('updateProject.html', function() {});
					$( '#dialog' ).dialog("open");
					}
					if(a=='del'){
					$( "#dialog" ).dialog("option", "title", 'Delete Project');
					$('#dialog').load('delProject.html', function() {});
					$( '#dialog' ).dialog("open");
					}
					if(a=='view'){
					$( "#dialog" ).dialog("option", "title", 'Project Details');
					$('#dialog').load('viewProject.jsp?project=' + a, function() {});
					$( '#dialog' ).dialog("open");
					}
					return false;
				});				
			});
		</script>
		<script type="text/javascript">
				function editProj(){
				document.getElementById("edit").style.display="block";
				document.getElementById("view_main").style.visibility="hidden";				
				}				
		</script>
	</head>
	<body>
		<div class="demo" style="display:block;border:2px solid #cccccc;margin:17px;overflow:auto;">
		<div id="dialog" title="Project Details"></div>
		<div class="toolbox">
			<a href="createProject.jsp"><img id="add" src="../images/plus_ic.gif"  width=40 height: 40 
			style=" opacity:1;filter:alpha(opacity=100)" title = "add project" 
			onmouseover="this.style.opacity=0.4;this.filters.alpha.opacity=40" 
			onmouseout="this.style.opacity=1;this.filters.alpha.opacity=100"        ></a>
			<!--<img class="opener" id="add" src="../images/plus_ic.gif"  width=40 height: 40 
			onclick="javascript:getDetails('createProject.jsp');"
			style=" opacity:1;filter:alpha(opacity=100)" title = "add project" 
			onmouseover="this.style.opacity=0.4;this.filters.alpha.opacity=40" 
			onmouseout="this.style.opacity=1;this.filters.alpha.opacity=100"        >-->
			<img class="opener" id="up" src="../images/plus_ic.gif"   width=40 height: 40
			onclick="javascript:getDetails('updateProject.html');"
			style=" opacity:1;filter:alpha(opacity=100)"  title = "update project"
			onmouseover="this.style.opacity=0.4;this.filters.alpha.opacity=40" 
			onmouseout="this.style.opacity=1;this.filters.alpha.opacity=100"        >
			<img class="opener" id="del" src="../images/minus_ic.gif"   width=40 height: 40
			onclick="javascript:getDetails('delProject.html');"
			style="opacity:1;filter:alpha(opacity=100)"  title="delete project" 
			onmouseover="this.style.opacity=0.4;this.filters.alpha.opacity=40" 
			onmouseout="this.style.opacity=1;this.filters.alpha.opacity=100"        >

		</div> <!-- Tool Box Div end -->
		<div id="main">
			<%
			for(int i=1;i<13;i++){
			out.print("<div class=\"img_tab\">");
			out.print("<table><tr><th>Project"+i+"</th></tr><tr><th>");
			if(i%2==0){
			if(i%3==0){
			out.print("<a class=\"opener\" id=\"view\" style=\"curser:pointer\"><img src=\"../images/red_rnd_rectangle.gif\" width=\"100\" height=\"60\"></a>");
			}else{
			out.print("<a class=\"opener\" id=\"view\" style=\"curser:pointer\"><img src=\"../images/grn_rnd_rectangle.gif\" width=\"100\" height=\"60\"></a>");
			}
			}else{
			out.print("<a class=\"opener\" id=\"view\" style=\"curser:pointer\"><img src=\"../images/blnd_grey_rnd_rectangle.gif\" width=\"100\" height=\"60\"></a>");
			}
			out.print("</th></tr><tr><th>");
			out.print("<img src=\"../images/toolbar2.gif\" width=\"100\" height=\"20\">");
			out.print("</th></tr></table></div>");
			}
			%>
		</div>
	</div>
	</body>
</html>
