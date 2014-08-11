<?xml version="1.0" encoding="ISO-8859-1"?>

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	<xsl:import href="CharacterTemplate2.xsl"/>

	<xsl:output method="html" encoding="utf-8" indent="yes" />

	<xsl:template match="/">
	<html xmlns="http://www.w3.org/1999/xhtml">
		<head>
			<title>
				<xsl:value-of select="/Character[1]/@name"/>
			</title>
			<link href="http://stevehodge.net/assistantdm/static/character.css" rel="stylesheet" title="new" type="text/css" />
			<script src="http://stevehodge.net/assistantdm/static/dialog_box.js"></script>
		</head>
		<body style="font-size:50%;">
			<div id="content">
				<xsl:apply-templates/>
			</div>
		</body>
	</html>
	</xsl:template>

</xsl:stylesheet>
