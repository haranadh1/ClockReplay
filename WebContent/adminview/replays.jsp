<html>
	<head>
		<script type="text/javascript" src="../Scripts/jquery-1.6.2.js"></script>
		<link rel="stylesheet" type="text/css" href="../css/ApplicationLeftFrame.css">
		<link type="text/css" href="../css/custom-theme/jquery-ui-1.8.14.custom.css" rel="stylesheet" />
		<script type="text/javascript" src="../js/jquery-1.5.1.min.js"></script>
		<script type="text/javascript" src="../js/jquery-ui-1.8.14.custom.min.js"></script>
		<script type="text/javascript">
			$(function() {
				// Accordion
				$("#accordion").accordion({
					header : "h3"
				});
			});
			function show(link) {
				parent.contentframe.location.href = link;
				//alert(link);
			}
		</script>
		<script type="text/javascript">
			$(document).ready(function() {
				$('.replay> tbody > tr:odd').css("background-color", "#FCF6CF");
			});

		</script>
		<script type="text/javascript">
			function showDetails() {
				var name = document.service_name.value;
				alert("hi" + name);
			}

			var cols = new Array();
			cols[0] = "A";
			cols[1] = "B";
			cols[2] = "C";
			function getData(_row,sid) {
				var name=sid;
				var host = document.getElementById("s"+_row+cols[0]).innerHTML;
				var port = document.getElementById("s"+_row+cols[1]).innerHTML;
				document.location.href = "delReplay.jsp?name="+name+"&host=" + host + "&port=" + port;
			}

			function over(_row) {
				document.getElementById("r"+_row).style.backgroundColor = "#FCA";
			}

			function out(_row) {
				document.getElementById("r"+_row).style.backgroundColor = "#ACF";
			}
		</script>
		<style type="text/css">
.content{font-size:.7em;}
#addbut{background: url('../images/plus_ic.gif');width:30px;}
/*override accordion style */
.ui-accordion {margin:10px 25%;width: 50%; }
.ui-accordion .ui-accordion-header { cursor: pointer; position: relative; margin-top: 1px; zoom: 1; }
.ui-accordion .ui-accordion-li-fix { display: inline; }
.ui-accordion .ui-accordion-header-active { border-bottom: 0 !important; }
.ui-accordion .ui-accordion-header a { display: block; font-size: .75em; padding: .5em .5em .5em .7em; }
.ui-accordion-icons .ui-accordion-header a { padding-left: 2.2em; }
.ui-accordion .ui-accordion-header .ui-icon { position: absolute; left: .5em; top: 50%; margin-top: -8px; }
.ui-accordion .ui-accordion-content { padding: 1em 2.2em; border-top: 0; margin-top: -2px; position: relative; top: 1px; margin-bottom: 2px; overflow: auto; display: none; zoom: 1; }
.ui-accordion .ui-accordion-content-active { display: block; }
		</style>
	</head>
	<body bgcolor="#FFFAF1">
		<div id="replay_div" style="display:block;border:1px solid #cccccc;margin:17px;text-align:center">
			<h3 align="center">Replay Services</h3>
			<div style="height:26px;background-color:#cccc88;margin:5px 25%;width: 50%;border:2px solid #ffdd88;text-size:10px;">
			<form action="addReplay.jsp" method="post">
			Name:<input type="text" id="name" name="name" size="8" />
			Host:<select name="host" id="host">
			<option>--Select Host--</option>
			<jsp:include page="select_nodes.jsp" flush="true" />
			</select>			
			Port:<input type="text" id="port"name="port" size="8" />			
			<input type="submit" value="" style="background: url('../images/plus_ic.gif');width:30px"/> 			
			</form>			
			</div>			
			<!-- Accordion -->
			<div id="accordion">
			<%
			for(int i=1;i<10;i++){
			String name="service"+i,host="SatyamSofts",port="1111";
			out.print("<div>");
			out.print("<h3><a href=\"#\">"+name+"</a></h3>");
			out.print("<div class=\"content\">");
			out.print("<table class=\"replay\" align=\"center\" border=\"1\">");
			out.print("<tr class=\"d\">");
			out.print("<th>Host</th><th>Port</th><th></th>");
			out.print("</tr>");
			out.print("<tr id=\"r"+i+"\" onclick=\"getData("+i+","+name+")\" onmouseover=\"over("+i+")\" onmouseout=\"out("+i+")\">");
			out.print("<td id=\"s"+i+"A\">"+host+"</td>");
			out.print("<td id=\"s"+i+"B\">"+port+"</td>");
			out.print("<td id=\"s"+i+"C\"><a href=\"#\"><img src=\"../images/minus_ic.gif\"/></a></td>");
			out.print("</tr>");
			out.print("</table>");
			out.print("</div>");
			out.print("</div>");
			}
			%>
			</div>			
		</div>
	</body>
</html>