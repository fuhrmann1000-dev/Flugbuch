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
<title>DaDaBIK - http://www.dadabik.org/ - Administration interface</title>
<link rel="stylesheet" href="css/styles_screen.css" type ="text/css" media="screen">
<link rel="stylesheet" href="css/styles_print.css" type ="text/css" media="print">
<meta name="Generator" content="DaDaBIK 3.2 - http://www.dadabik.org/">
<script language="JavaScript">
<!--
function fill_cap(city_field){

	temp = 'document.contacts_form.'+city_field+'.value';

	city = eval(temp);
	cap=open("fill_cap.php?city="+escape(city),"schermo","toolbar=no,directories=no,menubar=no,width=170,height=250,resizable=yes");
}
//-->
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
<table class="main_table" cellpadding="10">
<tr>
<td valign="top">
<a href="index.php">Exit the administration area</a>
<h1>DaDaBIK - Administration</h1>