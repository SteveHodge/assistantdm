var express = require('express');
var mustache = require('mustache');
var fs = require('fs');
var spells = require('./spells');
var util = require('util');

var main_template = fs.readFileSync('./main.mustache', 'binary');
var tab_templates = {
	prepare: fs.readFileSync('./prepare.mustache', 'binary'),
	learn: fs.readFileSync('./learn.mustache', 'binary'),
	scribe: fs.readFileSync('./scribe.mustache', 'binary')
};

var app = express();

app.use(express.favicon());
app.use(express.logger('dev'));
app.use(express.json());

app.get('/:name/config', function(req, res, next) {
	'use strict';

	spells.getConfig(req.params.name, function(err, config) {
		if (err) { return next('Character "'+req.params.name+'" config cannot be loaded:\n'+err); }
		// proper JSON output (but not so compact):
		//res.type('application/json');
		//res.send(JSON.stringify(config, null, 2));
		// debug output (but not quite JSON):
		res.type('text/plain');
		res.send(util.inspect(config, { depth: null }));
	});
});

app.get('/:name/character', function(req, res, next) {
	'use strict';

	spells.getCharacter(req.params.name, function(err, data) {
		if (err) { return next('Character "'+req.params.name+'" cannot be loaded:\n'+err); }
		// proper JSON output (but not so compact):
		//res.type('application/json');
		//res.send(JSON.stringify(data, null, 2));
		// debug output (but not quite JSON):
		res.type('text/plain');
		res.send(util.inspect(data, { depth: null }));
	});
});

app.put('/:name/spells', function(req, res, next) {
	'use strict';
	
	spells.setSpells(req.params.name, req.body, function(err) {
		if (err) { return next('Error saving spells for '+req.params.name+':\n'+err); }
		res.send(200);
	});
});

app.get('/:name/spells', function(req, res, next) {
	'use strict';

	spells.getSpells(req.params.name, function(err, data) {
		if (err) { return next('Character "'+req.params.name+'" spells cannot be loaded:\n'+err); }
		// proper JSON output (but not so compact):
		//res.type('application/json');
		//res.send(JSON.stringify(data, null, 2));
		// debug output (but not quite JSON):
		res.type('text/plain');
		res.send(util.inspect(data, { depth: null }));
	});
});

app.put('/initiative.json', saveFile);
app.put('/photo1.jpg', saveFile);
app.put('/tokens1.json', saveFile);
app.put('/tokens1.png', saveFile);

var subscribers = new Array();

app.get('/updates/webcam', function(req, res, next) {
	req.socket.setTimeout(Infinity);

	console.log('subscribed '+util.inspect(req.headers));

	res.writeHead(200, {
		'Content-Type': 'text/event-stream',
		'Cache-Control': 'no-cache',
		'Connection': 'keep-alive'
	});
	res.write('retry: 10000\n');
	res.write('\n');

	//track subscriber
	subscribers.push({
		'req': req,
		'res': res
	});

	req.on('close', function() {
		for (var i = 0; i < subscribers.length; i++) {
			if (subscribers[i] && subscribers[i].req === req) {
				console.log('subscriber closed');
				subscribers[i] = undefined;
				break;
			}
		}
	});
});

// development only
//if ('development' === app.get('env')) {
	app.use(express.errorHandler());
//}

app.get('/updates/subscribers', function(req, res) {
	var html = '<html><body><ul>';
	for (var i = 0; i < subscribers.length; i++) {
		if (subscribers[i]) {
			html += '<li>' + util.inspect(subscribers[i].req.headers) + '</li>';
		}
	}
	html += '</ul></body></html>';
	res.send(html);
});

app.get('/:name', function(req, res, next) {
	'use strict';

	spells.getConfig(req.params.name, function(err, config) {
		var i, data = {};

		if (err) { return next('Character "'+req.params.name+'" cannot be loaded:\n'+err); }

		data.title = req.params.name;
		data.content = '';
		for (i = 0; i < config.length; i++) {
			if (config[i].type !== 'cast') {
				data.content += mustache.to_html(tab_templates[config[i].type], config[i]);
			} else {
				data.spells = config[i].spells;
			}
		}
		
		res.send(mustache.to_html(main_template, data));
	});
});

function saveFile(req, res, next) {
	'use strict';

	var data = new Buffer('');
	req.on('data', function(chunk) {
		data = Buffer.concat([data, chunk]);
	});
	req.on('end', function() {
		fs.writeFile('..'+req.path, data, function(err) {
			if (err) { return next('Error saving initiative.json\n'+err); }
			console.log("Updated "+req.path+" ("+data.length+" bytes)");
			res.send(200);
			
			for (var i = 0; i < subscribers.length; i++) {
				if (subscribers[i]) {
					console.log('SSE sent ('+i+')');
					subscribers[i].res.write('data: '+req.path+'\n\n');
				}
			}
		});
	});
}

var server = app.listen(8888, function() {
	'use strict';

	console.log('Listening on port %d', server.address().port);
});
