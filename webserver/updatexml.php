<?php
header('Content-Type: text/event-stream');
header('Cache-Control: no-cache');
header('Connection: close');	// required by mobile safari

$file = $_GET["name"].".xml";

if (!isset($_GET["name"]) || !file_exists($file)) {
	echo "No character "+$file;
	return;
}

$last[$file] = filemtime($file);

while (1) {
	sleep(1);
	clearstatcache();
	$t = filemtime($file);
	if ($t > $last[$file]) {
		echo "data: ".$file."\n\n";
		ob_flush();
		flush();
		$last[$file] = $t;
	}
}
echo "error - unreachable code";

?>
