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
ob_start();




ini_set('session.cookie_path', $site_path);
session_start();

if (!isset($_POST)){
	$_POST=$HTTP_POST_VARS;
}
if (!isset($_GET)){
	$_GET=$HTTP_GET_VARS;
}
if (!isset($_FILES)){
	$_FILES=$HTTP_POST_FILES;
}
if (!isset($_SESSION)){
	$_SESSION=$HTTP_SESSION_VARS;
}

// the var is set in check_login but check_login it's not included by e.g. admin, and it's useful for some functions (e.g. build_tables_names_array) to have it set
$current_user_is_administrator = 0;
?>