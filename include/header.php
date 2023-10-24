<?php
/*
***********************************************************************************
DaDaBIK (DaDaBIK is a DataBase Interfaces Kreator) http://www.dadabik.org/
Copyright (C) 2001-2005  Eugenio Tacchini

This program is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
as published by the Free Software Foundation; either version 2
of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.

If you want to contact me by e-mail, this is my address: eugenio.tacchini@unicatt.it

***********************************************************************************

I teach Web development at the Master of MiNE (Management in the Network Economy) Program, a Berkeley-style program in Italy.
If you want to know more about it please visit: http://mine.pc.unicatt.it/

***********************************************************************************
*/
?>
<!doctype html public "-//W3C//DTD HTML 4.0 Transitional//EN">
<html>
<head>
<title>DCB</title>
<link rel="stylesheet" href="css/styles_screen.css" type ="text/css" media="screen">
<link rel="stylesheet" href="css/styles_print.css" type ="text/css" media="print">
<meta name="Generator" content="DaDaBIK 3.2  - http://www.dadabik.org/">
<script language="JavaScript">
<!--
function fill_cap(city_field){

	temp = 'document.contacts_form.'+city_field+'.value';

	city = eval(temp);
	cap=open("fill_cap.php?city="+escape(city),"schermo","toolbar=no,directories=no,menubar=no,width=170,height=250,resizable=yes");
}
//-->
</script>

<script language="Javascript1.2">
<!-- // load htmlarea
_editor_url = "include/htmlarea/";                     // URL to htmlarea files
var win_ie_ver = parseFloat(navigator.appVersion.split("MSIE")[1]);
if (navigator.userAgent.indexOf('Mac')        >= 0) { win_ie_ver = 0; }
if (navigator.userAgent.indexOf('Linux')      >= 0) { win_ie_ver = 0; }
if (navigator.userAgent.indexOf('Windows CE') >= 0) { win_ie_ver = 0; }
if (navigator.userAgent.indexOf('Opera')      >= 0) { win_ie_ver = 0; }
if (win_ie_ver >= 5.5) {
  document.write('<scr' + 'ipt src="' +_editor_url+ 'editor.js"');
  document.write(' language="Javascript1.2"></scr' + 'ipt>');  
}
else {
	document.write('<scr'+'ipt>function editor_generate() { return false; }</scr'+'ipt>');
}
// -->
</script>


<script language="Javascript">
//opens a js popup with customizable options. Popup will close and open
//again upon call from pwd-generator link
var mywindow;
function generic_js_popup(url,name,w,h){
	if (mywindow!=null && !mywindow.closed){
	mywindow.close();
	}
var options;
options = "resizable=yes,toolbar=0,status=1,menubar=0,scrollbars=1, width=" + w + ",height=" + h + ",left="+(screen.width-w)/2+",top="+(screen.height-h)/6; 
mywindow = window.open(url,name,options);
mywindow.focus();
}
</script>
</head>

<body 
<?php
if (isset($_GET["type_mailing"])){
	if ($_GET["type_mailing"] == "labels") {
		echo " leftmargin=\"0\" topmargin=\"0\" marginwidth=\"0\" marginheight=\"0\" onload=\"javascript:alert('".$normal_messages_ar["print_warning"]."')\"";
	} // end if
} // end if
?>
>
<table width="980" class="main_table" cellpadding="2">
<tr>
<td valign="top"><h3>Hauptflugbuch SLP Altes Lager</h3>
<ul>
<li><strong>Start-/Landezeit</strong> bitte immer in <strong>UTC</strong> eintragen. UTC ist zur Sommerzeit  -2h, bei Winterzeit -1h. Bitte das Format <strong>00:00</strong> einhalten.</li>
<li>Beim Anfliegen von Verkehrslandepl&auml;tzen bitte die <a href="http://worldaerodata.com/countries/Germany.php" target="_blank"><strong>ICAO-Codes</strong></a> eintragen.</li>
<li>Eingabe der laufenden Fl&uuml;ge bitte sp&auml;testens zum <strong>Monatsende</strong>, da dann ein Ausdruck f&uuml;r die Beh&ouml;rde erfolgt.</li>
</ul>
