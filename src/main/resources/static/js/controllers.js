(function() {
	'use strict';
	var controllers = angular.module('idaControllers', ['ngSanitize']);

	controllers.controller('DashboardController', ['$scope', '$window', '$location', 'ida.remote', 'ida.alarm', 'ida.switchPage', function($scope, $window, $location, remote, alarm, switchPage) {
		$scope.$on('ida.reloadDashboards', function(event) {
			$window.location.reload();
		});

		$scope.$on('ida.alarm.trigger', function(event) {
			switchPage.toAlarmTelegram();
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
			if (!nextPageFlip) return;

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

	controllers.controller('AlarmTelegramController', ['$scope', '$http', '$interval', 'ida.alarm', 'ida.switchPage', function($scope, $http, $interval, alarm, switchPage) {
		var ZOOM_FLIP_DELAY = 15000;
		var zoomLevels = [17, 14];
		var currentZoomLevel = 0;

		var map;
		var marker;
		var hydrantsLayer;
		var nextZoomFlip;

		$scope.mapValid = true;

		function showTelegram(telegram) {
			$scope.keyword = telegram.keyword;
			$scope.address = telegram.address;
			$scope.additionalInformation = telegram.additionalInformation;
			$scope.mapValid = true;

			showMapByAddress(telegram.address);
		}

		function initMap() {
			map = L.map('map', {
				maxZoom: 17,
				zoom: 15,
				zoomControl: false,
				attributionControl: false,
				fadeAnimation: false,
				zoomAnimation: false,
				markerZoomAnimation: false
			});
			L.tileLayer('http://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
				maxZoom: 18
			}).addTo(map);
			hydrantsLayer = L.tileLayer('http://openfiremap.org/hytiles/{z}/{x}/{y}.png', {
				maxZoom: 17
			});
			hydrantsLayer.addTo(map);
			marker = L.marker([0, 0]).addTo(map);
		}

		function showMap(lat, lon) {
			if (nextZoomFlip) {
				$interval.cancel(nextZoomFlip);
				nextZoomFlip = undefined;
			}
			currentZoomLevel = 0;
			var position = L.latLng(lat, lon);
			map.setView(position, zoomLevels[currentZoomLevel]);
			hydrantsLayer.addTo(map);
			marker.setLatLng(position).update();
			$scope.mapValid = true;
			nextZoomFlip = $interval(flipZoomLevel, ZOOM_FLIP_DELAY);
		}

		function showMapByAddress(address) {
			var encodedAddress = encodeURIComponent(address);
			console.log("Showing map for " + encodedAddress);
			$http({
				method: 'GET',
				url: 'http://nominatim.openstreetmap.org?q=' + encodedAddress + '&format=json'
			})
			.then(
				function onSuccess(response) {
					console.log("Found address");
					console.log(response.data);
					if (response.data.length === 0) {
						$scope.mapValid = false;
						return;
					}

					var lat = Number(response.data[0].lat);
					var lon = Number(response.data[0].lon);

					showMap(lat, lon);
				},
				function onError(response) {
					console.log("Cannot find address");
					console.log(response);
					$scope.mapValid = false;
				}
			);
		}

		function flipZoomLevel() {
			currentZoomLevel = (currentZoomLevel + 1) % zoomLevels.length;
			if (currentZoomLevel === 0) {
				hydrantsLayer.addTo(map);
			} else {
				map.removeLayer(hydrantsLayer);
			}
			map.setZoom(zoomLevels[currentZoomLevel]);
		}

		$scope.$on('ida.alarm.trigger', function(event, telegram) {
			showTelegram(telegram);
		});
		$scope.$on('ida.alarm.reset', function(event) {
			switchPage.toDashboard();
		});
		$scope.$on('destroy', function() {
			$interval.cancel(nextZoomFlip);
			map.remove();
		});

		initMap();
		showTelegram(alarm.current());
	}]);

	controllers.controller('AlarmInputController', ['$scope', 'ida.switchPage', 'ida.alarm', function($scope, switchPage, alarm) {
		$scope.keyword = "";
		$scope.address = "";
		$scope.additionalInformation = "";

		$scope.triggerAlarm = function() {
			alarm.triggerAlarm({
				keyword: $scope.keyword,
				address: $scope.address,
				additionalInformation: $scope.additionalInformation
			});
			// switchPage.toAdmin();
		};
		$scope.resetAlarm = function() {
			alarm.resetAlarm();
		};
		$scope.cancel = function() {
			switchPage.toAdmin();
		};
	}]);

	controllers.controller('AdminController', ['$scope', 'ida.switchPage', 'ida.state', 'ida.remote', 'ida.alarm', function($scope, switchPage, state, remote, alarm) {
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
			switchPage.toAlarmInput();
		};

		$scope.resetAlarm = function() {
			alarm.resetAlarm();
		};
	}]);
})();