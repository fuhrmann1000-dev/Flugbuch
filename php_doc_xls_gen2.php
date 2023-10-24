<?php 
/* 
--Dynamic Excel or Word File from MySQL-- 
php-doc-xls-gen for php/MySQL: (.doc or .xls dumper): 

This script takes the contents of a MySQL table and dumps it to 
either a dynamically-generated MS Word File (file with ending '.doc') 
or a dynamically-generated MS Excel File (file with ending '.xls'). 

Prerequisites:  You Must have MS Word and/or MS Excel installed on 
the same computer as your web browser for this to work. 

How to use: 
1)edit the MySQL Connection Info below for your MySQL connection 
  & for the name of the MySQL table that you would like to make the dump for 
2)save this file somewhere on your server 
3)link to this file from another page: 
  a)for Word dumps: 
    <a href="this_file_name.php?w=1">link to word dump</a> 
  b)for Excel dumps: 
    <a href="this_file_name.php">link to excel dump</a> 
4)how to reuse this code to create a dump for ANY MySQL table on your server: 
  a)comment-out this line below under MySQL Connection Info: 
    //$DB_TBLName = "your_table_name"; 
  b)include the name of your MySQL table in links to this page as an extra parameter: 
    ie: for word dump-- 
    <a href="this_file_name.php?w=1&DB_TBLName=your_table_name">link to word dump</a> 
    ie: for excel dump-- 
    <a href="this_file_name.php?DB_TBLName=your_table_name">link to excel dump</a> 
     
To change the formatting of the Word or Excel File generated: 
change the respective parts of the coding for the word or the excel file that format 
the database info sent to the browser.  Most useful for this are the escape characters 
for tabs ('\t') & line returns ('\n').  Experiment with these until you get the formatting 
that you desire. 

This code is freeware (GPL).  Please feel free to do with it what you'd like. 
Comments, bugs, fixes to:  
churmtom@hotmail.com 

Nov. 25th, 2001 
*/ 

//EDIT YOUR MySQL Connection Info: 
$DB_Server = "mysql4.dcb.org";     //your MySQL Server 
$DB_Username = "db72683_22"; //your MySQL User Name 
$DB_Password = "GKtSMjLL7y";    //your MySQL Password 
$DB_DBName = "db72683_22";    //your MySQL Database Name 
$DB_TBLName = "flugbuch";    //your MySQL Table Name  
//$DB_TBLName may also be commented out & passed to the browser 
//as a parameter, so that this code may be easily reused for  
//any MySQL table  

/*     
Leave this connection info as it is, 
just edit the above     
*/ 
//create MySQL connection 
$Connect = @mysql_connect($DB_Server, $DB_Username, $DB_Password)  
    or die("Couldn't connect."); 
//select database     
$Db = @mysql_select_db($DB_DBName, $Connect) 
    or die("Couldn't select database."); 

//if this parameter is included (w=1), file returned will be in word format ('.doc') 
//if parameter is not included, file returned will be in excel format ('.xls') 
if ($w==1){ 
$file_type = "msword"; 
$file_ending = "doc"; 
} 
else { 
$file_type = "vnd.ms-excel"; 
$file_ending = "xls"; 
} 
//header info for browser: determines file type ('.doc' or '.xls') 
header("Content-Type: application/$file_type"); 
header("Content-Disposition: attachment; filename=flugbuch.$file_ending"); 
header("Pragma: no-cache"); 
header("Expires: 0"); 

//define date for title: EDIT this to create the time-format you need 
$now_date = date('d-m-Y H:i'); 
//define title for .doc or .xls file 
$title = ""; 
//define sql query 
$sql = "Select * from $DB_TBLName ORDER BY Datum"; 

/*    Database Connection    */ 
$Connect = @mysql_connect($DB_Server, $DB_Username, $DB_Password)  
    or die("Couldn't connect in 'connect_defs.php'."); 
     
$ALT_Db = @mysql_select_db($DB_DBName, $Connect) 
    or die("Couldn't select database in 'connect_defs.php'."); 

$result = @mysql_query($sql,$Connect) 
    or die(mysql_error()); 

/*    Start of Formatting for Word or Excel    */ 

if ($w==1) 
/*    FORMATTING FOR WORD DOCUMENTS ('.doc')   */ 
{ 
//create title with timestamp: 
echo("$title\n\n"); 
//define separator (defines columns in excel & tabs in word) 
$sep = "\n"; //new line character 

    $i = 0; 
    while($row = mysql_fetch_row($result)) 
    { 
        //set_time_limit(60); // HaRa 
        $schema_insert = ""; 
        for($j=0; $j<mysql_num_fields($result);$j++) 
        { 
        //define field names 
        $field_name = mysql_field_name($result,$j); 
        //will show name of fields 
        $schema_insert .= "$field_name:\t"; 
            if(!isset($row[$j])) { 
                $schema_insert .= "NULL".$sep; 
                } 
            elseif ($row[$j] != "") { 
                $schema_insert .= "$row[$j]".$sep; 
                } 
            else { 
                $schema_insert .= "".$sep; 
                } 
        } 
        $schema_insert = str_replace($sep."$", "", $schema_insert); 
        $schema_insert .= "\t"; 
        print(trim($schema_insert)); 
        //end of each mysql row 
        //creates line to separate data from each MySQL table row 
        print "\n----------------------------------------------------\n"; 
        $i++; 
} 
} 
else  
/*    FORMATTING FOR EXCEL DOCUMENTS ('.xls')   */ 
{ 
//create title with timestamp: 
echo("$title\n"); 
//define separator (defines columns in excel & tabs in word) 
$sep = "\t"; //tabbed character 

//start of printing column names as names of MySQL fields 
for ($i = 0; $i < mysql_num_fields($result); $i++) { 
echo mysql_field_name($result,$i) . "\t"; 
} 
print("\n"); 
//end of printing column names 

//start while loop to get data 
/*       
note: the following while-loop was taken from phpMyAdmin 2.1.0. 
--from the file "lib.inc.php". 
*/ 
    $i = 0; 
    while($row = mysql_fetch_row($result)) 
    { 
        //set_time_limit(60); // HaRa 
        $schema_insert = ""; 
        for($j=0; $j<mysql_num_fields($result);$j++) 
        { 
            if(!isset($row[$j])) 
                $schema_insert .= "NULL".$sep; 
            elseif ($row[$j] != "") 
                $schema_insert .= "$row[$j]".$sep; 
            else 
                $schema_insert .= "".$sep; 
        } 
        $schema_insert = str_replace($sep."$", "", $schema_insert); 
        $schema_insert .= "\t"; 
        print(trim($schema_insert)); 
        print "\n"; 
        $i++; 
    } 
    return (true);     
} 
?>