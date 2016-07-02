(function() {
	'use strict';
	var app = angular.module('firekioskApp', [
	                                             'ngRoute',
	                                             'ds.clock',
	                                             'firekioskControllers',
	                                             'firekioskFilters',
	                                             'firekioskServices'
	                                        ]);

	app.config(['$routeProvider', function($routeProvider) {
		$routeProvider
			.when('/kiosk', {
				templateUrl: '/partials/kiosk.html',
				controller: 'KioskController'
			})
			.when('/alarm', {
				templateUrl: '/partials/alarm_telegram.html',
				controller: 'AlarmTelegramController'
			})
			.when('/alarm_input', {
				templateUrl: '/partials/alarm_input.html',
				controller: 'AlarmInputController'
			})
			.when('/admin', {
				templateUrl: '/partials/admin.html',
				controller: 'AdminController'
			})
			.otherwise({
				redirectTo: '/kiosk'
	   		});
	   }]);	
})();
