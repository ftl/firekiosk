(function() {
	'use strict';
	var app = angular.module('firekioskApp', [
	                                             'ngRoute',
	                                             'firekioskMembershipFee',
	                                             'firekioskBirthday'
	                                        ]);

	app.config(['$routeProvider', function($routeProvider) {
		$routeProvider
			.when('/membershipfee', {
				templateUrl: '/partials/membershipfee.html',
				controller: 'MembershipFeeController'
			})
			.when('/birthday', {
				templateUrl: '/partials/birthday.html',
				controller: 'BirthdayListController'
			})
			.otherwise({
				redirectTo: '/birthday'
	   		});
	   }]);	
})();
