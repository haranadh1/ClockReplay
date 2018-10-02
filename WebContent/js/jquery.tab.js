$(document).ready(function(){
	
	/* Default active tab */
	activeTab = "#tab1"
	activeTabContent = "#content1";

	$(".option").click(function(){
		
		deactivatedTab = "#" + $(this).attr('id');
		deactivatedTabContent = $(deactivatedTab).attr('tab');
		
	    id= $(this).attr('tab');
		
		/* Deactivate current tab using css */
		$(activeTab).removeClass('active');
		$(activeTabContent).addClass('hide');
		
		/* Activate tab that was clicked on */
		$(deactivatedTab).addClass('active');
		$(deactivatedTabContent).removeClass('hide');
		
		
		/* Set the new activated tab */
		activeTab = deactivatedTab;
		activeTabContent = deactivatedTabContent;
		
		
		
	});


});

