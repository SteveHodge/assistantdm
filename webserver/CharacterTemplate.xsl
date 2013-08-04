<?xml version="1.0" encoding="ISO-8859-1"?>

<!DOCTYPE stylesheet [
<!ENTITY nbsp  "&#160;" >
]>

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	<xsl:output method="html" encoding="utf-8" indent="yes" />

<xsl:template name="weapon">
	<xsl:param name="weapon"/>
	<td class="L">&nbsp;</td>
	<td colspan="17" rowspan="3" class="weapon-name-class">
		<xsl:value-of select="$weapon/@name"/>
	</td>
	<td colspan="15" rowspan="3" class="bab-class" onclick="showDialog('{$weapon/@name}','{$weapon/@info}','info');">
		<xsl:choose>
			<xsl:when test="Attacks/@total_defense = 'true'">
				<s><xsl:value-of select="$weapon/@attacks"/></s>
			</xsl:when>
			<xsl:otherwise>
				<xsl:value-of select="$weapon/@attacks"/>
			</xsl:otherwise>
		</xsl:choose>
	</td>
	<td colspan="13" rowspan="3" class="data-value-class">
			<xsl:value-of select="$weapon/@damage"/>
	</td>
	<td colspan="9" rowspan="3" class="data-value-class">
		<xsl:value-of select="$weapon/@critical"/>
	</td>
</xsl:template>

<xsl:template name="weapon2">
	<xsl:param name="weapon"/>
	<td class="L">&nbsp;</td>
	<td colspan="5" rowspan="3" class="data-value-class">
		<xsl:if test="not($weapon/@range)">0</xsl:if>
		<xsl:value-of select="$weapon/@range"/>
		ft
	</td>
	<td colspan="5" rowspan="3" class="data-value-class">
		<xsl:if test="not($weapon/@weight)">0</xsl:if>
		<xsl:value-of select="$weapon/@weight"/>
		lb
	</td>
	<td colspan="9" rowspan="3" class="data-value-class">
		<xsl:value-of select="$weapon/@type"/>
	</td>
	<td colspan="6" rowspan="3" class="data-value-class">
		<xsl:value-of select="$weapon/@size"/>
	</td>
	<td colspan="29" rowspan="3" class="data-value-class">
		<xsl:value-of select="$weapon/@properties"/>
	</td>
</xsl:template>

<xsl:template name="ability">
	<xsl:param name="ability"/>
	<td/>
	<td colspan="4" rowspan="3" class="data-value-important-class" onclick="showDialog('{$ability/@type}','{$ability/@info}','info');">
		<xsl:value-of select="$ability/@total"/>
	</td>
	<td/>
	<td colspan="4" rowspan="3" class="data-value-important-class">
		<xsl:value-of select="$ability/@modifier"/>
	</td>
	<td>&nbsp;</td>
	<td colspan="4" rowspan="3" class="temp-value-class">
		<xsl:value-of select="$ability/@temp"/>
	</td>
	<td/>
	<td colspan="4" rowspan="3" class="temp-value-class">
		<xsl:value-of select="$ability/@temp-modifier"/>
	</td>
	<td/>
	<td/>
</xsl:template>

<xsl:template name="ability-mod">
	<xsl:param name="ability"/>
	<xsl:choose>
		<xsl:when test="$ability/@temp-modifier">
			<xsl:value-of select="$ability/@temp-modifier"/>
		</xsl:when><xsl:otherwise>
			<xsl:value-of select="$ability/@modifier"/>
		</xsl:otherwise>
	</xsl:choose>
</xsl:template>

<xsl:template name="save">
	<xsl:param name="save"/>
	<xsl:param name="ability"/>
					<td/>
					<td colspan="4" rowspan="3" class="data-value-important-class" onclick="showDialog('{$save/@type}','{$save/@info}','info');">
						<xsl:value-of select="$save/@total"/>
					</td>
					<td rowspan="3" class="equation-class">=</td>
					<td colspan="4" rowspan="3" class="data-value-class">
						<xsl:value-of select="$save/@base"/>
					</td>
					<td rowspan="3" class="equation-class">+</td>
					<td colspan="4" rowspan="3" class="data-value-class">
						<xsl:call-template name="ability-mod">
							<xsl:with-param name="ability" select="$ability"/>
						</xsl:call-template>
					</td>
					<td rowspan="3" class="equation-class">+</td>
					<td colspan="4" rowspan="3" class="data-value-class">
						<xsl:value-of select="$save/@mods"/>
					</td>
					<td rowspan="3" class="equation-class">+</td>
					<td colspan="4" rowspan="3" class="temp-value-class">
						<xsl:value-of select="$save/@misc"/>
					</td>
</xsl:template>

<xsl:template name="skill">
	<xsl:param name="skill"/>
					<td/>
					<td/>
					<td colspan="2" rowspan="2" class="checkbox-class">
						<xsl:choose>
							<xsl:when test="$skill/@cross-class">
								&#x2612;
							</xsl:when>
							<xsl:when test="$skill/@type">
		            &#x2b1c;
							</xsl:when>
							<xsl:otherwise>
							</xsl:otherwise>
						</xsl:choose>
					</td>
					<td colspan="16" rowspan="2" class="skills-name-class">
						<xsl:value-of select="$skill/@type"/>
						<xsl:if test="$skill/@untrained = 'true'">
							<span style="font-size:1.05em;"><sup>1</sup></span>
						</xsl:if>
					</td>
					<td colspan="4" rowspan="2" class="skills-ability-class">
						<xsl:value-of select="$skill/@ability"/>
					</td>
					<td rowspan="2"/>
					<td colspan="4" rowspan="2" class="skills-total-class B" onclick="showDialog('{$skill/@type}','{$skill/@info}','info');">
						<xsl:value-of select="$skill/@total"/>
					</td>
					<td rowspan="2" class="equation-class">=</td>
					<td colspan="4" rowspan="2" class="skills-data-class">
						<xsl:value-of select="$skill/@ability-modifier"/>
					</td>
					<td rowspan="2" class="equation-class">+</td>
					<td colspan="4" rowspan="2" class="skills-data-class">
						<xsl:value-of select="$skill/@ranks"/>
					</td>
					<td rowspan="2" class="equation-class">+</td>
					<xsl:choose>
						<xsl:when test="$skill/@misc != 0">
							<td colspan="4" rowspan="2" class="skills-data-class">
								<xsl:value-of select="$skill/@misc"/>
 							</td>
						</xsl:when><xsl:otherwise>
							<td colspan="4" rowspan="2" class="skills-data-class" style="color:white;">
			          0
							</td>
						</xsl:otherwise>
					</xsl:choose>
					<td class="R">&nbsp;</td>
					<td/>
</xsl:template>

<xsl:template match="Character">
		<div align="center">
			<table border="0" cellpadding="0" cellspacing="0" style="border-collapse: collapse;table-layout:fixed;width:114.75em;font-size:100%;">
				<col style="width:1.1666em" span="102"/>
				<tr style="height:2.5em">
					<td colspan="102"/>
				</tr>
				<tr style="height:1.0625em">
					<td/>
					<td class="T L R" colspan="100">&nbsp;</td>
					<td/>
				</tr>
				<tr style="height:1.0625em">
					<td/>
					<td class="L">&nbsp;</td>
					<td colspan="31" rowspan="2" class="info-value-important-class">
						<xsl:value-of select="@name"/>
					</td>
					<td/>
					<td colspan="22" rowspan="2" class="info-value-important-class">
						<xsl:value-of select="@player"/>
					</td>
					<td/>
					<td colspan="15" rowspan="2" class="info-value-class">
						<xsl:value-of select="@region"/>
					</td>
					<td/>
					<td colspan="27" rowspan="8" style="vertical-align:top;">&nbsp;
						<span style="position:absolute;z-index:1;width:37.8em; height:10.65em">
							<img width="504" height="142" style="width:37.8em; height:10.65em" src="logo2.png" alt="DandDLogo" />
						</span>
					</td>
					<td class="R">&nbsp;</td>
					<td/>
				</tr>
				<tr style="height:1.0625em">
					<td/>
					<td class="L">&nbsp;</td>
					<td/>
					<td/>
					<td/>
					<td class="R">&nbsp;</td>
					<td/>
				</tr>
				<tr style="height:1.0625em">
					<td/>
					<td class="L">&nbsp;</td>
					<td colspan="31">CHARACTER NAME</td>
					<td/>
					<td colspan="22">PLAYER</td>
					<td/>
					<td colspan="15">REGION</td>
					<td/>
					<td class="R">&nbsp;</td>
					<td/>
				</tr>
				<tr style="height:1.0625em">
					<td/>
					<td class="L">&nbsp;</td>
					<td colspan="15" rowspan="2" class="info-value-small-class">
						<xsl:value-of select="Level/@class"/>
					</td>
					<td/>
					<td colspan="15" rowspan="2" class="info-value-class">
						<xsl:value-of select="@race"/>
					</td>
					<td/>
					<td colspan="6" rowspan="2" class="info-value-class">
						<xsl:value-of select="@gender"/>
					</td>
					<td/>
					<td colspan="15" rowspan="2" class="info-value-class">
						<xsl:value-of select="@alignment"/>
					</td>
					<td/>
					<td colspan="15" rowspan="2" class="info-value-class">
						<xsl:value-of select="@deity"/>
					</td>
					<td/>
					<td class="R">&nbsp;</td>
					<td/>
				</tr>
				<tr style="height:1.0625em">
					<td/>
					<td class="L">&nbsp;</td>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td class="R">&nbsp;</td>
					<td/>
				</tr>
				<tr style="height:1.0625em">
					<td/>
					<td class="L">&nbsp;</td>
					<td colspan="15">CLASS</td>
					<td/>
					<td colspan="15">RACE</td>
					<td/>
					<td colspan="6">GENDER</td>
					<td/>
					<td colspan="15">ALIGNMENT</td>
					<td/>
					<td colspan="15">DEITY</td>
					<td/>
					<td class="R">&nbsp;</td>
					<td/>
				</tr>
				<tr style="height:1.0625em">
					<td/>
					<td class="L">&nbsp;</td>
					<td colspan="7" rowspan="2" class="info-value-small-class">
						<xsl:value-of select="Level/@level"/>
					</td>
					<td/>
					<td colspan="7" rowspan="2" class="info-value-small-class">
						<xsl:value-of select="@size"/>
					</td>
					<td/>
					<td colspan="15" rowspan="2" class="info-value-class">
						<xsl:value-of select="@type"/>
					</td>
					<td/>
					<td colspan="6" rowspan="2" class="info-value-class">
						<xsl:value-of select="@age"/>
					</td>
					<td/>
					<td colspan="7" rowspan="2" class="info-value-class">
						<xsl:value-of select="@height"/>
					</td>
					<td/>
					<td colspan="7" rowspan="2" class="info-value-class">
						<xsl:value-of select="@weight"/>
					</td>
					<td/>
					<td colspan="7" rowspan="2" class="info-value-class">
						<xsl:value-of select="@eye-colour"/>
					</td>
					<td/>
					<td colspan="7" rowspan="2" class="info-value-class">
						<xsl:value-of select="@hair-colour"/>
					</td>
					<td/>
					<td class="R">&nbsp;</td>
					<td/>
				</tr>
				<tr style="height:1.0625em">
					<td/>
					<td class="L">&nbsp;</td>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td class="R">&nbsp;</td>
					<td/>
				</tr>
				<tr style="height:1.0625em">
					<td/>
					<td class="L">&nbsp;</td>
					<td colspan="7">LEVEL</td>
					<td/>
					<td colspan="7">SIZE</td>
					<td/>
					<td colspan="15">TYPE</td>
					<td/>
					<td colspan="6">AGE</td>
					<td/>
					<td colspan="7">HEIGHT</td>
					<td/>
					<td colspan="7">WEIGHT</td>
					<td/>
					<td colspan="7">EYES</td>
					<td/>
					<td colspan="7">HAIR</td>
					<td/>
					<td colspan="27" rowspan="2" class="sheet-title-class">CHARACTER RECORD SHEET</td>
					<td class="R">&nbsp;</td>
					<td/>
				</tr>
				<tr style="height:1.0625em">
					<td/>
					<td class="L" colspan="72">&nbsp;</td>
					<td class="R">&nbsp;</td>
					<td/>
				</tr>
				<tr style="height:1.0625em">
					<td/>
					<td class="L">&nbsp;</td>
					<td colspan="6" rowspan="2" class="data-box-desc-class" style="vertical-align:bottom;">ABILITY NAME</td>
					<td/>
					<td colspan="4" rowspan="2" class="data-box-desc-class">ABILITY SCORE</td>
					<td/>
					<td colspan="4" rowspan="2" class="data-box-desc-class">ABILITY MODIFIER</td>
					<td/>
					<td colspan="4" rowspan="2" class="data-box-desc-class">TEMP. SCORE</td>
					<td/>
					<td colspan="4" rowspan="2" class="data-box-desc-class">TEMP. MODIFIER</td>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td colspan="4" rowspan="2" class="data-box-desc-total-class" style="vertical-align:middle;">TOTAL</td>
					<td/>
					<td colspan="15" rowspan="2" class="data-box-desc-class">WOUNDS</td>
					<td/>
					<td colspan="15" rowspan="2" class="data-box-desc-class">NONLETHAL DAMAGE</td>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td colspan="22" rowspan="2" class="speed-heading-class">SPEED</td>
					<td class="R">&nbsp;</td>
					<td/>
				</tr>
				<tr style="height:1.0625em">
					<td/>
					<td class="L">&nbsp;</td>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td class="R">&nbsp;</td>
					<td/>
				</tr>
				<tr style="height:1.0625em">
					<td/>
					<td class="L">&nbsp;</td>
					<td colspan="6" rowspan="2" class="title-class">STR</td>
					<xsl:call-template name="ability">
						<xsl:with-param name="ability" select="AbilityScores/AbilityScore[@type='Strength']"/>
					</xsl:call-template>
					<td colspan="6" rowspan="2" class="title-class">HP</td>
					<td/>
					<td colspan="4" rowspan="3" class="data-value-important-class" onclick="showDialog('Hitpoints','{HitPoints/@info}','info');">
						<xsl:value-of select="HitPoints/@maximum"/>
					</td>
					<td/>
					<td colspan="15" rowspan="3" class="data-value-class" style="text-align:left;" onclick="showDialog('Hitpoints','{HitPoints/@info}','info');">
						<xsl:value-of select="HitPoints/@wounds"/>
					</td>
					<td/>
					<td colspan="15" rowspan="3" class="data-value-class" style="text-align:left;" onclick="showDialog('Hitpoints','{HitPoints/@info}','info');">
						<xsl:value-of select="HitPoints/@non-lethal"/>
					</td>
					<td/>
					<td/>
					<td/>
					<td/>
					<td class="R">&nbsp;</td>
					<td colspan="22" rowspan="3" class="data-value-class">
						<xsl:value-of select="@speed"/>
					</td>
					<td class="R">&nbsp;</td>
					<td/>
				</tr>
				<tr style="height:1.0625em">
					<td/>
					<td class="L">&nbsp;</td>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td class="R">&nbsp;</td>
					<td/>
				</tr>
				<tr style="height:1.0625em">
					<td/>
					<td class="L">&nbsp;</td>
					<td colspan="6" class="subtitle-class">STRENGTH</td>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td colspan="6" class="subtitle-class">HIT POINTS</td>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td class="R">&nbsp;</td>
					<td/>
				</tr>
				<tr style="height:1.0625em">
					<td/>
					<td class="L R" colspan="100">&nbsp;</td>
					<td/>
				</tr>
				<tr style="height:1.0625em">
					<td/>
					<td class="L">&nbsp;</td>
					<td colspan="6" rowspan="2" class="title-class">DEX</td>
					<xsl:call-template name="ability">
						<xsl:with-param name="ability" select="AbilityScores/AbilityScore[@type='Dexterity']"/>
					</xsl:call-template>
					<td colspan="6" rowspan="2" class="title-class">AC</td>
					<td/>
					<td colspan="4" rowspan="3" class="data-value-important-class">
						<xsl:value-of select="AC/@total"/>
					</td>
					<td rowspan="3" class="equation-class">=</td>
					<td colspan="3" rowspan="3" class="ac-base-value-class">10</td>
					<td rowspan="3" class="equation-class">+</td>
					<td colspan="4" rowspan="3" class="data-value-class">
						<xsl:value-of select="AC/ACComponent[@type='Armor']/@value"/>
						<xsl:if test="not(AC/ACComponent[@type='Armor']/@value)">+0</xsl:if>
					</td>
					<td rowspan="3" class="equation-class">+</td>
					<td colspan="4" rowspan="3" class="data-value-class">
						<xsl:value-of select="AC/ACComponent[@type='Shield']/@value"/>
						<xsl:if test="not(AC/ACComponent[@type='Shield']/@value)">+0</xsl:if>
					</td>
					<td rowspan="3" class="equation-class">+</td>
					<td colspan="4" rowspan="3" class="data-value-class">
						<xsl:value-of select="AC/ACComponent[@type='Dexterity']/@value"/>
						<xsl:if test="not(AC/ACComponent[@type='Dexterity']/@value)">+0</xsl:if>
					</td>
					<td rowspan="3" class="equation-class">+</td>
					<td colspan="4" rowspan="3" class="data-value-class">
						<xsl:value-of select="AC/ACComponent[@type='Size']/@value"/>
						<xsl:if test="not(AC/ACComponent[@type='Size']/@value)">+0</xsl:if>
					</td>
					<td rowspan="3" class="equation-class">+</td>
					<td colspan="4" rowspan="3" class="data-value-class">
						<xsl:value-of select="AC/ACComponent[@type='Natural Armor']/@value"/>
						<xsl:if test="not(AC/ACComponent[@type='Natural Armor']/@value)">+0</xsl:if>
					</td>
					<td rowspan="3" class="equation-class">+</td>
					<td colspan="4" rowspan="3" class="data-value-class">
						<xsl:value-of select="AC/ACComponent[@type='Deflection']/@value"/>
						<xsl:if test="not(AC/ACComponent[@type='Deflection']/@value)">+0</xsl:if>
					</td>
					<td rowspan="3" class="equation-class">+</td>
					<td colspan="4" rowspan="3" class="data-value-class">
						<xsl:value-of select="AC/ACComponent[@type='Misc']/@value"/>
						<xsl:if test="not(AC/ACComponent[@type='Misc']/@value)">+0</xsl:if>
					</td>
					<td/>
					<td/>
					<td colspan="4" rowspan="3" class="data-value-class">
						<xsl:value-of select="AC/@armor-check-penalty"/>
					</td>
					<td/>
					<td colspan="13" rowspan="3" class="data-value-class">
						<xsl:value-of select="@damage-reduction"/>
					</td>
					<td class="R">&nbsp;</td>
					<td/>
				</tr>
				<tr style="height:1.0625em">
					<td/>
					<td class="L">&nbsp;</td>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td class="R">&nbsp;</td>
					<td/>
				</tr>
				<tr style="height:1.0625em">
					<td/>
					<td class="L">&nbsp;</td>
					<td colspan="6" class="subtitle-class">
            DEXTERITY
					</td>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td colspan="6" class="subtitle-class">
            ARMOR CLASS
					</td>
					<td/>
					<td/>
					<td/>
					<td/>
					<td class="R">&nbsp;</td>
					<td/>
				</tr>
				<tr style="height:1.0625em">
					<td/>
					<td class="L">&nbsp;</td>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td colspan="4" rowspan="2" class="data-box-desc-total-over-class">TOTAL</td>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td colspan="4" rowspan="2" class="data-box-desc-under-class">ARMOR BONUS</td>
					<td/>
					<td colspan="4" rowspan="2" class="data-box-desc-under-class">SHIELD BONUS</td>
					<td/>
					<td colspan="4" rowspan="2" class="data-box-desc-under-class">DEX MODIFIER</td>
					<td/>
					<td colspan="4" rowspan="2" class="data-box-desc-under-class">SIZE MODIFIER</td>
					<td/>
					<td colspan="4" rowspan="2" class="data-box-desc-under-class">NATURAL ARMOR</td>
					<td/>
					<td colspan="4" rowspan="2" class="data-box-desc-under-class">DEFLECT MOD</td>
					<td/>
					<td colspan="4" rowspan="2" class="data-box-desc-under-class">MISC MOD</td>
					<td/>
					<td/>
					<td colspan="4" rowspan="3" class="data-box-desc-under-class">ARMOR CHECK PENALTY</td>
					<td/>
					<td colspan="13" rowspan="2" class="data-box-desc-class">DAMAGE REDUCTION</td>
					<td class="R">&nbsp;</td>
					<td/>
				</tr>
				<tr style="height:1.0625em">
					<td/>
					<td class="L">&nbsp;</td>
					<td colspan="6" rowspan="2" class="title-class">CON</td>
					<xsl:call-template name="ability">
						<xsl:with-param name="ability" select="AbilityScores/AbilityScore[@type='Constitution']"/>
					</xsl:call-template>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td class="R">&nbsp;</td>
					<td/>
				</tr>
				<tr style="height:1.0625em">
					<td/>
					<td class="L">&nbsp;</td>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td class="R">&nbsp;</td>
					<td/>
				</tr>
				<tr style="height:1.0625em">
					<td/>
					<td class="L">&nbsp;</td>
					<td colspan="6" class="subtitle-class">
            CONSTITUTION
					</td>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td class="R">&nbsp;</td>
					<td/>
				</tr>
				<tr style="height:1.0625em">
					<td/>
					<td class="L">&nbsp;</td>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td colspan="6" rowspan="2" class="title-class">TOUCH</td>
					<td/>
					<td colspan="4" rowspan="3" class="data-value-important-class">
						<xsl:value-of select="AC/@touch"/>
					</td>
					<td/>
					<td colspan="10" rowspan="2" class="title-class">FLAT-FOOTED</td>
					<td/>
					<td colspan="4" rowspan="3" class="data-value-important-class">
						<xsl:value-of select="AC/@flat-footed"/>
					</td>
					<td/>
					<td colspan="2" rowspan="4" class="skills-cross-class-heading">
						<div style="transform: translateY(-2.1em) rotate(-90deg); -webkit-transform: translateY(-2.1em) rotate(-90deg);">
							CROSS-CLASS
						</div>
					</td>
					<td colspan="27" rowspan="2" class="skills-title-class">SKILLS</td>
					<td colspan="8" rowspan="2" class="skills-max-ranks-heading">MAX RANKS</td>
					<td colspan="5" rowspan="2" class="skills-max-ranks-value">
            9 / 4.5
					</td>
					<td class="R">&nbsp;</td>
					<td/>
				</tr>
				<tr style="height:1.0625em">
					<td/>
					<td class="L">&nbsp;</td>
					<td colspan="6" rowspan="2" class="title-class">INT</td>
					<xsl:call-template name="ability">
						<xsl:with-param name="ability" select="AbilityScores/AbilityScore[@type='Intelligence']"/>
					</xsl:call-template>
					<td/>
					<td/>
					<td/>
					<td/>
					<td class="R">&nbsp;</td>
					<td/>
				</tr>
				<tr style="height:1.0625em">
					<td/>
					<td class="L">&nbsp;</td>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td colspan="6" class="subtitle-class">ARMOR CLASS</td>
					<td/>
					<td/>
					<td colspan="10" class="subtitle-class">ARMOR CLASS</td>
					<td/>
					<td/>
					<td colspan="16" rowspan="2" class="xl202">SKILL NAME</td>
					<td colspan="4" rowspan="2" class="xl196">KEY ABILITY</td>
					<td/>
					<td colspan="4" rowspan="2" class="xl201">SKILL MODIFIER</td>
					<td/>
					<td colspan="4" rowspan="2" class="xl196">ABILITY MODIFIER</td>
					<td/>
					<td colspan="4" rowspan="2" class="xl202">RANKS</td>
					<td/>
					<td colspan="4" rowspan="2" class="xl196">MISC. MODIFIER</td>
					<td class="R">&nbsp;</td>
					<td/>
				</tr>
				<tr style="height:1.0625em">
					<td/>
					<td class="L">&nbsp;</td>
					<td colspan="6" class="subtitle-class">INTELLIGENCE</td>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td class="R">&nbsp;</td>
					<td/>
				</tr>
				<tr style="height:1.0625em">
					<td/>
          <td class="L" colspan="55">&nbsp;</td>
					<td/>
					<td/>
					<td colspan="42"/>
					<td class="R">&nbsp;</td>
					<td/>
				</tr>
				<tr style="height:1.0625em">
					<td/>
					<td class="L">&nbsp;</td>
					<td colspan="6" rowspan="2" class="title-class">WIS</td>
					<xsl:call-template name="ability">
						<xsl:with-param name="ability" select="AbilityScores/AbilityScore[@type='Wisdom']"/>
					</xsl:call-template>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<xsl:call-template name="skill">
						<xsl:with-param name="skill" select="Skills/Skill[1]"/>
					</xsl:call-template>
				</tr>
				<tr style="height:1.0625em">
					<td/>
					<td class="L">&nbsp;</td>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td class="R">&nbsp;</td>
					<td/>
				</tr>
				<tr style="height:1.0625em">
					<td/>
					<td class="L">&nbsp;</td>
					<td colspan="6" class="subtitle-class">WISDOM</td>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td colspan="11" rowspan="2" class="title-class">INITIATIVE</td>
					<td/>
					<td colspan="4" rowspan="3" class="data-value-important-class" onclick="showDialog('Initiative','{Initiative/@info}','info');">
						<xsl:value-of select="Initiative/@total"/>
					</td>
					<td rowspan="3" class="equation-class">=</td>
					<td colspan="4" rowspan="3" class="data-value-class">
						<xsl:call-template name="ability-mod">
							<xsl:with-param name="ability" select="AbilityScores/AbilityScore[@type='Dexterity']"/>
						</xsl:call-template>
					</td>
					<td rowspan="3" class="equation-class">+</td>
					<td colspan="4" rowspan="3" class="data-value-class">
						<xsl:value-of select="Initiative/@misc"/>
						<xsl:if test="not(Initiative/@misc)">+0</xsl:if>
					</td>
					<xsl:call-template name="skill">
						<xsl:with-param name="skill" select="Skills/Skill[2]"/>
					</xsl:call-template>
				</tr>
				<tr style="height:1.0625em">
					<td/>
					<td class="L">&nbsp;</td>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td class="R">&nbsp;</td>
					<td/>
				</tr>
				<tr style="height:1.0625em">
					<td/>
					<td class="L">&nbsp;</td>
					<td colspan="6" rowspan="2" class="title-class">CHA</td>
					<xsl:call-template name="ability">
						<xsl:with-param name="ability" select="AbilityScores/AbilityScore[@type='Charisma']"/>
					</xsl:call-template>
					<td colspan="11" class="subtitle-class">MODIFIER</td>
					<td/>
					<xsl:call-template name="skill">
						<xsl:with-param name="skill" select="Skills/Skill[3]"/>
					</xsl:call-template>
				</tr>
				<tr style="height:1.0625em">
					<td/>
					<td class="L">&nbsp;</td>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td colspan="4" rowspan="2" class="data-box-desc-total-over-class">TOTAL</td>
					<td/>
					<td colspan="4" rowspan="2" class="data-box-desc-under-class">DEX MODIFIER</td>
					<td/>
					<td colspan="4" rowspan="2" class="data-box-desc-under-class">MISC. MODIFIER</td>
					<td/>
					<td/>
					<td class="R">&nbsp;</td>
					<td/>
				</tr>
				<tr style="height:1.0625em">
					<td/>
					<td class="L">&nbsp;</td>
					<td colspan="6" class="subtitle-class">CHARISMA</td>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<xsl:call-template name="skill">
						<xsl:with-param name="skill" select="Skills/Skill[4]"/>
					</xsl:call-template>
				</tr>
				<tr style="height:1.0625em">
					<td/>
          <td class="L" colspan="55">&nbsp;</td>
					<td/>
					<td/>
					<td class="R">&nbsp;</td>
					<td/>
				</tr>
				<tr style="height:1.0625em">
					<td/>
					<td class="L">&nbsp;</td>
					<td colspan="11" rowspan="2" class="data-box-desc-total-class">SAVING THROWS</td>
					<td/>
					<td colspan="4" rowspan="2" class="data-box-desc-total-class">TOTAL</td>
					<td/>
					<td colspan="4" rowspan="2" class="data-box-desc-class">BASE<br /> SAVE</td>
					<td/>
					<td colspan="4" rowspan="2" class="data-box-desc-class">ABILITY MODIFIER</td>
					<td/>
					<td colspan="4" rowspan="2" class="data-box-desc-class">MISC. &amp; MAGIC</td>
					<td/>
					<td colspan="4" rowspan="2" class="data-box-desc-class">TEMP MODIFIER</td>
					<td/>
					<td colspan="17" class="data-box-desc-class L T R">CONDITIONAL MODIFIERS</td>
					<xsl:call-template name="skill">
						<xsl:with-param name="skill" select="Skills/Skill[5]"/>
					</xsl:call-template>
				</tr>
				<tr style="height:1.0625em">
					<td/>
					<td class="L">&nbsp;</td>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td colspan="17" rowspan="12" class="L B R conditional-modifiers-class">
						<xsl:for-each select="Buffs/Buff/Modifier[@condition]">
							<xsl:value-of select="@description"/><br/>
						</xsl:for-each>
					</td>
					<td/>
					<td/>
					<td class="R">&nbsp;</td>
					<td/>
				</tr>
				<tr style="height:1.0625em">
					<td/>
					<td class="L">&nbsp;</td>
					<td colspan="11" rowspan="2" class="title-class">FORTITUDE</td>
					<xsl:call-template name="save">
						<xsl:with-param name="save" select="SavingThrows/Save[@type='Fortitude']"/>
						<xsl:with-param name="ability" select="AbilityScores/AbilityScore[@type='Constitution']"/>
					</xsl:call-template>
					<td rowspan="3"/>
					<xsl:call-template name="skill">
						<xsl:with-param name="skill" select="Skills/Skill[6]"/>
					</xsl:call-template>
				</tr>
				<tr style="height:1.0625em">
					<td/>
					<td class="L">&nbsp;</td>
					<td/>
					<td/>
					<td/>
					<td class="R">&nbsp;</td>
					<td/>
				</tr>
				<tr style="height:1.0625em">
					<td/>
					<td class="L">&nbsp;</td>
					<td colspan="11" class="subtitle-class">(CONSTITUTION)</td>
					<td/>
					<xsl:call-template name="skill">
						<xsl:with-param name="skill" select="Skills/Skill[7]"/>
					</xsl:call-template>
				</tr>
				<tr style="height:1.0625em">
					<td/>
					<td class="L">&nbsp;</td>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td class="R">&nbsp;</td>
					<td/>
				</tr>
				<tr style="height:1.0625em">
					<td/>
					<td class="L">&nbsp;</td>
					<td colspan="11" rowspan="2" class="title-class">REFLEX</td>
					<xsl:call-template name="save">
						<xsl:with-param name="save" select="SavingThrows/Save[@type='Reflex']"/>
						<xsl:with-param name="ability" select="AbilityScores/AbilityScore[@type='Dexterity']"/>
					</xsl:call-template>
					<td rowspan="3"/>
					<xsl:call-template name="skill">
						<xsl:with-param name="skill" select="Skills/Skill[8]"/>
					</xsl:call-template>
				</tr>
				<tr style="height:1.0625em">
					<td/>
					<td class="L">&nbsp;</td>
					<td/>
					<td/>
					<td/>
					<td class="R">&nbsp;</td>
					<td/>
				</tr>
				<tr style="height:1.0625em">
					<td/>
					<td class="L">&nbsp;</td>
					<td colspan="11" class="subtitle-class">(DEXTERITY)</td>
					<td/>
					<xsl:call-template name="skill">
						<xsl:with-param name="skill" select="Skills/Skill[9]"/>
					</xsl:call-template>
				</tr>
				<tr style="height:1.0625em">
					<td/>
					<td class="L">&nbsp;</td>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td class="R">&nbsp;</td>
					<td/>
				</tr>
				<tr style="height:1.0625em">
					<td/>
					<td class="L">&nbsp;</td>
					<td colspan="11" rowspan="2" class="title-class">WILL</td>
					<xsl:call-template name="save">
						<xsl:with-param name="save" select="SavingThrows/Save[@type='Will']"/>
						<xsl:with-param name="ability" select="AbilityScores/AbilityScore[@type='Wisdom']"/>
					</xsl:call-template>
					<td rowspan="3"/>
					<xsl:call-template name="skill">
						<xsl:with-param name="skill" select="Skills/Skill[10]"/>
					</xsl:call-template>
				</tr>
				<tr style="height:1.0625em">
					<td/>
					<td class="L">&nbsp;</td>
					<td/>
					<td/>
					<td/>
					<td class="R">&nbsp;</td>
					<td/>
				</tr>
				<tr style="height:1.0625em">
					<td/>
					<td class="L">&nbsp;</td>
					<td colspan="11" class="subtitle-class">(WISDOM)</td>
					<td/>
					<xsl:call-template name="skill">
						<xsl:with-param name="skill" select="Skills/Skill[11]"/>
					</xsl:call-template>
				</tr>
				<tr style="height:1.0625em">
					<td/>
          <td class="L" colspan="55">&nbsp;</td>
					<td/>
					<td/>
					<td class="R">&nbsp;</td>
					<td/>
				</tr>
				<tr style="height:1.0625em">
					<td/>
					<td class="L">&nbsp;</td>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td colspan="4" class="data-box-desc-class">TEMP.</td>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td colspan="12" rowspan="3" class="title-smaller-class">SPELL RESISTANCE</td>
					<td/>
					<td colspan="4" rowspan="3" class="data-value-important-class">
						<xsl:value-of select="@spell-resistance"/>
					</td>
					<xsl:call-template name="skill">
						<xsl:with-param name="skill" select="Skills/Skill[12]"/>
					</xsl:call-template>
				</tr>
				<tr style="height:1.0625em">
					<td/>
					<td class="L">&nbsp;</td>
					<td colspan="16" rowspan="3" class="title-class">BASE ATTACK BONUS</td>
					<td/>
					<td colspan="9" rowspan="3" class="bab-class">
						<xsl:value-of select="Attacks/@attacks"/>
					</td>
					<td/>
					<td colspan="4" rowspan="3" class="temp-value-class">&nbsp;</td>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td class="R">&nbsp;</td>
					<td/>
				</tr>
				<tr style="height:1.0625em">
					<td/>
					<td class="L">&nbsp;</td>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<xsl:call-template name="skill">
						<xsl:with-param name="skill" select="Skills/Skill[13]"/>
					</xsl:call-template>
				</tr>
				<tr style="height:1.0625em">
					<td/>
					<td class="L">&nbsp;</td>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td class="R">&nbsp;</td>
					<td/>
				</tr>
				<tr style="height:1.0625em">
					<td/>
					<td class="L">&nbsp;</td>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td colspan="12" rowspan="3" class="title-smaller-class">ARCANE SPELL FAILURE *</td>
					<td/>
					<td colspan="4" rowspan="3" class="data-value-class">
						<xsl:value-of select="@arcane-spell-failure"/>
					</td>
					<xsl:call-template name="skill">
						<xsl:with-param name="skill" select="Skills/Skill[14]"/>
					</xsl:call-template>
				</tr>
				<tr style="height:1.0625em">
					<td/>
					<td class="L">&nbsp;</td>
					<td colspan="9" rowspan="2" class="title-class">GRAPPLE</td>
					<td/>
					<td colspan="4" rowspan="3" class="data-value-important-class">
						<xsl:value-of select="Attacks/Attack[@type='Grapple']/@total"/>
					</td>
					<td rowspan="3" class="equation-class">=</td>
					<td colspan="4" rowspan="3" class="data-value-class">
						<xsl:value-of select="Attacks/@base"/>
					</td>
					<td rowspan="3" class="equation-class">+</td>
					<td colspan="4" rowspan="3" class="data-value-class">
						<xsl:call-template name="ability-mod">
							<xsl:with-param name="ability" select="AbilityScores/AbilityScore[@type='Strength']"/>
						</xsl:call-template>
					</td>
					<td rowspan="3" class="equation-class">+</td>
					<td colspan="4" rowspan="3" class="data-value-class">
						<xsl:value-of select="Attacks/@size-modifier"/>
					</td>
					<td rowspan="3" class="equation-class">+</td>
					<td colspan="4" rowspan="3" class="data-value-class">
						<xsl:value-of select="Attacks/Attack[@type='Grapple']/@misc"/>
					</td>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td class="R">&nbsp;</td>
					<td/>
				</tr>
				<tr style="height:1.0625em">
					<td/>
					<td class="L">&nbsp;</td>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<xsl:call-template name="skill">
						<xsl:with-param name="skill" select="Skills/Skill[15]"/>
					</xsl:call-template>
				</tr>
				<tr style="height:1.0625em">
					<td/>
					<td class="L">&nbsp;</td>
					<td colspan="9" class="subtitle-class">MODIFIER</td>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td class="R">&nbsp;</td>
					<td/>
				</tr>
				<tr style="height:1.0625em">
					<td/>
					<td class="L">&nbsp;</td>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td colspan="4" rowspan="2" class="data-box-desc-total-over-class">TOTAL</td>
					<td/>
					<td colspan="4" rowspan="2" class="data-box-desc-under-class">BASE ATTACK</td>
					<td/>
					<td colspan="4" rowspan="2" class="data-box-desc-under-class">STR MODIFIER</td>
					<td/>
					<td colspan="4" rowspan="2" class="data-box-desc-under-class">SIZE MODIFIER</td>
					<td/>
					<td colspan="4" rowspan="2" class="data-box-desc-under-class">MISC. MODIFIER</td>
					<td/>
					<td/>
					<td/>
					<td colspan="12" rowspan="3" class="title-smaller-class">ACTION POINTS</td>
					<td/>
					<td colspan="4" rowspan="3" class="data-value-important-class">
						<xsl:value-of select="@action-points"/>
					</td>
					<xsl:call-template name="skill">
						<xsl:with-param name="skill" select="Skills/Skill[16]"/>
					</xsl:call-template>
				</tr>
				<tr style="height:1.0625em">
					<td/>
					<td class="L">&nbsp;</td>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td class="R">&nbsp;</td>
					<td/>
				</tr>
				<tr style="height:1.0625em">
					<td/>
					<td class="L">&nbsp;</td>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<xsl:call-template name="skill">
						<xsl:with-param name="skill" select="Skills/Skill[17]"/>
					</xsl:call-template>
				</tr>
				<tr style="height:1.0625em">
					<td/>
          <td class="L" colspan="55">&nbsp;</td>
					<td/>
					<td/>
					<td class="R">&nbsp;</td>
					<td/>
				</tr>
				<tr style="height:1.0625em">
					<td/>
					<td class="L">&nbsp;</td>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td colspan="12" rowspan="2" class="data-box-desc-total-class">TOTAL</td>
					<td/>
					<td colspan="9" rowspan="2" class="data-box-desc-class" style="vertical-align:bottom">BASE ATTACK BONUS</td>
					<td/>
					<td colspan="4" rowspan="2" class="data-box-desc-class">STR MODIFIER</td>
					<td/>
					<td colspan="4" rowspan="2" class="data-box-desc-class">SIZE MODIFIER</td>
					<td/>
					<td colspan="4" rowspan="2" class="data-box-desc-class">MISC. MODIFIER</td>
					<td/>
					<td colspan="4" rowspan="2" class="data-box-desc-class">TEMP. MODIFIER</td>
					<xsl:call-template name="skill">
						<xsl:with-param name="skill" select="Skills/Skill[18]"/>
					</xsl:call-template>
				</tr>
				<tr style="height:1.0625em">
					<td/>
					<td class="L">&nbsp;</td>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td class="R">&nbsp;</td>
					<td/>
				</tr>
				<tr style="height:1.0625em">
					<td/>
					<td class="L">&nbsp;</td>
					<td colspan="11" rowspan="2" class="title-class">MELEE</td>
					<td/>
					<td colspan="12" rowspan="3" class="total-attack-bonus-class" onclick="showDialog('Melee Attack','{Attacks/Attack[@type='Melee']/@info}','info');">
						<xsl:choose>
							<xsl:when test="Attacks/@total_defense = 'true'">
								<s><xsl:value-of select="Attacks/Attack[@type='Melee']/@attacks"/></s>
							</xsl:when>
							<xsl:otherwise>
								<xsl:value-of select="Attacks/Attack[@type='Melee']/@attacks"/>
							</xsl:otherwise>
						</xsl:choose>
					</td>
					<td rowspan="3" class="equation-class">=</td>
					<td colspan="9" rowspan="3" class="bab-class">
						<xsl:value-of select="Attacks/@base"/>
					</td>
					<td rowspan="3" class="equation-class">+</td>
					<td colspan="4" rowspan="3" class="data-value-class">
						<xsl:call-template name="ability-mod">
							<xsl:with-param name="ability" select="AbilityScores/AbilityScore[@type='Strength']"/>
						</xsl:call-template>
					</td>
					<td rowspan="3" class="equation-class">+</td>
					<td colspan="4" rowspan="3" class="data-value-class">
						<xsl:value-of select="Attacks/@size-modifier"/>
					</td>
					<td rowspan="3" class="equation-class">+</td>
					<td colspan="4" rowspan="3" class="data-value-class">
						<xsl:value-of select="Attacks/Attack[@type='Melee']/@misc"/>
					</td>
					<td rowspan="3" class="equation-class">+</td>
					<td colspan="4" rowspan="3" class="temp-value-class">
						<xsl:value-of select="Attacks/Attack[@type='Melee']/@temp-modifier"/>
					</td>
					<xsl:call-template name="skill">
						<xsl:with-param name="skill" select="Skills/Skill[19]"/>
					</xsl:call-template>
				</tr>
				<tr style="height:1.0625em">
					<td/>
					<td class="L">&nbsp;</td>
					<td/>
					<td/>
					<td/>
					<td class="R">&nbsp;</td>
					<td/>
				</tr>
				<tr style="height:1.0625em">
					<td/>
					<td class="L">&nbsp;</td>
					<td colspan="11" class="subtitle-class">
            ATTACK BONUS
					</td>
					<td/>
					<xsl:call-template name="skill">
						<xsl:with-param name="skill" select="Skills/Skill[20]"/>
					</xsl:call-template>
				</tr>
				<tr style="height:1.0625em">
					<td/>
          <td class="L" colspan="55">&nbsp;</td>
					<td/>
					<td/>
					<td class="R">&nbsp;</td>
					<td/>
				</tr>
				<tr style="height:1.0625em">
					<td/>
					<td class="L">&nbsp;</td>
					<td colspan="11" rowspan="2" class="title-class">RANGED</td>
					<td/>
					<td colspan="12" rowspan="3" class="total-attack-bonus-class" onclick="showDialog('Ranged Attack','{Attacks/Attack[@type='Ranged']/@info}','info');">
						<xsl:choose>
							<xsl:when test="Attacks/@total_defense = 'true'">
								<s><xsl:value-of select="Attacks/Attack[@type='Ranged']/@attacks"/></s>
							</xsl:when>
							<xsl:otherwise>
								<xsl:value-of select="Attacks/Attack[@type='Ranged']/@attacks"/>
							</xsl:otherwise>
						</xsl:choose>
					</td>
					<td rowspan="3" class="equation-class">=</td>
					<td colspan="9" rowspan="3" class="bab-class">
						<xsl:value-of select="Attacks/@base"/>
					</td>
					<td rowspan="3" class="equation-class">+</td>
					<td colspan="4" rowspan="3" class="data-value-class">
						<xsl:call-template name="ability-mod">
							<xsl:with-param name="ability" select="AbilityScores/AbilityScore[@type='Dexterity']"/>
						</xsl:call-template>
					</td>
					<td rowspan="3" class="equation-class">+</td>
					<td colspan="4" rowspan="3" class="data-value-class">
						<xsl:value-of select="Attacks/@size-modifier"/>
					</td>
					<td rowspan="3" class="equation-class">+</td>
					<td colspan="4" rowspan="3" class="data-value-class">
						<xsl:value-of select="Attacks/Attack[@type='Ranged']/@misc"/>
					</td>
					<td rowspan="3" class="equation-class">+</td>
					<td colspan="4" rowspan="3" class="temp-value-class">
						<xsl:value-of select="Attacks/Attack[@type='Ranged']/@temp-modifier"/>
					</td>
					<xsl:call-template name="skill">
						<xsl:with-param name="skill" select="Skills/Skill[21]"/>
					</xsl:call-template>
				</tr>
				<tr style="height:1.0625em">
					<td/>
					<td class="L">&nbsp;</td>
					<td/>
					<td/>
					<td/>
					<td class="R">&nbsp;</td>
					<td/>
				</tr>
				<tr style="height:1.0625em">
					<td/>
					<td class="L">&nbsp;</td>
					<td colspan="11" class="subtitle-class">ATTACK BONUS</td>
					<td/>
					<xsl:call-template name="skill">
						<xsl:with-param name="skill" select="Skills/Skill[22]"/>
					</xsl:call-template>
				</tr>
				<tr style="height:1.0625em">
					<td/>
					<td class="L">&nbsp;</td>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td colspan="12" rowspan="2" class="data-box-desc-total-over-class">TOTAL</td>
					<td/>
					<td colspan="9" rowspan="2" class="data-box-desc-under-class">BASE ATTACK BONUS</td>
					<td/>
					<td colspan="4" rowspan="2" class="data-box-desc-under-class">DEX MODIFIER</td>
					<td/>
					<td colspan="4" rowspan="2" class="data-box-desc-under-class">SIZE MODIFIER</td>
					<td/>
					<td colspan="4" rowspan="2" class="data-box-desc-under-class">MISC. MODIFIER</td>
					<td/>
					<td colspan="4" rowspan="2" class="data-box-desc-under-class">TEMP. MODIFIER</td>
					<td/>
					<td/>
					<td class="R">&nbsp;</td>
					<td/>
				</tr>
				<tr style="height:1.0625em">
					<td/>
					<td class="L">&nbsp;</td>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<xsl:call-template name="skill">
						<xsl:with-param name="skill" select="Skills/Skill[23]"/>
					</xsl:call-template>
				</tr>
				<tr style="height:1.0625em">
					<td/>
					<td class="L" colspan="55">&nbsp;</td>
					<td/>
					<td/>
					<td class="R">&nbsp;</td>
					<td/>
				</tr>
				<tr style="height:1.0625em">
					<td/>
					<td class="L" colspan="55">&nbsp;</td>
					<xsl:call-template name="skill">
						<xsl:with-param name="skill" select="Skills/Skill[24]"/>
					</xsl:call-template>
				</tr>
				<tr style="height:1.0625em">
					<td/>
					<td class="L" colspan="55">&nbsp;</td>
					<td/>
					<td/>
					<td class="R">&nbsp;</td>
					<td/>
				</tr>
				<tr style="height:1.0625em">
					<td/>
					<td class="L">&nbsp;</td>
					<td colspan="17" rowspan="2" class="title-class">WEAPON</td>
					<td colspan="37"/>
					<xsl:call-template name="skill">
						<xsl:with-param name="skill" select="Skills/Skill[25]"/>
					</xsl:call-template>
				</tr>
				<tr style="height:1.0625em">
					<td/>
					<td class="L">&nbsp;</td>
					<td colspan="15" class="weapon-subtitle-class" style="border-left:none">TOTAL ATTACK BONUS</td>
					<td colspan="13" class="weapon-subtitle-class">DAMAGE</td>
					<td colspan="9" class="weapon-subtitle-class">CRITICAL</td>
					<td/>
					<td/>
					<td class="R">&nbsp;</td>
					<td/>
				</tr>
				<tr style="height:1.0625em">
					<td/>
					<xsl:call-template name="weapon">
						<xsl:with-param name="weapon" select="Attacks/AttackForm[1]"/>
					</xsl:call-template>
					<xsl:call-template name="skill">
						<xsl:with-param name="skill" select="Skills/Skill[26]"/>
					</xsl:call-template>
				</tr>
				<tr style="height:1.0625em">
					<td/>
					<td class="L">&nbsp;</td>
					<td/>
					<td/>
					<td class="R">&nbsp;</td>
					<td/>
				</tr>
				<tr style="height:1.0625em">
					<td/>
					<td class="L">&nbsp;</td>
					<xsl:call-template name="skill">
						<xsl:with-param name="skill" select="Skills/Skill[27]"/>
					</xsl:call-template>
				</tr>
				<tr style="height:1.0625em">
					<td/>
					<td class="L">&nbsp;</td>
					<td colspan="5" class="weapon-subtitle-class">RANGE</td>
					<td colspan="5" class="weapon-subtitle-class">WEIGHT</td>
					<td colspan="9" class="weapon-subtitle-class">TYPE</td>
					<td colspan="6" class="weapon-subtitle-class">SIZE</td>
					<td colspan="29" class="weapon-subtitle-class">SPECIAL PROPERTIES</td>
					<td/>
					<td/>
					<td class="R">&nbsp;</td>
					<td/>
				</tr>
				<tr style="height:1.0625em">
					<td/>
					<xsl:call-template name="weapon2">
						<xsl:with-param name="weapon" select="Attacks/AttackForm[1]"/>
					</xsl:call-template>
					<xsl:call-template name="skill">
						<xsl:with-param name="skill" select="Skills/Skill[28]"/>
					</xsl:call-template>
				</tr>
				<tr style="height:1.0625em">
					<td/>
					<td class="L">&nbsp;</td>
					<td/>
					<td/>
					<td class="R">&nbsp;</td>
					<td/>
				</tr>
				<tr style="height:1.0625em">
					<td/>
					<td class="L">&nbsp;</td>
					<xsl:call-template name="skill">
						<xsl:with-param name="skill" select="Skills/Skill[29]"/>
					</xsl:call-template>
				</tr>
				<tr style="height:1.0625em">
					<td/>
					<td class="L">&nbsp;</td>
					<td colspan="9" rowspan="2" class="weapon-ammo-label-class">AMMUNITION</td>
					<td colspan="21" rowspan="2" class="weapon-ammo-desc-class">
						<xsl:value-of select="Attacks/AttackForm[1]/@ammunition"/>
					</td>
					<td/>
					<td rowspan="2" class="checkbox-class" style="border-top:none">&#x2b1c;</td>
					<td rowspan="2" class="checkbox-class" style="border-top:none">&#x2b1c;</td>
					<td rowspan="2" class="checkbox-class" style="border-top:none">&#x2b1c;</td>
					<td rowspan="2" class="checkbox-class" style="border-top:none">&#x2b1c;</td>
					<td rowspan="2" class="checkbox-class" style="border-top:none">&#x2b1c;</td>
					<td/>
					<td rowspan="2" class="checkbox-class" style="border-top:none">&#x2b1c;</td>
					<td rowspan="2" class="checkbox-class" style="border-top:none">&#x2b1c;</td>
					<td rowspan="2" class="checkbox-class" style="border-top:none">&#x2b1c;</td>
					<td rowspan="2" class="checkbox-class" style="border-top:none">&#x2b1c;</td>
					<td rowspan="2" class="checkbox-class" style="border-top:none">&#x2b1c;</td>
					<td/>
					<td rowspan="2" class="checkbox-class" style="border-top:none">&#x2b1c;</td>
					<td rowspan="2" class="checkbox-class" style="border-top:none">&#x2b1c;</td>
					<td rowspan="2" class="checkbox-class" style="border-top:none">&#x2b1c;</td>
					<td rowspan="2" class="checkbox-class" style="border-top:none">&#x2b1c;</td>
					<td rowspan="2" class="checkbox-class" style="border-top:none">&#x2b1c;</td>
					<td/>
					<td rowspan="2" class="checkbox-class" style="border-top:none">&#x2b1c;</td>
					<td rowspan="2" class="checkbox-class" style="border-top:none">&#x2b1c;</td>
					<td rowspan="2" class="checkbox-class" style="border-top:none">&#x2b1c;</td>
					<td rowspan="2" class="checkbox-class" style="border-top:none">&#x2b1c;</td>
					<td rowspan="2" class="checkbox-class" style="border-top:none">&#x2b1c;</td>
					<td/>
					<td/>
					<td class="R">&nbsp;</td>
					<td/>
				</tr>
				<tr style="height:1.0625em">
					<td/>
					<td class="L">&nbsp;</td>
					<td/>
					<td/>
					<td/>
					<td/>
					<xsl:call-template name="skill">
						<xsl:with-param name="skill" select="Skills/Skill[30]"/>
					</xsl:call-template>
				</tr>
				<tr style="height:1.0625em">
					<td/>
					<td class="L" colspan="55">&nbsp;</td>
					<td/>
					<td/>
					<td class="R">&nbsp;</td>
					<td/>
				</tr>
				<tr style="height:1.0625em">
					<td/>
					<td class="L">&nbsp;</td>
					<td colspan="17" rowspan="2" class="title-class">WEAPON</td>
					<td colspan="37"/>
					<xsl:call-template name="skill">
						<xsl:with-param name="skill" select="Skills/Skill[31]"/>
					</xsl:call-template>
				</tr>
				<tr style="height:1.0625em">
					<td/>
					<td class="L">&nbsp;</td>
					<td colspan="15" class="weapon-subtitle-class" style="border-left:none">TOTAL ATTACK BONUS</td>
					<td colspan="13" class="weapon-subtitle-class">DAMAGE</td>
					<td colspan="9" class="weapon-subtitle-class">CRITICAL</td>
					<td/>
					<td/>
					<td class="R">&nbsp;</td>
					<td/>
				</tr>
				<tr style="height:1.0625em">
					<td/>
					<xsl:call-template name="weapon">
						<xsl:with-param name="weapon" select="Attacks/AttackForm[2]"/>
					</xsl:call-template>
					<xsl:call-template name="skill">
						<xsl:with-param name="skill" select="Skills/Skill[32]"/>
					</xsl:call-template>
				</tr>
				<tr style="height:1.0625em">
					<td/>
					<td class="L">&nbsp;</td>
					<td/>
					<td/>
					<td class="R">&nbsp;</td>
					<td/>
				</tr>
				<tr style="height:1.0625em">
					<td/>
					<td class="L">&nbsp;</td>
					<xsl:call-template name="skill">
						<xsl:with-param name="skill" select="Skills/Skill[33]"/>
					</xsl:call-template>
				</tr>
				<tr style="height:1.0625em">
					<td/>
					<td class="L">&nbsp;</td>
					<td colspan="5" class="weapon-subtitle-class">RANGE</td>
					<td colspan="5" class="weapon-subtitle-class">WEIGHT</td>
					<td colspan="9" class="weapon-subtitle-class">TYPE</td>
					<td colspan="6" class="weapon-subtitle-class">SIZE</td>
					<td colspan="29" class="weapon-subtitle-class">SPECIAL PROPERTIES</td>
					<td/>
					<td/>
					<td class="R">&nbsp;</td>
					<td/>
				</tr>
				<tr style="height:1.0625em">
					<td/>
					<xsl:call-template name="weapon2">
						<xsl:with-param name="weapon" select="Attacks/AttackForm[2]"/>
					</xsl:call-template>
					<xsl:call-template name="skill">
						<xsl:with-param name="skill" select="Skills/Skill[34]"/>
					</xsl:call-template>
				</tr>
				<tr style="height:1.0625em">
					<td/>
					<td class="L">&nbsp;</td>
					<td/>
					<td/>
					<td class="R">&nbsp;</td>
					<td/>
				</tr>
				<tr style="height:1.0625em">
					<td/>
					<td class="L">&nbsp;</td>
					<xsl:call-template name="skill">
						<xsl:with-param name="skill" select="Skills/Skill[35]"/>
					</xsl:call-template>
				</tr>
				<tr style="height:1.0625em">
					<td/>
					<td class="L">&nbsp;</td>
					<td colspan="9" rowspan="2" class="weapon-ammo-label-class">AMMUNITION</td>
					<td colspan="21" rowspan="2" class="weapon-ammo-desc-class">
						<xsl:value-of select="Attacks/AttackForm[2]/@ammunition"/>
					</td>
					<td/>
					<td rowspan="2" class="checkbox-class" style="border-top:none">&#x2b1c;</td>
					<td rowspan="2" class="checkbox-class" style="border-top:none">&#x2b1c;</td>
					<td rowspan="2" class="checkbox-class" style="border-top:none">&#x2b1c;</td>
					<td rowspan="2" class="checkbox-class" style="border-top:none">&#x2b1c;</td>
					<td rowspan="2" class="checkbox-class" style="border-top:none">&#x2b1c;</td>
					<td/>
					<td rowspan="2" class="checkbox-class" style="border-top:none">&#x2b1c;</td>
					<td rowspan="2" class="checkbox-class" style="border-top:none">&#x2b1c;</td>
					<td rowspan="2" class="checkbox-class" style="border-top:none">&#x2b1c;</td>
					<td rowspan="2" class="checkbox-class" style="border-top:none">&#x2b1c;</td>
					<td rowspan="2" class="checkbox-class" style="border-top:none">&#x2b1c;</td>
					<td/>
					<td rowspan="2" class="checkbox-class" style="border-top:none">&#x2b1c;</td>
					<td rowspan="2" class="checkbox-class" style="border-top:none">&#x2b1c;</td>
					<td rowspan="2" class="checkbox-class" style="border-top:none">&#x2b1c;</td>
					<td rowspan="2" class="checkbox-class" style="border-top:none">&#x2b1c;</td>
					<td rowspan="2" class="checkbox-class" style="border-top:none">&#x2b1c;</td>
					<td/>
					<td rowspan="2" class="checkbox-class" style="border-top:none">&#x2b1c;</td>
					<td rowspan="2" class="checkbox-class" style="border-top:none">&#x2b1c;</td>
					<td rowspan="2" class="checkbox-class" style="border-top:none">&#x2b1c;</td>
					<td rowspan="2" class="checkbox-class" style="border-top:none">&#x2b1c;</td>
					<td rowspan="2" class="checkbox-class" style="border-top:none">&#x2b1c;</td>
					<td/>
					<td/>
					<td class="R">&nbsp;</td>
					<td/>
				</tr>
				<tr style="height:1.0625em">
					<td/>
					<td class="L">&nbsp;</td>
					<td/>
					<td/>
					<td/>
					<td/>
					<xsl:call-template name="skill">
						<xsl:with-param name="skill" select="Skills/Skill[36]"/>
					</xsl:call-template>
				</tr>
        <tr style="height:1.0625em">
          <td/>
          <td class="L" colspan="55">&nbsp;</td>
          <td/>
          <td/>
          <td class="R">&nbsp;</td>
          <td/>
        </tr>
        <tr style="height:1.0625em">
          <td/>
          <td class="L">&nbsp;</td>
          <td colspan="17" rowspan="2" class="title-class">WEAPON</td>
          <td colspan="37"/>
          <xsl:call-template name="skill">
						<xsl:with-param name="skill" select="Skills/Skill[37]"/>
					</xsl:call-template>
				</tr>
				<tr style="height:1.0625em">
					<td/>
					<td class="L">&nbsp;</td>
					<td colspan="15" class="weapon-subtitle-class" style="border-left:none">TOTAL ATTACK BONUS</td>
					<td colspan="13" class="weapon-subtitle-class">DAMAGE</td>
					<td colspan="9" class="weapon-subtitle-class">CRITICAL</td>
					<td/>
					<td/>
					<td class="R">&nbsp;</td>
					<td/>
				</tr>
				<tr style="height:1.0625em">
					<td/>
					<xsl:call-template name="weapon">
						<xsl:with-param name="weapon" select="Attacks/AttackForm[3]"/>
					</xsl:call-template>
					<xsl:call-template name="skill">
						<xsl:with-param name="skill" select="Skills/Skill[38]"/>
					</xsl:call-template>
				</tr>
				<tr style="height:1.0625em">
					<td/>
					<td class="L">&nbsp;</td>
					<td/>
					<td/>
					<td class="R">&nbsp;</td>
					<td/>
				</tr>
				<tr style="height:1.0625em">
					<td/>
					<td class="L">&nbsp;</td>
					<xsl:call-template name="skill">
						<xsl:with-param name="skill" select="Skills/Skill[39]"/>
					</xsl:call-template>
				</tr>
				<tr style="height:1.0625em">
					<td/>
					<td class="L">&nbsp;</td>
					<td colspan="5" class="weapon-subtitle-class">RANGE</td>
					<td colspan="5" class="weapon-subtitle-class">WEIGHT</td>
					<td colspan="9" class="weapon-subtitle-class">TYPE</td>
					<td colspan="6" class="weapon-subtitle-class">SIZE</td>
					<td colspan="29" class="weapon-subtitle-class">SPECIAL PROPERTIES</td>
					<td/>
					<td/>
					<td class="R">&nbsp;</td>
					<td/>
				</tr>
				<tr style="height:1.0625em">
					<td/>
					<xsl:call-template name="weapon2">
						<xsl:with-param name="weapon" select="Attacks/AttackForm[3]"/>
					</xsl:call-template>
					<xsl:call-template name="skill">
						<xsl:with-param name="skill" select="Skills/Skill[40]"/>
					</xsl:call-template>
				</tr>
				<tr style="height:1.0625em">
					<td/>
					<td class="L">&nbsp;</td>
					<td/>
					<td/>
					<td class="R">&nbsp;</td>
					<td/>
				</tr>
				<tr style="height:1.0625em">
					<td/>
					<td class="L">&nbsp;</td>
					<xsl:call-template name="skill">
						<xsl:with-param name="skill" select="Skills/Skill[41]"/>
					</xsl:call-template>
				</tr>
				<tr style="height:1.0625em">
					<td/>
					<td class="L">&nbsp;</td>
					<td colspan="9" rowspan="2" class="weapon-ammo-label-class">AMMUNITION</td>
					<td colspan="21" rowspan="2" class="weapon-ammo-desc-class">
						<xsl:value-of select="Attacks/AttackForm[3]/@ammunition"/>
					</td>
					<td/>
					<td rowspan="2" class="checkbox-class" style="border-top:none">&#x2b1c;</td>
					<td rowspan="2" class="checkbox-class" style="border-top:none">&#x2b1c;</td>
					<td rowspan="2" class="checkbox-class" style="border-top:none">&#x2b1c;</td>
					<td rowspan="2" class="checkbox-class" style="border-top:none">&#x2b1c;</td>
					<td rowspan="2" class="checkbox-class" style="border-top:none">&#x2b1c;</td>
					<td/>
					<td rowspan="2" class="checkbox-class" style="border-top:none">&#x2b1c;</td>
					<td rowspan="2" class="checkbox-class" style="border-top:none">&#x2b1c;</td>
					<td rowspan="2" class="checkbox-class" style="border-top:none">&#x2b1c;</td>
					<td rowspan="2" class="checkbox-class" style="border-top:none">&#x2b1c;</td>
					<td rowspan="2" class="checkbox-class" style="border-top:none">&#x2b1c;</td>
					<td/>
					<td rowspan="2" class="checkbox-class" style="border-top:none">&#x2b1c;</td>
					<td rowspan="2" class="checkbox-class" style="border-top:none">&#x2b1c;</td>
					<td rowspan="2" class="checkbox-class" style="border-top:none">&#x2b1c;</td>
					<td rowspan="2" class="checkbox-class" style="border-top:none">&#x2b1c;</td>
					<td rowspan="2" class="checkbox-class" style="border-top:none">&#x2b1c;</td>
					<td/>
					<td rowspan="2" class="checkbox-class" style="border-top:none">&#x2b1c;</td>
					<td rowspan="2" class="checkbox-class" style="border-top:none">&#x2b1c;</td>
					<td rowspan="2" class="checkbox-class" style="border-top:none">&#x2b1c;</td>
					<td rowspan="2" class="checkbox-class" style="border-top:none">&#x2b1c;</td>
					<td rowspan="2" class="checkbox-class" style="border-top:none">&#x2b1c;</td>
					<td/>
					<td/>
					<td class="R">&nbsp;</td>
					<td/>
				</tr>
				<tr style="height:1.0625em">
					<td/>
					<td class="L">&nbsp;</td>
					<td/>
					<td/>
					<td/>
					<td/>
					<xsl:call-template name="skill">
						<xsl:with-param name="skill" select="Skills/Skill[42]"/>
					</xsl:call-template>
				</tr>
        <tr style="height:1.0625em">
          <td/>
          <td class="L" colspan="55">&nbsp;</td>
          <td/>
          <td/>
          <td class="R">&nbsp;</td>
          <td/>
        </tr>
        <tr style="height:1.0625em">
          <td/>
          <td class="L">&nbsp;</td>
          <td colspan="17" rowspan="2" class="title-class">WEAPON</td>
          <td colspan="37"/>
          <xsl:call-template name="skill">
						<xsl:with-param name="skill" select="Skills/Skill[43]"/>
					</xsl:call-template>
				</tr>
				<tr style="height:1.0625em">
					<td/>
					<td class="L">&nbsp;</td>
					<td colspan="15" class="weapon-subtitle-class" style="border-left:none">TOTAL ATTACK BONUS</td>
					<td colspan="13" class="weapon-subtitle-class">DAMAGE</td>
					<td colspan="9" class="weapon-subtitle-class">CRITICAL</td>
					<td/>
					<td/>
					<td class="R">&nbsp;</td>
					<td/>
				</tr>
				<tr style="height:1.0625em">
					<td/>
					<xsl:call-template name="weapon">
						<xsl:with-param name="weapon" select="Attacks/AttackForm[4]"/>
					</xsl:call-template>
					<xsl:call-template name="skill">
						<xsl:with-param name="skill" select="Skills/Skill[44]"/>
					</xsl:call-template>
				</tr>
				<tr style="height:1.0625em">
					<td/>
					<td class="L">&nbsp;</td>
					<td/>
					<td/>
					<td class="R">&nbsp;</td>
					<td/>
				</tr>
				<tr style="height:1.0625em">
					<td/>
					<td class="L">&nbsp;</td>
					<xsl:call-template name="skill">
						<xsl:with-param name="skill" select="Skills/Skill[45]"/>
					</xsl:call-template>
				</tr>
				<tr style="height:1.0625em">
					<td/>
					<td class="L">&nbsp;</td>
					<td colspan="5" class="weapon-subtitle-class">RANGE</td>
					<td colspan="5" class="weapon-subtitle-class">WEIGHT</td>
					<td colspan="9" class="weapon-subtitle-class">TYPE</td>
					<td colspan="6" class="weapon-subtitle-class">SIZE</td>
					<td colspan="29" class="weapon-subtitle-class">SPECIAL PROPERTIES</td>
					<td/>
					<td/>
					<td class="R">&nbsp;</td>
					<td/>
				</tr>
				<tr style="height:1.0625em">
					<td/>
					<xsl:call-template name="weapon2">
						<xsl:with-param name="weapon" select="Attacks/AttackForm[4]"/>
					</xsl:call-template>
					<xsl:call-template name="skill">
						<xsl:with-param name="skill" select="Skills/Skill[46]"/>
					</xsl:call-template>
				</tr>
				<tr style="height:1.0625em">
					<td/>
					<td class="L">&nbsp;</td>
					<td/>
					<td/>
					<td class="R">&nbsp;</td>
					<td/>
				</tr>
				<tr style="height:1.0625em">
					<td/>
					<td class="L">&nbsp;</td>
					<xsl:call-template name="skill">
						<xsl:with-param name="skill" select="Skills/Skill[47]"/>
					</xsl:call-template>
				</tr>
				<tr style="height:1.0625em">
					<td/>
					<td class="L">&nbsp;</td>
					<td colspan="9" rowspan="2" class="weapon-ammo-label-class">AMMUNITION</td>
					<td colspan="21" rowspan="2" class="weapon-ammo-desc-class">
						<xsl:value-of select="Attacks/AttackForm[4]/@ammunition"/>
					</td>
					<td/>
					<td rowspan="2" class="checkbox-class" style="border-top:none">&#x2b1c;</td>
					<td rowspan="2" class="checkbox-class" style="border-top:none">&#x2b1c;</td>
					<td rowspan="2" class="checkbox-class" style="border-top:none">&#x2b1c;</td>
					<td rowspan="2" class="checkbox-class" style="border-top:none">&#x2b1c;</td>
					<td rowspan="2" class="checkbox-class" style="border-top:none">&#x2b1c;</td>
					<td/>
					<td rowspan="2" class="checkbox-class" style="border-top:none">&#x2b1c;</td>
					<td rowspan="2" class="checkbox-class" style="border-top:none">&#x2b1c;</td>
					<td rowspan="2" class="checkbox-class" style="border-top:none">&#x2b1c;</td>
					<td rowspan="2" class="checkbox-class" style="border-top:none">&#x2b1c;</td>
					<td rowspan="2" class="checkbox-class" style="border-top:none">&#x2b1c;</td>
					<td/>
					<td rowspan="2" class="checkbox-class" style="border-top:none">&#x2b1c;</td>
					<td rowspan="2" class="checkbox-class" style="border-top:none">&#x2b1c;</td>
					<td rowspan="2" class="checkbox-class" style="border-top:none">&#x2b1c;</td>
					<td rowspan="2" class="checkbox-class" style="border-top:none">&#x2b1c;</td>
					<td rowspan="2" class="checkbox-class" style="border-top:none">&#x2b1c;</td>
					<td/>
					<td rowspan="2" class="checkbox-class" style="border-top:none">&#x2b1c;</td>
					<td rowspan="2" class="checkbox-class" style="border-top:none">&#x2b1c;</td>
					<td rowspan="2" class="checkbox-class" style="border-top:none">&#x2b1c;</td>
					<td rowspan="2" class="checkbox-class" style="border-top:none">&#x2b1c;</td>
					<td rowspan="2" class="checkbox-class" style="border-top:none">&#x2b1c;</td>
					<td/>
					<td/>
					<td class="R">&nbsp;</td>
					<td/>
				</tr>
				<tr style="height:1.0625em">
					<td/>
					<td class="L">&nbsp;</td>
					<td/>
					<td/>
					<td/>
					<td/>
					<xsl:call-template name="skill">
						<xsl:with-param name="skill" select="Skills/Skill[48]"/>
					</xsl:call-template>
				</tr>
        <tr style="height:1.0625em">
          <td/>
          <td class="L" colspan="55">&nbsp;</td>
          <td/>
          <td/>
          <td class="R">&nbsp;</td>
          <td/>
        </tr>
        <tr style="height:1.0625em">
          <td/>
          <td class="L">&nbsp;</td>
          <td colspan="17" rowspan="2" class="title-class">WEAPON</td>
          <td colspan="37"/>
          <xsl:call-template name="skill">
						<xsl:with-param name="skill" select="Skills/Skill[49]"/>
					</xsl:call-template>
				</tr>
				<tr style="height:1.0625em">
					<td/>
					<td class="L">&nbsp;</td>
					<td colspan="15" class="weapon-subtitle-class" style="border-left:none">TOTAL ATTACK BONUS</td>
					<td colspan="13" class="weapon-subtitle-class">DAMAGE</td>
					<td colspan="9" class="weapon-subtitle-class">CRITICAL</td>
					<td/>
					<td/>
					<td class="R">&nbsp;</td>
					<td/>
				</tr>
				<tr style="height:1.0625em">
					<td/>
					<xsl:call-template name="weapon">
						<xsl:with-param name="weapon" select="Attacks/AttackForm[5]"/>
					</xsl:call-template>
					<xsl:call-template name="skill">
						<xsl:with-param name="skill" select="Skills/Skill[50]"/>
					</xsl:call-template>
				</tr>
				<tr style="height:1.0625em">
					<td/>
					<td class="L">&nbsp;</td>
					<td/>
					<td/>
					<td class="R">&nbsp;</td>
					<td/>
				</tr>
				<tr style="height:1.0625em">
					<td/>
					<td class="L">&nbsp;</td>
					<xsl:call-template name="skill">
						<xsl:with-param name="skill" select="Skills/Skill[51]"/>
					</xsl:call-template>
				</tr>
				<tr style="height:1.0625em">
					<td/>
					<td class="L">&nbsp;</td>
					<td colspan="5" class="weapon-subtitle-class">RANGE</td>
					<td colspan="5" class="weapon-subtitle-class">WEIGHT</td>
					<td colspan="9" class="weapon-subtitle-class">TYPE</td>
					<td colspan="6" class="weapon-subtitle-class">SIZE</td>
					<td colspan="29" class="weapon-subtitle-class">SPECIAL PROPERTIES</td>
					<td/>
					<td/>
					<td class="R">&nbsp;</td>
					<td/>
				</tr>
				<tr style="height:1.0625em">
					<td/>
					<xsl:call-template name="weapon2">
						<xsl:with-param name="weapon" select="Attacks/AttackForm[5]"/>
					</xsl:call-template>
					<xsl:call-template name="skill">
						<xsl:with-param name="skill" select="Skills/Skill[52]"/>
					</xsl:call-template>
				</tr>
				<tr style="height:1.0625em">
					<td/>
					<td class="L">&nbsp;</td>
					<td/>
					<td/>
					<td class="R">&nbsp;</td>
					<td/>
				</tr>
				<tr style="height:1.0625em">
					<td/>
					<td class="L">&nbsp;</td>
					<xsl:call-template name="skill">
						<xsl:with-param name="skill" select="Skills/Skill[53]"/>
					</xsl:call-template>
				</tr>
				<tr style="height:1.0625em">
					<td/>
					<td class="L">&nbsp;</td>
					<td colspan="9" rowspan="2" class="weapon-ammo-label-class">AMMUNITION</td>
					<td colspan="21" rowspan="2" class="weapon-ammo-desc-class">
						<xsl:value-of select="Attacks/AttackForm[5]/@ammunition"/>
					</td>
					<td/>
					<td rowspan="2" class="checkbox-class" style="border-top:none">&#x2b1c;</td>
					<td rowspan="2" class="checkbox-class" style="border-top:none">&#x2b1c;</td>
					<td rowspan="2" class="checkbox-class" style="border-top:none">&#x2b1c;</td>
					<td rowspan="2" class="checkbox-class" style="border-top:none">&#x2b1c;</td>
					<td rowspan="2" class="checkbox-class" style="border-top:none">&#x2b1c;</td>
					<td/>
					<td rowspan="2" class="checkbox-class" style="border-top:none">&#x2b1c;</td>
					<td rowspan="2" class="checkbox-class" style="border-top:none">&#x2b1c;</td>
					<td rowspan="2" class="checkbox-class" style="border-top:none">&#x2b1c;</td>
					<td rowspan="2" class="checkbox-class" style="border-top:none">&#x2b1c;</td>
					<td rowspan="2" class="checkbox-class" style="border-top:none">&#x2b1c;</td>
					<td/>
					<td rowspan="2" class="checkbox-class" style="border-top:none">&#x2b1c;</td>
					<td rowspan="2" class="checkbox-class" style="border-top:none">&#x2b1c;</td>
					<td rowspan="2" class="checkbox-class" style="border-top:none">&#x2b1c;</td>
					<td rowspan="2" class="checkbox-class" style="border-top:none">&#x2b1c;</td>
					<td rowspan="2" class="checkbox-class" style="border-top:none">&#x2b1c;</td>
					<td/>
					<td rowspan="2" class="checkbox-class" style="border-top:none">&#x2b1c;</td>
					<td rowspan="2" class="checkbox-class" style="border-top:none">&#x2b1c;</td>
					<td rowspan="2" class="checkbox-class" style="border-top:none">&#x2b1c;</td>
					<td rowspan="2" class="checkbox-class" style="border-top:none">&#x2b1c;</td>
					<td rowspan="2" class="checkbox-class" style="border-top:none">&#x2b1c;</td>
					<td/>
					<td/>
					<td class="R">&nbsp;</td>
					<td/>
				</tr>
				<tr style="height:1.0625em">
					<td/>
					<td class="L">&nbsp;</td>
					<td/>
					<td/>
					<td/>
					<td/>
					<xsl:call-template name="skill">
						<xsl:with-param name="skill" select="Skills/Skill[54]"/>
					</xsl:call-template>
				</tr>
        <tr style="height:1.0625em">
          <td/>
          <td class="L" colspan="55">&nbsp;</td>
          <td/>
          <td/>
          <td class="R">&nbsp;</td>
          <td/>
        </tr>
        <tr style="height:1.0625em">
          <td/>
          <td class="L">&nbsp;</td>
          <td colspan="17" rowspan="2" class="title-class">WEAPON</td>
          <td colspan="37"/>
          <xsl:call-template name="skill">
						<xsl:with-param name="skill" select="Skills/Skill[55]"/>
					</xsl:call-template>
				</tr>
				<tr style="height:1.0625em">
					<td/>
					<td class="L">&nbsp;</td>
					<td colspan="15" class="weapon-subtitle-class" style="border-left:none">TOTAL ATTACK BONUS</td>
					<td colspan="13" class="weapon-subtitle-class">DAMAGE</td>
					<td colspan="9" class="weapon-subtitle-class">CRITICAL</td>
					<td/>
					<td/>
					<td class="R">&nbsp;</td>
					<td/>
				</tr>
				<tr style="height:1.0625em">
					<td/>
					<xsl:call-template name="weapon">
						<xsl:with-param name="weapon" select="Attacks/AttackForm[6]"/>
					</xsl:call-template>
					<xsl:call-template name="skill">
						<xsl:with-param name="skill" select="Skills/Skill[56]"/>
					</xsl:call-template>
				</tr>
				<tr style="height:1.0625em">
					<td/>
					<td class="L">&nbsp;</td>
					<td/>
					<td/>
					<td class="R">&nbsp;</td>
					<td/>
				</tr>
				<tr style="height:1.0625em">
					<td/>
					<td class="L">&nbsp;</td>
					<xsl:call-template name="skill">
						<xsl:with-param name="skill" select="Skills/Skill[57]"/>
					</xsl:call-template>
				</tr>
				<tr style="height:1.0625em">
					<td/>
					<td class="L">&nbsp;</td>
					<td colspan="5" class="weapon-subtitle-class">RANGE</td>
					<td colspan="5" class="weapon-subtitle-class">WEIGHT</td>
					<td colspan="9" class="weapon-subtitle-class">TYPE</td>
					<td colspan="6" class="weapon-subtitle-class">SIZE</td>
					<td colspan="29" class="weapon-subtitle-class">SPECIAL PROPERTIES</td>
					<td/>
					<td/>
					<td class="R">&nbsp;</td>
					<td/>
				</tr>
				<tr style="height:1.0625em">
					<td/>
					<xsl:call-template name="weapon2">
						<xsl:with-param name="weapon" select="Attacks/AttackForm[6]"/>
					</xsl:call-template>
					<td/>
					<td/>
					<td/>
					<td/>
					<td colspan="40" rowspan="5" class="skills-notes-class">
            Skills marked with <sup>1</sup> can be used normally even if the character has zero (0) skill ranks.<br />
            Skills marked with <span style="font-size:1.1428em;">&#x2612;</span> are cross-class skills.<br />
            * ARMOR CHECK PENALTY applies.&nbsp;&nbsp;&nbsp;&nbsp;** Twice ARMOR CHECK PENALTY applies.
					</td>
					<td class="R">&nbsp;</td>
					<td/>
				</tr>
				<tr style="height:1.0625em">
					<td/>
					<td class="L">&nbsp;</td>
					<td/>
					<td/>
					<td/>
					<td/>
					<td class="R">&nbsp;</td>
					<td/>
				</tr>
				<tr style="height:1.0625em">
					<td/>
					<td class="L">&nbsp;</td>
					<td/>
					<td/>
					<td/>
					<td/>
					<td class="R">&nbsp;</td>
					<td/>
				</tr>
				<tr style="height:1.0625em">
					<td/>
					<td class="L">&nbsp;</td>
					<td colspan="9" rowspan="2" class="weapon-ammo-label-class">AMMUNITION</td>
					<td colspan="21" rowspan="2" class="weapon-ammo-desc-class">
						<xsl:value-of select="Attacks/AttackForm[6]/@ammunition"/>
					</td>
					<td/>
					<td rowspan="2" class="checkbox-class" style="border-top:none">&#x2b1c;</td>
					<td rowspan="2" class="checkbox-class" style="border-top:none">&#x2b1c;</td>
					<td rowspan="2" class="checkbox-class" style="border-top:none">&#x2b1c;</td>
					<td rowspan="2" class="checkbox-class" style="border-top:none">&#x2b1c;</td>
					<td rowspan="2" class="checkbox-class" style="border-top:none">&#x2b1c;</td>
					<td/>
					<td rowspan="2" class="checkbox-class" style="border-top:none">&#x2b1c;</td>
					<td rowspan="2" class="checkbox-class" style="border-top:none">&#x2b1c;</td>
					<td rowspan="2" class="checkbox-class" style="border-top:none">&#x2b1c;</td>
					<td rowspan="2" class="checkbox-class" style="border-top:none">&#x2b1c;</td>
					<td rowspan="2" class="checkbox-class" style="border-top:none">&#x2b1c;</td>
					<td/>
					<td rowspan="2" class="checkbox-class" style="border-top:none">&#x2b1c;</td>
					<td rowspan="2" class="checkbox-class" style="border-top:none">&#x2b1c;</td>
					<td rowspan="2" class="checkbox-class" style="border-top:none">&#x2b1c;</td>
					<td rowspan="2" class="checkbox-class" style="border-top:none">&#x2b1c;</td>
					<td rowspan="2" class="checkbox-class" style="border-top:none">&#x2b1c;</td>
					<td/>
					<td rowspan="2" class="checkbox-class" style="border-top:none">&#x2b1c;</td>
					<td rowspan="2" class="checkbox-class" style="border-top:none">&#x2b1c;</td>
					<td rowspan="2" class="checkbox-class" style="border-top:none">&#x2b1c;</td>
					<td rowspan="2" class="checkbox-class" style="border-top:none">&#x2b1c;</td>
					<td rowspan="2" class="checkbox-class" style="border-top:none">&#x2b1c;</td>
					<td/>
					<td/>
					<td/>
					<td/>
					<td class="R">&nbsp;</td>
					<td/>
				</tr>
				<tr style="height:1.0625em">
					<td/>
					<td class="L">&nbsp;</td>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td class="R">&nbsp;</td>
					<td/>
				</tr>
				<tr style="height:2.5em">
					<td/>
					<td class="L B R" colspan="100">&nbsp;</td>
					<td/>
				</tr>
				<tr style="height:1.0625em">
					<td colspan="102"/>
				</tr>
			</table>
		</div>

</xsl:template>
</xsl:stylesheet>
