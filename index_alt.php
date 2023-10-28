<?php
// Report all PHP errors
error_reporting(E_ALL);

// include config, language, functions, common, check_installation, check_table and header
include("./include/config.php");

require_once('include/Flug.php');
require_once('include/FlugDB.php');

$flugDB = new FlugDB();

if (isset($mail_feature) and $mail_feature == 1) {
	// include mail classes
	include("./include/mail_classes/class.html.mime.mail.inc");
	include("./include/mail_classes/class.smtp.inc");
} // and if
include("./include/languages/" . $language . ".php");
include("./include/functions.php");
include("./include/common_start.php");
include("./include/check_login.php");
include("./include/header.php");

// HTTP Variables:
/***************** GET ***************************
***************************************************/
// link export_to_csv, set to 1
if (isset($_GET["export_to_csv"])) {
	$export_to_csv = $_GET["export_to_csv"];
} // end if

// contains the where clause, without limit and order e.g. "field = 'value'" (all url encoded)
// navigation bar, order, delete and delete all links, export to csv, show_all link, check existing mail
// why strepslashes? The first time $where_clause is calculated from build_where_clause, and it's ok because all the field contents come from POST with slashes, so when I pass it through links new slashes are added and I have to strip them
if (isset($_GET["where_clause"])) {
	$where_clause = stripslashes($_GET["where_clause"]);
} // end if

// the current page in records results (0......n)
// navigation bar, order, delete and delete all links, export to csv, show_all link
if (isset($_GET["page"])) {
	$page = $_GET["page"];
} // end if

// the field to use to order the results
// navigation bar, order, delete and delete all links, export to csv
// why strepslashes? The first time $order is calculated in the code, so when I pass it through links new slashes are added if the field name contains quotes and I have to strip them
if (isset($_GET["order"])) {
	$order = stripslashes($_GET["order"]);
} // end

// the order type ('ASC'|'DESC')
// navigation bar, order, delete and delete all links, export to csv
if (isset($_GET["order_type"])) {
	$order_type = $_GET["order_type"];
} // end

// the function of this page I wanto to execute ('edit'|'delete'|'search'....)
// navigation bar, order, edit, detail, delete and delete all links, export to csv, bottom links, insert/edit/search form, insert_duplication form
if (isset($_GET["function"])) { // from the homepage
	$function = $_GET["function"];
} // end
else {
	$function = "search";
} // end else

// the field used to identify a single record in edit, delete and detail functions
// edit, delete, detail links, edit form
if (isset($_GET["where_field"])) {
	$where_field = $_GET["where_field"];
} // end if

// the value (of where_field) used to identify a single record in edit, delete and detail functions
// edit, delete, detail links, edit form
if (isset($_GET["where_value"])) {
	$where_value = $_GET["where_value"];
} // end if

// set to 1 when a research has been just executed
// from the search form
if (isset($_GET["execute_search"])) {
	$execute_search = $_GET["execute_search"];
} // end if

// set to 1 after an update
// redirect after update
if (isset($_GET["just_updated"])) {
	$just_updated = $_GET["just_updated"];
} // end if

// set to 1 after an update with no authorization
// update case
if (isset($_GET["just_updated_no_authorization"])) {
	$just_updated_no_authorization = $_GET["just_updated_no_authorization"];
} // end if

// set to 1 after a delete with no authorization
// delete case
if (isset($_GET["just_delete_no_authorization"])) {
	$just_delete_no_authorization = $_GET["just_delete_no_authorization"];
} // end if

// set to 1 after an insert
// redirect after insert
if (isset($_GET["just_inserted"])) {
	$just_inserted = $_GET["just_inserted"];
} // end if

// set to 1 after a delete multiple with authentication enabled
// redirect after delete_all
if (isset($_GET["just_delete_all_authorizated"])) {
	$just_delete_all_authorizated = $_GET["just_delete_all_authorizated"];
} // end if

// from index.php: when the user want to check an existing mailing, set 0 because I don't want to show the add to mailing form below the results table. Otherwise I set it to 1
if (isset($_GET['show_add_to_mailing_form'])) {
	$show_add_to_mailing_form = (int) $_GET['show_add_to_mailing_form'];
} // end if
else {
	$show_add_to_mailing_form = 1;
} // end else

// insert_duplication_form, set to 1 if the user want to insert anyway
if (isset($_GET["insert_duplication"])) {
	$insert_duplication = $_GET["insert_duplication"];
} // end if

/***************** POST ***************************
All the field contents come from POST, and I use them directly ($_POST)


***************************************************/
$action = $dadabik_main_file;

$show_record_numbers_change_table = 1;

// get the array containg label ant other information about the fields
$fields_labels_ar = build_fields_labels_array();

switch ($function) {
	case "insert":
		if ($enable_insert == "1") {
			//			if (!isset($insert_duplication) || $insert_duplication != '1'){ // otherwise would be checked for two times
			// check values
			$check = 0;
			$check = check_required_fields($_POST, $fields_labels_ar);
			if ($check == 0) {
				txt_out($normal_messages_ar["required_fields_missed"], "error_messages_form");
				$go_back_button = "<br>" . $normal_messages_ar["please"] . " <a href=\"javascript:history.back(-1)\">" . $submit_buttons_ar["go_back"] . "</a> " . $normal_messages_ar["and_check_form"];
				txt_out($go_back_button, "error_messages_form");
			} // end if ($check == 0)
			else { // required fields are ok
				// check field lengths
				$check = 0;
				$check = check_length_fields($_POST, $fields_labels_ar);
				if ($check == 0) {
					txt_out($normal_messages_ar["fields_max_length"], "error_messages_form");
					$go_back_button = "<br>" . $normal_messages_ar["please"] . " <a href=\"javascript:history.back(-1)\">" . $submit_buttons_ar["go_back"] . "</a> " . $normal_messages_ar["and_check_form"];
					txt_out($go_back_button, "error_messages_form");
				} // end if ($check == 0)
				else { // fields length are ok
					$check = 0;
					$content_error_type = "";
					$check = check_fields_types($_POST, $fields_labels_ar, $content_error_type);
					if ($check == 0) {
						txt_out($normal_messages_ar["{$content_error_type}_not_valid"], "error_messages_form");
						$go_back_button = "<br>" . $normal_messages_ar["please"] . " <a href=\"javascript:history.back(-1)\">" . $submit_buttons_ar["go_back"] . "</a> " . $normal_messages_ar["and_check_form"];
						txt_out($go_back_button, "error_messages_form");
					} // end if ($check == 0)
					else { // type field are ok
						$check = 0;
						$check = write_temp_uploaded_files($_FILES, $fields_labels_ar);
						if ($check == 0) {
							//Need to add the reason why the upload failed: file too large, improper filename (such as a .php file), or the file couldn't be found.
							txt_out($error_messages_ar["upload_error"], "error_messages_form");
							$go_back_button = "<br>" . $normal_messages_ar["please"] . " <a href=\"javascript:history.back(-1)\">" . $submit_buttons_ar["go_back"] . "</a> " . $normal_messages_ar["and_check_form"];
							txt_out($go_back_button, "error_messages_form");
						} // end if ($check == 0)
						else { // uploaded files are ok
							if (!isset($insert_duplication) || $insert_duplication != '1') {
								// check for duplicated insert in the database
								$sql = build_select_duplicated_query($_POST, $table_name, $fields_labels_ar, $string1_similar_ar, $string2_similar_ar);

								if ($sql != "") { // if there are some duplication
									$check = 0;
									txt_out("<h3>" . $normal_messages_ar["duplication_possible"] . "</h3>");
									if ($display_is_similar == 1) {
										for ($i = 0; $i < count($string1_similar_ar); $i++) {
											txt_out("<br>");
											txt_out($normal_messages_ar["i_think_that"]);
											txt_out($string1_similar_ar[$i]);
											txt_out($normal_messages_ar["is_similar_to"]);
											txt_out($string2_similar_ar[$i]);
										} // end for
									} // end if ($display_is_similar == 1)

									display_sql($sql);
									// execute the select query
									$res_records = execute_db($sql, $conn);

									$results_type = "possible_duplication";
									$where_clause = ""; // I don't need it here, I've just a fixed number of results.

									$results_table = build_results_table($fields_labels_ar, $table_name, $res_records, $results_type, "", "", $action, $where_clause, "", "", "");

									txt_out($normal_messages_ar["similar_records"]);

									$insert_duplication_form = build_insert_duplication_form($_POST, $fields_labels_ar, $table_name, $table_internal_name);

									echo $insert_duplication_form;
									echo $results_table;
								} // end if
							} // end if
							if ($check === 1) {

								// insert a new record
								insert_record($_FILES, $_POST, $fields_labels_ar, $table_name, $table_internal_name);

								if ($insert_again_after_insert == 1) {
									//txt_out("<p>".$normal_messages_ar["insert_result"]);
									txt_out($normal_messages_ar["record_inserted"]);

									txt_out("<h3>" . $normal_messages_ar["insert_record"] . "</h3>");

									$form_type = "insert";
									$res_details = "";

									// re-get the array containg label ant other information about the fields, could be changed in the insert (other choices......)
//										$fields_labels_ar = build_fields_labels_array($table_internal_name, "1");

									// display the form
									$form = build_form($table_name, $action, $fields_labels_ar, $form_type, $res_details, "", "");
									echo $form;
								} // end if
								else {
									$unique_field_name = get_unique_field($table_name);
									$location_url = $site_url . $dadabik_main_file . '?table_name=' . urlencode($table_name) . '&function=search&where_clause=&page=0&just_inserted=1';
									if ($unique_field_name != '') {
										$location_url .= '&order=' . $unique_field_name . '&order_type=desc';
									} // end if

									header('Location: ' . $location_url);
								} // end else
							} // end if
						} // end else
					} // end else
				} // end else
			} // end else
//			} // end if (!isset($insert_duplication) || $insert_duplication != '1')
/*
			else{  // $insert_duplication == "1"
				// insert a new record
				insert_record($_FILES, $_POST, $fields_labels_ar, $table_name, $table_internal_name);

				if ($insert_again_after_insert == 1) {
					txt_out("<p>".$normal_messages_ar["insert_result"]);
					txt_out($normal_messages_ar["record_inserted"]);

					txt_out("<h3>".$normal_messages_ar["insert_record"]."</h3>");

					$form_type = "insert";
					$res_details = "";

					// re-get the array containg label ant other information about the fields, could be changed in the insert (other choices......)
					$fields_labels_ar = build_fields_labels_array($table_internal_name, "1");

					// display the form
					$form = build_form($table_name, $action, $fields_labels_ar, $form_type, $res_details, "", "");
					echo $form;
				} // end if
				else{
					$unique_field_name = get_unique_field($table_name);
					$location_url=$site_url.'form.php?table_name='.urlencode($table_name).'&function=search&where_clause=&page=0&just_inserted=1';
					if ($unique_field_name != '') {
						$location_url .= '&order='.$unique_field_name.'&order_type=desc';
					} // end if

					header('Location: '.$location_url);
				} // end else
				
			} // end else
			*/
		} // end if
		break;
	case "search":

		// $page if index.php is called without parameters (the first time DaDaBIK is launched)
		if (!isset($page)) {
			$page = 0;
		} // end if

		// build the select query
		if (isset($execute_search) && $execute_search === '1') { // it's a search result, the user has just filled the search form, so we have to build the select query
			//if (!isset($where_clause)){ // it's a search result, the user has just filled the search form, so we have to build the select query
			$where_clause = build_where_clause($_POST, $fields_labels_ar, $table_name);
		} // end if
		elseif (!isset($where_clause)) { // when I call index for the first time
			$where_clause = '';
		} // end else

		// save the where_clause without the user part to pass 
		$where_clause_to_pass = $where_clause;

		if ($enable_authentication === 1 && $enable_browse_authorization === 1) { // $ID_user_field_name = '$current_user' where clause part in order to select only the records the current user owns
			$ID_user_field_name = get_ID_user_field_name($fields_labels_ar);

			if ($ID_user_field_name !== false) { // no ID_user fields available, don't use authorization
				if ($where_clause === '') {
					$where_clause = $quote . $table_name . $quote . '.' . $quote . $ID_user_field_name . $quote . " = '" . addslashes($current_user) . "'";
				} // end if
				else {
					$where_clause .= " AND " . $quote . $table_name . $quote . '.' . $quote . $ID_user_field_name . $quote . " = '" . addslashes($current_user) . "'";
				} // end else
			} // end if

		} // end if

		//$sql = "SELECT * FROM ".$quote.$table_name.$quote;

		//		$sql = build_select_part($fields_labels_ar, $table_name);

		if ($where_clause != "") {
			$sql .= " WHERE " . $where_clause;
		} // end if
		// execute the select without limit query to get the number of results
//		$res_records_without_limit = execute_db($sql, $conn);

		//		$select_without_limit = $sql; // I save it because I need it to pass it to build_add_to_mailing_form

		//		$results_number = get_num_rows_db($res_records_without_limit); // get the number of results

		$fluege = $flugDB->filterFluege();

		if (count($fluege) > 0) { // at least one record found

			$pages_number = get_pages_number(count($fluege), $records_per_page); // get the total number of pages

			if (isset($export_to_csv) && $export_to_csv == 1 && $export_to_csv_feature == 1) {
				$csv = build_csv($res_records_without_limit, $fields_labels_ar);
				//exit;

				ob_end_clean();
				//header('Content-Type: application/vnd.ms-excel');
				header("Content-Type: text/x-csv");
				header('Content-Disposition: attachment; filename="' . $table_name . '.csv"');
				//header('Content-Type: application/octet-stream');

				echo $csv;
				exit;
			} // end if

			if (!isset($order)) {
				// get the first field present in the results form as order
				$count_temp = 0;
				$fields_labels_ar_count = count($fields_labels_ar);
				/*				while (!isset($order) && $count_temp < $fields_labels_ar_count) {
															if ($fields_labels_ar[$count_temp]["present_results_search_field"] === '1') {
																$order = $fields_labels_ar[$count_temp]["name_field"];
															} // end if
															$count_temp++;
														} // end while
										*/
				if (!isset($order)) { // if no fields are present in the results form, just use the first field as order, the form wiil be empty, this is just to prevent error messages when composing the sql query
					$order = $fields_labels_ar[0]["name_field"];
				} // end if
			} // end if

			if (!isset($order_type)) {
				$order_type = "ASC";
			} // end if

			if ($page > ($pages_number - 1)) {
				$page = $pages_number - 1;
			} // end if

			//			$sql .= " ORDER BY ";

			// get the index of $fields_labels_ar corresponding to a field
			$count_temp = 0;
			foreach ($fields_labels_ar as $field) {
				if ($field['name_field'] === $order) {
					$field_index = $count_temp;
					break;
				} // end if
				$count_temp++;
			} // end foreach

			/*			if ($fields_labels_ar[$field_index]["primary_key_field_field"] !== ''){

											  $linked_fields_ar = explode($fields_labels_ar[$field_index]['separator_field'], $fields_labels_ar[$field_index]['linked_fields_field']);
											  $is_first = 1;
											  foreach ($linked_fields_ar as $linked_field){
												  //$sql .= $quote.$fields_labels_ar[$field_index]['primary_key_table_field'].$alias_prefix.$fields_labels_ar[$field_index]['alias_suffix_field'].$quote.'.'.$quote.$linked_field.$alias_prefix.$fields_labels_ar[$field_index]['alias_suffix_field'].$quote;
												  $sql .= $quote.$linked_field.$alias_prefix.$fields_labels_ar[$field_index]['alias_suffix_field'].$quote;
												  

												  if ($is_first === 1){ // add the order type just to the first field e.g. order by field_1 DESC, field_2, field_3
													  $sql .= ' '.$order_type;
													  $is_first = 0;
												  } // end if
												  $sql .= ', ';
											  } //end foreach
											  $sql = substr($sql, 0, -2); // deleter the last ', '
										  } // end if
										  else{
											  //$sql .= $table_name.'.'.$fields_labels_ar[$field_index]["name_field"];
											  $sql .= $quote.$table_name.$quote.'.'.$quote.$fields_labels_ar[$field_index]["name_field"].$quote;
											  $sql .= ' '.$order_type;
										  } // end else
							  */
			// add limit clause
			/*$sql .= " LIMIT ".$page*$records_per_page." , ".$records_per_page;

									   // execute the select query
									   $res_records = execute_db($sql, $conn);
						   */
			if (isset($just_inserted) && $just_inserted == "1") {
				//txt_out("<p>".$normal_messages_ar["insert_result"]);
				txt_out('<p>' . $normal_messages_ar["record_inserted"]);
			} // end if

			if (isset($just_delete_all_authorizated) && $just_delete_all_authorizated == "1" && $enable_browse_authorization === 0) {
				txt_out('<p>' . $error_messages_ar["deleted_only_authorizated_records"]);
			} // end if

			if (isset($just_delete_no_authorization) && $just_delete_no_authorization == "1") {
				txt_out('<p>' . $error_messages_ar["no_authorization_update_delete"]);
			} // end if

			//			display_sql($sql);

			txt_out("<br>" . count($fluege) . " " . $normal_messages_ar["records_found"], "n_results_found");

			/*			if ($enable_delete == "1" && $enable_delete_all_feature === 1) {
											  echo " <a class=\"onlyscreen\" onclick=\"if (!confirm('".$normal_messages_ar['confirm_delete?']."')){ return false;}else if (!confirm('".$normal_messages_ar['really?']."')){ return false;}\" href=\"".$action."?table_name=". urlencode($table_name)."&function=delete_all&where_clause=".urlencode($where_clause_to_pass)."&page=".$page."&order=".urlencode($order)."&order_type=".$order_type."\">".$normal_messages_ar['delete_all']."</a>";
										  } // end if
							  */
			if (count($fluege) > $records_per_page) { // display the navigation bar

				txt_out("<br><font class=\"page_n_of_m\">" . $normal_messages_ar["page"] . ($page + 1) . $normal_messages_ar["of"] . $pages_number . "</font>"); // "Page n of x" statement

				// build the navigation tool
				$navigation_tool = build_navigation_tool($where_clause_to_pass, $pages_number, $page, $action, "", $order, $order_type);

				// display the navigation tool
				echo "&nbsp;&nbsp;&nbsp;&nbsp;" . $navigation_tool . "<br><br>";
			} // end if ($results_number > $records_per_page)

			$results_type = "search";

			// build the HTML results table
//			$results_table = build_results_table($fields_labels_ar, $table_name, $res_records, $results_type, "", "", $action, $where_clause_to_pass, $page, $order, $order_type);
//			echo $results_table;

			?>
			<table id="dg" title="Flugbuch" style="height:600px" toolbar="#toolbar" pagination="true" idField="id" ctrlSelect="true"
				autoRowHeight="false" pagePosition="bottom" rownumbers="false" fitColumns="true" striped="true" singleSelect="true">
				<thead>
					<tr>
						<th field="Datum" width=93 sortable="true" id="Datum" name="Datum" editor="{ type:'validatebox', options:{ required:true, validType:'minDate[6,8]'}}">Datum</td>
					</tr>
				</thead>
			</table>
			<table>
				<?php
				foreach ($fluege as $flug) {
					?>
					<tr>
						<td>
							<div style="" class="datagrid-cell datagrid-cell-c2-Datum">
								<?= $flug->datum ?>
							</div>
						</td>
						<td>
							<?= $flug->startzeit ?>
						</td>
						<td>
							<?= $flug->landezeit ?>
						</td>
						<td>
							<?= $flug->kennzeichen ?>
						</td>
						<td>
							<?= $flug->muster ?>
						</td>
						<td>
							<?= $flug->pilot ?>
						</td>
						<td>
							<?= $flug->besatzung ?>
						</td>
						<td>
							<?= $flug->gaeste ?>
						</td>
						<td>
							<?= $flug->flugart ?>
						</td>
						<td>
							<?= $flug->startplatz ?>
						</td>
						<td>
							<?= $flug->zielplatz ?>
						</td>
						<td>
							<?= $flug->flugleiter ?>
						</td>
						<td>
							<?= $flug->geschleppter ?>
						</td>
						<!-- <td><?= $flug->anzahl ?></td>-->
						<td>
							<?= $flug->schlepphoehe ?>
						</td>
						<td>
							<?= $flug->betrag ?>
						</td>
						<td>
							<?= $flug->bezahlt ?>
						</td>
						<td>
							<?= $flug->bstart ?>
						</td>
						<td>
							<?= $flug->bstop ?>
						</td>
						<td>
							<?= $flug->bemerkung ?>
						</td>
					</tr>
					<?php
				}
				?>
			</table>
			<?php

			if ($export_to_csv_feature == 1) {
				echo "<a href=\"" . $action . "?table_name=" . urlencode($table_name) . "&function=" . $function . "&where_clause=" . urlencode($where_clause_to_pass) . "&page=" . $page . "&order=" . urlencode($order) . "&order_type=" . $order_type . "&export_to_csv=1\">";

				txt_out($normal_messages_ar["export_to_csv"] . "</a>", "export_to_csv");

				echo "</a>";
			}

			if (isset($mail_feature) && $mail_feature == 1 && $show_add_to_mailing_form === 1) { // e-mail feature activated & show_add_to_mailing_form enabled

				$sql = "select name_mailing from mailing_tab where sent_mailing = '0' order by date_created_mailing desc";

				// execute the query
				$res_mailing = execute_db($sql, $conn);
				if (get_num_rows_db($res_mailing) > 0) { // at least one mailing created
					$add_to_mailing_form = build_add_to_mailing_form($res_mailing, $select_without_limit, $results_number);
					txt_out("<br><table><tr><td>" . $add_to_mailing_form . "</td><td valign=\"top\">" . $normal_messages_ar["all_records_found"] . "</td></tr></table>");
				} // end if
			} // end if
		} // end if
		else {
			display_sql($sql);
			txt_out($normal_messages_ar["no_records_found"]);
		} // end else

		break;
	case "details":
		if ($enable_details == "1" && ($enable_authentication === 0 || $enable_browse_authorization === 0 || current_user_is_owner($where_field, $where_value, $table_name, $fields_labels_ar))) {
			// build the details select query
			//$sql = "select * from ".$quote.$table_name.$quote." where ".$quote.$where_field.$quote." = '".$where_value."'";

			$sql = build_select_part($fields_labels_ar, $table_name);

			$sql .= " where " . $quote . $table_name . $quote . '.' . $quote . $where_field . $quote . " = '" . $where_value . "' LIMIT 1";

			display_sql($sql);

			txt_out("<h3>" . $normal_messages_ar["details_of_record"] . "</h3>");

			// execute the select query
//			$res_details = execute_db("$sql", $conn);

			// build the HTML details table
			$details_table = build_details_table($fields_labels_ar, $res_details);

			// display the HTML details table
			echo $details_table;
		} // end if
		else {
			txt_out("<p>" . $error_messages_ar["no_authorization_view"] . "</p>");
		} // end else
		break;
	case "edit":
		if ($enable_edit == "1" && ($enable_authentication === 0 || $enable_browse_authorization === 0 || current_user_is_owner($where_field, $where_value, $table_name, $fields_labels_ar))) {

			if (isset($just_updated) && $just_updated == "1") {
				//txt_out("<h3>".$normal_messages_ar["update_result"]."</h3>");
				txt_out("<p>" . $normal_messages_ar["record_updated"] . "</p>");
			}

			if (isset($just_updated_no_authorization) && $just_updated_no_authorization == "1") {
				txt_out("<p>" . $error_messages_ar["no_authorization_update_delete"] . "</p>");
			}

			// build the details select query
			$sql = "select * from " . $quote . $table_name . $quote . " where " . $quote . $table_name . $quote . '.' . $quote . $where_field . $quote . " = '" . $where_value . "'";

			display_sql($sql);
			txt_out("<h3>" . $normal_messages_ar["edit_record"] . "</h3>");
			if (required_field_present($fields_labels_ar)) { // at least one required field
				txt_out("<p>" . $normal_messages_ar["required_fields_red"] . "</p>");
			} // end if

			// execute the select query
//			$res_details = execute_db($sql, $conn);

			$form_type = "update";

			// display the form
			$form = build_form($table_name, $action, $fields_labels_ar, $form_type, $res_details, $where_field, $where_value);
			echo $form;
		} // end if
		else {
			txt_out("<p>" . $error_messages_ar["no_authorization_view"] . "</p>");
		} // end else
		break;
	case "update":
		if ($enable_edit == "1") {
			$check = 0;
			$check = check_required_fields($_POST, $fields_labels_ar);
			if ($check == 0) {
				txt_out($normal_messages_ar["required_fields_missed"], "error_messages_form");
				$go_back_button = "<br>" . $normal_messages_ar["please"] . " <a href=\"javascript:history.back(-1)\">" . $submit_buttons_ar["go_back"] . "</a> " . $normal_messages_ar["and_check_form"];
				txt_out($go_back_button, "error_messages_form");
			} // end if ($check == 0)
			else { // required fields are ok
				// check field lengths
				$check = 0;
				$check = check_length_fields($_POST, $fields_labels_ar);
				if ($check == 0) {
					txt_out($normal_messages_ar["fields_max_length"], "error_messages_form");
					$go_back_button = "<br>" . $normal_messages_ar["please"] . " <a href=\"javascript:history.back(-1)\">" . $submit_buttons_ar["go_back"] . "</a> " . $normal_messages_ar["and_check_form"];
					txt_out($go_back_button, "error_messages_form");
				} // end if ($check == 0)
				else { // fields length are ok
					$check = 0;
					$content_error_type = "";
					$check = check_fields_types($_POST, $fields_labels_ar, $content_error_type);
					if ($check == 0) {
						txt_out($normal_messages_ar["{$content_error_type}_not_valid"], "error_messages_form");
						$go_back_button = '<br>' . $normal_messages_ar["please"] . " <a href=\"javascript:history.back(-1)\">" . $submit_buttons_ar["go_back"] . "</a> " . $normal_messages_ar["and_check_form"];
						txt_out($go_back_button, "error_messages_form");
					} // end if ($check == 0)
					else { // type field are ok
						$check = 0;
						$check = write_temp_uploaded_files($_FILES, $fields_labels_ar);

						if ($check == 0) {
							//Need to add the reason why the upload failed: file too large, improper filename (such as a .php file), or the file couldn't be found.
							txt_out($error_messages_ar["upload_error"], "error_messages_form");
							$go_back_button = "<br>" . $normal_messages_ar["please"] . " <a href=\"javascript:history.back(-1)\">" . $submit_buttons_ar["go_back"] . "</a> " . $normal_messages_ar["and_try_again"];
							txt_out($go_back_button, "error_messages_form");
						} else { // filed uploaded are ok
							$update_type = "internal";

							if ($enable_authentication === 0 || $enable_update_authorization === 0 || current_user_is_owner($where_field, $where_value, $table_name, $fields_labels_ar)) {

								// update the record
								update_record($_FILES, $_POST, $fields_labels_ar, $table_name, $table_internal_name, $where_field, $where_value, $update_type);
								header('Location:' . $site_url . $dadabik_main_file . '?table_name=' . urlencode($table_name) . '&function=edit&where_field=' . urlencode($where_field) . "&where_value=" . urlencode($where_value) . '&just_updated=1');
							} // end if
							else {
								header('Location:' . $site_url . $dadabik_main_file . '?table_name=' . urlencode($table_name) . '&function=edit&where_field=' . urlencode($where_field) . "&where_value=" . urlencode($where_value) . '&just_updated_no_authorization=1');
							} // end else

						} // end else
					} // end else
				} // end else
			} // end else
		} // end if
		break;
	case "delete":
		if ($enable_delete == "1") {

			$location_url = $site_url . $dadabik_main_file . '?table_name=' . urlencode($table_name) . '&function=search&where_clause=' . urlencode($where_clause);
			if (isset($page) && isset($order) && isset($order_type)) {
				$location_url .= '&page=' . $page . '&order=' . urlencode($order) . '&order_type=' . $order_type;
			} else {
				$location_url .= '&page=0';
			}


			if ($enable_authentication === 0 || $enable_delete_authorization === 0 || current_user_is_owner($where_field, $where_value, $table_name, $fields_labels_ar)) {
				delete_record($table_name, $where_field, $where_value);
			} // end if
			else {
				$location_url .= '&just_delete_no_authorization=1';
			} // end else

			header('Location: ' . $location_url);
		} // end if
		break;
	case "delete_all":
		if ($enable_delete == "1" && $enable_delete_all_feature === 1) {

			$ID_user_field_name = get_ID_user_field_name($fields_labels_ar);

			delete_multiple_records($table_name, $where_clause, $ID_user_field_name);

			$location_url = $site_url . $dadabik_main_file . '?table_name=' . urlencode($table_name) . "&function=search&where_clause=&page=0";

			if ($enable_browse_authorization === 0 && $ID_user_field_name !== false) { // if the user see just his owns records the message doesn't make sense
				$location_url .= '&just_delete_all_authorizated=1';
			} // end if

			header('Location: ' . $location_url);
		} // end if
		break;
	case "show_insert_form":
		if ($enable_insert == "1") {
			txt_out("<h3>" . $normal_messages_ar["insert_record"] . "</h3>");
			if (required_field_present($fields_labels_ar)) { // at least one required field
				txt_out("<p>" . $normal_messages_ar["required_fields_red"] . "</p>");
			} // end if
			$form_type = "insert";
			$res_details = "";
			// display the form
			$form = build_form($table_name, $action, $fields_labels_ar, $form_type, $res_details, "", "");
			echo $form;
		} // end if
		break;
	case "show_search_form":
		txt_out("<h3>" . $normal_messages_ar["search_records"] . "</h3>");

		$form_type = "search";
		$res_details = "";
		// display the form
		$form = build_form($table_name, $action, $fields_labels_ar, $form_type, $res_details, "", "");
		echo $form;
		break;
} // end swtich ($function)

// include footer
include("./include/footer.php");
?>