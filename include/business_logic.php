<?php

require_once('DB.php');

/*
function check_login()
//goal: check on role to adjust permission in case $current_user is an admin
// input:
// output:
{
	/*global $simple_login_user;

	$simple_login_user->CheckLogin();
	$simple_login_user->ManageUsers(); // put this line if you want the admin to see the manager interface
	*/
/*lz:*****************ADVANCED LOGIN**************/
//goal: this function is only called when entering the users table (set in config and called in common-start) to check
//that only users with admin-role have permission to access this table
/*
	global $auth;
	
	$role = $auth->getSessionVar('role');
	
	return $role;
	
} // end function check_login()
*/

function check_login()
// goal: check if the user is logged, if not display the login page
// input:
// output:
{
	global $simple_login_user;

	$simple_login_user->CheckLogin();
	$simple_login_user->ManageUsers(); // put this line if you want the admin to see the manager interface

} // end function check_login()


/*
function get_user()
// goal: get the current user
// input: nothing
// output: $user
{
	/*global $simple_login_user;
	
	$user = $simple_login_user->userName();*/
/*lz:*****************ADVANCED LOGIN**************/
/*
	global $auth;
	
	$user = $auth->getSessionVar('login');

	return $user;
} // end function get_ID_user
*/

function get_user()
// goal: get the current user
// input: nothing
// output: $user
{
	global $simple_login_user;

	$user = $simple_login_user->userName();

	return $user;
} // end function get_ID_user


function current_user_is_owner($where_field, $where_value, $table_name, $fields_labels_ar)
{
	// goal: check if authentication is required and, if yes, if the current user is the owner so that he can delete/update the record.
// input: $where_field, $where_value, $table_name (to identify the record), $fields_labels_ar
// output: true|false

	global $current_user, $conn, $db_name, $host, $user,	$pass;

	$db = new DB();
	$db->connect($host, $user, $pass, $db_name);

	// get the name of the field that has ID_user type
	$ID_user_field_name = get_ID_user_field_name($fields_labels_ar);

	if ($ID_user_field_name === false) {
		return true; // no ID_user field type, no authentication needed
	} // end if
	else {
		// check if the owner of the record is current_user
		$sql = "SELECT " . $ID_user_field_name . " FROM "  . $table_name  . " WHERE " .  $where_field  . " = '" . $where_value . "' AND "  . $ID_user_field_name . " = '" . addslashes($current_user) . "'";

		$res = $db->execute($sql);
//		$num_rows = get_num_rows_db($res);

/*		if ($num_rows === 1) {
			return true;
		} // end if
		else {
			return false;
		} // end else*/
	} // end else

} // end function current_user_is_owner()

function get_ID_user_field_name($fields_labels_ar)
// goal: get the name of the first ID_user type field
//input: $fields_labels_ar
//output: the field name or false if there aren't any ID_user field so the authentication is not needed
{
	$ID_user_field_name = false;

	$fields_labels_ar_count = count($fields_labels_ar);
	$i = 0;

	while ($i < $fields_labels_ar_count && $ID_user_field_name === false) {
		if ($fields_labels_ar[$i]['type_field'] === 'ID_user') {
			$ID_user_field_name = $fields_labels_ar[$i]['name_field'];
		} // end if
		$i++;
	} // end while

	return $ID_user_field_name;
} // end function get_ID_user_field_name()

function get_user_infos_ar_from_username_password($username_user, $password_user)
// goal: get information about a user, starting from the username and the password
// input: $username_user, $password_user
// output: the array $user_infos_ar or FALSE if no user is associated to the username and password couple
{
	global $db_name, $host, $user,	$pass, $users_table_name, $users_table_username_field, $users_table_password_field, $users_table_user_type_field, $quote;

	$sql = "SELECT " . $quote . $users_table_username_field . $quote . ", " . $quote . $users_table_user_type_field . $quote . " FROM " . $quote . $users_table_name . $quote . " WHERE " . $quote . $users_table_username_field . $quote . " = '" . $username_user . "' AND " . $quote . $users_table_password_field . $quote . " = '" . md5($password_user) . "'";

	$db = new DB();
	$db->connect($host, $user, $pass, $db_name);
	$db->execute($sql);

/*
	if (get_num_rows_db($res) === 1) {
		$row = fetch_row_db($res);
		$user_infos_ar['username_user'] = $row[$users_table_username_field];
		$user_infos_ar['user_type_user'] = $row[$users_table_user_type_field];
		return $user_infos_ar;
	} // end if
	else {
		return false;
	} // end else
*/
} // end function get_user_infos_ar_from_username_password()




function build_form($table_name, $action, $fields_labels_ar, $form_type, $res_details, $where_field, $where_value)
// goal: build a tabled form by using the info specified in the array $fields_labels_ar
// input: $table_name, array containing labels and other info about fields, $action (the action of the form), $form_type, $res_details, $where_field, $where_value (the last three useful just for update forms)
// global: $submit_buttons_ar, the array containing the values of the submit buttons, $normal_messages_ar, the array containig the normal messages, $select_operator_feature, wheter activate or not displaying "and/or" in the search form, $default_operator, the default operator if $select_operator_feature is not activated, $db_name, $size_multiple_select, the size (number of row) of the select_multiple_menu fields, $table_name
// output: $form, the html tabled form
{
	global $conn, $submit_buttons_ar, $normal_messages_ar, $select_operator_feature, $default_operator, $db_name, $size_multiple_select, $upload_relative_url, $show_top_buttons, $quote, $enable_authentication, $enable_browse_authorization, $current_user;

	switch ($form_type) {
		case 'insert':
			$function = 'insert';
			break;
		case 'update':
			$function = 'update';
			break;
		case 'ext_update':
			$function = 'ext_update';
			break;
		case 'search':
			$function = 'search';
			break;
	} // end switch

	$form = "";
	$form .= "<form name=\"contacts_form\" method=\"post\" action=\"" . $action . "?table_name=" . urlencode($table_name) . "&page=0&function=" . $function;

	if ($form_type == "update" or $form_type == "ext_update") {
		$form .= "&where_field=" . urlencode($where_field) . "&where_value=" . urlencode($where_value);
	}

	if ($form_type == "search") {
		$form .= "&execute_search=1";
	}

	$form .= "\" enctype=\"multipart/form-data\"><table>";

	switch ($form_type) {
		case "insert":
			$number_cols = 4;
			$field_to_ceck = "present_insert_form_field";
			break;
		case "update":
			$number_cols = 4;
			$field_to_ceck = "present_insert_form_field";
			$details_row = fetch_row_db($res_details); // get the values of the details
			if ($show_top_buttons == 1) {
				$form .= "<tr class=\"tr_button_form\"><td colspan=\"" . $number_cols . "\" class=\"td_button_form\"><input class=\"button_form\" type=\"submit\" value=\"" . $submit_buttons_ar[$form_type] . "\"></td></tr>";
			}
			break;
		case "ext_update":
			$number_cols = 4;
			$field_to_ceck = "present_ext_update_form_field";
			$details_row = fetch_row_db($res_details); // get the values of the details
			if ($show_top_buttons == 1) {
				$form .= "<tr class=\"tr_button_form\"><td colspan=\"" . $number_cols . "\" class=\"td_button_form\"><input class=\"button_form\" type=\"submit\" value=\"" . $submit_buttons_ar[$form_type] . "\"></td></tr>";
			}
			break;
		case "search":
			$number_cols = 3;
			$field_to_ceck = "present_search_form_field";
			if ($select_operator_feature == "1") {
				$form .= "<tr class=\"tr_operator_form\"><td colspan=\"" . $number_cols . "\" class=\"td_button_form\"><select name=\"operator\"><option value=\"and\">" . $normal_messages_ar["all_conditions_required"] . "</option><option value=\"or\">" . $normal_messages_ar["any_conditions_required"] . "</option></select></td></tr>";
			} // end if
			else {
				$form .= "<input type=\"hidden\" name=\"operator\" value=\"" . $default_operator . "\">";
			} // end else
			if ($show_top_buttons == 1) {
				$form .= "<tr class=\"tr_button_form\"><td colspan=\"" . $number_cols . "\"><input  class=\"button_form\" type=\"submit\" value=\"" . $submit_buttons_ar[$form_type] . "\"></td></tr>";
			}
			break;
	} // end switch

	for ($i = 0; $i < count($fields_labels_ar); $i++) {
		if ($fields_labels_ar[$i][$field_to_ceck] == "1") { // the user want to display the field in the form

			// build the first coloumn (label)
			//////////////////////////////////
			// I put a table inside the cell to get the same margin of the second coloumn
			$form .= "<tr><td align=\"right\" valign=\"top\"><table><tr><td class=\"td_label_form\">";
			if ($fields_labels_ar[$i]["required_field"] == "1" and $form_type != "search") {
				$form .= "<font class=\"required_field_labels\">";
			} // end if 
			$form .= $fields_labels_ar[$i]["label_field"] . " ";
			if ($fields_labels_ar[$i]["required_field"] == "1") {
				$form .= "</font>";
			} // end if
			$form .= "</td></tr></table></td><td>";
			//////////////////////////////////
			// end build the first coloumn (label)

			// build the second coloumn (input field)
			/////////////////////////////////////////
			$field_name_temp = $fields_labels_ar[$i]["name_field"];
			$primary_key_field_field = $fields_labels_ar[$i]["primary_key_field_field"];
			if ($primary_key_field_field != "") {
				/*
							if (substr($foreign_key_temp, 0, 4) == "SQL:"){
								$sql = substr($foreign_key_temp, 4, strlen($foreign_key_temp)-4);
							} // end if
							else{
							*/
				$primary_key_field_field = $fields_labels_ar[$i]["primary_key_field_field"];
				$primary_key_table_field = $fields_labels_ar[$i]["primary_key_table_field"];
				$primary_key_db_field = $fields_labels_ar[$i]["primary_key_db_field"];
				$linked_fields_field = $fields_labels_ar[$i]["linked_fields_field"];
				$linked_fields_ar = explode($fields_labels_ar[$i]["separator_field"], $linked_fields_field);
				$linked_fields_order_by_field = $fields_labels_ar[$i]["linked_fields_order_by_field"];
				if ($linked_fields_order_by_field !== '') {
					$linked_fields_order_by_ar = explode($fields_labels_ar[$i]["separator_field"], $linked_fields_order_by_field);
				} // end if
				else {
					unset($linked_fields_order_by_ar);
				} // end else

				$linked_fields_order_type_field = $fields_labels_ar[$i]["linked_fields_order_type_field"];

				$sql = "SELECT " . $quote . $primary_key_field_field . $quote;

				$count_temp = count($linked_fields_ar);
				for ($j = 0; $j < $count_temp; $j++) {
					$sql .= ", " . $quote . $linked_fields_ar[$j] . $quote;
				}
				$sql .= " FROM " . $quote . $primary_key_table_field . $quote;

				/*
							   if ($enable_authentication === 1 && $enable_browse_authorization === 1) { // $ID_user_field_name = '$current_user' where clause part in order to select only the records the current user owns
								   $ID_user_field_name = get_ID_user_field_name($fields_labels_ar);

								   if ($ID_user_field_name !== false) { // no ID_user fields available, don't use authorization
									   $sql .= " WHERE ".$quote.$ID_user_field_name.$quote." = '".$current_user."'";
								   } // end if
							   } // end if
							   */

				if (isset($linked_fields_order_by_ar)) {
					$sql .= " ORDER BY ";
					$count_temp = count($linked_fields_order_by_ar);
					for ($j = 0; $j < $count_temp; $j++) {
						$sql .= $quote . $linked_fields_order_by_ar[$j] . $quote . ", ";
					}
					$sql = substr($sql, 0, -2); // delete the last ","
					$sql .= " " . $linked_fields_order_type_field;
				} // end if

				/*
							   29/04/2003 old version

							   $primary_key_field_temp = substr( $foreign_key_temp, (strpos($foreign_key_temp, ".") + 1) ); // remove "table_name."

							   $primary_key_table_temp = substr($foreign_key_temp, 0, strpos($foreign_key_temp, ".")); // remove ".field_name"

							   $sql = "SELECT DISTINCT ".$quote.$primary_key_field_temp.$quote." FROM ".$quote.$primary_key_table_temp.$quote; // e.g. select distinct ID from my_table
							   */
				//} // end else
				/*
							// select the primary key database
							if ($fields_labels_ar[$i]["primary_key_db_field"] != ""){
								select_db($fields_labels_ar[$i]["primary_key_db_field"], $conn);
							} // end if
							*/

				$res_primary_key = execute_db($sql, $conn);

				/*
							// re-select the main database
							select_db($db_name, $conn);
							*/
			} // end if

			if ($form_type == "search") {
				$select_type_select = build_select_type_select($field_name_temp . "_select_type", $fields_labels_ar[$i]["select_type_field"], 0); // build the select type select form (is_equal....)
				$select_type_date_select = build_select_type_select($field_name_temp . "_select_type", $fields_labels_ar[$i]["select_type_field"], 1); // build the select type select form (is_equal....) for date fields, with the first option blank
			} // end if
			else {
				$select_type_select = "";
				$select_type_date_select = "";
			} // end else
			$form .= "<table border=\"0\"><tr>";
			switch ($fields_labels_ar[$i]["type_field"]) {
				case "text":
				case "ID_user":
				case "unique_ID":
					$form .= "<td class=\"td_input_form\">" . $select_type_select . "<input type=\"text\" name=\"" . $field_name_temp . "\"";
					if ($fields_labels_ar[$i]["width_field"] != "") {
						$form .= " size=\"" . $fields_labels_ar[$i]["width_field"] . "\"";
					} // end if
					$form .= " maxlength=\"" . $fields_labels_ar[$i]["maxlength_field"] . "\"";
					if ($form_type == "update" or $form_type == "ext_update") {
						$form .= " value=\"" . htmlspecialchars($details_row[$field_name_temp]) . "\"";
					} // end if
					if ($form_type == "insert") {
						$form .= " value=\"" . $fields_labels_ar[$i]["prefix_field"] . $fields_labels_ar[$i]["default_value_field"] . "\"";
					} // end if
					$form .= ">";
					if (($form_type == "update" or $form_type == "insert") and $fields_labels_ar[$i]["content_field"] == "city") {
						$form .= "<a name=\"" . $field_name_temp . "\" href=\"#" . $field_name_temp . "\" onclick=\"javascript:fill_cap('" . $field_name_temp . "')\">?</a>";
					} // end if
					$form .= "</td>"; // add the second coloumn to the form
					break;
				case "generic_file":
				case "image_file":
					if ($form_type == "search") { // build a textbox instead of a file input
						$form .= "<td class=\"td_input_form\">" . $select_type_select . "<input type=\"text\" name=\"" . $field_name_temp . "\" size=\"" . $fields_labels_ar[$i]["width_field"] . "\">";
					} else {
						$form .= "<td class=\"td_input_form\"><input type=\"file\" name=\"" .
							$field_name_temp . "\" size=\"" . $fields_labels_ar[$i]["width_field"] . "\">";
						if ($form_type == "update" or $form_type == "ext_update") {
							$file_name_temp = $details_row[$field_name_temp];
							if ($file_name_temp != "") {
								$form .= "<br>" . $normal_messages_ar["current_upload"] . ": <a href=\"" . $upload_relative_url;
								$form .= rawurlencode($file_name_temp);
								$form .= "\">";
								$form .= htmlspecialchars($file_name_temp);
								$form .= "</a> <input type=\"checkbox\" value=\"" . htmlspecialchars($file_name_temp) . "\" name=\"" . $field_name_temp . "_file_uploaded_delete\"> (" . $normal_messages_ar['delete'] . ")";
							} // end if
						} // end if
					}
					$form .= "</td>"; // add the second coloumn to the form
					break;
				case "textarea":
					$form .= "<td class=\"td_input_form\">" . $select_type_select . "</td>";
					$form .= "<td class=\"td_input_form\"><textarea cols=\"" . $fields_labels_ar[$i]["width_field"] . "\" rows=\"" . $fields_labels_ar[$i]["height_field"] . "\" name=\"" . $field_name_temp . "\">";
					if ($form_type == "update" or $form_type == "ext_update") {
						$form .= htmlspecialchars($details_row[$field_name_temp]);
					} // end if
					if ($form_type == "insert") {
						$form .= $fields_labels_ar[$i]["prefix_field"] . $fields_labels_ar[$i]["default_value_field"];
					} // end if

					$form .= "</textarea></td>"; // add the second coloumn to the form
					break;
				case "rich_editor":
					$form .= "<td class=\"td_input_form\">" . $select_type_select . "</td>";
					$form .= "<td class=\"td_input_form\"><textarea cols=\"" . $fields_labels_ar[$i]["width_field"] . "\" rows=\"" . $fields_labels_ar[$i]["height_field"] . "\" name=\"" . $field_name_temp . "\">";
					if ($form_type == "update" or $form_type == "ext_update") {
						$form .= htmlspecialchars($details_row[$field_name_temp]);
					} // end if
					if ($form_type == "insert") {
						$form .= $fields_labels_ar[$i]["prefix_field"] . $fields_labels_ar[$i]["default_value_field"];
					} // end if

					$form .= "</textarea></td>"; // add the second coloumn to the form
					$form .= "<script language=\"javascript1.2\">editor_generate('" . $field_name_temp . "');</script>";
					break;
				case "password":
					$form .= "<td class=\"td_input_form\">" . $select_type_select . "<input type=\"password\" name=\"" . $field_name_temp . "\" size=\"" . $fields_labels_ar[$i]["width_field"] . "\" maxlength=\"" . $fields_labels_ar[$i]["maxlength_field"] . "\"";
					if ($form_type == "update" or $form_type == "ext_update") {
						$form .= " value=\"" . htmlspecialchars($details_row[$field_name_temp]) . "\"";
					} // end if

					$form .= "></td>"; // add the second coloumn to the form
					break;
				case "date":
					//$operator_select = "";
					switch ($form_type) {
						case "update":
							split_date($details_row[$field_name_temp], $day, $month, $year);
							$date_select = build_date_select($field_name_temp, $day, $month, $year);
							break;
						case "insert":
							$date_select = build_date_select($field_name_temp, "", "", "");
							break;
						case "search":
							//$operator_select = build_date_select_type_select($field_name_temp."_select_type");
							$date_select = build_date_select($field_name_temp, "", "", "");
							break;
					} // end switch
					$form .= "<td class=\"td_input_form\">" . $select_type_date_select . "</td>" . $date_select; // add the second coloumn to the form
					break;
				case "insert_date":
				case "update_date":
					//$operator_select = "";
					$date_select = "";
					switch ($form_type) {
						case "search":
							//$operator_select = build_date_select_type_select($field_name_temp."_select_type");
							$date_select = build_date_select($field_name_temp, "", "", "");
							break;
					} // end switch
					$form .= "<td class=\"td_input_form\">" . $select_type_date_select . "</td>" . $date_select . "</td>"; // add the second coloumn to the form
					break;
				case "select_multiple_menu":
				case "select_multiple_checkbox":

					// first part, retreive value from the option specified (select_options_field)

					$form .= "<td class=\"td_input_form\">"; // first part of the second coloumn of the form

					if ($fields_labels_ar[$i]["type_field"] == "select_multiple_menu") {
						$form .= "<select name=\"" . $field_name_temp . "[]\" size=" . $size_multiple_select . " multiple>";
					} // end if

					if ($fields_labels_ar[$i]["select_options_field"] != "") {
						$options_labels_temp = substr($fields_labels_ar[$i]["select_options_field"], 1, -1); // delete the first and the last separator

						$select_labels_ar = explode($fields_labels_ar[$i]["separator_field"], $options_labels_temp);

						$select_labels_ar_number = count($select_labels_ar);

						for ($j = 0; $j < $select_labels_ar_number; $j++) {
							if ($fields_labels_ar[$i]["type_field"] == "select_multiple_menu") {
								$form .= "<option value=\"" . htmlspecialchars($select_labels_ar[$j]) . "\"";
							} // end if
							elseif ($fields_labels_ar[$i]["type_field"] == "select_multiple_checkbox") {
								$form .= "<input type=\"checkbox\" name=\"" . $field_name_temp . "[]\" value=\"" . htmlspecialchars($select_labels_ar[$j]) . "\"";
							} // end elseif

							if ($form_type == "update" or $form_type == "ext_update") {
								$options_values_temp = substr($details_row[$field_name_temp], 1, -1); // delete the first and the last separator

								$select_values_ar = explode($fields_labels_ar[$i]["separator_field"], $options_values_temp);

								if (in_array($select_labels_ar[$j], $select_values_ar)) {
									if ($fields_labels_ar[$i]["type_field"] == "select_multiple_menu") {
										$form .= " selected";
									} // end if
									elseif ($fields_labels_ar[$i]["type_field"] == "select_multiple_checkbox") {
										$form .= " checked";
									} // end elseif
								}

								/*
														$found_flag = 0;
														$z = 0;
														$count_temp = count($select_values_ar)
														
														while ($z < $count_temp and $found_flag == 0){
															if ($select_labels_ar[$j] == $select_values_ar[$z]){
																if ($fields_labels_ar[$i]["type_field"] == "select_multiple_menu"){
																	$form .= " selected";
																} // end if
																elseif ($fields_labels_ar[$i]["type_field"] == "select_multiple_checkbox"){
																	$form .= " checked";
																} // end elseif
																$found_flag = 1;
															} // end if
															$z++;
														} // end while
														*/
							} // end if
							if ($fields_labels_ar[$i]["type_field"] == "select_multiple_menu") {
								$form .= "> " . $select_labels_ar[$j] . "</option>";
							} // end if
							elseif ($fields_labels_ar[$i]["type_field"] == "select_multiple_checkbox") {
								$form .= "> " . $select_labels_ar[$j] . "<br>";
							} // end elseif
						} // end for - central part of the form row
					} // end if

					// second part, retreive value from the foreign key

					if ($fields_labels_ar[$i]["foreign_key_field"] != "") {
						if (get_num_rows_db($res_primary_key) > 0) {
							$fields_number = num_fields_db($res_primary_key);
							while ($primary_key_row = fetch_row_db($res_primary_key)) {

								$primary_key_value = "";
								for ($z = 0; $z < $fields_number; $z++) {
									$primary_key_value .= $primary_key_row[$z];
									$primary_key_value .= " - ";
								} // end for
								$primary_key_value = substr($primary_key_value, 0, -3); // delete the last " - "

								if ($fields_labels_ar[$i]["type_field"] == "select_multiple_menu") {
									$form .= "<option value=\"" . htmlspecialchars($primary_key_value) . "\"";
								} // end if
								elseif ($fields_labels_ar[$i]["type_field"] == "select_multiple_checkbox") {
									$form .= "<input type=\"checkbox\" name=\"" . $field_name_temp . "[]\" value=\"" . htmlspecialchars($primary_key_value) . "\"";
								} // end elseif

								if ($form_type == "update" or $form_type == "ext_update") {
									$options_values_temp = substr($details_row[$field_name_temp], 1, -1); // delete the first and the last separator
									$select_values_ar = explode($fields_labels_ar[$i]["separator_field"], $options_values_temp);

									if (in_array($primary_key_value, $select_values_ar)) {
										if ($fields_labels_ar[$i]["type_field"] == "select_multiple_menu") {
											$form .= " selected";
										} // end if
										elseif ($fields_labels_ar[$i]["type_field"] == "select_multiple_checkbox") {
											$form .= " checked";
										} // end elseif
									}

									/*
															   while ($z<$count_temp and $found_flag == 0){
																   if ($primary_key_value == $select_values_ar[$z]){
																	   if ($fields_labels_ar[$i]["type_field"] == "select_multiple_menu"){
																		   $form .= " selected";
																	   } // end if
																	   elseif ($fields_labels_ar[$i]["type_field"] == "select_multiple_checkbox"){
																		   $form .= " checked";
																	   } // end elseif
																	   $found_flag = 1;
																   } // end if
																   $z++;
															   } // end while
															   */

								} // end if
								if ($fields_labels_ar[$i]["type_field"] == "select_multiple_menu") {
									$form .= "> " . $primary_key_value . "</option>";
								} // end if
								elseif ($fields_labels_ar[$i]["type_field"] == "select_multiple_checkbox") {
									$form .= "> " . $primary_key_value . "<br>"; // second part of the form row
								} // end elseif
							} // end while
						} // end if
					} // end if ($fields_labels_ar[$i]["foreign_key_field"] != "")

					if ($fields_labels_ar[$i]["type_field"] == "select_multiple_menu") {
						$form .= "</select>";
					} // end if

					$form .= "</td>"; // last part of the second coloumn of the form
					break;

				case "select_single":
					$form .= "<td class=\"td_input_form\">" . $select_type_select . "<select name=\"" . $field_name_temp . "\">"; // first part of the second coloumn of the form

					$form .= "<option value=\"\"></option>"; // first blank option

					$field_temp = substr($fields_labels_ar[$i]["select_options_field"], 1, -1); // delete the first and the last separator

					if (trim($field_temp) !== '') {
						$select_values_ar = explode($fields_labels_ar[$i]["separator_field"], $field_temp);

						$count_temp = count($select_values_ar);
						for ($j = 0; $j < $count_temp; $j++) {
							$form .= "<option value=\"" . htmlspecialchars($select_values_ar[$j]) . "\"";

							if (($form_type == "update" or $form_type == "ext_update") and $select_values_ar[$j] == $details_row[$field_name_temp]) {
								$form .= " selected";
							} // end if

							$form .= ">" . $select_values_ar[$j] . "</option>"; // second part of the form row
						} // end for
					} // end if

					if ($fields_labels_ar[$i]["primary_key_field_field"] != "") {
						if (get_num_rows_db($res_primary_key) > 0) {
							$fields_number = num_fields_db($res_primary_key);
							while ($primary_key_row = fetch_row_db($res_primary_key)) {

								$primary_key_value = $primary_key_row[0];
								$linked_fields_value = "";
								for ($z = 1; $z < $fields_number; $z++) {
									$linked_fields_value .= $primary_key_row[$z];
									$linked_fields_value .= " - ";
								} // end for
								$linked_fields_value = substr($linked_fields_value, 0, -3); // delete the last " - 

								$form .= "<option value=\"" . htmlspecialchars($primary_key_value) . "\"";

								if (($form_type == "update" or $form_type == "ext_update") and $primary_key_value == $details_row[$field_name_temp]) {
									$form .= " selected";
								} // end if

								$form .= ">" . $linked_fields_value . "</option>"; // second part of the form row
							} // end while
						} // end if
					} // end if ($fields_labels_ar[$i]["foreign_key_field"] != "")

					if ($fields_labels_ar[$i]["other_choices_field"] == "1" and ($form_type == "insert" or $form_type == "update")) {
						$form .= "<option value=\"......\">" . $normal_messages_ar["other...."] . "</option>"; // last option with "other...."
					} // end if

					$form .= "</select>";

					if ($fields_labels_ar[$i]["other_choices_field"] == "1" and ($form_type == "insert" or $form_type == "update")) {
						$form .= "<input type=\"text\" name=\"" . $field_name_temp . "_other____" . "\" maxlength=\"" . $fields_labels_ar[$i]["maxlength_field"] . "\"";

						if ($fields_labels_ar[$i]["width_field"] != "") {
							$form .= " size=\"" . $fields_labels_ar[$i]["width_field"] . "\"";
						} // end if
						$form .= ">"; // text field for other....
					} // end if

					$form .= "</td>"; // last part of the second coloumn of the form
					break;
			} // end switch
			/////////////////////////////////////////	
			// end build the second coloumn (input field)

			if ($form_type == "insert" or $form_type == "update" or $form_type == "ext_update") {
				$form .= "<td class=\"td_hint_form\">" . $fields_labels_ar[$i]["hint_insert_field"] . "</td>"; // display the insert hint if it's the insert form
			} // end if
			$form .= "</tr></table></td></tr>";
		} // end if ($fields_labels_ar[$i]["$field_to_ceck"] == "1")
	} // enf for loop for each field in the label array

	$form .= "<tr><td class=\"tr_button_form\" colspan=\"" . $number_cols . "\"><input type=\"submit\" class=\"button_form\" value=\"" . $submit_buttons_ar[$form_type] . "\"></td></tr></table></form>";
	return $form;
} // end build_form function

function build_select_type_select($field_name, $select_type, $first_option_blank)
// goal: build a select with the select type of the field (e.g. is_equal, contains....)
// input: $field_name, $select_type (e.g. is_equal/contains), $first_option_blank(0|1)
// output: $select_type_select
// global: $normal_messages_ar, the array containing the normal messages
{
	global $normal_messages_ar;

	$select_type_select = "";

	$operators_ar = explode("/", $select_type);

	if (count($operators_ar) > 1) { // more than on operator, need a select
		$select_type_select .= "<select name=\"" . $field_name . "\">";
		$count_temp = count($operators_ar);
		if ($first_option_blank === 1) {
			$select_type_select .= "<option value=\"\"></option>";
		} // end if
		for ($i = 0; $i < $count_temp; $i++) {
			$select_type_select .= "<option value=\"" . $operators_ar[$i] . "\">" . $normal_messages_ar[$operators_ar[$i]] . "</option>";
		} // end for
		$select_type_select .= "</select>";
	} // end if
	else { // just an hidden
		$select_type_select .= "<input type=\"hidden\" name=\"" . $field_name . "\" value=\"" . $operators_ar[0] . "\">";
	}

	return $select_type_select;
} // end function build_select_type_select

function check_required_fields($post, $fields_labels_ar)
// goal: check if the user has filled all the required fields
// input: all the fields values ($_POST), $_FILES (for uploaded files) and the array containing infos about fields ($fields_labels_ar)
// output: $check, set to 1 if the check is ok, otherwise 0
{
	$i = 0;
	$check = 1;
	$count_temp = count($fields_labels_ar);
	while ($i < $count_temp and $check == 1) {
		if ($fields_labels_ar[$i]["required_field"] == "1" and $fields_labels_ar[$i]["present_insert_form_field"] == "1") {
			$field_name_temp = $fields_labels_ar[$i]["name_field"];
			switch ($fields_labels_ar[$i]["type_field"]) {
				case "date":
					break; // date is always filled
				case "select_single":
					if ($fields_labels_ar[$i]["other_choices_field"] == "1" and $post[$field_name_temp] == "......") {
						$field_name_other_temp = $field_name_temp . "_other____";
						if ($post["$field_name_other_temp"] == "") {
							$check = 0;
						} // end if
					} // end if
					else {
						if ($post[$field_name_temp] == "") {
							$check = 0;
						} // end if
					} // end else
					break;
				default:
					if ($post[$field_name_temp] == $fields_labels_ar[$i]["prefix_field"]) {
						$post[$field_name_temp] = "";
					} // end if
					if ($post[$field_name_temp] == "") {
						$check = 0;
					} // end if
					break;
			} // end switch
		} // end if
		$i++;
	} // end while
	return $check;
} // end function check_required_fields

function check_length_fields($post, $fields_labels_ar)
// goal: check if the text, password, textarea, rich_editor, select_single, select_multiple_checkbox, select_multiple_menu fields contains too much text
// input: all the fields values ($_POST) and the array containing infos about fields ($fields_labels_ar)
// output: $check, set to 1 if the check is ok, otherwise 0
{
	$i = 0;
	$check = 1;
	$count_temp = count($fields_labels_ar);
	while ($i < $count_temp and $check == 1) {
		$field_name_temp = $fields_labels_ar[$i]["name_field"];
		// I use isset for select_multiple because could be unset
		if ($fields_labels_ar[$i]["maxlength_field"] != "" && isset($post[$field_name_temp])) {
			switch ($fields_labels_ar[$i]["type_field"]) {
				case "text":
				case "password":
				case "textarea":
				case "rich_editor":
					if (strlen($post[$field_name_temp]) > $fields_labels_ar[$i]["maxlength_field"]) {
						$check = 0;
					} // end if
					break;
				case "select_multiple_checkbox":
				case "select_multiple_menu":
					$count_temp_2 = count($post[$field_name_temp]);
					$value_temp = "";
					for ($j = 0; $j < $count_temp_2; $j++) {
						$value_temp .= $fields_labels_ar[$i]["separator_field"] . $post[$field_name_temp][$j];
					}
					$value_temp .= $fields_labels_ar[$i]["separator_field"]; // add the last separator
					if (strlen($value_temp) > $fields_labels_ar[$i]["maxlength_field"]) {
						$check = 0;
					} // end if

					break;
				case "select_single":
					if ($fields_labels_ar[$i]["other_choices_field"] == "1" and $post[$field_name_temp] == "......") {
						$field_name_other_temp = $field_name_temp . "_other____";
						if (strlen($_POST[$field_name_other_temp]) > $fields_labels_ar[$i]["maxlength_field"]) {
							$check = 0;
						} // end if
					} // end if
					else {
						if (strlen($post[$field_name_temp]) > $fields_labels_ar[$i]["maxlength_field"]) {
							$check = 0;
						} // end if
					} // end else
					break;
			} // end switch
		} // end if
		$i++;
	} // end while
	return $check;
} // end function check_length_fields

function write_temp_uploaded_files($files, $fields_labels_ar)
// goal: write an upload file with a temporary name, checking the size and the file extension, the correct name will be assigned later in insert_record or update_record
// input: all the uploaded files ($_FILES) and the array containing infos about fields ($fields_labels_ar)
// output: $check, set to 1 everyhings is fine (also the write procedure), otherwise 0
{
	global $upload_directory, $max_upload_file_size, $allowed_file_exts_ar, $allowed_all_files;
	$uploaded_file_names_ar = array();
	$i = 0;
	$check = 1;
	$count_temp = count($fields_labels_ar);
	while ($i < $count_temp and $check == 1) {
		switch ($fields_labels_ar[$i]["type_field"]) {
			case "generic_file":
			case "image_file":
				$field_name_temp = $fields_labels_ar[$i]["name_field"];

				if ($files[$field_name_temp]['name'] != '') {

					$file_name_temp = $_FILES[$field_name_temp]['tmp_name'];
					$file_name = stripslashes($_FILES[$field_name_temp]['name']);
					$file_name = get_valid_name_uploaded_file($file_name, 1);

					$file_size = $_FILES[$field_name_temp]['size'];

					$file_suffix_temp = strrchr($file_name, ".");
					$file_suffix_temp = substr($file_suffix_temp, 1); // remove the first dot

					if (!in_array(strtolower($file_suffix_temp), $allowed_file_exts_ar) && $allowed_all_files != 1) {
						$check = 0;
					} else {
						if ($file_size > $max_upload_file_size) {
							$check = 0;
						} else { //go ahead and copy the file into the upload directory
							if (!(move_uploaded_file($file_name_temp, $upload_directory . 'dadabik_tmp_file_' . $file_name))) {
								$check = 0;
							}
						} // end else
					} // end else
				} // end if
				break;
		} // end switch
		$i++;
	} // end while
	return $check;
} // end function write_temp_uploaded_files

function check_fields_types($post, $fields_labels_ar, &$content_error_type)
// goal: check if the user has well filled the form, according to the type of the field (e.g. no numbers in alphabetic fields, emails and urls correct)
// input: all the fields values ($_POST) and the array containing infos about fields ($fields_labels_ar), &$content_error_type, a string that change according to the error made (alphabetic, numeric, email, phone, url....)
// output: $check, set to 1 if the check is ok, otherwise 0
{
	$i = 0;
	$check = 1;
	$count_temp = count($fields_labels_ar);
	while ($i < $count_temp and $check == 1) {
		$field_name_temp = $fields_labels_ar[$i]["name_field"];
		if (isset($_POST[$field_name_temp])) { // otherwise it has not been filled
			if ($_POST[$field_name_temp] == $fields_labels_ar[$i]["prefix_field"]) {
				$_POST[$field_name_temp] = "";
			} // end if
			if ($fields_labels_ar[$i]["type_field"] == "select_single" && $fields_labels_ar[$i]["other_choices_field"] == "1" and $_POST[$field_name_temp] == "......") { // other field filled
				$field_name_temp = $field_name_temp . "_other____";
			} // end if
			if (($fields_labels_ar[$i]["type_field"] == "text" || $fields_labels_ar[$i]["type_field"] == "textarea" || $fields_labels_ar[$i]["type_field"] == "rich_editor" || $fields_labels_ar[$i]["type_field"] == "select_single" || $fields_labels_ar[$i]["type_field"] == "select_multiple_menu" || $fields_labels_ar[$i]["type_field"] == "select_multiple_checkbox") and $fields_labels_ar[$i]["present_insert_form_field"] == "1" and $_POST[$field_name_temp] != "") {

				switch ($fields_labels_ar[$i]["content_field"]) {
					case "alphabetic":
						if ($fields_labels_ar[$i]["type_field"] == "select_multiple_menu" || $fields_labels_ar[$i]["type_field"] == "select_multiple_checkbox") {
							$count_temp_2 = count($_POST[$field_name_temp]);
							$j = 0;
							while ($j < $count_temp_2 && $check == 1) {
								if (contains_numerics($_POST[$field_name_temp][$j])) {
									$check = 0;
									$content_error_type = $fields_labels_ar[$i]["content_field"];
								} // end if
								$j++;
							}
						} else {
							if (contains_numerics($_POST[$field_name_temp])) {
								$check = 0;
								$content_error_type = $fields_labels_ar[$i]["content_field"];
							} // end if
						}
						break;
					case "numeric":
						if ($fields_labels_ar[$i]["type_field"] == "select_multiple_menu" || $fields_labels_ar[$i]["type_field"] == "select_multiple_checkbox") {
							$count_temp_2 = count($post[$field_name_temp]);
							$j = 0;
							while ($j < $count_temp_2 && $check == 1) {
								if (!is_numeric($post[$field_name_temp][$j])) {
									$check = 0;
									$content_error_type = $fields_labels_ar[$i]["content_field"];
								} // end if
								$j++;
							}
						} else {
							if (!is_numeric($post[$field_name_temp])) {
								$check = 0;
								$content_error_type = $fields_labels_ar[$i]["content_field"];
							} // end if
						}
						break;
					case "phone":
						if ($fields_labels_ar[$i]["type_field"] == "select_multiple_menu" || $fields_labels_ar[$i]["type_field"] == "select_multiple_checkbox") {
							$count_temp_2 = count($post[$field_name_temp]);
							$j = 0;
							while ($j < $count_temp_2 && $check == 1) {
								if (!is_valid_phone($post[$field_name_temp][$j])) {
									$check = 0;
									$content_error_type = $fields_labels_ar[$i]["content_field"];
								} // end if
								$j++;
							}
						} else {
							if (!is_valid_phone($post[$field_name_temp])) {
								$check = 0;
								$content_error_type = $fields_labels_ar[$i]["content_field"];
							} // end if
						}
						break;
					case "email":
						if ($fields_labels_ar[$i]["type_field"] == "select_multiple_menu" || $fields_labels_ar[$i]["type_field"] == "select_multiple_checkbox") {
							$count_temp_2 = count($post[$field_name_temp]);
							$j = 0;
							while ($j < $count_temp_2 && $check == 1) {
								if (!is_valid_email($post[$field_name_temp][$j])) {
									$check = 0;
									$content_error_type = $fields_labels_ar[$i]["content_field"];
								} // end if
								$j++;
							}
						} else {
							if (!is_valid_email($post[$field_name_temp])) {
								$check = 0;
								$content_error_type = $fields_labels_ar[$i]["content_field"];
							} // end if
						}
						break;
					case "url":
						if ($fields_labels_ar[$i]["type_field"] == "select_multiple_menu" || $fields_labels_ar[$i]["type_field"] == "select_multiple_checkbox") {
							$count_temp_2 = count($post[$field_name_temp]);
							$j = 0;
							while ($j < $count_temp_2 && $check == 1) {
								if (!is_valid_url($post[$field_name_temp][$j])) {
									$check = 0;
									$content_error_type = $fields_labels_ar[$i]["content_field"];
								} // end if
								$j++;
							}
						} else {
							if (!is_valid_url($post[$field_name_temp])) {
								$check = 0;
								$content_error_type = $fields_labels_ar[$i]["content_field"];
							} // end if
						}
						break;
				} // end switch
			} // end if
		} // end if
		elseif ($fields_labels_ar[$i]["type_field"] == "date") {
			$day = $post[$field_name_temp . "_day"];
			$month = $post[$field_name_temp . "_month"];
			$year = $post[$field_name_temp . "_year"];
			if (!is_numeric($day) || !is_numeric($month) || !is_numeric($year) || !checkdate($month, $day, $year)) {
				$check = 0;
				$content_error_type = "date";
			} // end if
		}
		$i++;
	} // end while
	return $check;
} // end function check_fields_types

function build_select_duplicated_query($post, $table_name, $fields_labels_ar, &$string1_similar_ar, &$string2_similar_ar)
// goal: build the select query to select the record that can be similar to the record inserted
// input: all the field values ($_POST), $table_name, $fields_labels_ar, &$string1_similar_ar, &$string2_similar_ar (the two array that will contain the similar string found)
// output: $sql, the sql query
// global $percentage_similarity, the percentage after that two strings are considered similar, $number_duplicated_records, the maximum number of records to be displayed as duplicated
{
	global $percentage_similarity, $number_duplicated_records, $conn, $quote;

	// get the unique key of the table
	$unique_field_name = get_unique_field($table_name);

	if ($unique_field_name != "") { // a unique key exists, ok, otherwise I'm not able to select the similar record, which field should I use to indicate it?

		/*
			  reset ($_POST);
			  while (list($key, $value) = each ($_POST)){
				  $$key = $value;
			  } // end while
			  */

		$sql = "";
		$sql_select_all = "";
		$sql_select_all = "SELECT " . $quote . $unique_field_name . $quote . ", "; // this is used to select the records to check similiarity
		//$select = "SELECT * FROM ".$quote.$table_name.$quote."";
		$select = build_select_part($fields_labels_ar, $table_name);
		$where_clause = "";

		// build the sql_select_all clause
		$j = 0;
		// build the $fields_to_check_ar array, containing the field to check for similiarity
		$fields_to_check_ar = array();
		$count_temp = count($fields_labels_ar);
		for ($i = 0; $i < $count_temp; $i++) {
			if ($fields_labels_ar[$i]["check_duplicated_insert_field"] == "1") {
				//if (${$fields_labels_ar[$i]["name_field"]} != ""){
				$fields_to_check_ar[$j] = $fields_labels_ar[$i]["name_field"]; // I put in the array only if the field is non empty, otherwise I'll check it even if I don't need it
				//} // end if
				$sql_select_all .= $quote . $fields_labels_ar[$i]["name_field"] . $quote . ", ";
				$j++;
			} // end if
		} // end for
		$sql_select_all = substr($sql_select_all, 0, -2); // delete the last ", "
		$sql_select_all .= " FROM " . $quote . $table_name . $quote;
		// end build the sql_select_all clause

		// at the end of the above procedure I'll have, for example, "select ID, name, email from table" if ID is the unique key, name and email are field to check

		// execute the select query
		$res_contacts = execute_db($sql_select_all, $conn);

		if (get_num_rows_db($res_contacts) > 0) {
			while ($contacts_row = fetch_row_db($res_contacts)) { // *A* for each record in the table
				$count_temp = count($fields_to_check_ar);
				for ($i = 0; $i < $count_temp; $i++) { // *B* and for each field the user has inserted
					$z = 0;
					$found_similarity = 0; // set to 1 when a similarity is found, so that it's possible to exit the loop (if I found that a record is similar it doesn't make sense to procede with other fields of the same record)

					// *C* check if the field inserted are similiar to the other fields to be checked in this record (*A*)
					$count_temp_2 = count($fields_to_check_ar);
					while ($z < $count_temp_2 and $found_similarity == 0) {
						$string1_temp = $_POST[$fields_to_check_ar[$i]]; // the field the user has inserted
						$string2_temp = $contacts_row[$z + 1]; // the field of this record (*A*); I start with 1 because 0 is alwais the unique field (e.g. ID, name, email)

						similar_text(strtolower($string1_temp), strtolower($string2_temp), $percentage);
						if ($percentage >= $percentage_similarity) { // the two strings are similar
							$where_clause .= $quote . $unique_field_name . $quote . " = \"" . $contacts_row[$unique_field_name] . "\" or ";
							$found_similarity = 1;
							$string1_similar_ar[] = $string1_temp;
							$string2_similar_ar[] = $string2_temp;
						} // end if the two strings are similar
						$z++;
					} // end while

				} // end for loop for each field to check
			} // end while loop for each record
		} // end if (get_num_rows_db($res_contacts) > 0)

		$where_clause = substr($where_clause, 0, -4); // delete the last " or "
		if ($where_clause != "") {
			$sql = $select . " where " . $where_clause . " limit 0," . $number_duplicated_records;
		} // end if
		else { // no duplication
			$sql = "";
		} // end else*
	} // end if if ($unique_field_name != "")
	else { // no unique keys
		$sql = "";
	} // end else
	return $sql;
} // end function build_select_duplicated_query

function build_insert_duplication_form($post, $fields_labels_ar, $table_name, $table_internal_name)
// goal: build a tabled form composed by two buttons: "Insert anyway" and "Go back"
// input: all the field values ($_POST), $fields_labels_ar, $conn, $table_name, $table_internal_name
// output: $form, the form
// global $submit_buttons_ar, the array containing the caption on submit buttons
{
	global $submit_buttons_ar, $dadabik_main_file;

	$form = "";

	$form .= "<table><tr><td>";

	$form .= "<form action=\"" . $dadabik_main_file . "?table_name=" . urlencode($table_name) . "&function=insert&insert_duplication=1\" method=\"post\">";

	/*
	   // re-post all the fields values
	   reset ($_POST);
	   while (list($key, $value) = each ($_POST)){
		   $$key = $value;
	   } // end while
	   */

	$count_temp = count($fields_labels_ar);
	for ($i = 0; $i < $count_temp; $i++) {

		$field_name_temp = $fields_labels_ar[$i]["name_field"];

		if ($fields_labels_ar[$i]["present_insert_form_field"] == "1") {
			switch ($fields_labels_ar[$i]["type_field"]) {
				case "select_multiple_menu":
				case "select_multiple_checkbox":
					if (isset($post[$field_name_temp])) {
						$count_temp_2 = count($$field_name_temp);
						for ($j = 0; $j < $count_temp_2; $j++) {
							$form .= "<input type=\"hidden\" name=\"" . $field_name_temp . "[" . $j . "]" . "\" value=\"" . htmlspecialchars(stripslashes($_POST[$field_name_temp][$j])) . "\">"; // add the field value to the sql statement
						} // end for
					} // end if
					break;
				case "date":
					$year_field = $field_name_temp . "_year";
					$month_field = $field_name_temp . "_month";
					$day_field = $field_name_temp . "_day";

					$form .= "<input type=\"hidden\" name=\"" . $year_field . "\" value=\"" . $_POST[$year_field] . "\">";
					$form .= "<input type=\"hidden\" name=\"" . $month_field . "\" value=\"" . $_POST[$month_field] . "\">";
					$form .= "<input type=\"hidden\" name=\"" . $day_field . "\" value=\"" . $_POST[$day_field] . "\">";
					break;
				case "select_single":
					if ($fields_labels_ar[$i]["other_choices_field"] == "1" and $_POST[$field_name_temp] == "......") { // other choice filled
						$field_name_other_temp = $field_name_temp . "_other____";
						$form .= "<input type=\"hidden\" name=\"" . $field_name_temp . "\" value=\"" . htmlspecialchars(stripslashes($_POST[$field_name_temp])) . "\">";
						$form .= "<input type=\"hidden\" name=\"" . $field_name_other_temp . "\" value=\"" . htmlspecialchars(stripslashes($_POST[$field_name_other_temp])) . "\">";
					} // end if
					else {
						$form .= "<input type=\"hidden\" name=\"" . $field_name_temp . "\" value=\"" . htmlspecialchars(stripslashes($_POST[$field_name_temp])) . "\">";
					} // end else
					break;
				default: // textual field
					if ($post[$fields_labels_ar[$i]["name_field"]] == $fields_labels_ar[$i]["prefix_field"]) { // the field contain just the prefix
						$post[$fields_labels_ar[$i]["name_field"]] = "";
					} // end if

					$form .= "<input type=\"hidden\" name=\"" . $field_name_temp . "\" value=\"" . htmlspecialchars(stripslashes($_POST[$fields_labels_ar[$i]["name_field"]])) . "\">";
					break;
			} // end switch
		} // end if
	} // end for
	$form .= "<input type=\"submit\" value=\"" . $submit_buttons_ar["insert_anyway"] . "\"></form>";

	$form .= "</td><td>";

	$form .= "<form><input type=\"button\" value=\"" . $submit_buttons_ar["go_back"] . "\" onclick=\"javascript:history.back(-1)\"></form>";

	$form .= "</td></tr></table>";

	return $form;
} // end function build_insert_duplication_form

function build_print_labels_form($name_mailing)
// goal: build an HTML form with a button "Print labels" to print labels
// input: $name_mailing
// ouput: $print_labels_form
// global: $submit_buttons_ar, the array containing the values of the submit buttons
{
	global $submit_buttons_ar;
	$print_labels_form = "";
	$print_labels_form .= "<form action=\"mail.php\" method=\"GET\">";
	$print_labels_form .= "<input type=\"hidden\" name=\"function\" value=\"send\">";
	$print_labels_form .= "<input type=\"hidden\" name=\"type_mailing\" value=\"labels\">";
	$print_labels_form .= "<input type=\"hidden\" name=\"name_mailing\" value=\"" . $name_mailing . "\">";
	$print_labels_form .= "<input type=\"submit\" value=\"" . $submit_buttons_ar["print_labels"] . "\">";
	$print_labels_form .= "</form>";

	return $print_labels_form;
} // end function build_print_labels_form

function build_email_form($from_addresses_ar, $mailing_row)
// goal: build a tabled HTML form with field for a new mailing
// input: $from_addresses_ar, the array containing the from addresses, $mailing_row a row, result set of a mailing, when call from edit mailing
// ouput: $form
// global: $submit_buttons_ar, the array containing the values of the submit buttons and $normal_messages_ar, the array containg the normal messages
{
	global $submit_buttons_ar, $normal_messages_ar;
	$form = "";
	$action = 'mail.php';
	if (is_array($mailing_row)) { // come from edit	
		$action .= '?function=update&old_name_mailing=' . urlencode($mailing_row['name_mailing']);
	} // end if
	else {
		$action .= '?function=new_create';
	} // end else

	if (get_cfg_var("file_uploads")) {
		$form .= "<form action=\"" . $action . "\" method=\"post\" enctype=\"multipart/form-data\">";
	} // end if
	else {
		$form .= "<form action=\"" . $action . "\" method=\"post\">";
	} // end else
	$form .= "<table>";

	$form .= "<tr><th>" . $normal_messages_ar["mailing_name"] . "</th><td><input type=\"text\" name=\"name_mailing\"";
	if (isset($mailing_row['name_mailing'])) {
		$form .= ' value="' . htmlspecialchars($mailing_row['name_mailing']) . '"';
	}
	$form .= ">&nbsp;" . $normal_messages_ar["specify_mailing_name"] . "</td></tr>";

	$form .= "<tr><th>" . $normal_messages_ar["mailing_type"] . "</th><td>" . $normal_messages_ar["e-mail"] . "<input type=\"radio\" name=\"type_mailing\" value=\"email\"";
	if (isset($mailing_row['type_mailing']) && $mailing_row['type_mailing'] == 'email') {
		$form .= ' checked';
	}
	$form .= ">&nbsp;&nbsp;&nbsp;&nbsp;" . $normal_messages_ar["normal_mail"] . "<input type=\"radio\" name=\"type_mailing\" value=\"normal_mail\"";
	if (isset($mailing_row['type_mailing']) && $mailing_row['type_mailing'] == 'normal_mail') {
		$form .= ' checked';
	}
	$form .= "></td></tr>";

	$form .= "<tr><td colspan=\"2\"><i>" . $normal_messages_ar["email_specific_fields"] . "</i></td></tr>";

	$form .= "<tr><td colspan=\"2\">&nbsp;</td></tr>";

	$form .= "<tr><th><font color=\"#0000ff\">" . $normal_messages_ar["from"] . "</font></th><td>";
	$form .= "<select name=\"from_mailing\">";
	for ($i = 0; $i < count($from_addresses_ar); $i++) {
		$form .= "<option value=\"$i\"";
		if (isset($mailing_row['from_mailing']) && $mailing_row['from_mailing'] == $i) {
			$form .= ' selected';
		}
		$form .= ">\"" . $from_addresses_ar[$i][0] . "\" &lt;" . $from_addresses_ar[$i][1] . "&gt;</option>";
	} // end for
	$form .= "</select>";
	$form .= "</td></tr>";

	$form .= "<tr><th><font color=\"#0000ff\">" . $normal_messages_ar["subject"] . "</th><td><input type=\"text\" name=\"subject_mailing\"";
	if (isset($mailing_row['subject_mailing'])) {
		$form .= ' value="' . htmlspecialchars($mailing_row['subject_mailing']) . '"';
	}
	$form .= "></font></td></tr>";

	$form .= "<tr><th>" . $normal_messages_ar["body"] . "</th><td><input type=\"text\" name=\"salute_mailing\"";
	if (isset($mailing_row['salute_mailing'])) {
		$form .= ' value="' . htmlspecialchars($mailing_row['salute_mailing']) . '"';
	} else {
		$form .= "value=\"" . $normal_messages_ar["dear"] . "\"";
	}

	$form .= ">" . $normal_messages_ar["john_smith"] . "<br><textarea name=\"body_mailing\" cols=\"30\" rows=\"10\">";
	if (isset($mailing_row['body_mailing'])) {
		$form .= htmlspecialchars($mailing_row['body_mailing']);
	}
	$form .= "</textarea></td></tr>";

	if (get_cfg_var("file_uploads")) {
		$form .= "<tr><th><font color=\"#0000ff\">" . $normal_messages_ar["attachment"] . "</th><td><input type=\"file\" name=\"attachment_mailing\"></font>";
		if (is_array($mailing_row) && $mailing_row['attachment_name_mailing'] !== '') {
			$form .= ' (' . htmlspecialchars($mailing_row['attachment_name_mailing']) . ') ';
		}
		$form .= "</td></tr>";
	} // end if



	$form .= "<tr><td colspan=\"2\">&nbsp;</td></tr>";
	$form .= "<tr><td>" . $normal_messages_ar["additional_notes_mailing"] . "</th><td><textarea name=\"notes_mailing\" cols=\"30\" rows=\"10\">";
	if (isset($mailing_row['notes_mailing'])) {
		$form .= htmlspecialchars($mailing_row['notes_mailing']);
	}
	$form .= "</textarea></td></tr>";
	$form .= "<tr><td colspan=\"2\">&nbsp;</td></tr>";

	$form .= "</table>";
	if (is_array($mailing_row)) { // come from edit	
		$form .= "<input type=\"submit\" value=\"" . $submit_buttons_ar["save_this_mailing"] . "\">";
	} else {
		$form .= "<input type=\"submit\" value=\"" . $submit_buttons_ar["create_this_mailing"] . "\">";
	}
	$form .= "</form>";

	return $form;
} // end function build_email_form

/*
function build_email_form($from_addresses_ar)
// goal: build a tabled HTML form with field for a new mailing
// input: $from_addresses_ar, the array containing the from addresses
// ouput: $form
{
	global $submit_buttons_ar, $normal_messages_ar;
	$form = "";
	if (get_cfg_var("file_uploads")){
		$form .= "<form action=\"mail.php?function=new_create\" method=\"post\" enctype=\"multipart/form-data\">";
	} // end if
	else{
		$form .= "<form action=\"mail.php?function=new_create\" method=\"post\">";
	} // end else
	$form .= "<table>";
	$form .= "<tr><th>".$normal_messages_ar["mailing_name"]."</th><td><input type=\"text\" name=\"name_mailing\">&nbsp;".$normal_messages_ar["specify_mailing_name"]."</td></tr>";
	$form .= "<tr><th>".$normal_messages_ar["mailing_type"]."</th><td>".$normal_messages_ar["e-mail"]."<input type=\"radio\" name=\"type_mailing\" value=\"email\">&nbsp;&nbsp;&nbsp;&nbsp;".$normal_messages_ar["normal_mail"]."<input type=\"radio\" name=\"type_mailing\" value=\"normal_mail\"></td></tr>";
	$form .= "<tr><td colspan=\"2\"><i>".$normal_messages_ar["email_specific_fields"]."</i></td><td>";
	$form .= "<tr><td colspan=\"2\">&nbsp;</td><td>";
	$form .= "<tr><th><font color=\"#0000ff\">".$normal_messages_ar["from"]."</font></th><td>";
	$form .= "<select name=\"from_mailing\">";

	for ($i=0; $i<count($from_addresses_ar); $i++){
		$form .= "<option value=\"".$i."\">\"".$from_addresses_ar[$i][0]."\" &lt;".$from_addresses_ar[$i][1]."&gt;</option>";
	} // end for

	$form .= "</select>";

	$form .= "</td></tr>";
	$form .= "<tr><th><font color=\"#0000ff\">".$normal_messages_ar["subject"]."</th><td><input type=\"text\" name=\"subject_mailing\"></font></td></tr>";
	$form .= "<tr><th>".$normal_messages_ar["body"]."</th><td><input type=\"text\" name=\"salute_mailing\" value=\"".$normal_messages_ar["dear"]."\">".$normal_messages_ar["john_smith"]."<br><textarea name=\"body_mailing\" cols=\"30\" rows=\"10\"></textarea></td></tr>";
	if (get_cfg_var("file_uploads")){
		$form .= "<tr><th><font color=\"#0000ff\">".$normal_messages_ar["attachment"]."</th><td><input type=\"file\" name=\"attachment_mailing\"></font></td></tr>";
	} // end if
	$form .= "<tr><td colspan=\"2\">&nbsp;</td><td>";
	$form .= "<tr><td>".$normal_messages_ar["additional_notes_mailing"]."</th><td><textarea name=\"notes_mailing\" cols=\"30\" rows=\"10\"></textarea></td></tr>";
	$form .= "<tr><td colspan=\"2\">&nbsp;</td><td>";	
	$form .= "</table>";
	$form .= "<input type=\"submit\" value=\"".$submit_buttons_ar["create_this_mailing"]."\">";
	$form .= "</form>";

	return $form;
} // end function build_email_form
*/
function build_add_to_mailing_form($res_mailing, $select_without_limit, $results_number)
// goal: build a tabled HTML form with add to mailing button and a select for choose mailing
// input: $res_mailing, the result set of a sql select on all the mailing, $select_without_limit, the sql select on records, to pass as hidden value, $results_number, the number of records found in the search, to pass as hidden value
// output: $form, the form
{
	global $submit_buttons_ar;
	$mailing_select = build_mailing_select(0);
	$form = "";
	$form .= "<form action=\"mail.php\" method=\"GET\">";
	$form .= "<input type=\"submit\" value=\"" . $submit_buttons_ar["add_to_mailing"] . "\">";
	$form .= " " . $mailing_select . " ";
	$form .= "<input type=\"hidden\" name=\"function\" value=\"add_contacts\">";
	$form .= "<input type=\"hidden\" name=\"select_without_limit\" value=\"" . $select_without_limit . "\">";
	$form .= "<input type=\"hidden\" name=\"results_number\"value=\"" . $results_number . "\">";
	$form .= "</form>";

	return $form;
} // end function build_add_to_mailing_form

function build_mailing_select($show_sent_mailing)
// goal: build a select to choose the mailing
// input: $show_sent_mailing (0|1) retreive or not the sent mailing
// output: the html select or false if no mailings are found
{
	global $conn, $quote, $normal_messages_ar;
	$mailing_select = "";

	// get, for each mailing, the name, the number of contact associated, the sent_mailing(1|0) info
	$sql = "SELECT mailing_tab.name_mailing, mailing_contacts_tab.name_mailing AS name_mailing_2, count(*) as mailing_contact_number, sent_mailing from mailing_tab LEFT JOIN mailing_contacts_tab ON mailing_tab.name_mailing = mailing_contacts_tab.name_mailing";

	if ($show_sent_mailing === 0) {
		$sql .= " WHERE sent_mailing = '0'";
	} // end if

	$sql .= " GROUP BY mailing_tab.name_mailing ORDER BY sent_mailing, date_created_mailing DESC";

	// execute the query
	$res_mailing = execute_db($sql, $conn);

	if (get_num_rows_db($res_mailing) > 0) {
		$mailing_select .= "<select name=\"name_mailing\">";

		while ($mailing_row = fetch_row_db($res_mailing)) {
			$name_mailing = $mailing_row["name_mailing"];
			$name_mailing_2 = $mailing_row["name_mailing_2"];
			if ($name_mailing_2 === NULL) { // it means that no contacts are associated with the mailing, I set $mailing_contact_number to 0 to avoid (1)
				$mailing_contact_number = 0;
			} // end if
			else {
				$mailing_contact_number = $mailing_row["mailing_contact_number"];
			} // end else
			$sent_mailing = (int) $mailing_row["sent_mailing"];

			$mailing_select .= "<option value=\"" . $name_mailing . "\">" . $name_mailing . " (" . $mailing_contact_number . ")";
			if ($sent_mailing === 1) {
				$mailing_select .= ' (' . $normal_messages_ar['sent'] . ')';
			} // end if
			$mailing_select .= "</option>";
		} // end while

		$mailing_select .= "</select>";
	} // end if
	else {
		$mailing_select = false;
	} // end else
	return $mailing_select;
} // end function build_mailing_select

function build_where_clause_mailing_select()
// goal: build a select whose value is the where clause needed to select the contacts of a mailing
// input:
// output: $the html select or FALSE if no mailings are found
{
	global $conn, $quote, $normal_messages_ar;
	$mailing_select = "";

	// get, for each mailing, the name, the number of contact associated, the sent_mailing(1|0) info
	$sql = "SELECT mailing_tab.name_mailing, mailing_contacts_tab.name_mailing AS name_mailing_2, count(*) as mailing_contact_number, sent_mailing from mailing_tab LEFT JOIN mailing_contacts_tab ON mailing_tab.name_mailing = mailing_contacts_tab.name_mailing GROUP BY mailing_tab.name_mailing ORDER BY sent_mailing, date_created_mailing DESC";

	// execute the query
	$res_mailing = execute_db($sql, $conn);

	if (get_num_rows_db($res_mailing) > 0) {
		$where_clause_mailing_select .= "<select name=\"where_clause\">";

		while ($mailing_row = fetch_row_db($res_mailing)) {
			$name_mailing = $mailing_row["name_mailing"];
			$name_mailing_2 = $mailing_row["name_mailing_2"];
			if ($name_mailing_2 === NULL) { // it means that no contacts are associated with the mailing, I set $mailing_contact_number to 0 to avoid (1)
				$mailing_contact_number = 0;
			} // end if
			else {
				$mailing_contact_number = $mailing_row["mailing_contact_number"];
			} // end else
			$sent_mailing = (int) $mailing_row["sent_mailing"];

			$where_clause_mailing_select .= "<option value=\"name_mailing='" . $name_mailing . "'\">" . $name_mailing . " (" . $mailing_contact_number . ")";
			if ($sent_mailing === 1) {
				$where_clause_mailing_select .= ' (' . $normal_messages_ar['sent'] . ')';
			} // end if
			$where_clause_mailing_select .= "</option>";
		} // end while

		$where_clause_mailing_select .= "</select>";
	} // end if
	else {
		$where_clause_mailing_select = false;
	} // end else

	return $where_clause_mailing_select;
} // end function build_where_clause_mailing_select

function build_change_table_select($exclude_not_allowed = 1, $inlcude_users_table = 0)
// goal: build a select to choose the table
// input: $exclude_not_allowed, $inlcude_users_table (1 if it is necessary to include the users table, even if the user is not admin (useful in admin.php)
// output: $select, the html select
{
	global $conn, $table_name, $autosumbit_change_table_control;
	$change_table_select = "";
	$change_table_select .= "<select name=\"table_name\" class=\"select_change_table\"";
	if ($autosumbit_change_table_control == 1) {
		$change_table_select .= "onchange=\"javascript:document.change_table_form.submit()\"";
	}
	$change_table_select .= ">";

	if ($exclude_not_allowed == 1) {
		if ($inlcude_users_table == 0) {
			// get the array containing the names of the tables installed(excluding not allowed)
			$tables_names_ar = build_tables_names_array(1, 1, 0);
		} // end if
		else {
			// get the array containing the names of the tables installed(excluding not allowed)
			$tables_names_ar = build_tables_names_array(1, 1, 1);
		} // end else
	} // end if
	else {
		if ($inlcude_users_table == 0) {
			// get the array containing the names of the tables installed
			$tables_names_ar = build_tables_names_array(0, 1, 0);
		} // end if
		else {
			// get the array containing the names of the tables installed
			$tables_names_ar = build_tables_names_array(0, 1, 1);
		} // end else
	} // end else

	$count_temp = count($tables_names_ar);
	for ($i = 0; $i < $count_temp; $i++) {
		$change_table_select .= "<option value=\"" . htmlspecialchars($tables_names_ar[$i]) . "\"";
		if ($table_name == $tables_names_ar[$i]) {
			$change_table_select .= " selected";
		}
		$change_table_select .= ">" . $tables_names_ar[$i] . "</option>";
	} // end for
	$change_table_select .= "</select>";
	if ($count_temp == 1) {
		return "";
	} // end if
	else {
		return $change_table_select;
	} // end else
} // end function build_change_table_select
/*
function build_change_table_select_old($conn, $db_name, $exclude_not_allowed=1)
// goal: build a select to choose the table
// input: $conn, $db_name, $exclude_not_allowed
// output: $select, the html select
{
	global $table_name;
	$change_table_select = "";
	$change_table_select .= "<select name=\"table_name\">";

	if ($exclude_not_allowed == 1){

		// get the array containing the names of the tables (excluding "dadabik_" ones)
		$tables_names_ar = build_tables_names_array($db_name);
	} // end if
	else{
		// get the array containing the names of the tables (excluding "dadabik_" ones)
		$tables_names_ar = build_tables_names_array($db_name, 1, 0, 1);
	} // end else

	for($i=0; $i<count($tables_names_ar); $i++){
		$change_table_select .= "<option value=\"".htmlspecialchars($tables_names_ar[$i])."\"";
		if ($table_name == $tables_names_ar[$i]){
			$change_table_select .= " selected";
		}
		$change_table_select .= ">".$tables_names_ar[$i]."</option>";
	} // end for
	$change_table_select .= "</select>";
	if (count($tables_names_ar) == 1){
		return "";
	} // end if
	else{
		return $change_table_select;
	} // end else
} // end function build_change_table_select
*/
function mailing_counter($name_mailing)
// goal: count how many contact are present in a mailing
// input:  $name_mailing, the name of the mailing
// output: $mailing_count, the number of contact
{
	global $conn;
	$sql = "select count(*) as mailing_count from mailing_contacts_tab where name_mailing = '" . $name_mailing . "'";

	// execute the query
	$res_mailing_count = execute_db($sql, $conn);
	$mailing_count_row = fetch_row_db($res_mailing_count);
	$mailing_count = $mailing_count_row["mailing_count"];
	return $mailing_count;
} // end function mailing_counter

function table_contains($db_name_2, $table_name, $field_name, $value)
// goal: check if a table contains a record which has a field set to a specified value
// input: $db_name, $table_name, $field_name, $value
// output: true or false
{
	global $conn, $quote, $db_name;
	/*
	   if ( $db_name_2 != "") {
		   select_db($db_name_2, $conn);
	   }
	   */
	$sql = "SELECT COUNT(" . $quote . $field_name . $quote . ") FROM " . $quote . $table_name . $quote . " WHERE " . $quote . $field_name . $quote . " = '" . $value . "'";
	$res_count = execute_db($sql, $conn);
	$count_row = fetch_row_db($res_count);
	if ($count_row[0] > 0) {
		return true;
	} // end if
	/*
	   // re-select the old db
	   if ( $db_name_2 != "") {
		   select_db($db_name, $conn);
	   }
	   */
	return false;
} // end function table_contains

function insert_record($files, $post, $fields_labels_ar, $table_name, $table_internal_name)
// goal: insert a new record in the table
// input $_FILES (needed for the name of the files), $_POST (the array containing all the values inserted in the form), $fields_labels_ar, $table_name, $table_internal_name
// output: nothing
{
	global $conn, $db_name, $quote, $upload_directory, $current_user;

	/*
	   // get the post variables of the form
	   reset ($_POST);
	   while (list($key, $value) = each ($_POST)){
		   $$key = $value;
	   } // end while
	   */

	$uploaded_file_names_count = 0;

	// build the insert statement
	/////////////////////////////
	$sql = "";
	$sql .= "INSERT INTO " . $quote . $table_name . $quote . " (";

	$count_temp = count($fields_labels_ar);
	for ($i = 0; $i < $count_temp; $i++) {
		if ($fields_labels_ar[$i]["present_insert_form_field"] == "1" || $fields_labels_ar[$i]["type_field"] == "insert_date" || $fields_labels_ar[$i]["type_field"] == "update_date" || $fields_labels_ar[$i]["type_field"] == "ID_user" || $fields_labels_ar[$i]["type_field"] == "unique_ID") { // if the field is in the form or need to be inserted because it's an insert data, an update data, an ID_user or a unique_ID
			$sql .= $quote . $fields_labels_ar[$i]["name_field"] . $quote . ", "; // add the field name to the sql statement
		} // end if
	} // end for

	$sql = substr($sql, 0, (strlen($sql) - 2));

	$sql .= ") VALUES (";

	for ($i = 0; $i < $count_temp; $i++) {
		if ($fields_labels_ar[$i]["present_insert_form_field"] == "1") { // if the field is in the form
			switch ($fields_labels_ar[$i]["type_field"]) {
				case "generic_file":
				case "image_file":

					$name_field_temp = $fields_labels_ar[$i]["name_field"];
					$file_name = stripslashes($_FILES[$name_field_temp]['name']);

					if ($file_name != '') {
						$file_name = get_valid_name_uploaded_file($file_name, 0);
					}

					$sql .= "'" . addslashes($file_name) . "', "; // add the field value to the sql statement
					//$sql .= "'".addslashes($uploaded_file_names_ar[$uploaded_file_names_count])."', "; // add the field value to the sql statement
					$uploaded_file_names_count++;

					if ($file_name != '') {
						// rename the temp name of the uploaded file
						copy($upload_directory . 'dadabik_tmp_file_' . $file_name, $upload_directory . $file_name);
						unlink($upload_directory . 'dadabik_tmp_file_' . $file_name);
					} // end if
					break;
				case "select_multiple_menu":
				case "select_multiple_checkbox":
					$field_name_temp = $fields_labels_ar[$i]["name_field"];
					$sql .= "'";
					if (isset($_POST[$fields_labels_ar[$i]["name_field"]])) { // otherwise the user hasn't checked any options
						$count_temp_2 = count($_POST[$fields_labels_ar[$i]["name_field"]]);
						for ($j = 0; $j < $count_temp_2; $j++) {
							$sql .= $fields_labels_ar[$i]["separator_field"] . $_POST[$field_name_temp][$j]; // add the field value to the sql statement
						} // end for
						$sql .= $fields_labels_ar[$i]["separator_field"]; // add the last separator
					} // end if
					$sql .= "', ";
					break;
				case "date":
					$field_name_temp = $fields_labels_ar[$i]["name_field"];
					$year_field = $field_name_temp . "_year";
					$month_field = $field_name_temp . "_month";
					$day_field = $field_name_temp . "_day";

					$mysql_date_value = $_POST[$year_field] . "-" . $_POST[$month_field] . "-" . $_POST[$day_field];
					$sql .= "'" . $mysql_date_value . "', "; // add the field value to the sql statement

					break;
				case "select_single":
					$field_name_temp = $fields_labels_ar[$i]["name_field"];
					$field_name_other_temp = $fields_labels_ar[$i]["name_field"] . "_other____";

					if ($fields_labels_ar[$i]["other_choices_field"] == "1" and $_POST[$field_name_temp] == "......" and $_POST[$field_name_other_temp] != "") { // insert the "other...." choice
						$primary_key_field_field = $fields_labels_ar[$i]["primary_key_field_field"];
						if ($primary_key_field_field != "") {

							$linked_fields_ar = explode($fields_labels_ar[$i]["separator_field"], $fields_labels_ar[$i]["linked_fields_field"]);

							$primary_key_field_field = insert_other_field($fields_labels_ar[$i]["primary_key_db_field"], $fields_labels_ar[$i]["primary_key_table_field"], $linked_fields_ar[0], $_POST[$field_name_other_temp]);
							/*
												 if (substr($foreign_key_temp, 0, 4) != "SQL:"){ // with arbitrary sql statement the insert in the primary key table is not supported yet
													 insert_other_field($foreign_key_temp, $_POST[$field_name_other_temp]);
												 } // end if
												 */
							$sql .= "'" . $primary_key_field_field . "', "; // add the last ID inserted to the sql statement
						} // end if ($foreign_key_temp != "")
						else { // no foreign key field
							$sql .= "'" . $_POST[$field_name_other_temp] . "', "; // add the field value to the sql statement
							if (strpos($fields_labels_ar[$i]["select_options_field"], $fields_labels_ar[$i]["separator_field"] . $_POST[$field_name_other_temp] . $fields_labels_ar[$i]["separator_field"] === false)) { // the other field inserted is not already present in the $fields_labels_ar[$i]["select_options_field"] so we have to add it

								udpate_options($fields_labels_ar[$i], $field_name_temp, $_POST[$field_name_other_temp]);

								// re-get the array containg label ant other information about the fields changed with the above instruction
								$fields_labels_ar = build_fields_labels_array($table_internal_name, "1");
							} // end if
						} // end else
					} // end if
					else {
						$sql .= "'" . $_POST[$field_name_temp] . "', "; // add the field value to the sql statement
					} // end else
					break;
				default: // textual field and select single
					if ($_POST[$fields_labels_ar[$i]["name_field"]] == $fields_labels_ar[$i]["prefix_field"]) { // the field contain just the prefix
						$_POST[$fields_labels_ar[$i]["name_field"]] = "";
					} // end if
					$sql .= "'" . $_POST[$fields_labels_ar[$i]["name_field"]] . "', "; // add the field value to the sql statement
					break;
			} // end switch

		} // end if
		elseif ($fields_labels_ar[$i]["type_field"] == "insert_date" or $fields_labels_ar[$i]["type_field"] == "update_date") { // if the field is not in the form but need to be inserted because it's an insert data or an update data
			$sql .= "'" . date("Y-m-d H:i:s") . "', "; // add the field name to the sql statement
		} // end elseif
		elseif ($fields_labels_ar[$i]["type_field"] == "ID_user") { // if the field is not in the form but need to be inserted because it's an ID_user
			$sql .= "'" . $current_user . "', "; // add the field name to the sql statement
		} // end elseif
		elseif ($fields_labels_ar[$i]["type_field"] == "unique_ID") { // if the field is not in the form but need to be inserted because it's a password record
			$pass = md5(uniqid(microtime(), 1)) . getmypid();
			$sql .= "'" . $pass . "', "; // add the field name to the sql statement
		} // end elseif
	} // end for

	$sql = substr($sql, 0, (strlen($sql) - 2));

	$sql .= ")";
	/////////////////////////////
	// end build the insert statement

	display_sql($sql);

	// insert the record
	$res_insert = execute_db($sql, $conn);
} // end function insert_record

function update_record($files, $post, $fields_labels_ar, $table_name, $table_internal_name, $where_field, $where_value, $update_type)
// goal: insert a new record in the main database
// input $_FILES (needed for the name of the files), $_POST (the array containing all the values inserted in the form, $fields_labels_ar, $table_name, $table_internal_name, $where_field, $where_value, $update_type (internal or external)
// output: nothing
// global: $ext_updated_field, the field in which we set if a field has been updated
{
	global $conn, $ext_updated_field, $quote, $use_limit_in_update, $upload_directory;
	$uploaded_file_names_count = 0;

	/*
	   // get the post variables of the form
	   reset ($_POST);
	   while (list($key, $value) = each ($_POST)){
		   $$key = $value;
	   } // end while
	   */

	switch ($update_type) {
		case "internal":
			$field_to_check = "present_insert_form_field";
			break;
		case "external":
			$field_to_check = "present_ext_update_form_field";
			break;
	} // end switch

	// build the update statement
	/////////////////////////////
	$sql = "";
	$sql .= "UPDATE " . $quote . $table_name . $quote . " SET ";

	$count_temp = count($fields_labels_ar);
	for ($i = 0; $i < $count_temp; $i++) {
		$field_name_temp = $fields_labels_ar[$i]["name_field"];
		// I use isset for select_multiple because could be unset
		//if ($fields_labels_ar[$i][$field_to_check] == "1" && isset($_POST[$field_name_temp]) or $fields_labels_ar[$i]["type_field"] == "update_date"){ // if the field is in the form or need to be inserted because it's an update data

		// no, since I don't handle select_multiple anymore, I delete isset, and anyway it was not correct because for a file $_POST[$field_name_temp] is not set
		if ($fields_labels_ar[$i][$field_to_check] == "1" or $fields_labels_ar[$i]["type_field"] == "update_date") { // if the field is in the form or need to be inserted because it's an update data
			switch ($fields_labels_ar[$i]["type_field"]) {
				case "generic_file":
				case "image_file":
					$file_name = stripslashes($_FILES[$field_name_temp]['name']);
					if ($file_name != '') { // the user has selected a new file to upload

						$sql .= $quote . $field_name_temp . $quote . " = "; // add the field name to the sql statement

						$file_name = get_valid_name_uploaded_file($file_name, 0);

						$sql .= "'" . addslashes($file_name) . "', "; // add the field value to the sql statement
						$uploaded_file_names_count++;

						// rename the temp name of the uploaded file
						copy($upload_directory . 'dadabik_tmp_file_' . $file_name, $upload_directory . $file_name);
						unlink($upload_directory . 'dadabik_tmp_file_' . $file_name);

						if (isset($_POST[$field_name_temp . '_file_uploaded_delete'])) { // the user want to delete a file previoulsy uploaded
							unlink($upload_directory . stripslashes($_POST[$field_name_temp . '_file_uploaded_delete']));
						} // end if
					} elseif (isset($_POST[$field_name_temp . '_file_uploaded_delete'])) { // the user want to delete a file previoulsy uploaded
						$sql .= $quote . $field_name_temp . $quote . " = "; // add the field name to the sql statement
						$sql .= "'', "; // add the field value to the sql statement
						unlink($upload_directory . stripslashes($_POST[$field_name_temp . '_file_uploaded_delete']));
					}
					break;
				case "select_multiple_menu":
				case "select_multiple_checkbox":
					$sql .= $quote . $field_name_temp . $quote . " = "; // add the field name to the sql statement
					$sql .= "'";
					$count_temp_2 = count($_POST[$field_name_temp]);
					for ($j = 0; $j < $count_temp_2; $j++) {
						$sql .= $fields_labels_ar[$i]["separator_field"] . $_POST[$field_name_temp][$j]; // add the field value to the sql statement
					} // end for
					$sql .= $fields_labels_ar[$i]["separator_field"]; // add the last separator
					$sql .= "', ";
					break;
				case "update_date":
					$sql .= $quote . $field_name_temp . $quote . " = "; // add the field name to the sql statement
					$sql .= "'" . date("Y-m-d H:i:s") . "', "; // add the field name to the sql statement
					break;
				case "date":
					$sql .= $quote . $field_name_temp . $quote . " = "; // add the field name to the sql statement
					$field_name_temp = $field_name_temp;
					$year_field = $field_name_temp . "_year";
					$month_field = $field_name_temp . "_month";
					$day_field = $field_name_temp . "_day";

					$mysql_date_value = $_POST[$year_field] . "-" . $_POST[$month_field] . "-" . $_POST[$day_field];
					$sql .= "'" . $mysql_date_value . "', "; // add the field value to the sql statement

					break;
				case 'select_single':
					$field_name_other_temp = $field_name_temp . "_other____";

					if ($fields_labels_ar[$i]["other_choices_field"] == "1" and $_POST[$field_name_temp] == "......" and $_POST[$field_name_other_temp] != "") { // insert the "other...." choice

						$primary_key_field_field = $fields_labels_ar[$i]["primary_key_field_field"];
						if ($primary_key_field_field != "") {
							$linked_fields_ar = explode($fields_labels_ar[$i]["separator_field"], $fields_labels_ar[$i]["linked_fields_field"]);

							$primary_key_field_field = insert_other_field($fields_labels_ar[$i]["primary_key_db_field"], $fields_labels_ar[$i]["primary_key_table_field"], $linked_fields_ar[0], $_POST[$field_name_other_temp]);
							/*
												 if (substr($foreign_key_temp, 0, 4) != "SQL:"){ // with arbitrary sql statement the insert in the primary key table is not supported yet

													 insert_other_field($foreign_key_temp, $_POST[$field_name_other_temp]);

												 } // end if
												 */
							$sql .= $quote . $field_name_temp . $quote . " = "; // add the field name to the sql statement
							$sql .= "'" . $primary_key_field_field . "', "; // add the field value to the sql statement
						} // end if ($foreign_key_temp != "")
						else { // no foreign key field
							$sql .= $quote . $field_name_temp . $quote . " = "; // add the field name to the sql statement
							$sql .= "'" . $_POST[$field_name_other_temp] . "', "; // add the field value to the sql statement
							if (strpos($fields_labels_ar[$i]["select_options_field"], $fields_labels_ar[$i]["separator_field"] . $_POST[$field_name_other_temp] . $fields_labels_ar[$i]["separator_field"]) === false) { // the other field inserted is not already present in the $fields_labels_ar[$i]["select_options_field"] so we have to add it

								udpate_options($fields_labels_ar[$i], $field_name_temp, $_POST[$field_name_other_temp]);

								// re-get the array containg label ant other information about the fields changed with the above instruction
								$fields_labels_ar = build_fields_labels_array($table_internal_name, "1");
							} // end if
						} // end else
					} // end if
					else {
						$sql .= $quote . $field_name_temp . $quote . " = "; // add the field name to the sql statement
						$sql .= "'" . $_POST[$field_name_temp] . "', "; // add the field value to the sql statement
					} // end else

					break;
				default: // textual field
					$sql .= $quote . $field_name_temp . $quote . " = "; // add the field name to the sql statement
					$sql .= "'" . $_POST[$field_name_temp] . "', "; // add the field value to the sql statement
					break;
			} // end switch
		} // end if
	} // end for
	$sql = substr($sql, 0, -2); // delete the last two characters: ", "
	$sql .= " where " . $quote . $where_field . $quote . " = '" . $where_value . "'";
	if ($use_limit_in_update === 1) {
		$sql .= " LIMIT 1";
	} // end if
	/////////////////////////////
	// end build the update statement

	display_sql($sql);

	// update the record
	$res_update = execute_db($sql, $conn);

	if ($update_type == "external") {

		$sql = "UPDATE " . $quote . $table_name . $quote . " SET " . $quote . $ext_updated_field . $quote . " = '1' WHERE " . $quote . $where_field . $quote . " = '" . $where_value . "'";
		if ($use_limit_in_update === 1) {
			$sql .= " LIMIT 1";
		} // end if

		display_sql($sql);

		// update the record
		$res_update = execute_db($sql, $conn);
	} // end if
} // end function update_record
/*
function build_select_query($_POST, $table_name, $table_internal_name)
{
	global $conn, $db_name, $quote;
	// get the post variables of the form
	reset ($_POST);
	while (list($key, $value) = each ($_POST)){
		$$key = $value;
	} // end while
	
	$sql = "";
	$select = "";
	$where = "";
	$select = "select * from ".$quote."$table_name".$quote."";
	
	// get the array containg label and other information about the fields
	$fields_labels_ar = build_fields_labels_array($table_internal_name, "1");

	// build the where clause
	for ($i=0; $i<count($fields_labels_ar); $i++){
		$field_type_temp = $fields_labels_ar[$i]["type_field"];
		$field_name_temp = $fields_labels_ar[$i]["name_field"];
		$field_separator_temp = $fields_labels_ar[$i]["separator_field"];
		$field_select_type_temp = $fields_labels_ar[$i]["select_type_field"];

		if ($fields_labels_ar[$i]["present_search_form_field"] == "1"){
			switch ($field_type_temp){
				case "select_multiple_menu":
				case "select_multiple_checkbox":
					if (isset($$field_name_temp)){
						for ($j=0; $j<count(${$field_name_temp}); $j++){ // for each possible check
							if (${$field_name_temp}[$j] != ""){
								$where .= $quote.$field_name_temp."".$quote." like '%".$field_separator_temp."".${$field_name_temp}[$j].$field_separator_temp."%'";
								$where .= " $operator ";
							} // end if
						} // end for
					} // end if
					break;
				case "date":
				case "insert_date":
				case "update_date":
					$operator_field_name_temp = $field_name_temp."_operator";				
					if ($$operator_field_name_temp != ""){
						$year_field = $field_name_temp."_year";
						$month_field = $field_name_temp."_month";
						$day_field = $field_name_temp."_day";

						$mysql_date_value = $$year_field."-".$$month_field."-".$$day_field;

						$where .= "DATE_FORMAT(".$quote."$field_name_temp".$quote.", '%Y-%m-%d') ".$$operator_field_name_temp." '".$mysql_date_value."' $operator ";;
					} // end if				
					break;
				default:
					if ($$field_name_temp != ""){ // if the user has filled the field
						$select_type_field_name_temp = $field_name_temp."_select_type";
						if ($$select_type_field_name_temp != ""){ 
							switch ($$select_type_field_name_temp){								
								case "like":
									$where .= $quote.$field_name_temp."".$quote." like '%".${$field_name_temp}."%'";
									break;
								case "exactly":
									$where .= "".$quote."".$field_name_temp."".$quote." = '".${$field_name_temp}."'";
									break;
								default:
									$where .= $quote.$field_name_temp."".$quote." ".$$select_type_field_name_temp." '".${$field_name_temp}."'";			
									break;
							} // end switch
						} // end if
						else{
							switch ($field_select_type_temp){									
								case "like":
									$where .= $quote.$field_name_temp."".$quote." like '%".${$field_name_temp}."%'";
									break;
								case "exactly":
									$where .= $quote.$field_name_temp."".$quote." = '".${$field_name_temp}."'";
									break;
								default:
									$where .= $quote.$field_name_temp."".$quote." $field_select_type_temp '".${$field_name_temp}."'";
									break;
							} // end switch
						} // end else
						$where .= " $operator ";
					} // end if
					break;
			} //end switch
		} // end if
	} // end for ($i=0; $i<count($fields_labels_ar); $i++)

	$where = substr($where, 0, strlen($where) - (strlen($operator)+2)); // delete the last " and " or " or "
	if ($where != ""){
			$where = " where ".$where;
	} // end if

	// compose the entire sql statement
	$sql = $select.$where;

	return $sql;
} // end function build_select_query
*/
function build_where_clause($post, $fields_labels_ar, $table_name)
// goal: build the where clause of a select sql statement e.g. "field_1 = 'value' AND field_2 LIKE '%value'"
// input: $_POST, $fields_labels_ar, $table_name
{
	global $quote;

	/*
	   // get the post variables of the form
	   reset ($_POST);
	   while (list($key, $value) = each ($_POST)){
		   $$key = $value;
	   } // end while
	   */

	$where_clause = "";

	$count_temp = count($fields_labels_ar);
	// build the where clause
	for ($i = 0; $i < $count_temp; $i++) {
		$field_type_temp = $fields_labels_ar[$i]["type_field"];
		$field_name_temp = $fields_labels_ar[$i]["name_field"];
		$field_separator_temp = $fields_labels_ar[$i]["separator_field"];
		$field_select_type_temp = $fields_labels_ar[$i]["select_type_field"];

		if ($fields_labels_ar[$i]["present_search_form_field"] == "1") {
			switch ($field_type_temp) {
				case "select_multiple_menu":
				case "select_multiple_checkbox":
					if (isset($_POST[$field_name_temp])) {
						$count_temp_2 = count($_POST[$field_name_temp]);
						for ($j = 0; $j < $count_temp_2; $j++) { // for each possible check
							if ($_POST[$field_name_temp][$j] != "") {
								$where_clause .= $quote . $table_name . $quote . '.' . $quote . $field_name_temp . $quote . " LIKE '%" . $field_separator_temp . $_POST[$field_name_temp][$j] . $field_separator_temp . "%'";
								$where_clause .= " AND ";
							} // end if
						} // end for
					} // end if
					break;
				case "date":
				case "insert_date":
				case "update_date":
					$select_type_field_name_temp = $field_name_temp . "_select_type";
					if ($_POST[$select_type_field_name_temp] != "") {
						$year_field = $field_name_temp . "_year";
						$month_field = $field_name_temp . "_month";
						$day_field = $field_name_temp . "_day";

						$mysql_date_value = $_POST[$year_field] . "-" . $_POST[$month_field] . "-" . $_POST[$day_field];

						switch ($_POST[$select_type_field_name_temp]) {
							case "is_equal":
								$where_clause .= $quote . $table_name . $quote . '.' . $quote . $field_name_temp . $quote . " = '" . $mysql_date_value . "'";
								break;
							case "greater_than":
								$where_clause .= $quote . $table_name . $quote . '.' . $quote . $field_name_temp . $quote . " > '" . $mysql_date_value . "'";
								break;
							case "less_then":
								$where_clause .= $quote . $table_name . $quote . '.' . $quote . $field_name_temp . $quote . " < '" . $mysql_date_value . "'";
								break;
						} // end switch

						//$where_clause .= $quote.$table_name.$quote.'.'.$quote.$field_name_temp.$quote." ".$_POST[$select_type_field_name_temp]." '".$mysql_date_value."' ".$_POST["operator"]." ";

						$where_clause .= " " . $_POST["operator"] . " ";
					} // end if
					break;
				default:
					if ($_POST[$field_name_temp] != "") { // if the user has filled the field
						$select_type_field_name_temp = $field_name_temp . "_select_type";
						switch ($_POST[$select_type_field_name_temp]) {
							case "is_equal":
								$where_clause .= $quote . $table_name . $quote . '.' . $quote . $field_name_temp . $quote . " = '" . $_POST[$field_name_temp] . "'";
								break;
							case "contains":
								$where_clause .= $quote . $table_name . $quote . '.' . $quote . $field_name_temp . $quote . " LIKE '%" . $_POST[$field_name_temp] . "%'";
								break;
							case "starts_with":
								$where_clause .= $quote . $table_name . $quote . '.' . $quote . $field_name_temp . $quote . " LIKE '" . $_POST[$field_name_temp] . "%'";
								break;
							case "ends_with":
								$where_clause .= $quote . $table_name . $quote . '.' . $quote . $field_name_temp . $quote . " LIKE '%" . $_POST[$field_name_temp] . "'";
								break;
							case "greater_than":
								$where_clause .= $quote . $table_name . $quote . '.' . $quote . $field_name_temp . $quote . " > '" . $_POST[$field_name_temp] . "'";
								break;
							case "less_then":
								$where_clause .= $quote . $table_name . $quote . '.' . $quote . $field_name_temp . $quote . " < '" . $_POST[$field_name_temp] . "'";
								break;
						} // end switch
						//} // end else
						$where_clause .= " " . $_POST["operator"] . " ";
					} // end if
					break;
			} //end switch
		} // end if
	} // end for ($i=0; $i<count($fields_labels_ar); $i++)

	if ($where_clause !== '') {
		$where_clause = substr($where_clause, 0, -(strlen($_POST["operator"]) + 2)); // delete the last " and " or " or "
	} // end if

	return $where_clause;
} // end function build_select_query

function get_field_correct_displaying($field_value, $field_type, $field_content, $field_separator, $display_mode)
// get the correct mode to display a field, according to its content (e.g. format data, display select multiple in different rows without separator and so on
// input: $field_value, $field_type, $field_content, $field_separator, $display_mode (results_table|details_page)
// output: $field_to_display, the field value ready to be displayed
// global: $word_wrap_col, the coloumn at which a string will be wrapped in the results
{
	global $word_wrap_col, $enable_word_wrap_cut, $upload_relative_url;
	$field_to_display = "";
	switch ($field_type) {
		case "generic_file":
			if ($field_value != '') {
				// I don't use htmlspecialchars on the first because e.g. if a filename contains &, &amp; doesn't work in the link
				$field_to_display = "<a href=\"" . $upload_relative_url . rawurlencode($field_value) . "\">" . htmlspecialchars($field_value) . "</a>";
			}
			break;
		case "image_file":
			$field_value = htmlspecialchars($field_value);
			if ($field_value != '') {
				$field_to_display = "<img src=\"" . $upload_relative_url . $field_value . "\">";
			}
			break;
		case "select_multiple_menu":
		case "select_multiple_checkbox":
			$field_value = htmlspecialchars($field_value);
			$field_value = substr($field_value, 1, -1); // delete the first and the last separator
			$select_values_ar = explode($field_separator, $field_value);
			$count_temp = count($select_values_ar);
			for ($i = 0; $i < $count_temp; $i++) {
				if ($display_mode == "results_table") {
					$field_to_display .= nl2br(wordwrap($select_values_ar[$i], $word_wrap_col)) . "<br>";
				} else {
					$field_to_display .= nl2br($select_values_ar[$i]) . "<br>";
				}
			} // end for
			break;
		case "insert_date":
		case "update_date":
		case "date":
			$field_value = htmlspecialchars($field_value);
			if ($field_value != '0000-00-00 00:00:00' && $field_value != '0000-00-00') {
				$field_to_display = format_date($field_value);
			} // end if
			else {
				$field_to_display = "";
			} // end else
			break;
		case "rich_editor":
			$field_to_display = $field_value;
			break;

		default: // e.g. text, textarea and select sinlge
			if ($field_content !== 'html') {
				$field_value = htmlspecialchars($field_value);

				if ($display_mode == "results_table") {
					$displayed_part = wordwrap($field_value, $word_wrap_col, "\n", $enable_word_wrap_cut);
				} // end if
				else {
					$displayed_part = $field_value;
				} // end else

			} // end if
			else {
				$displayed_part = $field_value;
			} // end else

			if ($field_content == "email") {
				$field_to_display = "<a href=\"mailto:" . $field_value . "\">" . $displayed_part . "</a>";
			} // end if
			elseif ($field_content == "url") {
				$field_to_display = "<a href=\"" . $field_value . "\">" . $displayed_part . "</a>";
			} // end elseif
			else {
				$field_to_display = nl2br($displayed_part);
			} // end else
			break;
	} // end switch
	return $field_to_display;
} // function get_field_correct_displaying

function get_field_correct_csv_displaying($field_value, $field_type, $field_content, $field_separator)
// get the correct mode to display a field in a csv, according to its content (e.g. format data, display select multiple in different rows without separator and so on
// input: $field_value, $field_type, $field_content, $field_separator
// output: $field_to_display, the field value ready to be displayed
{
	$field_to_display = "";
	switch ($field_type) {
		case "select_multiple_menu":
		case "select_multiple_checkbox":
			$field_value = substr($field_value, 1, -1); // delete the first and the last separator
			$select_values_ar = explode($field_separator, $field_value);
			$count_temp = count($select_values_ar);
			for ($i = 0; $i < $count_temp; $i++) {
				$field_to_display .= $select_values_ar[$i] . "\n";
			} // end for
			break;
		case "insert_date":
		case "update_date":
		case "date":
			if ($field_value != '0000-00-00 00:00:00' && $field_value != '0000-00-00') {
				$field_to_display = format_date($field_value);
			} // end if
			else {
				$field_to_display = "";
			} // end else
			break;
		default:
			$field_to_display = str_replace("\r", '', $field_value);
	} // end switch
	return $field_to_display;
} // function get_field_correct_csv_displaying

function build_results_table($fields_labels_ar, $table_name, $res_records, $results_type, $name_mailing, $page, $action, $where_clause, $page2, $order, $order_type)
// goal: build an HTML table for basicly displaying the results of a select query or show a check mailing results
// input: $table_name, $res_records, the results of the query, $results_type (search, possible_duplication......), $name_mailing, the name of the current mailing, $page, the results page (useful for check mailing), $action (e.g. index.php or mail.php), $where_clause, $page (o......n), $order, $order_type
// output: $results_table, the HTML results table
// global: $submit_buttons_ar, the array containing the values of the submit buttons, $edit_target_window, the target window for edit/details (self, new......), $delete_icon, $edit_icon, $details_icon (the image files to use as icons), $enable_edit, $enable_delete, $enable_details (whether to enable (1) or not (0) the edit, delete and details features 
{
	global $submit_buttons_ar, $normal_messages_ar, $edit_target_window, $delete_icon, $edit_icon, $details_icon, $enable_edit, $enable_delete, $enable_details, $conn, $quote, $ask_confirmation_delete, $word_wrap_col, $word_wrap_fix_width, $alias_prefix, $dadabik_main_file;

	if ($action == "mail.php") {
		$function = "check_form";
	} // end if
	else {
		$function = "search";
	} // end elseif

	$unique_field_name = get_unique_field($table_name);

	// build the results HTML table
	///////////////////////////////

	$results_table = "";
	$results_table .= "<table class=\"results\">";

	// build the table heading
	$results_table .= "<tr>";


	$results_table .= "<th class=\"results\">&nbsp;</td>"; // skip the first column for edit, delete and details

	$count_temp = count($fields_labels_ar);
	for ($i = 0; $i < $count_temp; $i++) {
		if ($fields_labels_ar[$i]["present_results_search_field"] == "1") { // the user want to display the field in the basic search results page

			$label_to_display = $fields_labels_ar[$i]["label_field"];

			if ($word_wrap_fix_width === 1) {

				$spaces_to_add = $word_wrap_col - strlen($label_to_display);

				if ($spaces_to_add > 0) {
					for ($j = 0; $j < $spaces_to_add; $j++) {
						$label_to_display .= '&nbsp;';
					}
				}
			} // end if

			$results_table .= "<th class=\"results\">";

			if ($results_type == "search") {
				if ($order != $fields_labels_ar[$i]["name_field"]) { // the results are not ordered by this field at the moment
					$link_class = "order_link";
					$new_order_type = "ASC";
				} else {
					$link_class = "order_link_selected";
					if ($order_type == "DESC") {
						$new_order_type = "ASC";
					} else {
						$new_order_type = "DESC";
					}
				} // end else if ($order != $fields_labels_ar[$i]["name_field"])

				$results_table .= "<a class=\"" . $link_class . "\" href=\"" . $action . "?table_name=" . urlencode($table_name) . "&function=" . $function . "&where_clause=" . urlencode($where_clause) . "&page=" . $page . "&order=" . urlencode($fields_labels_ar[$i]["name_field"]) . "&order_type=" . $new_order_type . "\">" . $label_to_display . "</a></th>"; // insert the linked name of the field in the <th>
			} else {
				$results_table .= $label_to_display . "</th>"; // insert the  name of the field in the <th>
			} // end if

		} // end if
	} // end for
	$results_table .= "</tr>";

	$td_results_class = 'results_1';
	$td_controls_class = 'controls_1';

	// build the table body
	while ($records_row = fetch_row_db($res_records)) {

		if ($td_results_class === 'results_1') {
			$td_results_class = 'results_2';
			$td_controls_class = 'controls_2';
		} // end if
		else {
			$td_results_class = 'results_1';
			$td_controls_class = 'controls_1';
		} // end else

		// set where clause for delete and update
		///////////////////////////////////////////
		if ($unique_field_name != "") { // exists a unique number
			$where_field = $unique_field_name;
			$where_value = $records_row[$unique_field_name];
		} // end if
		///////////////////////////////////////////
		// end build where clause for delete and update

		$results_table .= "<tr>";
		$results_table .= "<td class=\"" . $td_controls_class . "\">";

		/*
			  if ($results_type == "check_mailing"){
				  $results_table .= "<table cellspacing=\"0\" cellpadding=\"0\"><tr><td><form method=post action=\"mail.php\"><input type=\"hidden\" name=\"function\" value=\"remove_contact\"><input type=\"hidden\" name=\"name_mailing\" value=\"".$name_mailing."\"><input type=\"hidden\" name=\"where_field\" value=\"".$where_field."\"><input type=\"hidden\" name=\"where_value\" value=\"".$where_value."\"><input type=\"hidden\" name=\"page\" value=\"".$page."\"><input type=\"submit\" value=\"".$submit_buttons_ar["remove_from_mailing"]."\"></form></td></tr></table>";
			  } // end if
			  */


		//elseif ($unique_field_name != "" and ($results_type == "search" or $results_type == "possible_duplication")){ // exists a unique number: edit, delete, details make sense
		if ($unique_field_name != "" and ($results_type == "search" or $results_type == "possible_duplication")) { // exists a unique number: edit, delete, details make sense

			if ($enable_edit == "1") { // display the edit icon 
				$results_table .= "<a class=\"onlyscreen\" target=\"_" . $edit_target_window . "\" href=\"" . $dadabik_main_file . "?table_name=" . urlencode($table_name) . "&function=edit&where_field=" . urlencode($where_field) . "&where_value=" . urlencode($where_value) . "\"><img border=\"0\" src=\"" . $edit_icon . "\" alt=\"" . $submit_buttons_ar["edit"] . "\" title=\"" . $submit_buttons_ar["edit"] . "\"></a>";
			} // end if

			if ($enable_delete == "1") { // display the delete icon
				$results_table .= "<a class=\"onlyscreen\"";
				if ($ask_confirmation_delete == 1) {
					$results_table .= " onclick=\"if (!confirm('" . str_replace('\'', '\\\'', $normal_messages_ar['confirm_delete?']) . "')){ return false;}\"";
				}
				$results_table .= " href=\"" . $dadabik_main_file . "?table_name=" . urlencode($table_name) . "&function=delete";

				if ($results_type == "search") {
					$results_table .= "&where_clause=" . urlencode($where_clause) . "&page=" . $page . "&order=" . urlencode($order) . "&order_type=" . $order_type;
				}

				$results_table .= "&where_field=" . urlencode($where_field) . "&where_value=" . urlencode($where_value) . "\"><img border=\"0\" src=\"" . $delete_icon . "\" alt=\"" . $submit_buttons_ar["delete"] . "\" title=\"" . $submit_buttons_ar["delete"] . "\">";
			} // end if

			if ($enable_details == "1") { // display the details icon
				$results_table .= "<a class=\"onlyscreen\" target=\"_" . $edit_target_window . "\" href=\"" . $dadabik_main_file . "?table_name=" . urlencode($table_name) . "&function=details&where_field=" . urlencode($where_field) . "&where_value=" . urlencode($where_value) . "\"><img border=\"0\" src=\"" . $details_icon . "\" alt=\"" . $submit_buttons_ar["details"] . "\" title=\"" . $submit_buttons_ar["details"] . "\"></a>";
			} // end if

		} // end if
		$results_table .= "</td>";
		for ($i = 0; $i < $count_temp; $i++) {
			if ($fields_labels_ar[$i]["present_results_search_field"] == "1") { // the user want to display the field in the search results page
				$results_table .= "<td class=\"" . $td_results_class . "\">"; // start the cell

				$field_name_temp = $fields_labels_ar[$i]["name_field"];
				$field_type = $fields_labels_ar[$i]["type_field"];
				$field_content = $fields_labels_ar[$i]["content_field"];
				$field_separator = $fields_labels_ar[$i]["separator_field"];


				//$foreign_key_temp = $fields_labels_ar[$i]["foreign_key_field"];

				$field_values_ar = array(); // reset the array containing values to display, otherwise for each loop I have the previous values

				$primary_key_field_field = $fields_labels_ar[$i]["primary_key_field_field"];
				if ($primary_key_field_field != "") {

					$primary_key_field_field = $fields_labels_ar[$i]["primary_key_field_field"];
					$primary_key_table_field = $fields_labels_ar[$i]["primary_key_table_field"];
					$primary_key_db_field = $fields_labels_ar[$i]["primary_key_db_field"];
					$linked_fields_field = $fields_labels_ar[$i]["linked_fields_field"];
					$alias_suffix_field = $fields_labels_ar[$i]["alias_suffix_field"];
					$linked_fields_ar = explode($fields_labels_ar[$i]["separator_field"], $linked_fields_field);


					for ($j = 0; $j < count($linked_fields_ar); $j++) {
						$field_values_ar[$j] = $records_row[$linked_fields_ar[$j] . $alias_prefix . $alias_suffix_field];
					} // end for
				} else {
					//$field_value = $records_row[$field_name_temp];
					$field_values_ar[0] = $records_row[$field_name_temp];
				}

				$count_temp_2 = count($field_values_ar);
				for ($j = 0; $j < $count_temp_2; $j++) {
					$field_to_display = get_field_correct_displaying($field_values_ar[$j], $field_type, $field_content, $field_separator, "results_table"); // get the correct display mode for the field

					if ($field_to_display == "") {
						$field_to_display .= "&nbsp;";
					}

					$results_table .= $field_to_display . "&nbsp;"; // at the field value to the table
				}
				$results_table = substr($results_table, 0, -6); // delete the last &nbsp;
				$results_table .= "</td>"; // end the cell
			} // end if
		} // end for

		$results_table .= "</tr>";
	} // end while
	$results_table .= "</table>";

	return $results_table;

} // end function build_results_table

function build_csv($res_records, $fields_labels_ar)
// build a csv, starting from a recordset
// input: $res_record, the recordset, $fields_labels_ar
{
	global $csv_separator, $alias_prefix;
	$csv = "";
	$count_temp = count($fields_labels_ar);

	// write heading
	for ($i = 0; $i < $count_temp; $i++) {
		if ($fields_labels_ar[$i]["present_results_search_field"] == "1") {
			$csv .= "\"" . str_replace("\"", "\"\"", $fields_labels_ar[$i]["label_field"]) . "\"" . $csv_separator;
		}
	}
	$csv = substr($csv, 0, -1); // delete the last ","
	$csv .= "\n";

	// write other rows
	while ($records_row = fetch_row_db($res_records)) {
		for ($i = 0; $i < $count_temp; $i++) {
			if ($fields_labels_ar[$i]["present_results_search_field"] == "1") {

				$field_name_temp = $fields_labels_ar[$i]["name_field"];
				$field_type = $fields_labels_ar[$i]["type_field"];
				$field_content = $fields_labels_ar[$i]["content_field"];
				$field_separator = $fields_labels_ar[$i]["separator_field"];

				$field_values_ar = array(); // reset the array containing values to display, otherwise for each loop I have the previous values

				$primary_key_field_field = $fields_labels_ar[$i]["primary_key_field_field"];
				if ($primary_key_field_field != "") {

					$primary_key_field_field = $fields_labels_ar[$i]["primary_key_field_field"];
					$primary_key_table_field = $fields_labels_ar[$i]["primary_key_table_field"];
					$primary_key_db_field = $fields_labels_ar[$i]["primary_key_db_field"];
					$linked_fields_field = $fields_labels_ar[$i]["linked_fields_field"];
					$linked_fields_ar = explode($fields_labels_ar[$i]["separator_field"], $linked_fields_field);
					$alias_suffix_field = $fields_labels_ar[$i]["alias_suffix_field"];

					for ($j = 0; $j < count($linked_fields_ar); $j++) {
						$field_values_ar[$j] .= $records_row[$linked_fields_ar[$j] . $alias_prefix . $alias_suffix_field];
					} // end for

					/*
								   $linked_field_values_ar = build_linked_field_values_ar($row_records[$field_name_temp], $primary_key_field_field, $primary_key_table_field, $primary_key_db_field, $linked_fields_ar);
								   */

					/*
								   if (substr($foreign_key_temp, 0, 4) == "SQL:"){
									   $sql = substr($foreign_key_temp, 4, strlen($foreign_key_temp)-4);
								   } // end if
								   else{
								   */

					//$field_values_ar = $linked_field_values_ar;
				} else {
					$field_values_ar[0] = $records_row[$field_name_temp];
				}
				$csv .= "\"";

				$count_temp_2 = count($field_values_ar);
				for ($j = 0; $j < $count_temp_2; $j++) {

					$field_to_display = get_field_correct_csv_displaying($field_values_ar[$j], $field_type, $field_content, $field_separator);

					$csv .= str_replace("\"", "\"\"", $field_to_display) . " ";
				}
				$csv = substr($csv, 0, -1); // delete the last space
				$csv .= "\"" . $csv_separator;
			}
		} // end for
		$csv = substr($csv, 0, -1); // delete the last ","
		$csv .= "\n";
	}
	return $csv;
} // end function build_csv

function build_details_table($fields_labels_ar, $res_details)
// goal: build an html table with details of a record
// input: $fields_labels_ar $res_details (the result of the query)
// ouptut: $details_table, the html table
{
	global $conn, $quote, $alias_prefix;

	// build the table
	$details_table = "";

	$details_table .= "<table>";

	while ($details_row = fetch_row_db($res_details)) { // should be just one

		$count_temp = count($fields_labels_ar);
		for ($i = 0; $i < $count_temp; $i++) {
			if ($fields_labels_ar[$i]["present_details_form_field"] == "1") {
				$field_name_temp = $fields_labels_ar[$i]["name_field"];

				$field_values_ar = array(); // reset the array containing values to display, otherwise for each loop if I don't call build_linked_field_values_ar I have the previous values

				$primary_key_field_field = $fields_labels_ar[$i]["primary_key_field_field"];
				if ($primary_key_field_field != "") {
					$primary_key_field_field = $fields_labels_ar[$i]["primary_key_field_field"];
					$primary_key_table_field = $fields_labels_ar[$i]["primary_key_table_field"];
					$primary_key_db_field = $fields_labels_ar[$i]["primary_key_db_field"];
					$linked_fields_field = $fields_labels_ar[$i]["linked_fields_field"];
					$linked_fields_ar = explode($fields_labels_ar[$i]["separator_field"], $linked_fields_field);
					$alias_suffix_field = $fields_labels_ar[$i]["alias_suffix_field"];

					for ($j = 0; $j < count($linked_fields_ar); $j++) {
						$field_values_ar[$j] = $details_row[$linked_fields_ar[$j] . $alias_prefix . $alias_suffix_field];

					} // end for

					/*
								   $linked_field_values_ar = build_linked_field_values_ar($details_row[$field_name_temp], $primary_key_field_field, $primary_key_table_field, $primary_key_db_field, $linked_fields_ar);
								   */
					/*
								   if (substr($foreign_key_temp, 0, 4) == "SQL:"){
									   $sql = substr($foreign_key_temp, 4, strlen($foreign_key_temp)-4);
								   } // end if
								   else{
								   */
					//$field_values_ar = $linked_field_values_ar;
				} else {
					$field_values_ar[0] = $details_row[$field_name_temp];
				}

				$count_temp_2 = count($field_values_ar);
				$details_table .= "<tr><td class=\"td_label_details\"><b>" . $fields_labels_ar[$i]["label_field"] . "</b></td><td class=\"td_value_details\">";
				for ($j = 0; $j < $count_temp_2; $j++) {
					$field_to_display = get_field_correct_displaying($field_values_ar[$j], $fields_labels_ar[$i]["type_field"], $fields_labels_ar[$i]["content_field"], $fields_labels_ar[$i]["separator_field"], "details_table"); // get the correct display mode for the field

					$details_table .= $field_to_display . "&nbsp;"; // at the field value to the table
				}
				$details_table = substr($details_table, 0, -6); // delete the last &nbsp;
				$details_table .= "</td></tr>";

				//$field_to_display = get_field_correct_displaying($details_row[$field_name_temp], $fields_labels_ar[$i]["type_field"], $fields_labels_ar[$i]["content_field"], $fields_labels_ar[$i]["separator_field"], "details_table"); // get the correct display mode for the field

				//$details_table .= "<tr><td class=\"td_label_details\"><b>".$fields_labels_ar[$i]["label_field"]."</b></td><td class=\"td_value_details\">".$field_to_display."</td></tr>";
			} // end if
		} // end for
	} // end while

	$details_table .= "</table>";

	return $details_table;
} // end function build_details_table

function build_navigation_tool($where_clause, $pages_number, $page, $action, $name_mailing, $order, $order_type)
// goal: build a set of link to go forward and back in the result pages
// input: $where_clause, $pages_number (total number of pages), $page (the current page 0....n), $action, the action page (e.g. index.php or mail.php), $name_mailing, the name of the current mailing, $order, the field used to order the results
// output: $navigation_tool, the html navigation tool
{
	global $table_name, $quote;

	if ($action == "mail.php") {
		$function = "check_form";
	} // end if
	else {
		$function = "search";
	} // end elseif
	$navigation_tool = "";

	$page_group = (int) ($page / 10); // which group? (from 0......n) e.g. page 12 is in the page_group 1 
	$total_groups = ((int) (($pages_number - 1) / 10)) + 1; // how many groups? e.g. with 32 pages 4 groups
	$start_page = $page_group * 10; // the navigation tool start with $start_page, end with $end_page
	if ($start_page + 10 > $pages_number) {
		$end_page = $pages_number;
	} // end if
	else {
		$end_page = $start_page + 10;
	} // end else

	if ($page_group > 1) {
		$navigation_tool .= "<a class=\"navig\" href=\"" . $action . "?&table_name=" . urlencode($table_name) . "&function=" . $function . "&where_clause=" . urlencode($where_clause) . "&page=0&order=" . urlencode($order) . "&order_type=" . urlencode($order_type) . "\" title=\"1\">&lt;&lt;</a> ";
	} // end if
	if ($page_group > 0) {
		$navigation_tool .= "<a class=\"navig\" href=\"" . $action . "?&table_name=" . urlencode($table_name) . "&function=" . $function . "&where_clause=" . urlencode($where_clause) . "&page=" . ((($page_group - 1) * 10) + 9) . "&order=" . urlencode($order) . "&order_type=" . urlencode($order_type) . "\" title=\"" . ((($page_group - 1) * 10) + 10) . "\">&lt;</a> ";
	} // end if

	for ($i = $start_page; $i < $end_page; $i++) {
		if ($i != $page) {
			$navigation_tool .= "<a class=\"navig\" href=\"" . $action . "?&table_name=" . urlencode($table_name) . "&function=" . $function . "&where_clause=" . urlencode($where_clause) . "&page=" . $i . "&order=" . urlencode($order) . "&order_type=" . urlencode($order_type) . "\">" . ($i + 1) . "</a> ";
		} // end if
		else {
			$navigation_tool .= "<font class=\"navig\">" . ($i + 1) . "</font> ";
		} //end else
	} // end for

	if (($page_group + 1) < ($total_groups)) {
		$navigation_tool .= "<a class=\"navig\" href=\"" . $action . "?&table_name=" . urlencode($table_name) . "&function=" . $function . "&where_clause=" . urlencode($where_clause) . "&page=" . (($page_group + 1) * 10) . "&order=" . urlencode($order) . "&order_type=" . urlencode($order_type) . "\" title=\"" . ((($page_group + 1) * 10) + 1) . "\">&gt;</a> ";
	} // end elseif
	if (($page_group + 1) < ($total_groups - 1)) {
		$navigation_tool .= "<a class=\"navig\" href=\"" . $action . "?&table_name=" . urlencode($table_name) . "&function=" . $function . "&where_clause=" . urlencode($where_clause) . "&page=" . ($pages_number - 1) . "&order=" . urlencode($order) . "&order_type=" . urlencode($order_type) . "\" title=\"" . $pages_number . "\">&gt;&gt;</a> ";
	} // end if
	return $navigation_tool;
} // end function build_navigation_tool

function build_are_you_sure_send_form($name_mailing)
// goal: build a form with a confirmation message and buttons yes/no before sending a mailing
// input: $mailing_name, the name of the mailing
// output: $are_you_sure_send_form, the form with the buttons
// global $submit_buttons_ar, the array containing the value of submit buttons
{
	global $submit_buttons_ar;

	$are_you_sure_send_form = "";

	$are_you_sure_send_form .= "<table><tr>";
	$are_you_sure_send_form .= "<td><form method=\"GET\" action=\"mail.php\"><input type=\"hidden\" name=\"function\" value=\"send\"><input type=\"hidden\" name=\"send_sure\" value=\"1\"><input type=\"hidden\" name=\"name_mailing\" value=\"" . $name_mailing . "\"><input type=\"submit\" value=\"" . $submit_buttons_ar["yes"] . "\"></form></td>";
	$are_you_sure_send_form .= "<td><form><input type=\"button\" onclick=\"javascript:history.back(-1)\" value=\"" . $submit_buttons_ar["no"] . "\"></form></td>";
	$are_you_sure_send_form .= "</tr></table>";

	return $are_you_sure_send_form;
} // end function build_are_you_sure_send_form

/*
function build_remove_all_form($name_mailing)
// goal: build a form with a button remove all for the check mailing page
// input: $name_mailing, the name of the mailing
// output: $remove_all_form, the form
// global: $submit_buttons_ar, the array containing the value of submit buttons
{
	global $submit_buttons_ar;
	$remove_all_form = "<form action=\"mail.php\" method=\"post\">";
	$remove_all_form .= "<input type=\"hidden\" name=\"remove_type\" value=\"all\">";
	$remove_all_form .= "<input type=\"hidden\" name=\"name_mailing\" value=\"$name_mailing\">";
	$remove_all_form .= "<input type=\"hidden\" name=\"function\" value=\"remove_contact\">";
	$remove_all_form .= "<input type=\"submit\" value=\"".$submit_buttons_ar["remove_all_from_mailing"]."\">";
	$remove_all_form .= "</form>";
	return $remove_all_form;
} // end function build_remove_all_form
*/

function delete_record($table_name, $where_field, $where_value)
// goal: delete one record
{
	global $conn, $quote;
	$sql = "DELETE FROM " . $quote . $table_name . $quote . " WHERE " . $quote . $where_field . $quote . " = '" . $where_value . "' LIMIT 1";
	display_sql($sql);

	// execute the select query
	$res_contacts = execute_db($sql, $conn);

} // end function delete_record

function delete_multiple_records($table_name, $where_clause, $ID_user_field_name)
// goal: delete a group of record according to a where clause
// input: $table_name, $where_clause, $ID_user_field_name (if it is not false, delete only the records that the current user owns
{
	global $conn, $quote, $current_user, $enable_authentication, $enable_delete_authorization;

	if ($enable_authentication === 1 && $enable_delete_authorization === 1 && $ID_user_field_name !== false) { // check also the user
		if ($where_clause !== '') {
			$where_clause .= ' AND ';
		} // end if
		$where_clause .= $quote . $ID_user_field_name . $quote . " = '" . $current_user . "'";
	} // end if

	$sql = '';
	$sql .= "DELETE FROM " . $quote . $table_name . $quote;
	if ($where_clause !== '') {
		$sql .= " WHERE " . $where_clause;
	} // end if
	display_sql($sql);

	// execute the select query
	$res_contacts = execute_db($sql, $conn);

} // end function delete_multiple_records

function required_field_present($fields_labels_ar)
// goal: check if there are at least one required field
// input: $fields_labels_ar
// output: true or false
{

	$i = 0;
	$found = 0;
	$count_temp = count($fields_labels_ar);
	while ($i < $count_temp && $found == 0) {
		if ($fields_labels_ar[$i]["required_field"] == "1") {
			$found = 1;
		}
		$i++;
	}
	if ($found == 1) {
		return true;
	} else {
		return false;
	}
} // end function required_field_present

function create_internal_table($table_internal_name)
// goal: drop (if present) the old internal table and create the new one.
// input: $table_internal_name
{
	global $conn, $quote;

	// drop the old table
	$sql = "DROP TABLE IF EXISTS " . $quote . $table_internal_name . $quote;
	$res_table = execute_db($sql, $conn);

	// create the new one
	$sql = "CREATE TABLE " . $quote . $table_internal_name . $quote . " (
	name_field varchar(50) NOT NULL default '',
	label_field varchar(255) NOT NULL default '',
	type_field ENUM('text','textarea','rich_editor','password','insert_date','update_date','date','select_single','generic_file','image_file','ID_user','unique_ID') NOT NULL default 'text',
	content_field ENUM('alphabetic','alphanumeric','numeric','url','email','html','phone','city') NOT NULL DEFAULT 'alphanumeric',
	present_search_form_field ENUM('0','1') DEFAULT '1' NOT NULL,
	present_results_search_field ENUM('0','1') DEFAULT '1' NOT NULL,
	present_details_form_field ENUM('0','1') DEFAULT '1' NOT NULL,
	present_insert_form_field ENUM('0','1') DEFAULT '1' NOT NULL,
	present_ext_update_form_field ENUM('0','1') DEFAULT '1' NOT NULL,
	required_field ENUM('0','1') DEFAULT '0' NOT NULL,
	check_duplicated_insert_field ENUM('0','1') DEFAULT '0' NOT NULL,
	other_choices_field ENUM ('0','1') DEFAULT '0' NOT NULL,
	select_options_field text NOT NULL default '',
	primary_key_field_field VARCHAR(255) NOT NULL,
	primary_key_table_field VARCHAR(255) NOT NULL,
	primary_key_db_field VARCHAR(50) NOT NULL,
	linked_fields_field TEXT NOT NULL,
	linked_fields_order_by_field TEXT NOT NULL,
	linked_fields_order_type_field VARCHAR(255) NOT NULL,
	select_type_field varchar(100) NOT NULL default 'is_equal/contains/starts_with/ends_with/greater_than/less_then',
	prefix_field TEXT NOT NULL default '',
	default_value_field TEXT NOT NULL default '',
	width_field VARCHAR(5) NOT NULL,
	height_field VARCHAR(5) NOT NULL,
	maxlength_field VARCHAR(5) NOT NULL default '100',
	hint_insert_field VARCHAR(255) NOT NULL,
	order_form_field smallint(6) NOT NULL,
	separator_field varchar(2) NOT NULL default '~',
	PRIMARY KEY  (name_field)
	) TYPE=MyISAM
	";
	$res_table = execute_db($sql, $conn);
} // end function create_internal_table

function create_table_list_table()
// goal: drop (if present) the old table list and create the new one.
{
	global $conn, $table_list_name, $quote;

	// drop the old table
	$sql = "DROP TABLE IF EXISTS " . $quote . $table_list_name . $quote;
	$res_table = execute_db($sql, $conn);

	// create the new one
	$sql = "CREATE TABLE " . $quote . $table_list_name . $quote . " (
	name_table varchar(255) NOT NULL default '',
	allowed_table tinyint(4) NOT NULL default '0',
	enable_insert_table varchar(5) NOT NULL default '',
	enable_edit_table varchar(5) NOT NULL default '',
	enable_delete_table varchar(5) NOT NULL default '',
	enable_details_table varchar(5) NOT NULL default '',
	PRIMARY KEY  (name_table)
	) TYPE=MyISAM
	";
	$res_table = execute_db($sql, $conn);

	//$sql = "INSERT INTO ".$quote.$table_list_name.$quote." (name_table, allowed_table, enable_insert_table, enable_edit_table, enable_delete_table, enable_details_table) VALUES ('".addslashes($users_table_name)."', '1', '1', '1', '1', '1')";

	//$res_table = execute_db($sql, $conn);
} // end function create_table_list_table

function create_users_table()
// goal: drop (if present) the old users table and create the new one.
{
	global $conn, $users_table_name, $quote;

	// drop the old table
	$sql = "DROP TABLE IF EXISTS " . $quote . $users_table_name . $quote;
	$res_table = execute_db($sql, $conn);

	// create the new one
	$sql = "CREATE TABLE " . $quote . $users_table_name . $quote . " (
	ID_user int(10) unsigned NOT NULL auto_increment,
	user_type_user varchar(50) NOT NULL,
	username_user varchar(50) NOT NULL,
	password_user varchar(32) NOT NULL,
	PRIMARY KEY  (ID_user),
	UNIQUE (username_user)
	) TYPE=InnoDB
	";
	$res_table = execute_db($sql, $conn);

	$sql = "INSERT INTO " . $quote . $users_table_name . $quote . " (user_type_user, username_user, password_user) VALUES ('admin', 'root', '" . md5('letizia') . "')";

	$res_table = execute_db($sql, $conn);


} // end function create_users_table

function table_allowed($table_name)
// goal: check if a table is allowed to be managed by DaDaBIK
// input: $table_name
// output: true or false
{
	global $conn, $table_list_name, $quote;
	if (table_exists($table_list_name)) {
		$sql = "SELECT " . $quote . "allowed_table" . $quote . " FROM " . $quote . $table_list_name . $quote . " WHERE " . $quote . "name_table" . $quote . " = '" . $table_name . "'";
		$res_allowed = execute_db($sql, $conn);
		if (get_num_rows_db($res_allowed) == 1) {
			$row_allowed = fetch_row_db($res_allowed);
			$allowed_table = $row_allowed[0];
			if ($allowed_table == "0") {
				return false;
			} // end if
			else {
				return true;
			} // end else
		} // end if
		elseif (get_num_rows_db($res_allowed) == 0) { // e.g. I have an empty table or the table is not installed
			return false;
		} // end elseif
		else {
			exit;
		} // end else
	} // end if
	else {
		return false;
	} // end else
} // end function table_allowed()

function build_enabled_features_ar($table_name)
// goal: build an array containing "0" or "1" according to the fact that a feature (insert, edit, delete, details) is enabled or not
// input: $table_name
// output: $enabled_features_ar, the array
{
	global $conn, $table_list_name, $quote;
	$sql = "SELECT " . $quote . "enable_insert_table" . $quote . ", " . $quote . "enable_edit_table" . $quote . ", " . $quote . "enable_delete_table" . $quote . ", " . $quote . "enable_details_table" . $quote . " FROM " . $quote . $table_list_name . $quote . " WHERE " . $quote . "name_table" . $quote . " = '" . $table_name . "'";
	$res_enable = execute_db($sql, $conn);
	if (get_num_rows_db($res_enable) == 1) {
		$row_enable = fetch_row_db($res_enable);
		$enabled_features_ar["insert"] = $row_enable["enable_insert_table"];
		$enabled_features_ar["edit"] = $row_enable["enable_edit_table"];
		$enabled_features_ar["delete"] = $row_enable["enable_delete_table"];
		$enabled_features_ar["details"] = $row_enable["enable_details_table"];

		return $enabled_features_ar;
	} // end if
	else {
		db_error($sql);
	} // end else
} // end function build_enabled_features_ar($table_name)

function build_enable_features_checkboxes($table_name)
// goal: build the form that enable features
// input: name of the current table
// output: the html for the checkboxes
{
	$enabled_features_ar = build_enabled_features_ar($table_name);

	$enable_features_checkboxes = "";
	$enable_features_checkboxes .= "<input type=\"checkbox\" name=\"enable_insert\" value=\"1\"";
	$enable_features_checkboxes .= "";
	if ($enabled_features_ar["insert"] == "1") {
		$enable_features_checkboxes .= "checked";
	} // end if
	$enable_features_checkboxes .= ">Insert ";
	$enable_features_checkboxes .= "<input type=\"checkbox\" name=\"enable_edit\" value=\"1\"";
	if ($enabled_features_ar["edit"] == "1") {
		$enable_features_checkboxes .= "checked";
	} // end if
	$enable_features_checkboxes .= ">Edit ";
	$enable_features_checkboxes .= "<input type=\"checkbox\" name=\"enable_delete\" value=\"1\"";
	if ($enabled_features_ar["delete"] == "1") {
		$enable_features_checkboxes .= "checked";
	} // end if
	$enable_features_checkboxes .= ">Delete ";
	$enable_features_checkboxes .= "<input type=\"checkbox\" name=\"enable_details\" value=\"1\"";
	if ($enabled_features_ar["details"] == "1") {
		$enable_features_checkboxes .= "checked";
	} // end if
	$enable_features_checkboxes .= ">Details ";

	return $enable_features_checkboxes;
} // end function build_enable_features_checkboxes

function build_change_field_select($fields_labels_ar, $field_position)
// goal: build an html select with all the field names
// input: $fields_labels_ar, $field_position (the current selected option)
// output: the select
{
	global $conn, $table_name;

	$change_field_select = "";
	$change_field_select .= "<select name=\"field_position\">";
	$count_temp = count($fields_labels_ar);
	for ($i = 0; $i < $count_temp; $i++) {
		$change_field_select .= "<option value=\"" . $i . "\"";
		if ($i == $field_position) {
			$change_field_select .= " selected";
		} // end if
		$change_field_select .= ">" . $fields_labels_ar[$i]["name_field"] . "</option>";
	} // end for
	$change_field_select .= "</select>";

	return $change_field_select;
} // end function build_change_field_select

function build_int_table_field_form($field_position, $int_fields_ar, $fields_labels_ar)
// goal: build a part of the internal table manager form relative to one field
// input: $field_position, the position of the field in the internal form, $int_field_ar, the array of the field of the internal table (with labels and properties), $fields_labels_ar, the array containing the fields labels and other information about fields
// output: the html form part
{
	$int_table_form = "";
	$int_table_form .= "<table border=\"0\" cellpadding=\"6\"><tr bgcolor=\"#F0F0F0\"><td><table>";
	$count_temp = count($int_fields_ar);
	for ($i = 0; $i < $count_temp; $i++) {
		$int_table_form .= "<tr>";
		$int_field_name_temp = $int_fields_ar[$i][1];
		$int_table_form .= "<td>" . $int_fields_ar[$i][0] . "</td><td>";
		if ($i == 0) { // it's the name of the field, no edit needed, just show the name
			$int_table_form .= $fields_labels_ar[$field_position][$int_field_name_temp];
		} // end if
		else {
			switch ($int_fields_ar[$i][2]) {
				case "text":
					$int_table_form .= "<input type=\"text\" name=\"" . $int_field_name_temp . "_" . $field_position . "\" value=\"" . $fields_labels_ar[$field_position][$int_field_name_temp] . "\" size=\"" . $int_fields_ar[$i][3] . "\">";
					break;
				case "select_yn":
					$int_table_form .= "<select name=\"" . $int_field_name_temp . "_" . $field_position . "\">";
					$int_table_form .= "<option value=\"1\"";
					if ($fields_labels_ar[$field_position][$int_field_name_temp] == "1") {
						$int_table_form .= " selected";
					} // end if	
					$int_table_form .= ">Y</option>";
					$int_table_form .= "<option value=\"0\"";
					if ($fields_labels_ar[$field_position][$int_field_name_temp] == "0") {
						$int_table_form .= " selected";
					} // end if	
					$int_table_form .= ">N</option>";
					$int_table_form .= "</select>";
					break;
				case "select_custom":
					$int_table_form .= "<select name=\"" . $int_field_name_temp . "_" . $field_position . "\">";
					$temp_ar = explode("/", $int_fields_ar[$i][3]);
					$count_temp_2 = count($temp_ar);
					for ($j = 0; $j < $count_temp_2; $j++) {
						$int_table_form .= "<option value=\"" . $temp_ar[$j] . "\"";
						if ($fields_labels_ar[$field_position][$int_field_name_temp] == $temp_ar[$j]) {
							$int_table_form .= " selected";
						} // end if
						$int_table_form .= ">" . $temp_ar[$j] . "</option>";
					} // end for
					$int_table_form .= "</select>";
					break;
			} // end switch
		} // end else
		$int_table_form .= "</td>";
		$int_table_form .= "</tr>"; // end of the row
	} // end for
	$int_table_form .= "</table></td></tr></table><p>&nbsp;</p>"; // end of the row

	return $int_table_form;
} // end function build_int_table_field_form($field_position, $int_fields_ar, $fields_labels_ar)

function get_valid_name_uploaded_file($file_name, $check_temp_files)
{
	// goal: get a valid name (not already existant) for an uploaded file, e.g. if I upload a file with the name file.txt, and a file with the same name already exists, return file_2.txt, or file_3.txt.....; if .$check_temp_files is 1 check also the dadabik_tmp_file_ corresponding file names
// input: $file_name, $check_temp_files
// a valid name

	global $upload_directory;
	$valid_file_name = $file_name;
	$valid_name_found = 0;

	$dot_position = strpos($file_name, '.');

	$i = 2;
	do {
		if (file_exists($upload_directory . $valid_file_name) || file_exists($upload_directory . 'dadabik_tmp_file_' . $valid_file_name) && $check_temp_files === 1) { // a file with the same name is already present or a temporary file that will get the same name when the insert/update function will be executed is already present (and I need to check temp files)
			if ($dot_position === false) { // the file doesn't have an exension
				$valid_file_name = $file_name . '_' . $i; // from pluto to pluto_2
			} else {
				$valid_file_name = substr($file_name, 0, $dot_position) . '_' . $i . substr($file_name, $dot_position); // from pluto.txt to pluto_2.txt
			}
			$i++;
		} // end if
		else {
			$valid_name_found = 1;
		}
	} while ($valid_name_found == 0);

	return $valid_file_name;

} // end function get_valid_name_uploaded_file ($file_name)

function insert_other_field($primary_key_db, $primary_key_table, $field_name, $field_value_other)
// goal: insert in the primary key table the other.... field
// input: $primary_key_table, $primary_key_db, $linked_fields, $field_value_other
// outpu: the ID of the record inserted
{
	global $conn, $quote;

	if (!table_contains($primary_key_db, $primary_key_table, $field_name, $field_value_other)) { // check if the table doesn't contains the value inserted as other

		$sql_insert_other = "INSERT INTO " . $quote . $primary_key_table . $quote . " (" . $quote . $field_name . $quote . ") VALUES ('" . $field_value_other . "')";

		display_sql($sql_insert_other);

		/*
			  if ($primary_key_db!="") {
				  select_db($primary_key_db, $conn);
			  }
			  */

		// insert into the table of other
		$res_insert = execute_db($sql_insert_other, $conn);

		/*
			  // reselect the old db
			  if ($primary_key_db!="") {
				  select_db($primary_key_db, $conn);
			  }
			  */

		return get_last_ID_db();
	} // end if
} // end function insert_other_field($foreign_key, $field_value_other)

function udpate_options($fields_labels_ar_i, $field_name, $field_value_other)
// goal: upate the options of a field when a user select other....
// input: $fields_labels_ar_i (fields_labels_ar specific for a field), $field_name, $field_value_other
{
	global $conn, $quote, $table_internal_name;
	$select_options_field_updated = addslashes($fields_labels_ar_i["select_options_field"] . stripslashes($field_value_other) . $fields_labels_ar_i["separator_field"]);

	$sql_update_other = "UPDATE " . $quote . $table_internal_name . $quote . " SET " . $quote . "select_options_field" . $quote . " = '" . $select_options_field_updated . "' WHERE " . $quote . "name_field" . $quote . " = '" . $field_name . "'";
	display_sql($sql_update_other);

	// update the internal table
	$res_update = execute_db($sql_update_other, $conn);
} // end function udpate_options($fields_labels_ar_i, $field_name, $field_value_other)

function build_linked_field_values_ar($field_value, $primary_key_field_field, $primary_key_table_field, $primary_key_db_field, $linked_fields_ar)
// goal: build the array containing the linked field values starting from a field value
// input: $primary_key_field_field, $primary_key_table_field, $primary_key_db_field, $linked_fields_ar
// output: linked_field_values_ar
{
	global $conn, $quote;

	$sql = "SELECT ";

	$count_temp = count($linked_fields_ar);
	for ($i = 0; $i < $count_temp; $i++) {
		$sql .= $quote . $linked_fields_ar[$i] . $quote . ", ";
	} // end for
	$sql = substr($sql, 0, -2); // delete the last ", "
	$sql .= " FROM " . $quote . $primary_key_table_field . $quote . " WHERE " . $quote . $primary_key_field_field . $quote . " = '" . $field_value . "'";

	// execute the select query
	$res_linked_fields = execute_db($sql, $conn);

	$row_linked_fields = fetch_row_db($res_linked_fields);

	$count_temp = num_fields_db($res_linked_fields);
	for ($i = 0; $i < $count_temp; $i++) {
		$linked_field_values_ar[] = $row_linked_fields[$i];
	} // end for

	return $linked_field_values_ar;
} // end function build_linked_field_values_ar()


function build_select_part($fields_labels_ar, $table_name)
// goal: build the select part of a search query e.g.SELECT table_1.field_1, table_2.field2 from table_1 LEFT JOIN table_2 ON table_1.field_3 = table2.field_3
// input: $fields_labels_ar, $table_name
// output: the query
{
	global $quote, $alias_prefix;

	// get the primary key
	$unique_field_name = get_unique_field($table_name);

	$sql_fields_part = '';
	$sql_from_part = '';

	foreach ($fields_labels_ar as $field) {
		if ($field['present_results_search_field'] === '1' || $field['present_details_form_field'] === '1' || $field['name_field'] === $unique_field_name) { // include in the select stataments just the fields present in results or the primary key (useful to pass to the edit form)

			// if the field has linked fields, include each linked fields in the select statement and the corresponding table (wiht join) in the from part. Use alias for all in order to mantain name unicity, each field has is own alias_suffix_field so it is easy
			if ($field['primary_key_field_field'] !== '') {
				$linked_fields_ar = explode($field['separator_field'], $field['linked_fields_field']);

				foreach ($linked_fields_ar as $linked_field) {
					$sql_fields_part .= $quote . $field['primary_key_table_field'] . $alias_prefix . $field['alias_suffix_field'] . $quote . '.' . $quote . $linked_field . $quote . ' AS ' . $quote . $linked_field . $alias_prefix . $field['alias_suffix_field'] . $quote . ', ';
				} //end foreach

				$sql_from_part .= ' LEFT JOIN ' . $quote . $field['primary_key_table_field'] . $quote . ' AS ' . $quote . $field['primary_key_table_field'] . $alias_prefix . $field['alias_suffix_field'] . $quote;

				$sql_from_part .= ' ON ';
				$sql_from_part .= $quote . $table_name . $quote . '.' . $quote . $field['name_field'] . $quote . ' = ' . $quote . $field['primary_key_table_field'] . $alias_prefix . $field['alias_suffix_field'] . $quote . '.' . $quote . $field['primary_key_field_field'] . $quote;
			} // end if
			// if the field has not linked fiel, include just the field in the select statement
			else {
				$sql_fields_part .= $quote . $table_name . $quote . '.' . $quote . $field['name_field'] . $quote . ', ';
			} // end else
		} // end if
	} // end foreach

	$sql_fields_part = substr($sql_fields_part, 0, -2); // delete the last ', '

	// compose the final statement
	$sql = "SELECT " . $sql_fields_part . " FROM " . $quote . $table_name . $quote . $sql_from_part;

	return $sql;
} // end function build_select_part()

?>