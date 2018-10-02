//for project view dynamic tabs

		$(function() {
			var $tab_title_input = $( "#tab_title"),
				$tab_content_input = $( "#tab_content" );
			var tab_counter = 2;

			
			});

			// modal dialog init: custom buttons and a "close" callback reseting the form inside
			var $dialog = $( "#dialog" ).dialog({
				autoOpen: false,
				modal: true,
				buttons: {
					Add: function() {
						addTab();
						$( this ).dialog( "close" );
					},
					Cancel: function() {
						$( this ).dialog( "close" );
					}
				},
				open: function() {
					$tab_title_input.focus();
				},
				close: function() {
					$form[ 0 ].reset();
				}
			});

		
			// actual addTab function: adds new tab using the title input from the form above
			function addTab() {
				var tab_title = $tab_title_input.val() || "Tab " + tab_counter;
				$tabs.tabs( "add", "#tabs-" + tab_counter, tab_title );
				tab_counter++;
			}

			// addTab button: just opens the dialog
			$( "#add_tab_1" ).click(function() {
					//addTab();
					$tabs.tabs( "add", "#tabs-" + tab_counter, tab_title );
				});
			$( "#add_tab_2" ).click(function() {
					addTab();
				});

			// close icon: removing the tab on click
			// note: closable tabs gonna be an option in the future - see http://dev.jqueryui.com/ticket/3924
			$( "#tabs span.ui-icon-close" ).live( "click", function() {
				var index = $( "li", $tabs ).index( $( this ).parent() );
				$tabs.tabs( "remove", index );
			});
		});
		