<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
<head>
<title>Clock Replay DashBoard view</title>
<link rel="stylesheet" type="text/css" href="../css/layout-default-latest.css" />
<link rel="stylesheet" type="text/css" href="../css/style_header.css" />
<link rel="stylesheet" type="text/css" href="../css/dashboard.css">	
<link rel="stylesheet" type="text/css" href="../css/jquery.ui.all.css">
<script type="text/javascript" src="../js//ApplicationLeftFrame.js"></script>
<link rel="stylesheet" type="text/css" href="../css/ApplicationLeftFrame.css">
<link type="text/css" href="../css/jquery-ui-1.8.14.custom.css" rel="stylesheet" />
<script type="text/javascript" src="../js/jquery-1.4.2.min.js"></script>
<script type="text/javascript" src="../js/jquery-ui-1.8.14.custom.min.js"></script>
<!-- CUSTOMIZE/OVERRIDE THE DEFAULT CSS -->
<script type="text/javascript">
			$(function() {
				// Accordion
				$("#accordion").accordion({
					header : "h3"
				});

			});
</script>
		<style type="text/css">
			/*demo page css*/
			body {
				font: 62.5% "Trebuchet MS", sans-serif;
				margin: 15px;
			}
		#accordion .ui-icon { display: none; }
		#accordion .ui-accordion-header a { padding-left:5; }
		#accordion{width:150px;font-size;100px;}
		</style>
<style type="text/css">

	#tabs {
		margin-top: 1em;
	}
	#tabs li .ui-icon-close {
		float: left;
		margin: 0.4em 0.2em 0 0;
		cursor: pointer;
	}
	.add_tab {
		cursor: pointer;
	}

/* remove padding and scrolling from elements that contain an Accordion OR a content-div */
.ui-layout-center  , /* has content-div */ .ui-layout-west  ,
	/* has Accordion */ .ui-layout-east  , /* has content-div ... */
	.ui-layout-east .ui-layout-content { /* content-div has Accordion */
	padding: 0;
	overflow: hidden;
}

.ui-layout-center P.ui-layout-content {
	line-height: 1.4em;
	margin: 0; /* remove top/bottom margins from <P> used as content-div */
}

h3,h4 { /* Headers & Footer in Center & East panes */
	font-size: 1.1em;
	background: #EEF;
	border: 1px solid #BBB;
	border-width: 0 0 1px;
	padding: 7px 10px;
	margin: 0;
}

.ui-layout-east h4 { /* Footer in East-pane */
	font-size: 0.9em;
	font-weight: normal;
	border-width: 1px 0 0;
}
.ui-layout-container {
background: #EEF;
background-color:blue;
}
</style>
<!-- REQUIRED scripts for layout widget -->

<script type="text/javascript" src="../js/jquery-1.6.2.js"></script>
<script type="text/javascript" src="../js/clockreplay.common.js"></script>
<script type="text/javascript" src="../js/clockReplayTabs.js"></script>
<script type="text/javascript"
	src="../js/jquery-ui.min.js"></script>

<script type="text/javascript" src="../js/jquery.spasticNav.js"></script>

<script type="text/javascript" src="../js/jquery-latest.js"></script>

<script type="text/javascript" src="../js/jquery-ui-latest.js"></script>

<script type="text/javascript" src="../js/jquery.layout-latest.js"></script>

<script type="text/javascript" src="../js/ui/jquery.ui.position.js"></script>

<script type="text/javascript" src="../js/ui/jquery.ui.core.js"></script>

<script type="text/javascript" src="../js/ui/jquery.ui.widget.js"></script>
<script type="text/javascript" src="..js//ui/jquery.ui.tabs.js"></script>
<script type="text/javascript" src="../js/ui/jquery.ui.dialog.js"></script>
<script type="text/javascript" src="../js/ui/jquery.ui.button.js"></script>

<script type="text/javascript"
	src="../js/jquery.layout.resizePaneAccordions-1.0.js"></script>

<script type="text/javascript" src="../js/themeswitchertool.js"></script>

<script type="text/javascript" src="../js/debug.js"></script>

</head>

<body style="background-color: #fffaf1;" alink="#ee0000" link="#0000ee" vlink="#551a8b">
	<div class="ui-layout-south ui-widget-content ui-state-error"
		style="display: none;background:#fffaf1;">
		
		<a href="#" style="padding-left: 400pt;"> Site map </a>
		&nbsp;&nbsp;&nbsp;
		<a href="#"> CRP Policy </a>
		&nbsp;&nbsp;&nbsp;
		<a href="#"> Contact us </a>
	</div> <!-- div for south layout closed here -->

<div class="ui-layout-center" style="display: none;overflow:auto;background-color: #fffaf1;">
<div id="tabs">
<!-- 
<div style="float: right; margin-right: 160px;">
	<button onClick="removeUITheme(); myLayout.resizeAll()">Remove
	Theme</button>
</div>
 -->
	<div id="container">
	<ul id="nav" style="margin-top:-14px;margin-left:350px;height:34px;" >
		<li><a href="#">Home</a></li>
		<li id="selected"><a href="#">Dash Board</a></li>
		<li><a href="#">UI Play back</a></li>
		<li><a href="#">Profiler</a></li>
		<li><a href="#">DB Viewer</a></li>
		<li><a href="#"> <span class="add_tab" style="height:0px;" id="../adminview/CRPAdmin.html">Admin</span></a></li>
		<!--  <li><a href="#">About Us</a></li>   
	        <li><a href="#">Contact us</a></li> -->
	</ul>
	</div>

	<script type="text/javascript">
	    $('#nav').spasticNav();
	</script>
	<div style="margin-top:27px;display:block">
	</div>
	<ul>
		<li>
			<a href="#tabs-1">Dash board view</a><span class="ui-icon ui-icon-close">Remove Tab</span>
		</li>
		</span>
	</ul>
<div id="tabs-1" >	
<div class="leftcol" style="display:block;">
<jsp:include page="DashBoardLeftFrame.html"/>
</div>
<div style="position:absolute;margin-left:150px;top:50px" >
<table>
<tr>
<td>
<div class="bigr add_tab" id="../projectview/project.jsp" style="position: relative;">
	<div style="top: 0px; left: 0px; z-index: 0;"> 
	     <img  class="imgmain"  title="abc" alt="abc" src="../images/grn_rnd_rectangle.gif" > 
	</div>
	<div class="tophdrtext">
		<p class="rrhdrtext"><b> Load Test1</b> </p>
	</div>
<div class="bigrrmid" >
<p class="rrmidtext">
</p>
<table class="rrmidtext" 
 style="text-align: left; left: 128px; background-color: transparent; width: 356px;"
 border="0" cellpadding="2" cellspacing="2">
  <tbody>
    <tr>
      <td>Request-Response Pairs</td>
      <td style="background-color: transparent;">300</td>
    </tr>
    <tr>
      <td>CPU Overhead</td>
      <td>20%</td>
    </tr>
    <tr>
      <td>IO &nbsp;Overhead</td>
      <td>10%</td>
    </tr>
    <tr>
      <td>Disk Free Space</td>
      <td>67%</td>
    </tr>
    <tr>
      <td>Failures</td>
      <td>0</td>
    </tr>
    <tr>
      <td>Average Response Time</td>
      <td>20ms</td>
    </tr>
    <tr>
      <td>Capture Throughput</td>
      <td>2000 reqs/sec</td>
    </tr>
    <tr>
      <td>Records Filtered</td>
      <td>20%</td>
    </tr>
    <tr>
      <td>Compression Ratio</td>
      <td>1:6</td>
    </tr>
    <!-- 
    <tr>
    <td colspan="2">
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
		</div>
    </td>
    </tr>
     -->
  </tbody>
</table>
<p class="rrmidtext"><br>
</p>
</div>
</div> 
<!-- bigr end -->
</td>
<td>
<div class="bigr add_tab" id="../projectview/replay.jsp" style="position: relative;">
<div style="top: 0px; left: 0px; z-index: 0;"> 
<img class="imgmain"  title="abc" alt="abc" src="../images/red_rnd_rectangle.gif" > 
</div>
<div class="tophdrtext">
<p class="rrhdrtext"> Int Test </p>
</div>
<div class="bigrrmid" >
<p class="rrmidtext"></p>
<table class="rrmidtext" 
 style="text-align: left; left: 28px; background-color: transparent; width: 356px;"
 border="0" cellpadding="2" cellspacing="2">
  <tbody>
    <tr>
      <td>Requests Replayed </td>
      <td style="background-color: transparent;">1300</td>
    </tr>
    <tr>
      <td>CPU Overhead</td>
      <td>20%</td>
    </tr>
    <tr>
      <td>IO &nbsp;Overhead</td>
      <td>10%</td>
    </tr>
    <tr>
      <td>Disk Free Space</td>
      <td>67%</td>
    </tr>
    <tr>
      <td>Failures</td>
      <td>25</td>
    </tr>
    <tr>
      <td>Average Response Time</td>
      <td>20ms</td>
    </tr>
    <tr>
      <td>Replay Progress </td>
      <td>15%</td>
    </tr>
    <tr>
      <td>Current Active Replay Threads </td>
      <td>7</td>
    </tr>
    <tr>
      <td>Replay Throughput </td>
      <td>350 reqs/sec</td>
    </tr>
    <!-- 
    <tr>
    <td colspan="2">
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
		</div>
    </td>
    </tr>
     -->
  </tbody>
</table>
<p class="rrmidtext"><br>
</p>
</div>
</div>
</td>
</tr>

</table> <!-- Table end for displaying top 2 projects -->

<br>
	<div class="smallr" style="position: relative;">
		<div style="top: 0px; left: 0px; z-index: 0;"> <img
		 style="width: 200px; height: 137px;" alt="ghi"
		 src="../images/blnd_grey_rnd_rectangle.gif"> &nbsp; &nbsp; &nbsp; &nbsp; &nbsp;
		&nbsp; 
		</div>
		<div class="smallrect">
		<p class="rrhdrtext"> volume test </p>
		</div>
	</div>
	<div class="smallr" style="position: relative;">
		<div style="top: 0px; left: 0px; z-index: 0;"> <img
		 style="width: 200px; height: 137px;" alt="ghi"
		 src="../images/blnd_grey_rnd_rectangle.gif"> &nbsp; &nbsp; &nbsp; &nbsp; &nbsp;
		&nbsp; 
		</div>
		<div class="smallrect">
		<p class="rrhdrtext"> Cvs Test</p>
		</div>
	</div>
	<div class="smallr" style="position: relative;">
		<div style="top: 0px; left: 0px; z-index: 0;"> <img
		 style="width: 200px; height: 137px;" alt="ghi"
		 src="../images/blnd_grey_rnd_rectangle.gif"> &nbsp; &nbsp; &nbsp; &nbsp; &nbsp;
		&nbsp; 
		</div>
		<div class="smallrect">
		<p class="rrhdrtext"> Performance Test</p>
		</div>
	</div>
	<div class="smallr" style="position: relative;">
		<div style="top: 0px; left: 0px; z-index: 0;"> <img
		 style="width: 200px; height: 137px;" alt="ghi"
		 src="../images/blnd_grey_rnd_rectangle.gif"> &nbsp; &nbsp; &nbsp; &nbsp; &nbsp;
		&nbsp; 
		</div>
		<div class="smallrect">
		<p class="rrhdrtext"> Stress Test </p>
		</div>
	</div> <!-- smallr div closed -->
</div> <!-- inline div closed -->
</div> <!-- tabs-1 div closed -->
</div> <!-- Tabs div closed -->

</div> <!-- center layout closed -->

</body>
</html>