$(function(){
    $('#dg').edatagrid({
        url: 'get_flugbuch.php',
        saveUrl: 'save_flugbuch.php',
        updateUrl: 'update_flugbuch.php',
        destroyUrl: 'destroy_flugbuch.php',
        loadMsg: 'Lade Daten. Bitte warten...',
        autoSizeColumn: 'Datum',
        autoSave: 'true',
        pageSize: 20,
        pageList: [10,20,40,80,100],
    onBeforeEdit:function(index,row){
            row.editing = true;
            updateActions(index);
        },
    onBeginEdit:function(index,row){
                         if (row.Commit == "lock") {
                                        for(var i=0; i<row.length; i++){
                                        row[i]['editor'] = undefined;
                                }
                                        row.editing = false;
                                        updateActions(index);
                                }
                        },
    onAfterEdit:function(index,row){
            row.editing = false;
            updateActions(index);
            $('#dg').datagrid('reload');

        },
    onError: function(index,row){
                alert(row.message);
        row.editing = false;
         updateActions(index);
         $('#dg').datagrid('reload');
        

        },
    onCancelEdit:function(index,row){
            row.editing = false;
            updateActions(index);
        },

        });
    

});

 $.extend($.fn.validatebox.defaults.rules, {
    minLenght: {
        validator: function(value, param){
        if (value.length < param[0]) { return; }

        if (value.length >= param[1]) {
            var ss = value.split(':');
            var h = parseInt(ss[0]);
            var m = parseInt(ss[1]);
            } else {
            var h = parseInt(value.substr(0,2));
            var m = parseInt(value.substr(2,2));
            } 

        if ((h >= 0 && h<=23) && (m >=0 && m <=59)){
            return param[0],param[1];
        } 
        return param[0],param[1];
        
        },
        message: 'Bitte HHMM oder HH:MM eingeben!'
    },
    kennzeichen: {
    validator: function(value, param){
      if (value.length == param[0] && value.match(/.-.{4}/)){
        return param[0];
        }
    },
    message: 'Format (X-XXXX) beachten!'
    },
    minDate:{
    validator: function(value,param){

       if (value.length == param[1]) {
              var ys = value.split('.');
              var d = parseInt(ys[0]);
              var m = parseInt(ys[1]);
              var y = parseInt(ys[2]);
        } else {
              var d = parseInt(value.substr(0,2));
                 var m = parseInt(value.substr(2,2));
                  var y = parseInt(value.substr(4,2));
            }

    if ((d>=1 && d<=31) && (m >=1 && m <= 12) && (y<=25)){
        return param[0];
        }
    },message: 'Format beachten: ttmmjj !'
    }
    
});


function updateActions(index){
    $('#dg').edatagrid('updateRow',{
        index:index,
        row:{}
    });
}

function getRowIndex(target){
    var tr = $(target).closest('tr.datagrid-row');
    return parseInt(tr.attr('datagrid-row-index'));
}

function editrow(target){
    $('#dg').edatagrid('beginEdit', getRowIndex(target));
}

function deleterow(target){
      if (row.Commit != "lock") {

    $.messager.confirm('BestÃ¤tigen','Sind Sie sicher?',function(r){
        if (r){
            $('#dg').edatagrid('deleteRow', getRowIndex(target));
        }
    });

    } else {
     $.messager.alert('Gesperrt','Dieser Datensatz kann nicht mehr gelÃ¶scht werden!');
    }
}

function saverow(target){
    $('#dg').edatagrid('endEdit', getRowIndex(target));
    var changes = $('#dg').edatagrid('getChanges');
}

function cancelrow(target){
    $('#dg').edatagrid('cancelEdit', getRowIndex(target));
}

$(document).keyup(function(e) {
    if (e.keyCode == 27) { $('#dg').edatagrid('cancelRow');   // esc key
    } 
});

function formatAction(value,row,index){
     if ( row.Commit != "lock") {
    if (row.editing){
        var s = '<a href="#" onclick="saverow(this)"><img src="css/themes/icons/filesave.png" border=0></a>&nbsp;&nbsp; ';
        var c = '<a href="#" onclick="cancelrow(this)"><img src="css/themes/icons/cancel.png" border=0></a>';
        return s+c;
    } else {
        var e = '<a href="#" onclick="editrow(this)"><img src="css/themes/icons/edit_icon.png" width="16" height=16" border=0></a> ';
        return e;
    }} else {
        var e = '<img src="css/themes/icons/lock.png" alt="Gesperrt">';
                        return e;
     }
}

function setGeschleppter(){

alert ("ok");
}

function doSearch(value,name){
        $('#dg').edatagrid ('load',{
            sPilot : value , sName: name
    });
        
}

function sendReply(){
    $('#fmreply').form({
            url: 'upload.php',
        onSubmit: function(){
        $('#upload').window('close');
            },
        success:function(data){
            $('#uwin').window('open');
            var as = JSON.parse(data);	
            $('#uldata').datagrid('loadData', as); 	
            var divul = document.getElementById("ulmessage");
            divul.textContent = 'Anzahl der geladenen DatensÃ¤tze:'+as['total'];
            $('#rt_file').val('');
           }
        });
    $('#fmreply').submit();
}

function destroyReply(){
        $('#uwin').window('close');
}

function commitReply(){

}
function hilfe(){
          $('#hilfe').load( "hilfe.php");
          $('#hilfe').window('open');
        }

