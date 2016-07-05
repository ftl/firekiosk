(function() {
	'use strict';
	var filters = angular.module('firekioskFilters', []);
	
	filters.filter('page', function() {
		return function(input, pageIndex, pageSize) {
			pageIndex = +pageIndex;
			pageSize = +pageSize;
			var pageCount = Math.ceil(input.length / pageSize);
			var startIndex = Math.max(0, Math.min(pageIndex * pageSize, input.length - 1));
			return input.slice(startIndex);
		}
	});
})();
