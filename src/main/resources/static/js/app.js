(function() {
	'use strict';
	var app = angular.module('iotDashboardApp', [
	                                             'ngRoute',
	                                             'ds.clock',
	                                             'idaControllers',
	                                             'idaFilters',
	                                             'idaServices'
	                                        ]);

	app.config(['$routeProvider', function($routeProvider) {
		$routeProvider
			.when('/dashboard', {
				templateUrl: '/partials/dashboard.html',
				controller: 'DashboardController'
			})
			.when('/alarm', {
				templateUrl: '/partials/alarm_telegram.html',
				controller: 'AlarmTelegramController'
			})
			.when('/admin', {
				templateUrl: '/partials/admin.html',
				controller: 'AdminController'
			})
			.otherwise({
				redirectTo: '/dashboard'
	   		});
	   }]);	
})();
