/*
enhancements:
track other uses: per-day abilities, item charges, etc
player page better error reporting
clean up updater.js code. probably best of have a comprehensive version that figures out what needs updating dynamically
player configuration tab
reimplement character page using same code as player page
/debug/<character> - combined debug page with all config + subscribers to the character
/dm - dm log that tracks updates to characters spells (particularly casting) and/or feed back to AssistantDM

web structure:
/ - webcam (GET)
/<player> - complete page with character + webcam (GET)
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

var subscribers = new Array();

var spells_template = fs.readFileSync(__dirname+'/templates/spells.mustache', 'binary');
var sheet_template = fs.readFileSync(__dirname+'/templates/charactersheet.mustache', 'binary');
var char_template = fs.readFileSync(__dirname+'/templates/character.mustache', 'binary');
var player_template = fs.readFileSync(__dirname+'/templates/player.mustache', 'binary');

var app = express();
app.enable('trust proxy');

//app.use(express.compress());

app.use(express.favicon());
app.use(express.logger('dev'));

app.use('/static', express.static(__dirname+'/static'));

app.use(express.json());

app.get('/', function(req, res, next) {
	res.sendfile('static/webcam.html');
});

// updates
// CORS functionality
app.get('/updates/*', function(req, res, next) {
	var oneof  = false;
	if(req.headers.origin) {
		//console.log('CORS request from: '+req.headers.origin);
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

		res.send(mustache.to_html(spells_template, content));
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
// this has same route as character below. if a player config file doesn't exist then it's assumed to be a character
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

				if (config.character && config.sheet1) {
					if (fs.existsSync(__dirname+'/characters/'+config.character+'.xml')) {
						pageData.sheet1 = true;
						pageData.name = config.character;
					}
				}

				if (config.webcam) {
					pageData.webcam = true;
				}

				res.send(mustache.to_html(player_template, pageData));
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
			
			data.name = req.params.name;
			data.content = '';
			data.spells = '';
		}

		res.send(mustache.to_html(char_template, data));
	});
});

// webcam media
app.put('/static/initiative.json', function(req, res, next) { saveFile(req.path, req, res, next); });
app.put('/static/camera.jpg', function(req, res, next) { saveFile(req.path, req, res, next); });
app.put('/static/tokens.json', function(req, res, next) { saveFile(req.path, req, res, next); });
app.put('/static/tokens.png', function(req, res, next) { saveFile(req.path, req, res, next); });

// ------------------- debug routes -------------------
app.get('/debug/subscribers', function(req, res) {
	var html = '<html><body><ul>';
	for (var i = 0; i < subscribers.length; i++) {
		if (subscribers[i]) {
			html += '<li>' + subscribers[i].file + ': '+subscribers[i].req.ip+'<br>';
			html += subscribers[i].req.headers['user-agent'] + '<br>';
			if (subscribers[i].req.headers.authorization) {
				html += subscribers[i].req.headers.authorization + '<br>';
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
				//console.log('data: '+path.basename(file));
				if (subscribers[i] && (subscribers[i].file === '*' || subscribers[i].file === path.basename(file))) {
					subscribers[i].res.write('data: '+path.basename(file)+'\n\n');
				}
			}
		});
	});
}

function subscribe(file, req, res, next) {
	'use strict';

	req.socket.setTimeout(Infinity);

	console.log('subscribed '+req.ip+' ('+file+')'); //+':' +util.inspect(req.headers));

	res.writeHead(200, {
		'Content-Type': 'text/event-stream',
		'Cache-Control': 'no-cache',
	});
	res.write('retry: 10000\n');
	res.write('\n');

	//track subscriber
	subscribers.push({
		'req': req,
		'res': res,
		'file': file
	});

	req.on('close', function() {
		for (var i = 0; i < subscribers.length; i++) {
			if (subscribers[i] && subscribers[i].req === req) {
				//console.log('subscriber closed');
				subscribers[i] = undefined;
				break;
			}
		}
	});
}
