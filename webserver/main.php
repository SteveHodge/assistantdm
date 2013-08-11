<?php
header('Content-Type: text/event-stream');
header('Cache-Control: no-cache');
header('Connection: close');	// required by mobile safari

$file = array('initiative.txt','photo1.jpg','tokens1.png','tokens1.txt');

foreach($file as $f) {
	$last[$f] = filemtime($f);
}

while (1) {
	sleep(1);
	clearstatcache();
	foreach($file as $f) {
		$t = filemtime($f);
		if ($t > $last[$f]) {
			echo "data: ".$f."\n\n";
			ob_flush();
			flush();
			$last[$f] = $t;
		}
	}
}
echo "error - unreachable code";

?>
