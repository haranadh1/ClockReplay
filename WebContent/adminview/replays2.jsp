<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
	<head>
		<script type="text/javascript" src="../js/jquery-1.6.2.js"></script>
		<script type="text/javascript">
			$(document).ready(function() {
				$("img.rmv").click(function(){
					var a=$(this).parent().siblings(".hst").html();
					var b=$(this).parent().siblings(".prt").html();
					var c=event.target.id;
					var qs="?name="+$.trim(c)+"&host="+$.trim(a)+"&port="+$.trim(b);
					document.location.href ="delReplay.jsp"+qs;
					//alert(qs);
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
//height:300px;
overflow: auto;
margin:10px auto;
border:1px solid #99aaaa;
background-color:#eeeeee;
padding:5px;
width:510px;
}
div#addbox{
text-align:center;
background-color:#bbbbbb;
font-weight:bold;
margin:2px auto;
padding-left:5px;
border:1px solid #99aaaa;
border-radius:5px;
width:510px;
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
			<h2 align="center">Replay Services</h3>
			<div id="addbox">
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
			<div class="divb">
			<jsp:include page="select_rep_services.jsp"/>
			</div>
		</div>
	</body>
</html>
