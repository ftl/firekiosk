(function() {
	'use strict';
	var services = angular.module('idaServices', []);

	services.factory('ida.switchPage', ['$location', function($location) {
		function switchToPage(page) {
			$location.path(page);
		}
		
		return {
			toDashboard: function() {
				switchToPage("/dashboard");
			},
			toAlarmTelegram: function() {
				switchToPage("/alarm");
			},
			toAdmin: function() {
				switchToPage("/admin");
			}
		}
	}]);
	
	services.factory('ida.state', ['$rootScope', 'ida.mqtt', function($rootScope, mqtt) {
		var information = [];
		var rooms = [];

		handle('/ida/information', 'state.information', function(payload) { information = payload; });
		handle('/ida/rooms', 'state.rooms', function(payload) { rooms = payload; });

		function handle(destinationPath, name, callback) {
			mqtt.subscribe(destinationPath);
			$rootScope.$on(destinationPath, function(event, message) {
				var payload = JSON.parse(message.payloadString);
				callback(payload);
				$rootScope.$broadcast(name, payload);
			});
		}

		function set(name, object) {
			mqtt.send(name, JSON.stringify(object));
		}

		function setRetained(name, object) {
			mqtt.sendRetained(name, JSON.stringify(object));
		}

		return {
			setInformation: function(information) {
				setRetained('/ida/information', information);
			},
			getInformation: function() {
				return information;
			},
			setRooms: function(rooms) {
				setRetained('/ida/rooms', rooms);
			},
			getRooms: function() {
				return rooms;
			}
		};
	}]);

	services.factory('ida.remote', ['$rootScope', 'ida.mqtt', function($rootScope, mqtt) {
		handle('/ida/remote/reloadDashboards', 'ida.reloadDashboards');

		function handle(destinationPath, name) {
			mqtt.subscribe(destinationPath);
			$rootScope.$on(destinationPath, function(event, message) {
				$rootScope.$broadcast(name, JSON.parse(message.payloadString));
			});
		}

		return {
			reloadDashboards: function() {
				mqtt.send('/ida/remote/reloadDashboards', JSON.stringify(Date.now()));
			}
		};
	}]);

	services.factory('ida.alarm', ['$rootScope', 'ida.mqtt', function($rootScope, mqtt) {
		var alarmTelegram = {
			lat: 49.826302, 
			lon: 10.735984,
			keyword: "B4 Person",
			address: "Steigerwaldstraße 13, Burgebrach",
			additionalInformation: "laut MT befinden sich noch mehrere Personen im Gebäude"
		};
		handle('/ida/alarm/trigger', 'ida.alarm.trigger', function(payload) { alarmTelegram = payload; });
		handle('/ida/alarm/reset', 'ida.alarm.reset', function() { alarmTelegram = {}; });

		function handle(destinationPath, name, callback) {
			mqtt.subscribe(destinationPath);
			$rootScope.$on(destinationPath, function(event, message) {
				var payload = JSON.parse(message.payloadString);
				callback(payload);
				$rootScope.$broadcast(name, payload);
			});
		}

		return {
			current: function() {
				return alarmTelegram;
			},
			triggerAlarm: function(telegram) {
				mqtt.send('/ida/alarm/trigger', JSON.stringify(telegram));
			},
			resetAlarm: function() {
				mqtt.send('/ida/alarm/reset', JSON.stringify(Date.now()));
			}
		};
	}]);
	
	services.factory('ida.mqtt', ['$rootScope', '$location', '$http', function($rootScope, $location, $http) {
		var connected = false;
		var pendingSubscriptions = [];
		var pahoClient;

		function connect(hostname, port, path) {
			console.log("Connecting to MQTT broker on " + hostname + " port " + port + " path " + path);
			var client = new Paho.MQTT.Client(hostname, Number(port), path, "ida" + Date.now());

			client.onMessageArrived = function(message) {
				console.log("onMessageArrived " + message.destinationName);
				$rootScope.$broadcast(message.destinationName, message);
				$rootScope.$apply();
			};

			client.onConnectionLost = function(responseObject) {
				if (responseObject !== 0) {
					console.log("onConnectionLost: " + responseObject.errorMessage);
				}
				connected = false;
			};

			client.connect({
				onSuccess: function() {
					console.log("Connected to MQTT broker.");
					connected = true;
					applyPendingSubscriptions();
				},
				onFailure: function() {
					console.log("Cannot connect to MQTT broker.");
					connected = false;
				}
			});

			return client;
		}

		function addPendingSubscription(filter, options) {
			pendingSubscriptions.push({filter: filter, options: options});
		}

		function applyPendingSubscriptions() {
			for (var i in pendingSubscriptions) {
				var pendingSubscription = pendingSubscriptions[i];
				console.log("Applying pending subscription to " + pendingSubscription.filter);
				pahoClient.subscribe(pendingSubscription.filter, pendingSubscription.options);
			}
			pendingSubscriptions = [];
		}


		$http({
			method: 'GET',
			url: '/mqtt.conf'
		})
		.then(
			function onSuccess(response) {
				console.log("Found mqtt.conf");
				console.log(response.data);
				pahoClient = connect(response.data.hostname, response.data.port, response.data.path);
			},
			function onError(response) {
				console.log("Cannot find mqtt.conf, using defaults.");
				console.log(response);
				pahoClient = connect($location.host(), 18830, "/iotDashboard");
			}
		);

		return {
			subscribe: function(filter, options) {
				if (!connected) {
					console.log("Not connected, cannot subscribe to " + filter);
					addPendingSubscription(filter, options);
					return;
				}

				console.log("Subscribing to " + filter);
				pahoClient.subscribe(filter, options);
			},
			unsubscribe: function(filter, options) {
				if (!connected) {
					console.log("Not connected, cannot unsubscribe from " + filter);
					return;
				}

				console.log("Unsubscribing from " + filter);
				pahoClient.unsubscribe(filter, options);
			},
			send: function(destinationName, payload) {
				if (!connected) {
					console.log("Not connected, cannot send to " + destinationName);
					return;
				}

				var message = new Paho.MQTT.Message(payload);
				message.destinationName = destinationName;
				pahoClient.send(message);
			},
			sendRetained: function(destinationName, payload) {
				if (!connected) {
					console.log("Not connected, cannot send to " + destinationName);
					return;
				}

				var message = new Paho.MQTT.Message(payload);
				message.destinationName = destinationName;
				message.retained = true;
				pahoClient.send(message);
			}
		};
	}]);
})();
