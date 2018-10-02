(function($) {

	$.fn.spasticNav = function(options) {
	
		options = $.extend({
			overlap : 20,
			speed : 500,
			reset : 1500,
			color : '#0b2b61',
			easing : 'easeOutExpo'
		}, options);
	
		return this.each(function() {
		
		 	var nav = $(this),
		 		currentPageItem = $('#selected', nav),
		 		blob,
		 		reset;
		 		
		 	$('<li id="blob"></li>').css({
		 		width : currentPageItem.outerWidth(),
		 		height : "40px",
		 		left : currentPageItem.position().left,
		 		top : "-3px",
		 		backgroundColor : options.color
		 	}).appendTo(this);
		 	
		 	blob = $('#blob', nav);
		 	
			$('li:not(#blob)', nav).hover(function() {
				// mouse over
				clearTimeout(reset);
				blob.animate(
					{
						left : $(this).position().left,
						width : $(this).width()
					},
					{
						duration : options.speed,
						easing : options.easing,
						queue : false
					}
				);
			}, function() {
				// mouse out	
				reset = setTimeout(function() {
					blob.animate({
						width : currentPageItem.outerWidth(),
						left : currentPageItem.position().left
					}, options.speed)
				}, options.reset);
	
			});
		
		}); // end each
	
	};

})(jQuery);