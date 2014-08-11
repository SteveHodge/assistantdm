/*
enhancements:
reorder items/abilities
debug page with more detail on subscribers
/<character>/sheet2 - second page of character sheet
/debug/<character> - combined debug page with all config + subscribers to the character
/dm - dm log that tracks updates to characters spells (particularly casting) and/or feed back to AssistantDM

note on updates: mobile safari seems to have problems with the open SSE connection - it won't download
images (and some other types of static content) from the same domain. to avoid the issue updates are
requested from a different sub-domain (updates.stevehodge.net) which is not authenticated. CORS is used
to enable this. the alternate of having static content on a separate domain would mean that all static
content was publically accessible and we'd still need CORS for XSLT stylesheets (if CORS is even possible
there). this mobile safari limit may still cause problems if multiple tabs are opened - possibly only
one will successfully update (the player page feature should reduce the need to have multiple tabs open).
possibly mobile safari is waiting for the SSE request to complete so it can use the same keep-alive
connection to download images. perhaps cancelling keep-alive would fix this issue.

web structure:
/ - webcam (GET)
/<player> - configurable page with character, spells, and/or webcam (GET+POST config)
/<character> - full character page (character sheet + spells) (GET+PUT character sheet updates)
/<character>/xml - static character sheet (GET)
/<character>/spells - spell tracking page (GET+PUT spell updates)
/<character>/sheet - character sheet (GET)
/updates/all - SSE stream for all file updates (GET)
/updates/<character> - SSE stream for character updates (GET)
/debug/subscribers - SSE subscriptions (GET)
TODO /debug/<character> - json character all config (GET)
/debug/<character>/character - json spell choices (GET)
/debug/<character>/spells - json spell choices (GET)
/debug/<character>/config - json spell page configuration (GET)
/static/initiative.json (GET+PUT)
/static/camera.jpg (GET+PUT)
/static/tokens.json (GET+PUT)
/static/tokens.png (GET+PUT)
/static/* - other static files (GET)
/static/images/* - images used by dialog_box.js (GET)

directory structure:
characters/ - character json and xml files
node_modules/
templates/ - mustache files
server.js
spells.js
static/ - static files (including camera/token/initiative files)
*/

var express = require('express');
var mustache = require('mustache');
var fs = require('fs');
var spells = require('./spells');
var util = require('util');
var path = require('path');

var subscribers = [];
var sub_tracking = {};

var sheet_template = fs.readFileSync(__dirname+'/templates/charactersheet.mustache', 'binary');
var main_template = fs.readFileSync(__dirname+'/templates/main.mustache', 'binary');

var app = express();
app.enable('trust proxy');

//app.use(express.compress());

app.use(express.favicon());
app.use(express.logger(':remote-addr [:date] :method ":url" :status'));

app.use(express.json());
app.use(express.urlencoded());

app.get('/static/:name', function(req, res, next) {
	if (req.query.token) {
		if (!sub_tracking[req.query.token]) sub_tracking[req.query.token] = {};
		if (!sub_tracking[req.query.token][req.params.name]) sub_tracking[req.query.token][req.params.name] = {};
		sub_tracking[req.query.token][req.params.name].last_fetched = new Date();
		sub_tracking[req.query.token][req.params.name].latest = true;
	}
	next();
});

app.use('/static', express.static(__dirname+'/static'));

app.get('/', function(req, res, next) {
	res.sendfile('static/webcam.html');
});

// updates
// CORS functionality
app.get('/updates/*', function(req, res, next) {
	var oneof  = false;
	if(req.headers.origin) {
		console.log('CORS request from: '+req.headers.origin);
		res.header('Access-Control-Allow-Origin', req.headers.origin);
		oneof = true;
	}
	if(req.headers['access-control-request-method']) {
		console.log('CORS requested methods: '+req.headers['access-control-request-method']);
		res.header('Access-Control-Allow-Methods', req.headers['access-control-request-method']);
		oneof = true;
	}
	if(req.headers['access-control-request-headers']) {
		console.log('CORS requested headers: '+req.headers['access-control-request-headers']);
		res.header('Access-Control-Allow-Headers', req.headers['access-control-request-headers']);
		oneof = true;
	}
	if(oneof) {
		res.header('Access-Control-Max-Age', 60 * 60 * 24 * 365);
		res.header('Access-Control-Allow-Credentials', true);
	}
	
	// intercept OPTIONS method
	if (oneof && req.method == 'OPTIONS') {
		console.log('OPTIONS Ok');
		res.send(200);
	} else {
		return next();
	}
});

app.get('/updates/all', function(req, res, next) { subscribe('*', req, res, next); });

app.get('/updates/:name.xml', function(req, res, next) { subscribe(req.params.name+'.xml', req, res, next); });

// character spells
app.get('/:name/spells', function(req, res, next) {
	'use strict';

	spells.getContent(req.params.name, function(err, content) {
		if (err) { return next('Character "'+req.params.name+'" cannot be loaded:\n'+err); }

		content.title = content.name;
		content.saveurl = '/assistantdm/'+req.params.name+'/spells';
		res.send(mustache.to_html(main_template, content));
	});
});

app.put('/:name/spells', function(req, res, next) {
	'use strict';
	
	spells.setSpells(req.params.name, req.body, function(err) {
		if (err) { return next('Error saving spells for '+req.params.name+':\n'+err); }
		res.send(200);
	});
});

// character sheet
app.put('/:name', function(req, res, next) { saveFile('/characters/'+req.params.name+'.xml', req, res, next); });

app.get('/:name/sheet', function(req, res, next) {
	'use strict';
	
	var data = {name: req.params.name};
	res.send(mustache.to_html(sheet_template, data));
});

app.get('/:name/xml', function(req, res, next) {
	res.sendfile(__dirname+'/characters/'+req.params.name+'.xml');
});

// player page
// config saving:
app.post('/:name', function(req, res, next) {
	'use strict';

	var config = {
		webcam: req.body.webcam === 'on',
		sheet1: req.body.sheet1 === 'on',
		sheet2: req.body.sheet2 === 'on',
		spells: req.body.spells === 'on',
		character: req.body.character,
		fontsize: req.body.fontsize
	};
	if (!config.character) {
		config.sheet1 = false;
		config.sheet2 = false;
	}
	if (!config.fontsize) config.fontsize = 8;
	console.log(util.inspect(config));

	fs.writeFile(__dirname+'/players/'+req.params.name+'.player', JSON.stringify(config), function(err) {
		if (err) return next('Failed to save configuration');
		res.redirect('/assistantdm/'+req.params.name);
	});
});

// this has same route as character below. if a player config file doesn't exist then it's assumed to be a character and this
// handler will drop through to the next
app.get('/:name', function(req, res, next) {
	'use strict';
	
	var configFile = __dirname+'/players/'+req.params.name+'.player';
	fs.exists(configFile, function(exists) {
		if (!exists) return next();
		
		fs.readFile(configFile, function(err, data) {
			var config;
			var pageData = null;

			if (err) return next(err);
			
			try {
				config = JSON.parse(data);
							
				if (config.character && config.spells) {
					spells.getContent(config.character, function(err, spellsData) {
						if (!err) {
							pageData = spellsData;
						} else {
							pageData = {};
						}
						send();
					});
				} else {
					pageData = {};
					send();
				}
			} catch (e) {
				return next(e);
			}

			function send() {
				if (pageData === null) return;
				
				pageData.title = 'Assistant DM';
				pageData.fontsize = config.fontsize;
				pageData['fontsize'+config.fontsize] = true;
				pageData.saveurl = '/assistantdm/'+config.character+'/spells';
				pageData.name = config.character;
				pageData.webcam = config.webcam;
				pageData.config = true;

				if (config.character && (config.sheet1 || config.sheet2)) {
					if (fs.existsSync(__dirname+'/characters/'+config.character+'.xml')) {
						if (config.sheet1) pageData.sheet1 = true;
						if (config.sheet2) pageData.sheet2 = true;
					}
				}

				res.send(mustache.to_html(main_template, pageData));
			}

		});
	});
});

// full character (sheet + spells)
app.get('/:name', function(req, res, next) {
	'use strict';

	spells.getContent(req.params.name, function(err, data) {
		if (err) {
			if (!fs.existsSync(__dirname+'/characters/'+req.params.name+'.xml')) {
				// no config and no xml file = no character
				return next();
			}

			data = {};			
			data.name = req.params.name;
			data.content = '';
			data.spells = '';
		}

		data.title = data.name;
		data.sheet1 = true;
		data.sheet2 = true;
		data.fontsize = 8;
		data.saveurl = '/assistantdm/'+req.params.name+'/spells';
		res.send(mustache.to_html(main_template, data));
	});
});

// couldn't find player or character so assume a new player
app.get('/:name', function(req, res, next) {
	'use strict';

	var data = {
		title: 'Assistant DM',
		webcam: true,
		sheet1: false,
		sheet2: false,
		spells: false,
		fontsize: 8,
		name: '',
		config: true
	};

	res.send(mustache.to_html(main_template, data));
});


// webcam media
app.put('/static/initiative.json', function(req, res, next) { saveFile(req.path, req, res, next); });
app.put('/static/camera.jpg', function(req, res, next) { saveFile(req.path, req, res, next); });
app.put('/static/tokens.json', function(req, res, next) { saveFile(req.path, req, res, next); });
app.put('/static/tokens.png', function(req, res, next) { saveFile(req.path, req, res, next); });

// ------------------- debug routes -------------------
app.get('/debug/subscribers', function(req, res) {
	var html = '<html><head><style>';
	html += 'table {border-collapse: collapse;} table, th, td {border: 1px solid black;}';
	html += '</style></head><body><ul>';
	for (var i = 0; i < subscribers.length; i++) {
		if (subscribers[i]) {
			html += '<li>';
			if (subscribers[i].closed) html += '<b>Closed </b>';
			html += subscribers[i].req.ip + ', File: '+subscribers[i].file;
			html += ', Token: '+subscribers[i].token+', Since: '+subscribers[i].first_connect;
			html += ', Reconnects: '+subscribers[i].reconnects+'<br>';
			html += 'User agent: '+subscribers[i].req.headers['user-agent'] + '<br>';
			if (subscribers[i].req.headers.authorization) {
				html += 'Authorization: '+subscribers[i].req.headers.authorization + '<br>';
			}
			if(subscribers[i].token) {
				html += '<table style="border:thin solid black;"><tr><th>File</th><th>Last Downloaded</th><th>Last Updated</th><th>Got Latest</th></tr>';
				var tracking = sub_tracking[subscribers[i].token];
				for (var file in tracking) {
					if (tracking.hasOwnProperty(file)) {
						html += '<tr><td>'+file+'</td><td>'+tracking[file].last_fetched+'</td>';
						html += '<td>'+tracking[file].updated+'</td>';
						html += '<td>'+tracking[file].latest+'</td>';
						html += '</tr>';
					}
				}
				html += '</table>';
			}
		}
	}
	html += '</ul></body></html>';
	res.send(html);
});

app.get('/debug/:name/config', function(req, res, next) {
	'use strict';

	spells.getConfig(req.params.name, function(err, config) {
		if (err) { return next('Character "'+req.params.name+'" config cannot be loaded:\n'+err); }
		// proper JSON output (but not so readable):
		//res.type('application/json');
		//res.send(JSON.stringify(config, null, 2));
		// debug output (but not quite JSON):
		res.type('text/plain');
		res.send(util.inspect(config, { depth: null }));
	});
});

app.get('/debug/:name/character', function(req, res, next) {
	'use strict';

	spells.getCharacter(req.params.name, function(err, data) {
		if (err) { return next('Character "'+req.params.name+'" cannot be loaded:\n'+err); }
		// proper JSON output (but not so readable):
		//res.type('application/json');
		//res.send(JSON.stringify(data, null, 2));
		// debug output (but not quite JSON):
		res.type('text/plain');
		res.send(util.inspect(data, { depth: null }));
	});
});

app.get('/debug/:name/spells', function(req, res, next) {
	'use strict';

	spells.getSpells(req.params.name, function(err, data) {
		if (err) { return next('Character "'+req.params.name+'" spells cannot be loaded:\n'+err); }
		// proper JSON output (but not so readable):
		//res.type('application/json');
		//res.send(JSON.stringify(data, null, 2));
		// debug output (but not quite JSON):
		res.type('text/plain');
		res.send(util.inspect(data, { depth: null }));
	});
});

// development only
//if ('development' === app.get('env')) {
	app.use(express.errorHandler());
//}

var server = app.listen(8888, function() {
	'use strict';

	console.log('Listening on port %d', server.address().port);
});

function saveFile(file, req, res, next) {
	'use strict';

	var data = new Buffer('');
	req.on('data', function(chunk) {
		data = Buffer.concat([data, chunk]);
	});
	req.on('end', function() {
		//console.log('Updating '+__dirname+file);
		fs.writeFile(__dirname+file, data, function(err) {
			if (err) { return next('Error saving '+file+':\n'+err); }
			console.log('Updated '+file+' ('+data.length+' bytes)');
			res.send(200);
			
			for (var i = 0; i < subscribers.length; i++) {
				var f = path.basename(file);
				//console.log('data: '+f);
				if (subscribers[i] && (subscribers[i].file === '*' || subscribers[i].file === f)) {
					subscribers[i].res.write('data: '+f+'\n\n');
					var token = subscribers[i].token;
					if (token && sub_tracking[token] && sub_tracking[token][f]) {
						sub_tracking[token][f].updated = new Date();
						sub_tracking[token][f].latest = false;
					}
				}
			}
		});
	});
}

function subscribe(file, req, res, next) {
	'use strict';

	req.socket.setTimeout(Infinity);

	console.log('subscribed '+req.ip+' ('+file+')'); //+':' +util.inspect(req.headers));
	//console.log('token = '+req.query.token);

	res.writeHead(200, {
		'Content-Type': 'text/event-stream',
		'Cache-Control': 'no-cache',
	});
	res.write('retry: 10000\n');
	res.write('\n');

	//track subscriber
	// if there is an existing closed connection with the same ip address and token we'll replace that, otherwise we'll add a new one
	var found = false;
	for (var i = 0; i < subscribers.length; i++) {
		if (subscribers[i] && subscribers[i].token === req.query.token && subscribers[i].closed && subscribers[i].req.ip === req.ip) {
			found = true;
			subscribers[i].req = req;
			subscribers[i].res = res;
			subscribers[i].file = file;
			subscribers[i].closed = false;
			subscribers[i].reconnects++;
			break;
		}
	}	
	
	if (!found) {
		subscribers.push({
			'req': req,
			'res': res,
			'file': file,
			'token': req.query.token,
			'reconnects': 0,
			'first_connect': new Date
		});
	}

	req.on('close', function() {
		for (var i = 0; i < subscribers.length; i++) {
			if (subscribers[i] && subscribers[i].req === req) {
				//console.log('subscriber closed: '+req.ip);
				//subscribers[i] = undefined;
				subscribers[i].closed = true;
				break;
			}
		}
	});
}
