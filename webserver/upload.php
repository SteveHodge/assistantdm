<?php
//var_dump($_SERVER['REQUEST_METHOD']);
//var_dump($_SERVER['REQUEST_URL']);
//var_dump($_SERVER['PATH_INFO']);

if ($_SERVER['REQUEST_METHOD'] == "PUT") {
	$filename = substr($_SERVER['PATH_INFO'],1);
	if (($stream = fopen('php://input', "r")) != FALSE) {
		file_put_contents($filename,stream_get_contents($stream));
	}
}

// to write in chunks:
///* PUT data comes in on the stdin stream */
//$putdata = fopen("php://input", "r");
//
///* Open a file for writing */
//$fp = fopen("myputfile.ext", "w");
//
///* Read the data 1 KB at a time
//   and write to the file */
//while ($data = fread($putdata, 1024))
//  fwrite($fp, $data);
//
///* Close the streams */
//fclose($fp);
//fclose($putdata);
?>
