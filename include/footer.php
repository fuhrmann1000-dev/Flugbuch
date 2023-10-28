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
<?php

if ($show_record_numbers_change_table !== 0) {
	// get the number of contacts in the database
//	$sql = "SELECT COUNT(*) FROM " . $table_name;

	if ($enable_authentication === 1 && $enable_browse_authorization === 1) { // $ID_user_field_name = '$_SESSION['logged_user_infos_ar']['username_user']' where clause part in order to select only the records the current user owns
		$ID_user_field_name = get_ID_user_field_name($fields_labels_ar);

		if ($ID_user_field_name !== false) { // no ID_user fields available, don't use authorization
			$sql .= " WHERE " . $quote . $table_name . $quote . '.' . $quote . $ID_user_field_name . $quote . " = '" . addslashes($_SESSION['logged_user_infos_ar']['username_user']) . "'";
		} // end if

	} // end if

	// execute the select query
/*	$res_count = execute_db($sql, $conn);

	while ($count_row = fetch_row_db($res_count)) {
		$records_number = $count_row[0];
	} // end while
*/
	$change_table_select = build_change_table_select();
} // end if


?>

<?php
if ($show_record_numbers_change_table !== 0) {
	?>

	<hr class="onlyscreen">
	<table width="100%" class="onlyscreen">
		<tr>
			<td align="left">
				<?php echo '<font class="total_records">(' . $normal_messages_ar["total_records"] . ':' /*. $records_number */. ')</font>'; ?>
				| <a class="home" href="<?php echo $dadabik_main_file; ?>?table_name=<?php echo urlencode($table_name); ?>">
					<?php echo $normal_messages_ar["home"]; ?>
				</a>
				<?php
/*				if ($enable_insert == "1") {
					?>
					| <a class="bottom_menu"
						href="<?php $dadabik_main_file; ?>?function=show_insert_form&table_name=<?php echo urlencode($table_name); ?>">
						<?php echo $submit_buttons_ar["insert_short"]; ?>
					</a>
					<?php
				}
*/				
				?>
				| <a class="bottom_menu"
					href="<?php $dadabik_main_file; ?>?function=show_search_form&table_name=<?php echo urlencode($table_name); ?>">
					<?php echo $submit_buttons_ar["search_short"]; ?>
				</a> | <a class="bottom_menu"
					href="<?php $dadabik_main_file; ?>?function=search&where_clause=&page=0&table_name=<?php echo urlencode($table_name); ?>">
					<?php echo $normal_messages_ar["show_all"]; ?>
				</a>
				<?php
				if ($table_name === $users_table_name && ($function === 'edit' || $function === 'show_insert_form')) {
					?>
					| <a class="bottom_menu" href="javascript:void(generic_js_popup('pwd.php','',400,300))">
						<?php echo $login_messages_ar['pwd_gen_link']; ?>
					</a>
					<?php
				}
				?>
				<?php
				if ($enable_authentication === 1) {
					?>
					| <a class="bottom_menu" href="<?php echo $dadabik_login_file; ?>?function=logout">
						<?php echo $normal_messages_ar["logout"]; ?>
					</a>
					<?php
				}
				?>
				| <a class="bottom_menu" href="#">
					<?php echo $normal_messages_ar["top"]; ?>
				</a>
			</td>
		</tr>
		<tr>
			<td>
				<?php
				if ($change_table_select != "") {
					?>
				</td>
				<?php
				}
				?>
		</tr>
	</table>


	<?php
}
?>
<p><a href="php_doc_xls_gen.php" target="_blank">Hauptflugbuch als Exceltabelle downloaden</a></p>
<p><a href="php_doc_xls_gen2.php" target="_blank">Flugbuch mit allen Eintr�gen als Exceltabelle downloaden</a></p>
<h3>&nbsp;</h3>
<h3>Hauptflugbuch SLP Altes Lager 2011</h3>
<p><a href="Flugbuch_2011_DCB.xls" target="_blank">Hauptflugbuch 2011 als Exceltabelle downloaden</a></p>
<p><a href="Flugbuch_2011_DCB_komplett.xls" target="_blank">Flugbuch 2011 mit allen Eintr&auml;gen als Exceltabelle
		downloaden</a></p>
<p>&nbsp;</p>
<h3>Hauptflugbuch SLP Altes Lager 2010</h3>
<p><a href="Flugbuch_2010_DCB.xls" target="_blank">Hauptflugbuch 2010 als Exceltabelle downloaden</a></p>
<p><a href="Flugbuch_2010_DCB_komplett.xls" target="_blank">Flugbuch 2010 mit allen Eintr&auml;gen als Exceltabelle
		downloaden</a></p>
<p>&nbsp;</p>
<h3>Hauptflugbuch SLP Altes Lager 2009</h3>
<p><a href="Flugbuch_2009_DCB.xls" target="_blank">Hauptflugbuch 2009 als Exceltabelle downloaden</a></p>
<p><a href="Flugbuch_2009_DCB_komplett.xls" target="_blank">Flugbuch 2009 mit allen Eintr&auml;gen als Exceltabelle
		downloaden</a></p>
<p>&nbsp;</p>
<h3>Hauptflugbuch SLP Altes Lager 2008</h3>
<p><a href="Flugbuch_2008_DCB.xls" target="_blank">Hauptflugbuch 2008 als Exceltabelle downloaden</a></p>
<p><a href="Flugbuch_2008_DCB_komplett.xls" target="_blank">Flugbuch 2008 mit allen Eintr&auml;gen als Exceltabelle
		downloaden</a></p>
<p>&nbsp;</p>
<h3>Hauptflugbuch SLP Altes Lager 2007</h3>
<p><a href="Flugbuch_2007_DCB.xls" target="_blank">Hauptflugbuch 2007 als Exceltabelle downloaden</a></p>
<p><a href="Flugbuch_2007_DCB_komplett.xls" target="_blank">Flugbuch 2007 mit allen Eintr&auml;gen als Exceltabelle
		downloaden</a></p>
<p>&nbsp;</p>
<h3>Hauptflugbuch SLP Altes Lager 2006</h3>
<p><a href="Flugbuch_2006_DCB.xls" target="_blank">Hauptflugbuch 2006 als Exceltabelle downloaden</a></p>
<p><a href="Flugbuch_2006_DCB_komplett.xls" target="_blank">Flugbuch 2006 mit allen Eintr�gen als Exceltabelle
		downloaden</a></p>
<p>&nbsp;</p>
</td>
</tr>
</table>

</body>

</html>