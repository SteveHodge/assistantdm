<?php
$file = $_GET["name"].".xml";

if (!isset($_GET["name"]) || !file_exists($file)) {
	echo "No character "+$file;
	return;
}

header("Content-type: text/xml");

$last = filemtime($file);

while (1) {
	sleep(1);
	clearstatcache();
	if (filemtime($file) > $last) {
		echo file_get_contents($file);
		return;
	}
}
echo "error - unreachable code";

?>
