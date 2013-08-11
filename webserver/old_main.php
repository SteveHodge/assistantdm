<?php
$file = array('initiative.txt','photo1.jpg');

foreach($file as $f) {
	$last[$f] = filemtime($f);
}

while (1) {
	sleep(1);
	clearstatcache();
	foreach($file as $f) {
		if (filemtime($f) > $last[$f]) {
			echo "updated: ".$f."\n";
			if (substr_compare($f,".jpg",-4,4) != 0) {
				echo file_get_contents($f);
			}
			return;
		}
	}
}
echo "error - unreachable code";

?>
