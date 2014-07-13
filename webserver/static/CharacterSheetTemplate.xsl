<?xml version="1.0" encoding="ISO-8859-1"?>

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	<xsl:import href="/assistantdm/static/CharacterTemplate.xsl"/>

	<xsl:output method="html" encoding="utf-8" indent="yes" />

	<xsl:template match="/">
	<html xmlns="http://www.w3.org/1999/xhtml">
		<head>
			<title>
				<xsl:value-of select="/Character[1]/@name"/>
			</title>
			<link href="/assistantdm/static/character.css" rel="stylesheet" title="new" type="text/css" />
			<script type="text/javascript" src="/assistantdm/static/dialog_box.js"></script>
		</head>
		<body style="font-size:62.5%;">
			<div id="content">
				<xsl:apply-templates/>
			</div>
		</body>
	</html>
	</xsl:template>

</xsl:stylesheet>
