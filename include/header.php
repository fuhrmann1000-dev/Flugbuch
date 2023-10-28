<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
	<meta charset="UTF-8">
	<meta http-equiv="Content-Type" content="text/html" charset="UTF-8">
	<meta name="author" content="Marco Goebel (mg@zurk.org)">
	<link rel="stylesheet" type="text/css" href="css/themes/ui-cupertino/easyui.css">
	<link rel="stylesheet" type="text/css" href="css/main.css">
	<link rel="stylesheet" type="text/css" href="css/themes/icon.css">
	<script type="text/javascript" src="lib/jquery-1.6.1.min.js"></script>
	<script type="text/javascript" src="lib/jquery.easyui.min.js"></script>
	<script type="text/javascript" src="lib/jquery.edatagrid.js"></script>
        <script type="text/javascript" src="lib/locale/easyui-lang-de.js"></script>
        <script type="text/javascript" src="lib/datagrid-filter.js"></script>
        <script type="text/javascript" src="lib/function.js"></script>
	<title>Hauptflugbuch SLP Altes Lager</title>
</head>

<body <?php
if (isset($_GET["type_mailing"])) {
	if ($_GET["type_mailing"] == "labels") {
		echo " leftmargin=\"0\" topmargin=\"0\" marginwidth=\"0\" marginheight=\"0\" onload=\"javascript:alert('" . $normal_messages_ar["print_warning"] . "')\"";
	} // end if
} // end if
?>>
	<table width="980" class="main_table" cellpadding="2">
		<tr>
			<td valign="top">
				<h3>Hauptflugbuch SLP Altes Lager</h3>
				<ul>
					<li><strong>Start-/Landezeit</strong> bitte immer in <strong>UTC</strong> eintragen. UTC ist zur
						Sommerzeit -2h, bei Winterzeit -1h. Bitte das Format <strong>00:00</strong> einhalten.</li>
					<li>Beim Anfliegen von Verkehrslandepl&auml;tzen bitte die <a
							href="http://worldaerodata.com/countries/Germany.php"
							target="_blank"><strong>ICAO-Codes</strong></a> eintragen.</li>
					<li>Eingabe der laufenden Fl&uuml;ge bitte sp&auml;testens zum <strong>Monatsende</strong>, da dann
						ein Ausdruck f&uuml;r die Beh&ouml;rde erfolgt.</li>
				</ul>