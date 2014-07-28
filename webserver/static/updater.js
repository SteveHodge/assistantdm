var updater = {
	listeners:[],
	source: null,
	onError: null,	// handler for errors. will be passed the EventSource's readystate
	onOpen: null,	// handler for open events

	addListener: function(t, l) {
		updater.listeners.push({type: t, listener: l});
	},

	openConnection: function() {
		var type = null;
		
		if (updater.source !== null) return;
		
		if(typeof(EventSource) === "undefined") {
			alert("Server-sent events not supported.\nGet a better browser - Firefox, Chrome, Safari, and Opera all support this, IE is shit");
			return false;
		} 
		
		for (var i = 0; i < updater.listeners.length; i++) {
			if (type === null) {
				type = updater.listeners[i].type;
			} else if (type !== updater.listeners[i].type) {
				type = 'all';
				break;
			}
		}

		updater.source = new EventSource('http://updates.stevehodge.net/assistantdm/updates/'+type, { withCredentials: true });

		updater.source.addEventListener('open', function(e) {
			if (updater.onOpen) updater.onOpen();
		}, false);

		updater.source.addEventListener('error', function(event) {
			if (updater.onError) updater.onError(event.target.readyState);
			if (event.target.readyState === EventSource.CLOSED && updater.source) {
				updater.source.close();
			}
		}, false);
	
		updater.source.addEventListener('message', function(event) {
			var lines = event.data.split("\n");
			for (i = 0; i < lines.length; i++) {
				for (j = 0; j < updater.listeners.length; j++) {
					if (lines[i] === updater.listeners[j].type) {
						updater.listeners[j].listener();
					}
				}
			}
		}, false);

		return true;
	}
};
