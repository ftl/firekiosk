(function() {
	'use strict';
	var controllers = angular.module('idaControllers', ['ngSanitize']);
	
	controllers.controller('DashboardController', ['$scope', '$window', 'ida.remote', function($scope, $window, remote) {
		$scope.$on('ida.reloadDashboards', function(event) {
			$window.location.reload();
		});
	}]);
	
	controllers.controller('InformationDashboardController', ['$scope', '$interval', 'ida.state', function($scope, $interval, state) {
		var PAGE_FLIP_DELAY = 5000;
		var nextPageFlip;
		
		$scope.information = [];
		$scope.currentPage = 0;
		$scope.pageCount = 1;
		$scope.pageSize = 7;
		
		$scope.$on('state.information', function(event, information) {
			$scope.stopPageFlip();
			
			if (information === undefined || information.length === 0) {
				$scope.information = [];
			} else {
				$scope.information = information;
			}
			$scope.currentPage = 0;
			$scope.pageCount = Math.ceil($scope.information.length / $scope.pageSize);
			
			$scope.startPageFlip();
		});
		
		$scope.flipToNextPage = function() {
			$scope.currentPage = ($scope.currentPage + 1) % $scope.pageCount;
		};
		
		$scope.startPageFlip = function() {
			if ($scope.pageCount <= 1) return;
			
			nextPageFlip = $interval($scope.flipToNextPage, PAGE_FLIP_DELAY);
			console.log('Page flipping started');
		};
		
		$scope.stopPageFlip = function() {
			if (nextPageFlip === undefined) return;
			
			$interval.cancel(nextPageFlip);
			nextPageFlip = undefined;
			console.log('Page flipping stopped');
		};
		
		$scope.$on('destroy', function() {
			$scope.stopPageFlip();
		});
	}]);
	
	controllers.controller('RoomScheduleController', ['$scope', 'ida.state', function($scope, state) {
		$scope.rooms = [];
		
		$scope.$on('state.rooms', function(event, rooms) {
			if (rooms === undefined || rooms.length === 0) {
				$scope.rooms = [];
			} else {
				$scope.rooms = rooms;
			}
		});
	}]);
	
	controllers.controller('AdminController', ['$scope', 'ida.state', 'ida.remote', function($scope, state, remote) {
		$scope.information = "";
		$scope.room1 = "";
		$scope.room2 = "";
		
		$scope.publishInformation = function() {
			var lines = $scope.information.trim().split('\n');
			var information = [];
			for (var i in lines) {
				if (lines[i] === '') continue;
				information.push({text: lines[i]});
			}
			state.setInformation(information);
		};
		
		$scope.publishRoomSchedule = function() {
			var rooms = [];
			if ($scope.room1 !== '') {
				rooms.push({name: "Schulung 1", text: $scope.room1});
			}
			if ($scope.room2 !== '') {
				rooms.push({name: "Schulung 2", text: $scope.room2});
			}
			state.setRooms(rooms);
		}
		
		$scope.reloadDashboards = function() {
			remote.reloadDashboards();
		}
	}]);
})();