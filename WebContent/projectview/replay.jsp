<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
<head>
  <link rel="stylesheet" type="text/css" href="../css/project.css">
  <link rel="stylesheet" type="text/css" href="../css/layout-default-latest.css" />
  <link rel="stylesheet" type="text/css" href="../css/style_header.css" />	
  <link rel="stylesheet" href="../css/validationEngine.jquery.css" type="text/css"/>
  <link rel="stylesheet" href="../css/template.css" type="text/css"/>

  <meta content="text/html; charset=ISO-8859-1"
 http-equiv="content-type">
  <title>Project Replay</title>
  
<link rel="stylesheet" type="text/css" href="../css/ApplicationLeftFrame.css">
<link type="text/css" href="../css/custom-theme/jquery-ui-1.8.14.custom.css" rel="stylesheet" />
	<script type="text/javascript" src="../js/jquery-1.6.2.js"></script>
	<script type="text/javascript" src="../js/clockreplay.common.js"></script>
	<script type="text/javascript" src="../js/jquery.spasticNav.js"></script>
	<script type="text/javascript" src="http://ajax.googleapis.com/ajax/libs/jqueryui/1.7.2/jquery-ui.min.js"></script>
	<script type="text/javascript" src="../js/jquery-latest.js"></script>
	<script type="text/javascript" src="../js/jquery-ui-latest.js"></script>
	<script type="text/javascript" src="../js/jquery.layout-latest.js"></script>
	<script type="text/javascript" src="../js/jquery.layout.resizePaneAccordions-1.0.js"></script>
	<script type="text/javascript" src="../js/themeswitchertool.js"></script>
	<script type="text/javascript" src="../js/debug.js"></script>
	<script src="../js/ui/jquery.ui.core.js"></script> 
	<script src="../js/ui/jquery.ui.widget.js"></script> 
	<script src="../js/ui/jquery.ui.datepicker.js"></script>
	<script src="../js/ui/jquery.ui.tabs.js"></script>	
	<script src="../js/ui/jquery.ui.mouse.js"></script> 
	<script src="../js/ui/jquery.ui.draggable.js"></script> 
	<script src="../js/ui/jquery.ui.position.js"></script> 
	<script src="../js/ui/jquery.ui.resizable.js"></script> 
	<script src="../js/ui/jquery.ui.dialog.js"></script> 
	
	   <script src="../js/languages/jquery.validationEngine-en.js" type="text/javascript" charset="utf-8">
        </script>
        <script src="../js/jquery.validationEngine.js" type="text/javascript" charset="utf-8">
        </script>
   <script src="../js/ui/jquery.ui.core.js"></script> 
	<script src="../js/ui/jquery.ui.widget.js"></script> 
	<script src="../js/ui/jquery.ui.datepicker.js"></script>
	
	<script type="text/javascript">
		$(function() {
			$( "#date1" ).datepicker();
		});
		
         jQuery(document).ready( function() {
          
			  //toggle the componenet with class msg_body
			  jQuery(".heading").click(function()
			  {
			    jQuery(this).next(".content").slideToggle(500);
			  });
             // binds form submission and fields to the validation engine
             jQuery("#formID").validationEngine();
         });
    </script>
     <script type="text/javascript">
			$(function() {
				// Accordion
				$("#accordion").accordion({
					header : "h3"
				});

			});
	</script>
<style type="text/css">
	/*body css*/
	body {font: 62.5% "Trebuchet MS", sans-serif;margin: 15px;}
	/*override accordion style to hide icons*/
	#accordion .ui-icon { display: none; }
	#accordion .ui-accordion-header a { padding-left:5; }
	#accordion{width:150px;font-size;100px;}
</style>
</head>

<body style = "background-color: #fffaf1">
<!--  
<div class="ui-layout-north ui-widget-content" style="display: none;">
<div style="float: right; margin-right: 160px;">
<button onClick="removeUITheme(); myLayout.resizeAll()">Remove Theme</button>
</div>
<div id="container">
<ul id="nav">
	<li id="selected"><a href="#">Home</a></li>
	<li><a href="#">Dash Board</a></li>
	<li><a href="#">Project</a></li>
	<li><a href="#">Admin</a></li>
</ul>
</div>

<script type="text/javascript">
    $('#nav').spasticNav();
</script></div>
-->
<div class="ui-layout-south ui-widget-content ui-state-error"
	style="display: none;background:#fffaf1;">
	
	<a href="#" style="padding-left: 400pt;"> Site map </a>
	&nbsp;&nbsp;&nbsp;
	<a href="#"> CRP Policy </a>
	&nbsp;&nbsp;&nbsp;
	<a href="#"> Contact us </a>
	
</div>

<div class="ui-layout-center" style="display: none;overflow:auto;background:#fffaf1;">
<div class="leftcol" style="display:block;">
<jsp:include page="ReplayLeftFrame.html"/>
</div>
<div style="position:absolute;margin-left:150px;top:10px" >
<form id="formID" class="formular" method="post" action="">
	<div class="toolbox">
		<img src="../images/circle-capture_blnd.gif"   
			style="width=40; height: 40;opacity:1;filter:alpha(opacity=100)" title = "capture" 
			onmouseover="this.style.opacity=0.4;this.filters.alpha.opacity=40" 
			onmouseout="this.style.opacity=1;this.filters.alpha.opacity=100"        >
		<img src="../images/editor.gif"   
			style=" width=40; height: 40;opacity:1;filter:alpha(opacity=100)" title = "editor" 
			onmouseover="this.style.opacity=0.4;this.filters.alpha.opacity=40" 
			onmouseout="this.style.opacity=1;this.filters.alpha.opacity=100"        >
		<img src="../images/replay.gif"   
			style=" width=40; height: 40;opacity:1;filter:alpha(opacity=100)"  title = "replay"
			onmouseover="this.style.opacity=0.4;this.filters.alpha.opacity=40" 
			onmouseout="this.style.opacity=1;this.filters.alpha.opacity=100"        >
		<img src="../images/results.gif"   
			style="width=40; height: 40;opacity:1;filter:alpha(opacity=100)"  title="analyzer" 
			onmouseover="this.style.opacity=0.4;this.filters.alpha.opacity=40" 
			onmouseout="this.style.opacity=1;this.filters.alpha.opacity=100"        >
		<img src="../images/chart.gif"   
			style="width=40; height: 40;opacity:1;filter:alpha(opacity=100)"  title="synopsis" 
			onmouseover="this.style.opacity=0.4;this.filters.alpha.opacity=40" 
			onmouseout="this.style.opacity=1;this.filters.alpha.opacity=100"        >

	</div> <!-- Tool Box Div end -->
	<div class="formElements">
	<p class="heading">Pre Replay Settings</p>
	<div class="content">
	<table>
	<tr>
		<td colspan="3" style="margin-left:0px;width:500px;">
			<fieldset >
				<legend>
                    Host name mapping
                </legend>
                <label>
                <table border="1px" width="100%">
                <tr>
                <td>
                    Host name(c):
                </td>
                <td style="margin-left:200px;">
                	Host name(r):
                </td>
                </tr>
                <tr>
                <td>
                  Local host capture 1
                </td>
                <td style="margin-left:200px;"> 
					Remote replay1                   
                </td>
                </tr>
                 <tr>
                <td>
                  Local host capture 2
                </td>
                <td style="margin-left:200px;"> 
					Remote replay2                   
                </td>
                </tr>
                 <tr>
                <td>
                  Local host capture 3
                </td>
                <td style="margin-left:200px;"> 
					Remote replay3                   
                </td>
                </tr>
                </table>
                </label>
			</fieldset>
		</td>
		</tr>
	<!-- second -->
	<tr>
		<td colspan="3" style="margin-left:0px;width:500px;">
			<fieldset >
				<legend>
                    User name mapping
                </legend>
                <label>
                <table border="1px" width="100%">
                <tr>
                <td>
                    User name(c):
                </td>
                <td style="margin-left:200px;">
                	User name(r):
                </td>
                </tr>
                <tr>
                <td>
                  capture user:1
                </td>
                <td style="margin-left:200px;"> 
					Replay user:1                   
                </td>
                </tr>
                 <tr>
                <td>
                  capture user:2
                </td>
                <td style="margin-left:200px;"> 
					Replay user:2                
                </td>
                </tr>
                 <tr>
                <td>
                  capture user:3
                </td>
                <td style="margin-left:200px;"> 
				 Replay user:3                   
                </td>
                </tr>
                </table>
                </label>
			</fieldset>
		</td>
		</tr>	
	
	<!-- third -->
	<tr>
		<td colspan="3" style="margin-left:0px;width:500px;">
			<fieldset >
				<legend>
                    Port Mapping
                </legend>
                <label>
                <table border="1px" width="100%">
                <tr>
                <td>
                    Port(c):
                </td>
                <td style="margin-left:200px;">
                	Port(r):
                </td>
                </tr>
                <tr>
                <td>
                  Capture Port:1
                </td>
                <td style="margin-left:200px;"> 
					Replay Port:1                   
                </td>
                </tr>
                 <tr>
                <td>
                  Capture Port:2
                </td>
                <td style="margin-left:200px;"> 
					Replay Port:2                   
                </td>
                </tr>
                 <tr>
                <td>
                 Capture Port:3
                </td>
                <td style="margin-left:200px;"> 
					Replay Port:3                   
                </td>
                </tr>
                </table>
                </label>
			</fieldset>
		</td>
		</tr>
	
	<!-- fourth -->
	<tr>
		<td colspan="3" style="margin-left:0px;width:500px;">
			<fieldset >
				<legend>
                    Protocol Mapping
                </legend>
                <label>
                <table border="1px" width="100%">
                <tr>
                <td>
                    Protocol(c):
                </td>
                <td style="margin-left:200px;">
                	Protocol(r):
                </td>
                </tr>
                <tr>
                <td>
                  Capture Protocol:1
                </td>
                <td style="margin-left:200px;"> 
					Replay Protocol:1                   
                </td>
                </tr>
                 <tr>
                <td>
                  Capture Protocol:1
                </td>
                <td style="margin-left:200px;"> 
					Replay Protocol:1                   
                </td>
                </tr>
                 <tr>
                <td>
                  Capture Protocol:1
                </td>
                <td style="margin-left:200px;"> 
					Replay Protocol:1                
                </td>
                </tr>
                </table>
                </label>
			</fieldset>
		</td>
		</tr>	
	</table>
	</div> <!-- Div closed for "content" -->
	
	
  <p class="heading">Replay filter</p>
	<div class="content">
	<table>
	<tr>
		<td colspan="3" style="margin-left:0px;width:500px;">
			<fieldset >
				<legend>
                    Replay only sessions
                </legend>
                <label>
                <table border="1px" width="100%">
                <tr>
                <td>
                    Sessions
                </td>
                <td style="margin-left:200px;">
                	Ignore
                </td>
                </tr>
                <tr>
                <td>
                 Session:1
                </td>
                <td style="margin-left:200px;"> 
					<input type="checkbox" name="ignore1" id="ignore1"/>                   
                </td>
                </tr>
                 <tr>
                <td>
                  Session:2
                </td>
                <td style="margin-left:200px;"> 
					<input type="checkbox" name="ignore2" id="ignore2"/>                  
                </td>
                </tr>
                 <tr>
                <td>
                  Session:3
                </td>
                <td style="margin-left:200px;"> 
				 <input type="checkbox" name="ignore3" id="ignore3"/>   
                </td>
                </tr>
                </table>
                </label>
			</fieldset>
		</td>
		</tr>
	</table>
	</div> <!-- Div closed for "content" -->  

<p class="heading">Sample Data</p>
	<div class="content">
	<table>
	<tr>
		<td colspan="3" style="margin-left:0px;width:500px;">
			<fieldset >
				<legend>
                    Replay sample 
                </legend>
                 <label style="font-size:12px;">
                 Enter Duration of Replay 
                <br/><br>
                <span>Replay duration : </span>
                <input type="text" name="replayduration" id="replayduration" class="validate[required]"/>
            </label>
			</fieldset>
		</td>
		
		</tr>
	</table>
	</div> <!-- Div closed for "content" -->
<p class="heading">Replay customization</p>
	<div class="content">
	<table>
	<tr>
		<td colspan="3" style="margin-left:0px;width:400px;">
			<fieldset >
				<legend>
                    Guided Filters (Based on sample data)
                </legend>
                    <select name="Field name" id="fieldname" class="validate[required]">
                        <option value="">Field Name</option>
                        <option value="option1">Name</option>
                        <option value="option2">Size</option>
                        <option value="option3">Date</option>
                    </select>
                     <select name="old" id="new" class="validate[required]">
                        <option value="">Old</option>
                        <option value="option1">old name1</option>
                        <option value="option2">old name1 </option>
                        <option value="option3"> old name1 </option>
                    </select>
                     <select name="new" id="new" class="validate[required]">
                        <option value="">New</option>
                        <option value="option1">New name1</option>
                        <option value="option2">New name2</option>
                        <option value="option3">New name3</option>
                    </select>
			</fieldset>
		</td>
		</tr>
	<tr>
	<td colspan="3" style="margin-left:0px;width:500px;">
	<fieldset>
            <legend>
                Advanced Filters(Custom Java code)
            </legend>
            <label style="font-size:12px;">
                Enter Advanced Filters
                <br/><br>
            </label>
        </fieldset>
	</td>
	</tr>
	</table>
	</div> <!-- Div closed for "content" --> 

<p class="heading">Replay schedule</p>
	<div class="content">
	<table>
	<tr>
		<td style="margin-left:0px;width:200px;">
			<fieldset >
				<legend>
                     Schedule Date/Time 
                </legend>
                 <label style="font-size:12px;">
                  Schedule Date/Time 
                <br/><br>
                <span> Schedule Date/Time : </span>
                <input type="text" name="date1" id="date1" class="validate[required]"/>
            </label>
			</fieldset>
		</td>
		<td style="margin-left:200px;width:100px;">
		</td>
		<td style="margin-left:300px;width:200px;">
		<fieldset >
                <legend>
                    Pre-Replay script 
                </legend>
                 <label style="font-size:12px;">
                    <span>Pre-Replay script:</span>
                    <input class="validate[required] text-input" type="file" name="precapturescript" id=""precapturescript""/>
                </label>
            </fieldset>
		</td>
		</tr>
	</table>
	</div> <!-- Div closed for "content" -->	
<p class="heading">Replay runtime Settings</p>
	<div class="content">
	<table>
	<tr>
		<td colspan="3" style="margin-left:0px;width:500px;">
			<fieldset >
				<legend>
                    Decrypt Passkey  
                </legend>
                 <label style="font-size:12px;">
                Decrypt Passkey
                <input type="text" name="decryptpasskey" id=""decryptpasskey"" class="validate[required]"/>
            </label>
			</fieldset>
		</td>
		
		</tr>
	</table>
	</div> <!-- Div closed for "content" -->	
	</div>
</form>
</div> <!-- Div inline closed -->	
</div> <!--  Center layout Div closed -->

<!-- 
<div class="ui-layout-west" style="display: none;">
<div id="accordion1" class="basic">

<h3><a href="#">CRP Left Hand Menu -1</a></h3>
<div>
<h5>CRP Left Hand Menu -1 content</h5>
</div>

<h3><a href="#">CRP Left Hand Menu -2</a></h3>
<div>
<h5>CRP Left Hand Menu -2 content</h5>
</div>

<h3><a href="#">CRP Left Hand Menu -3</a></h3>
<div>
<h5>CRP Left Hand Menu -3 content</h5>
</div>

</div>

</div> 

 -->
</body>
</html>
