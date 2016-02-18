(function() {
	'use strict';
	var controllers = angular.module('idaControllers', ['ngSanitize']);

	controllers.controller('DashboardController', ['$scope', '$window', '$location', 'ida.remote', 'ida.alarm', function($scope, $window, $location, remote, alarm) {
		$scope.$on('ida.reloadDashboards', function(event) {
			$window.location.reload();
		});

		$scope.$on('ida.alarm.trigger', function(event, telegram) {
			$location.path("/alarm");
		});
	}]);

	controllers.controller('InformationDashboardController', ['$scope', '$interval', 'ida.state', function($scope, $interval, state) {
		var PAGE_FLIP_DELAY = 10000;
		var nextPageFlip;

		$scope.information = [];
		$scope.currentPage = 0;
		$scope.pageCount = 1;
		$scope.pageSize = 7;

		function setInformation(information) {
			if (information === undefined || information.length === 0) {
				$scope.information = [];
			} else {
				$scope.information = information;
			}
			$scope.currentPage = 0;
			$scope.pageCount = Math.ceil($scope.information.length / $scope.pageSize);
		}

		$scope.$on('state.information', function(event, information) {
			$scope.stopPageFlip();
			setInformation(information);
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

		setInformation(state.getInformation());
	}]);

	controllers.controller('RoomScheduleController', ['$scope', 'ida.state', function($scope, state) {
		$scope.rooms = [];

		function setRooms(rooms) {
			if (rooms === undefined || rooms.length === 0) {
				$scope.rooms = [];
			} else {
				$scope.rooms = rooms;
			}
		}

		$scope.$on('state.rooms', function(event, rooms) {
			setRooms(rooms);
		});

		setRooms(state.getRooms());
	}]);

	controllers.controller('AlarmTelegramController', ['$scope', '$location', 'ida.alarm', function($scope, $location, alarm) {
		var telegram = alarm.current();
		$scope.keyword = telegram.keyword;
		$scope.address = telegram.address;
		$scope.additionalInformation = telegram.additionalInformation;

		if (telegram.lat === undefined || telegram.lon === undefined) {
			telegram.lat = 49.826302;
			telegram.lon = 10.735984;
		}

		var map = L.map('map').setView([telegram.lat, telegram.lon], 17);
		L.tileLayer('http://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
			maxZoom: 18
		}).addTo(map);
		L.tileLayer('http://openfiremap.org/hytiles/{z}/{x}/{y}.png', {
			maxZoom: 18
		}).addTo(map);
		var marker = L.marker([telegram.lat, telegram.lon]).addTo(map);

		$scope.$on('ida.alarm.trigger', function(event, telegram) {
			// TODO update view based on telegram
		});
		$scope.$on('ida.alarm.reset', function(event) {
			$location.path("/dashboard");
		});

	}]);

	controllers.controller('AdminController', ['$scope', 'ida.state', 'ida.remote', 'ida.alarm', function($scope, state, remote, alarm) {
		$scope.information = "";
		$scope.room1 = "";
		$scope.room2 = "";

		$scope.publishInformation = function() {
			var lines = $scope.information.trim().split('\n');
			var information = [];
			for (var i in lines) {
				var line = lines[i];
				if (line === '') continue;

				var info;
				if (line.startsWith('!')) {
					info = {text: line.substr(1), label: '!'};
				} else {
					info = {text: line};
				}
				information.push(info);
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
		};

		$scope.reloadDashboards = function() {
			remote.reloadDashboards();
		};

		$scope.triggerAlarm = function() {
			alarm.triggerAlarm({lat: 49.826302, lon: 10.735984, keyword: "B4 Person", address: "Steigerwaldstraße 13, Burgebrach", additionalInformation: ""});
		};

		$scope.resetAlarm = function() {
			alarm.resetAlarm();
		};
	}]);
})();