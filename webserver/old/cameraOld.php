<?php
$files = array('photo1.jpg');
$widths = array(640);
$heights = array(480);
?>

<HTML><HEAD><TITLE>WebCam Page</TITLE>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">

<script language="JavaScript">
<!--
var freq = <?php 
	if (isset($_REQUEST['clock']) && $_REQUEST['clock'] > 0) {
		$clock = $_REQUEST['clock'];
	} else {
		$clock = 10;
	}
	echo $clock.";\n";
?>
var x = freq*1000;

function startClock(){
  x = x - 200;
  if (x % 1000 == 0) {
//    document.form1.clock.value = x / 1000;
	document.getElementById("counter").innerHTML = x / 1000;
  }
  timerID = setTimeout("startClock()", 200);
  if (x <= 0) {
    window.location.reload(true);
//    document.form1.submit();
    x = freq*1000;
  }
}

//  -->
</script>

</HEAD>

<BODY bgcolor="#FFFFFF" text="#000000" link="#0000FF" vlink="#800080" alink="#FF0000" onLoad="startClock();">

<TABLE width="175" cellpadding="1" bordercolorlight="#FFFFFF" bordercolordark="#000000" cellspacing="2" align="center">
<TR>
<?php
function getCombatTable() {
	$values = array();
	$file = fopen('initiative.txt','r');
	if ($file) {
		while (!feof($file)) {
			$line = fgets($file, 4096);
			$key = substr($line,0,strpos($line,'='));
			$value = trim(substr($line,strpos($line,'=')+1));
			if (isset($key) && isset($value)) {
				$values[$key]=$value;
			}
		}
		fclose($file);
	}

	$html = "";
	if ($values['lastindex']) {
		$round = $values['round'];
		if (!$round) $round = 0;
		$html .= "<table border=1 width=200>\n<tr><th>Combat Round</th><th>\n";
		$html .= $round . "</th><th>Initiative</th></tr>\n";
		for ($i = 1; $i <= $values['lastindex']; $i++) {
			if (isset($values["name".$i])) {
				$html .= "<tr><td colspan=2>".$values["name".$i]."</td>";
			} else {
				$html .= "<tr><td colspan=2>".$values["fixedname".$i]."</td>";
			}
			$html .= "<td>".$values["init".$i]."</td></tr>\n";
		}
		$html .= "</table>\n";
	}

	return $html;
}

foreach ($files as $i => $filename) {
	if ($i == 0) {
		echo "<td>";
		echo getCombatTable();
		echo "</td>";
	}
	echo "<TD valign='top'><table><tr>";
	if (file_exists($filename)) {
		echo "<a href={$filename} target=_blank><IMG WIDTH={$widths[$i]} HEIGHT={$heights[$i]} SRC='{$filename}'></a><br>";
		echo "This image last changed " . (time()-filectime($filename)) . " seconds ago";
		echo "</TD>";
	}
//	if ($i == 0) {
//		// put combat table in separate row
//		echo "<tr><td align=center colspan=2>";
//		echo getCombatTable();
//		echo "</td></tr>";
//	}	
	echo "</tr></table>";
}
?>
</tr>
<tr>
<TD align=center colspan=2>
<form name="form1" action="camera.php">
<?php
	if (isset($_REQUEST['clock']) && $_REQUEST['clock'] > 0) {
		$clock = $_REQUEST['clock'];
	} else {
		$clock = 10;
	}

	echo 'Images will be refreshed in <span id="counter">'.$clock.'</span> seconds. ';
	echo '<button onclick="window.location.reload(true)">Reload Now</button>';
	echo '<br>Set update frequency to every ';
	echo '<input name="clock" size="2" value="'.$clock.'"> seconds.';
?>
</form>
</TD>
</TR>
</TABLE>

</BODY>
</HTML>
