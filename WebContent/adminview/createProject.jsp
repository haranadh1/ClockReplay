<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
	<head>
		<script type="text/javascript" src="../js/jquery-1.6.2.js"></script>
		<script type="text/javascript">
			$(document).ready(function() {
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
			//background-color:#eeeeee;
			//margin:10px;
			padding:0px 10px;
			}
			div.divb{
			margin:10px auto;
			border:1px solid #99aaaa;
			background-color:#eeeeee;
			//margin:10px;
			padding:5px;
			width:400px;
			}
			p{
			margin:0px auto;
			padding:0px;
			}
			h3.heading{
			margin:1px auto;
			//margin-left:10px;
			font-size:12px;
			padding-left:5px;
			cursor:pointer;
			background-color:#cccccc;
			border:1px solid #99aaaa;
			border-radius:5px;
			width:360px;
			}
			h3:hover {
			background-color: #dddddd;
			}
			h3.active {
			background-color:#efefef;
			background-position: right 5px;
			}
			td{
			width:180px;
			}
			table.tab{
			margin-left:17px;
			display:none;
			background-color:#efefef;
			border:1px solid #99aaaa;
			width:360px;
			}
			td.serv_head{
			border:1px solid white;
			background-color:white;
			padding-left:10px;
			color:#010101;
			font-weight:bold;
			}
		</style>
	</head>
	<body>
		<div id="container">
			<h2 align="center">Create Project</h2>
			<form method="post" action="display.jsp">
				<div id="pname" class="divb">
					Project Name:
					<br>
					<input type="text" name="project_name" id="project_name" style="width: 20em;" class="validate[required]"/>
				</div>
				<div id="dsize" class="divb">
					Size of Data:
					<br>
					<select name="data_size" id="data_size" style="width: 20em;" class="validate[required]">
						<option value="">Choose the size</option>
						<option value="128MB">128MB</option>
						<option value="256MB">256MB</option>
						<option value="512MB">512MB</option>
						<option value="1GB">1GB</option>
					</select>
				</div>
				<div id="adm" class="divb">
					Administrator:
					<br>
					<select name="admin" id="admin" style="width: 20em;" class="validate[required]">
						<option value="">Choose the administrator</option>
						<option value="Satyam1">Satyam1</option>
						<option value="Satyam2">Satyam2</option>
						<option value="Satyam3">Satyam3</option>
						<option value="Satyam4">Satyam4</option>
					</select>
				</div>
				<div id="users" class="divb">
					Users:
					<br>
					<select name="users" id="users" style="width: 20em;" class="validate[required]" size="4" multiple="true">
						<option value="satyam">satyam</option>
						<option value="satya">satya</option>
						<option value="satyanarayana">satyanarayana</option>
					</select>
				</div>
				<div id="nodes" class="divb">
					Nodes:
					<br>
					<div id="divn" style="background-color:#fefefe;padding:5px 0px;border:1px solid #aaaaaa;">
					<jsp:include page="select_services.jsp"/>
					</div>
				</div>
				<div id="divs" class="divb" >
					<button id="create-user" type="submit">
						Create new project
					</button>
					<button id="cancel" type="reset">
						Cancel
					</button>
				</div>
			</form>
		</div>
	</body>
</html>
