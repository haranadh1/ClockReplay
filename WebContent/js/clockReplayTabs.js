$(function() {
	var list = new Array();
	var tab_title="";
	//var tab_title2="";
	var tab_counter = 2;
	//alert("tab tile:" + tab_title);
	// tabs init with a custom tab template and an "add" callback filling in the content
	var $tabs = $( "#tabs").tabs({
		tabTemplate : "<li><a href='#{href}'>#{label}</a> <span class='ui-icon ui-icon-close'>Remove Tab</span></li>",
		add : function(event, ui) {
			$tabs.tabs('select', '#' + ui.panel.id);
			var tab_content = tab_counter;
			$( ui.panel ).append("<iframe src='' width='100%' height='100%' name='f1' id='"+tab_content+"' frameborder='0'></iframe>");
			$("iframe#"+tab_content).attr("src",tab_title);


		}
	});

	// actual addTab function: adds new tab using the title input from the form above
	function addTab(str) {
		tab_title =str;
		$tabs.tabs("add", "#tabs-" + tab_counter, tab_title);
		tab_counter++;
	}

	// addTab button: just opens the dialog
	$( ".add_tab" )
	.click(function() {

		var ids=$(this).attr("id");		
		if(jQuery.inArray(ids, list)>-1){					
		//alert(ids+" in "+list);
		}else{					
		list.push(ids);
		addTab($(this).attr("id"));
		}
		
		return false;
	});
	// close icon: removing the tab on click
	// note: closable tabs gonna be an option in the future - see http://dev.jqueryui.com/ticket/3924
	$( "#tabs span.ui-icon-close" ).live("click", function() {
		var index = $( "li", $tabs ).index($( this ).parent());
		//alert("Index:"+index);
		$tabs.tabs("remove", index-5);
		//alert("B:"+list);
		list.splice(index-6, 1);
		//alert("A"+list);
	});
});
