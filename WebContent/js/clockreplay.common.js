	$(document).ready( function() {

		myLayout = $('body').layout({

			west__size:			200

		,	east__size:			300

			// RESIZE Accordion widget when panes resize

		,	west__onresize:		$.layout.callbacks.resizePaneAccordions

		,	east__onresize:		$.layout.callbacks.resizePaneAccordions

		});

		// ACCORDION - in the West pane

		$("#accordion1").accordion({ 
			clearStyle: true, fillSpace: true  
		});

		// ACCORDION - in the East pane - in a 'content-div'

		$("#accordion2").accordion({

			fillSpace:	true

		,	active:		1
		, clearStyle: true

		});

	$("#accordion3").accordion({

			fillSpace:	true
			
			, clearStyle: true

		,	active:		1

		});
		// THEME SWITCHER

		addThemeSwitcher('.ui-layout-north',{ top: '12px', right: '5px' });

		// if a new theme is applied, it could change the height of some content,

		// so call resizeAll to 'correct' any header/footer heights affected

		// NOTE: this is only necessary because we are changing CSS *AFTER LOADING* using themeSwitcher

		setTimeout( myLayout.resizeAll, 1000 ); /* allow time for browser to re-render with new theme */

	});
