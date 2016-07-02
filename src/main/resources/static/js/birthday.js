(function() {
	'use strict';
	var module = angular.module('firekioskBirthday', ['ngSanitize']);
	
	module.controller('BirthdayListController', ['$scope', '$http', function($scope, $http) {
		var month, i, birthday;
		
		$scope.monthNames = ["Januar", "Februar", "März", "April", "Mai", "Juni", "Juli", "August", "September", "Oktober", "November", "Dezember"];

		$scope.birthdays = {};
		for (month = 1; month <= 12; month += 1) {
			$scope.birthdays[month] = [];
		}

		function isSpecialAge(age) {
			return (age >= 50 && age % 10 == 0)
				|| (age >= 75 && age % 5 == 0);
		}
		
		$scope.filterSpecialBirthdays = false;
		$scope.specialBirthday = function(birthday) {
			return !$scope.filterSpecialBirthdays || isSpecialAge(birthday.age);
		}
		
		$http({method: 'GET', url: '/api/birthdays'}).success(function(data) {
			for (i in data) {
				birthday = data[i];
				$scope.birthdays[birthday.month].push(birthday);
			}
		});
	}]);
	

	module.filter('specialBirthday', function() {
		function isSpecial(birthday) {
			return (birthday.age >= 50 && birthday.age % 10 == 0)
				|| (birthday.age >= 75 && birthday.age % 5 == 0);
		}
		
		return function(birthdaysOfMonth) {
			var result, i, birthday;
			result = [];
			for (i in birthdaysOfMonth) {
				birthday = birthdaysOfMonth[i];
				if (isSpecial(birthday)) {
					result.push(birthday);
				}
			}
			return result;
		};
	});
	
	module.factory('firekiosk.birthdayList', ['$location', function($location) {
	}]);

	
})();