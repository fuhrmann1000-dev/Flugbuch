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
// include config, simpleLogin, language, functions, common, check_installation, check_table and header
include ("./include/config.php");
if (isset($mail_feature) and $mail_feature == 1){
	// include mail classes
	include ("./include/mail_classes/class.html.mime.mail.inc");
	include ("./include/mail_classes/class.smtp.inc");
} // and if
include ("./include/languages/".$language.".php");
include ("./include/functions.php");
include ("./include/common_start.php");
include ("./include/check_installation.php");
include ("./include/check_table.php");
include ("./include/header.php");

// HTTP Variables:
/***************** GET ***************************
***************************************************/

// the function of this page I wanto to execute ('show_login_form'|'logout'|....)
// check_login.php, show_login_form, index.php footer - logout
if (isset($_GET["function"])){ // from the homepage
	$function = $_GET["function"];
} // end
else{
	$function = "show_login_form";
} // end else

/***************** POST ***************************
All the field contents come from POST, and I use them directly ($_POST)


***************************************************/

$show_record_numbers_change_table = 0;

switch($function){
	case 'check_login':
		if ( $_POST['username_user'] === '' || $_POST['password_user'] === '') {
			txt_out('<p align="center">'.$login_messages_ar['username_password_are_required'].'</p>', 'error_messages_form');
			include './include/forms/login.php';
		} // end if
		else{
			$_SESSION['logged_user_infos_ar'] = get_user_infos_ar_from_username_password($_POST['username_user'], $_POST['password_user']);
			//var_dump ($_SESSION['logged_user_infos_ar']);
			//exit;
			if ( $_SESSION['logged_user_infos_ar'] !== false){
				header ('Location: '.$site_url.'index.php');
				die();
			} // end if
			else{
				unset($_SESSION['logged_user_infos_ar']);
				txt_out('<p align="center">'.$login_messages_ar['incorrect_login'].'</p>', 'error_messages_form');
				include './include/forms/login.php';
			} // end else
		} // end else
		break; // case 'check_login'
	case 'logout':
		unset($_SESSION['logged_user_infos_ar']);
		header ('Location: '.$site_url.$dadabik_login_file);
		die();
		break; // case 'logout'
	case 'show_login_form':
		include './include/forms/login.php';
		break; // case 'show_login_form'
} // end swtich ($function)

// include footer
include ("./include/footer.php");
?>