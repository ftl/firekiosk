(function() {
	'use strict';
	
	var module = angular.module('firekioskMembershipFee', ['ngSanitize']);

	module.controller('MembershipFeeController', ['$scope', '$http', function($scope, $http) {
		$scope.liableMembers = [];
		$scope.collectionDate = '2016-10-01';
		$scope.debitText = 'Jahresbeitrag 2016';

		$scope.refreshLiableMembers = function() {
			$http({method: 'GET', url: '/api/liableMembers?collectionDate=' + $scope.collectionDate}).success(function(data) {
				$scope.liableMembers = data;
			});
		}

		$scope.refreshLiableMembers();
	}]);
		
	module.filter('money', function() {
		return function(amount) {
			var paddedAmount, formattedAmount;
			
			if (!amount) return '0,00€';
			
			if (amount < 100) {
				paddedAmount = ('000' + amount).slice(-3);
			} else {
				paddedAmount = String(amount);
			}
			formattedAmount = paddedAmount.slice(0, -2) + ',' + paddedAmount.slice(-2) + '€';
			
			return formattedAmount;
		};
	});
	
	module.filter('iban', function() {
		return function(iban) {
			var formattedIban, i;

			i = 0;
			formattedIban = '';
			while (i < iban.length) {
				if (formattedIban.length > 0) {
					formattedIban += ' ';
				}
				formattedIban += iban.slice(i, i + 4);
				i += 4;
			}
			
			return formattedIban;
		};
	});
	
})();