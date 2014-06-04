
(function($) {

	/**
	 * return a date
	 * */
    $.fn.getAsDate = function() {
    	/*convert 'mm/dd/yyyy' to javascript date*/
    	var date = null;
		var dateArray = $(this).val().split('/');
		var date = dateArray[1];
		var month = dateArray[0];
		var year = dateArray[2];
		var valueAsDate = new Date(year, month, date);
		
		return valueAsDate;
	};

}(jQuery));