<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
<head>
  <link rel="stylesheet" type="text/css" href="../css/project.css">
  <link rel="stylesheet" type="text/css" href="../css/layout-default-latest.css" />
  <link rel="stylesheet" type="text/css" href="../css/style_header.css" />	
  <link rel="stylesheet" href="../css/validationEngine.jquery.css" type="text/css"/>
  <link rel="stylesheet" href="../css/template.css" type="text/css"/>
  <link rel="stylesheet" type="text/css" href="../css/ApplicationLeftFrame.css">
  
  <meta content="text/html; charset=ISO-8859-1"
 http-equiv="content-type">
  <title>Clock Replay Project view</title>
  
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
	<script type="text/javascript" src="../js/project.js"></script>
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
	.demoHeaders {
		margin-top: 2.5em;
	}
	/*override accordion style to hide icons*/
	#accordion .ui-icon { display: none; }
	#accordion .ui-accordion-header a { padding-left:5; }
	#accordion{width:150px;font-size;100px;}
	
	div.toolbox
	{
	top:0px;
	position:relative;
	height:45px;
	left: 900px;
	width:300px;
	}
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

<div class="ui-layout-south ui-widget-content ui-state-error"
	style="display: none;background:#fffaf1;">
	
	<a href="#" style="padding-left: 400pt;"> Site map </a>
	&nbsp;&nbsp;&nbsp;
	<a href="#"> CRP Policy </a>
	&nbsp;&nbsp;&nbsp;
	<a href="#"> Contact us </a>
</div>
-->
<div class="ui-layout-center" style="display: none;overflow:auto;background:#fffaf1;">
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
<div class="leftcol" style="display:block;width:150px;">
<!-- Accordion -->
<jsp:include page="ProjectLeftFrame.html" flush="true"/>
</div>
<div style="position:absolute;margin-left:150px;top:55px" >
<form id="formID" class="formular" method="post" action="">
	<div class="formElements">
	
	<div id="capturesettings" class="content" style="display:block">
	<p class="heading">Capture Settings</p>
	<table>
	<tr>
		<td style="margin-left:0px;width:200px;">
			<fieldset >
				<legend>
                    Capture Location
                </legend>
                <label style="font-size:12px;">
                <div>
                    <span>Please select a file:</span>
                    <input class="validate[required] text-input" type="file" name="file" id="file"/>
                </div>
                </label>
			</fieldset>
		</td>
		<td style="margin-left:200px;width:100px;">
		</td>
		<td style="margin-left:300px;width:200px;">
		<fieldset >
                <legend>
                    Size of Capture
                </legend>
                <label>
                    <select name="Size of Capture" id="sport" class="validate[required]">
                        <option value="">Choose the size</option>
                        <option value="option1">100MB</option>
                        <option value="option2">500MB</option>
                        <option value="option3">1GB</option>
                    </select>
                </label>
            </fieldset>
		</td>
		</tr>
	<tr>
	<td style="margin-left:0px;width:200px;">
	<fieldset>
            <legend>
                Host name
            </legend>
            <label style="font-size:12px;">
                Enter Host name to start the capture
                <br/><br>
                <span>Enter a URL : </span>
                <input type="text" name="hostname" id="hostname" class="validate[required]"/>
            </label>
        </fieldset>
	</td>
	<td style="margin-left:200px;width:100px;">
	</td>
	<td style="margin-left:300px;width:200px;">
	<fieldset>
            <legend>
                Port number
            </legend>
            <label style="font-size:12px;">
                Enter Port number to start the capture
                <br/><br>
                <span>Enter Port : </span>
                <input type="text" name="port" id="port" class="validate[required]"/>
            </label>
        </fieldset>
	</td>
	</tr>
	<tr>
	<td style="margin-left:0px;width:200px;">
	<fieldset>
            <legend>
                Compress data
            </legend>
            <label style="font-size:12px;">
                Enter Compress data
                <br/><br>
                <span>Compress data : </span>
                <input type="text" name="compressdata" id="compressdata" class="validate[required]"/>
            </label>
        </fieldset>
	</td>
	<td style="margin-left:200px;width:100px;">
	</td>
	<td style="margin-left:300px;width:200px;">
	<fieldset>
            <legend>
                Encrypt Passkey
            </legend>
            <label style="font-size:12px;">
                Encrypt Passkey
                <br/><br>
                <span>Encrypt Passkey : </span>
                <input type="text" name="passkey" id="passkey" class="validate[required]"/>
            </label>
        </fieldset>
	</td>
	</tr>
	<tr>
	<td style="margin-left:200px;width:200px;">
	<fieldset>
            <legend>
               Continuous Capture
            </legend>
            <label style="font-size:12px;">
                Continuous Capture
                <br/><br>
                Continuous Capture : 
                <input class="validate[required] radio" type="radio" name="group0" id="radio1" value="1"/>Yes: 
				<input class="validate[required] radio" type="radio" name="group0" id="radio2" value="2"/>No: 
            </label>
        </fieldset>
	</td>
	</tr>	
	<tr>
	<td colspan="3" style="margin-left:700px;width:200px">
	<input type="submit" value="Submit" />
	</td>
	</tr>
	</table>
	</div> <!-- Div closed for "content" -->
  
	<div id="prjsampledata" class="content" style="display:none">
	<p class="heading">Sample Data</p>
	<table>
	<tr>
		<td style="margin-left:0px;width:200px;">
			<fieldset >
				<legend>
                    Duration of Capture 
                </legend>
                 <label style="font-size:12px;">
                 Enter Duration of Capture 
                <br/><br>
                <span>Capture Duration : </span>
                <input type="text" name="captureduration" id="captureduration" class="validate[required]"/>
            </label>
			</fieldset>
		</td>
		<td style="margin-left:200px;width:100px;">
		</td>
		<td style="margin-left:300px;width:200px;">
		<fieldset >
                <legend>
                    Sample data files 
                </legend>
                 <label style="font-size:12px;">
                    <span>Specify sample data files(XML):</span>
                    <input class="validate[required] text-input" type="file" name="samplefile" id="samplefile"/>
                </label>
            </fieldset>
		</td>
		</tr>
	</table>
	</div> <!-- Div closed for "content" -->  

	<div id="capturefilters" class="content" style="display:none">
	<p class="heading">Capture Filters</p>
	<table>
	<tr>
		<td colspan="3" style="margin-left:0px;width:400px;">
			<fieldset >
				<legend>
                    Guided Filters
                </legend>
                    <select name="Field name" id="fieldname" class="validate[required]">
                        <option value="">Field Name</option>
                        <option value="option1">Name</option>
                        <option value="option2">Size</option>
                        <option value="option3">Date</option>
                    </select>
                     <select name="operator" id="operator" class="validate[required]">
                        <option value="">Operator</option>
                        <option value="option1">Equals</option>
                        <option value="option2">Greater than</option>
                        <option value="option3"> Less than </option>
                    </select>
                     <select name="value" id="value" class="validate[required]">
                        <option value="">Value</option>
                        <option value="option1">user1</option>
                        <option value="option2">user2</option>
                        <option value="option3">user3</option>
                    </select>
			</fieldset>
		</td>
		
		</tr>
	<tr>
	<td style="margin-left:0px;width:200px;">
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
	<td style="margin-left:200px;width:100px;">
	</td>
	<td style="margin-left:300px;width:300px;">
	<fieldset>
            <legend>
                Manual filters
            </legend>
            <label style="font-size:12px;">
                Protocols
                <br>
                Client type
            </label>
        </fieldset>
	</td>
	</tr>
	</table>
	</div> <!-- Div closed for "content" --> 

	<div id="captureschedule" class="content" style="display:none">
	<p class="heading">Capture schedule</p>
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
                    Pre-capture script 
                </legend>
                 <label style="font-size:12px;">
                    <span>Pre-capture script:</span>
                    <input class="validate[required] text-input" type="file" name="precapturescript" id=""precapturescript""/>
                </label>
            </fieldset>
		</td>
		</tr>
	
	</table>
	</div> <!-- Div closed for "content" -->	
	</div>
</form>	
</div> <!--  inline div closed -->
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
