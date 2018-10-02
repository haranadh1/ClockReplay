<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN"
"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
		<title>projectDemo</title>
		
		<style type="text/css">
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
div.botright {
display: block;
/*set the div in the bottom right corner*/
position: absolute;
top: 0px;
//right: 30%;
//left: 30%;
//width: 40%;
/*give it some background and border*/
background: #eee;
border: 1px solid #FFcc22;
}
body {
background-color: #fffaf1;
font-size: 75.5%;
}
#proj_tab td{
background-color:#33ff66;
}
#proj_tab th{
background-color:#00ff99;
}
#proj_tab{
padding:5px;
//background-color:#00ff44;
}
#tab{
padding:15px;
//background-color:#00ff44;
}
div#view_main{
border: 2px solid #FFcc22;
margin: 50px;
}
</style>
</head>
	<body>
		<div id="edit" style="display:none">
			<div id="tab">
			<table id="proj_tab" align="center"><form action="editProject.jsp">
			<tr bgcolor=#99ff99><td align="left">Active<td align="right">Capturing</tr>
			<tr><td align="right">Project Name:</td><th ><input type="text" name="proj_name" value="SatyamApp"></th></tr>
			<tr><td align="right">Project Admin:</td><th ><input type="text" name="admin" value="Satyam"></th></tr>
			<tr><td align="right">Project Users:</td><th ><input type="text" name="users" value="Satyam"></th></tr>
			<tr><th colspan="2"><input type="submit" name="ok" value="OK"/><input type="reset" name="cancel" value="Cancel"/></th></tr>
			</form></table>
			</div>
		</div>
		<div id="view_main" style="display:block">
			<div class="botleft" id="botright">
				<a  class="opener" id="del"> <img src="../images/minus.gif" width="35" height="25" border="0"

				title="Delete Project"/></a>
				<a  class="opener" id="update" onclick="editProj();"> <img src="../images/update.gif" width="35" height="25" border="0"

				title="Update Project"/></a>
			</div>
			<div id="tab">
			<table id="proj_tab" align="center">
			<tr bgcolor=#99ff99><td align="left">Active<td align="right">Capturing</tr>
			<tr><td align="right">Project Name:</td><th >SatyamApp</td></tr>
			<tr><td align="right">Data Size:</td><th >10 GB</td></tr>
			<tr><td align="right">Project Admin:</td><th >Satyam</td></tr>
			<tr><td align="right">Project Users:</td><th >Satyam<br>Satyam2<br>Satyam3</td></tr>
			<tr><td align="right">Nodes:</td><th>SatyamSofts<br>NFolks<br>ClockReplay<br></td></tr>
			</table>
			</div>
		</div>
	</body>
</html>
