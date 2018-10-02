<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
	<head>
		<script type="text/javascript" src="../js/jquery-1.6.2.js"></script>
		<script type="text/javascript">
			$(document).ready(function() {
				$("img.rmv").click(function(){				
				var a=$(this).parent().children()[0].html();
				var b=$(this).parent().children()[1].html();
				alert(a+":"+b);
				});
				$(".heading").click(function() {
					$(this).next(".tab").slideToggle("slow");
					$(this).next(".tab").siblings(".tab:visible").slideUp("fast")
					$(this).toggleClass("active");
					$(this).siblings("h3").removeClass("active");
				});
			});

		</script>
		<style type="text/css">
body{background-color:#fffaf1}
div#container{
margin:17px 10px auto;
border:1px solid #cccccc;
padding:10px;
}
div.divb{
height:300px;
overflow: auto;
margin:10px auto;
border:1px solid #99aaaa;
background-color:#eeeeee;
padding:5px;
width:520px;
}
div#addbox{
margin:2px auto;
padding-left:5px;
border:1px solid #99aaaa;
border-radius:5px;
width:500px;
}
p{
margin:0px auto;
padding:0px;
}
h3.heading{
margin:1px auto;
font-size:15px;
padding-left:5px;
cursor:pointer;
background-color:#cccccc;
border:1px solid #99aaaa;
border-radius:5px;
width:500px;
}
h3:hover {
	background-color: #dddddd;
}
h3.active {
	background-color:#efefef;
	background-position: right 5px;
}
img.rmv{cursor:pointer;}
td{
width:250px;
}
table.tab{
margin:1px auto;
display:none;
background-color:#efefef;
border:1px solid #99aaaa;
width:500px;
}
td.serv_head{
border:1px solid white;
background-color:white;
padding-left:10px;
color:#010101;
font-weight:bold;
}
td.serv_cont{
padding-left:10px;
}
		</style>
	</head>
	<body>
		<div id="container">
			<h2 align="center">Capture Services</h3>
			<div id="addbox">
			<form action="addCapture.jsp" method="post">
			Name:<input type="text" id="name" name="name" size="8" />
			Host:<select name="host" id="host">
			<option>--Select Host--</option>
			<jsp:include page="select_nodes.jsp" flush="true" />
			</select>			
			Port:<input type="text" id="port"name="port" size="8" />			
			<input type="submit" value="" style="background: url('../images/plus_ic.gif');width:30px"/> 			
			</form>			
			</div>	
			<div class="divb"><p class="node">
				<h3 class="heading">Node1 <!--<input type="checkbox" name="node" value="Node1"/>--></h3>
				<table class="tab">
					<tr>
						<td class="serv_head">Service</td><td class="serv_head">Port</td><td class="serv_head"></td>
					</tr>
					<tr>
						<td class="serv_cont">
						<!--<input type="checkbox" name="capture" value="Service1"/>-->
						Service1</td>
						<td class="serv_cont">
						<!--<input type="checkbox" name="capture" value="port1" />-->
						port1</td>
						<td><img src="../images/minus_ic.gif" class="rmv"></td>
					</tr>
					<tr>
						<td class="serv_cont">
						<!--<input type="checkbox" name="capture" value="Service1"/>-->
						Service1</td>
						<td class="serv_cont">
						<!--<input type="checkbox" name="capture" value="port1" />-->
						port1</td>
						<td><img src="../images/minus_ic.gif" class="rmv"></td>
					</tr>
					<tr>
						<td class="serv_cont">
						<!--<input type="checkbox" name="capture" value="Service1"/>-->
						Service1</td>
						<td class="serv_cont">
						<!--<input type="checkbox" name="capture" value="port1" />-->
						port1</td>
						<td><img src="../images/minus_ic.gif"class="rmv"></td>
					</tr>
				</table>
			</p>
			<p class="node">
				<h3 class="heading">Node2 <!--<input type="checkbox" name="node" value="Node1" />--></h3>
				<table class="tab">
					<tr>
						<td class="serv_head">Service</td><td class="serv_head">Port</td><td class="serv_head"></td>
					</tr>
					<tr>
						<td class="serv_cont">
						<!--<input type="checkbox" name="capture" value="Service1"/>-->
						Service1</td>
						<td class="serv_cont">
						<!--<input type="checkbox" name="capture" value="port1" />-->
						port1</td>
						<td><img src="../images/minus_ic.gif" class="rmv"></td>
					</tr>
				</table>
			</p>
			<p class="node">
				<h3 class="heading">Node3 <!--<input type="checkbox" name="node" value="Node1" />--></h3>
				<table class="tab">
					<tr>
						<td class="serv_head">Service</td><td class="serv_head">Port</td><td class="serv_head"></td>
					</tr>
					<tr>
						<td class="serv_cont">
						<!--<input type="checkbox" name="capture" value="Service1"/>-->
						Service1</td>
						<td class="serv_cont">
						<!--<input type="checkbox" name="capture" value="port1" />-->
						port1</td>
						<td><img src="../images/minus_ic.gif" class="rmv"></td>
					</tr>
				</table>
			</p>
		</div></div>
	</body>
</html>
