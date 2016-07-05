(function() {
	'use strict';
	var app = angular.module('firekioskApp', [
	                                             'ngRoute',
	                                             'firekioskBirthday'
	                                        ]);

	app.config(['$routeProvider', function($routeProvider) {
		$routeProvider
			.when('/birthday', {
				templateUrl: '/partials/birthday.html',
				controller: 'BirthdayListController'
			})
			.otherwise({
				redirectTo: '/birthday'
	   		});
	   }]);	
})();
