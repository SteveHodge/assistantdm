<?xml version="1.0" encoding="ISO-8859-1"?>

<!DOCTYPE stylesheet [
<!ENTITY nbsp  "&#160;" >
]>

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	<xsl:output method="html" encoding="utf-8" indent="yes" />

<xsl:template name="weapon">
	<xsl:param name="weapon"/>
	<td colspan="17" rowspan="3" class="weapon-name">
		<xsl:value-of select="$weapon/@name"/>
	</td>
	<td colspan="15" rowspan="3" class="bab" title="{$weapon/@name}" info="{$weapon/@info}" onclick="showInfo(this);">
		<xsl:choose>
			<xsl:when test="Attacks/@total_defense = 'true'">
				<s><xsl:value-of select="$weapon/@attacks"/></s>
			</xsl:when>
			<xsl:otherwise>
				<xsl:value-of select="$weapon/@attacks"/>
			</xsl:otherwise>
		</xsl:choose>
	</td>
	<td colspan="13" rowspan="3" class="data-value" title="{$weapon/@name}" info="{$weapon/@damage_info}" onclick="showInfo(this);">
			<xsl:value-of select="$weapon/@damage"/>
	</td>
	<td colspan="9" rowspan="3" class="data-value">
		<xsl:value-of select="$weapon/@critical"/>
	</td>
</xsl:template>

<!-- due to work around for firefox border bug the bottom right corner border must be completed with cells outside the template in the
following two rows:
					<td style="border-right:2px solid black;"></td>
					<td style="border-right:2px solid black; border-bottom:2px solid black;"></td>
-->
<xsl:template name="weapon2">
	<xsl:param name="weapon"/>
	<td colspan="5" rowspan="3" class="data-value">
		<xsl:if test="not($weapon/@range)">0</xsl:if>
		<xsl:value-of select="$weapon/@range"/>
		ft
	</td>
	<td colspan="5" rowspan="3" class="data-value">
		<xsl:if test="not($weapon/@weight)">0</xsl:if>
		<xsl:value-of select="$weapon/@weight"/>
		lb
	</td>
	<td colspan="9" rowspan="3" class="data-value">
		<xsl:value-of select="$weapon/@type"/>
	</td>
	<td colspan="6" rowspan="3" class="data-value">
		<xsl:value-of select="$weapon/@size"/>
	</td>
	<!-- the reduced width and lack of right border is part of work-around for firefox border bug -->
	<td colspan="28" style="border-right:none" rowspan="3" class="data-value">
		<xsl:value-of select="$weapon/@properties"/>
	</td>
	<td style="border-right:2px solid black;"></td>	<!-- also part of work around -->
</xsl:template>

<xsl:template name="ability">
	<xsl:param name="ability"/>
	<td/>
	<td colspan="4" rowspan="3" class="data-value-important" title="{$ability/@type}" info="{$ability/@info}" onclick="showInfo(this);">
		<xsl:value-of select="$ability/@total"/>
	</td>
	<td/>
	<td colspan="4" rowspan="3" class="data-value-important">
		<xsl:value-of select="$ability/@modifier"/>
	</td>
	<td>&nbsp;</td>
	<td colspan="4" rowspan="3" class="temp-value">
		<xsl:value-of select="$ability/@temp"/>
	</td>
	<td/>
	<td colspan="4" rowspan="3" class="temp-value">
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
					<td colspan="4" rowspan="3" class="data-value-important" title="{$save/@type}" info="{$save/@info}" onclick="showInfo(this);">
						<xsl:value-of select="$save/@total"/>
					</td>
					<td rowspan="3" class="equation">=</td>
					<td colspan="4" rowspan="3" class="data-value">
						<xsl:value-of select="$save/@base"/>
					</td>
					<td rowspan="3" class="equation">+</td>
					<td colspan="4" rowspan="3" class="data-value">
						<xsl:call-template name="ability-mod">
							<xsl:with-param name="ability" select="$ability"/>
						</xsl:call-template>
					</td>
					<td rowspan="3" class="equation">+</td>
					<td colspan="4" rowspan="3" class="data-value">
						<xsl:value-of select="$save/@mods"/>
					</td>
					<td rowspan="3" class="equation">+</td>
					<td colspan="4" rowspan="3" class="temp-value">
						<xsl:value-of select="$save/@misc"/>
					</td>
</xsl:template>

<xsl:template name="skill">
	<xsl:param name="skill"/>
					<td/>
					<td/>
					<td colspan="2" rowspan="2" class="checkbox">
						<xsl:choose>
							<xsl:when test="$skill/@cross">
								&#x2612;
							</xsl:when>
							<xsl:when test="$skill/@type">
								&#x2610;
							</xsl:when>
							<xsl:otherwise>
							</xsl:otherwise>
						</xsl:choose>
					</td>
					<td colspan="16" rowspan="2" class="skills-name">
						<xsl:value-of select="$skill/@type"/>
						<xsl:if test="$skill/@untrained = 'true'">
							<span style="font-size:1.05em;"><sup>1</sup></span>
						</xsl:if>
					</td>
					<td colspan="4" rowspan="2" class="skills-ability">
						<xsl:value-of select="$skill/@ability"/>
					</td>
					<td rowspan="2"/>
					<td colspan="4" rowspan="2" class="skills-total B" title="{$skill/@type}" info="{$skill/@info}" onclick="showInfo(this);">
						<xsl:value-of select="$skill/@total"/>
					</td>
					<td rowspan="2" class="equation">=</td>
					<td colspan="4" rowspan="2" class="skills-data">
						<xsl:value-of select="$skill/@ability-modifier"/>
					</td>
					<td rowspan="2" class="equation">+</td>
					<td colspan="4" rowspan="2" class="skills-data">
						<xsl:value-of select="$skill/@ranks"/>
					</td>
					<td rowspan="2" class="equation">+</td>
					<xsl:choose>
						<xsl:when test="$skill/@misc != 0">
							<td colspan="4" rowspan="2" class="skills-data">
								<xsl:value-of select="$skill/@misc"/>
 							</td>
						</xsl:when><xsl:otherwise>
							<td colspan="4" rowspan="2" class="skills-data" style="color:white;">
			          0
							</td>
						</xsl:otherwise>
					</xsl:choose>
</xsl:template>

<xsl:template match="Character">
		<div class="character-table">
			<table>
				<col style="width:1.1666em" span="98"/>
				<tr>
					<td colspan="30" rowspan="2" class="info-value-important">
						<xsl:value-of select="@name"/>
					</td>
					<td/>
					<td/>
					<td colspan="22" rowspan="2" class="info-value-important">
						<xsl:value-of select="@player"/>
					</td>
					<td/>
					<td colspan="15" rowspan="2" class="info-value">
						<xsl:value-of select="@region"/>
					</td>
					<td/>
					<td colspan="27" rowspan="8" style="vertical-align:top;">&nbsp;
						<span style="position:absolute;z-index:1;width:37.8em; height:10.65em">
							<img style="width:37.8em; height:10.65em" src="http://static.stevehodge.net/dndlogo.png" alt="DandDLogo" />
						</span>
					</td>
				</tr>
				<tr>
					<td style="border-bottom:2px solid black" />
					<td/>
					<td/>
				</tr>
				<tr>
					<td colspan="31">CHARACTER NAME</td>
					<td/>
					<td colspan="22">PLAYER</td>
					<td/>
					<td colspan="15">REGION</td>
					<td/>
				</tr>
				<tr>
					<td colspan="14" rowspan="2" class="info-value-small">
						<xsl:value-of select="Level/@class"/>
					</td>
					<td/>
					<td/>
					<td colspan="15" rowspan="2" class="info-value">
						<xsl:value-of select="@race"/>
					</td>
					<td/>
					<td colspan="6" rowspan="2" class="info-value">
						<xsl:value-of select="@gender"/>
					</td>
					<td/>
					<td colspan="15" rowspan="2" class="info-value">
						<xsl:value-of select="@alignment"/>
					</td>
					<td/>
					<td colspan="15" rowspan="2" class="info-value">
						<xsl:value-of select="@deity"/>
					</td>
					<td/>
				</tr>
				<tr>
					<td style="bottom-border:2px solid black;"/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
				</tr>
				<tr>
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
				</tr>
				<tr>
					<td colspan="7" rowspan="2" class="info-value-small">
						<xsl:value-of select="Level/@level"/>
					</td>
					<td/>
					<td colspan="7" rowspan="2" class="info-value-small">
						<xsl:value-of select="@size"/>
					</td>
					<td/>
					<td colspan="15" rowspan="2" class="info-value">
						<xsl:value-of select="@type"/>
					</td>
					<td/>
					<td colspan="6" rowspan="2" class="info-value">
						<xsl:value-of select="@age"/>
					</td>
					<td/>
					<td colspan="7" rowspan="2" class="info-value">
						<xsl:value-of select="@height"/>
					</td>
					<td/>
					<td colspan="7" rowspan="2" class="info-value">
						<xsl:value-of select="@weight"/>
					</td>
					<td/>
					<td colspan="7" rowspan="2" class="info-value">
						<xsl:value-of select="@eye-colour"/>
					</td>
					<td/>
					<td colspan="7" rowspan="2" class="info-value">
						<xsl:value-of select="@hair-colour"/>
					</td>
					<td/>
				</tr>
				<tr>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
				</tr>
				<tr>
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
					<td colspan="27" rowspan="2" class="sheet-title">CHARACTER RECORD SHEET</td>
				</tr>
				<tr>
					<td/>
				</tr>
				<tr>
					<td colspan="6" rowspan="2" class="data-box-desc" style="vertical-align:bottom;">ABILITY NAME</td>
					<td/>
					<td colspan="4" rowspan="2" class="data-box-desc">ABILITY SCORE</td>
					<td/>
					<td colspan="4" rowspan="2" class="data-box-desc">ABILITY MODIFIER</td>
					<td/>
					<td colspan="4" rowspan="2" class="data-box-desc">TEMP. SCORE</td>
					<td/>
					<td colspan="4" rowspan="2" class="data-box-desc">TEMP. MODIFIER</td>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td colspan="4" rowspan="2" class="data-box-desc-total" style="vertical-align:middle;">TOTAL</td>
					<td/>
					<td colspan="10" rowspan="2" class="data-box-desc">CURRENT</td>
					<td/>
					<td colspan="10" rowspan="2" class="data-box-desc">WOUNDS</td>
					<td/>
					<td colspan="10" rowspan="2" class="data-box-desc">NONLETHAL DAMAGE</td>
					<td/>
					<td/>
					<td/>
					<td/>
					<td colspan="22" rowspan="2" class="speed-heading">SPEED</td>
				</tr>
				<tr>
					<td/>
				</tr>
				<tr>
					<td colspan="6" rowspan="2" class="title">STR</td>
					<xsl:call-template name="ability">
						<xsl:with-param name="ability" select="AbilityScores/AbilityScore[@type='Strength']"/>
					</xsl:call-template>
					<td colspan="6" rowspan="2" class="title">HP</td>
					<td/>
					<td colspan="4" rowspan="3" class="data-value-important" title="Hit Points" info="{HitPoints/@info}" onclick="showInfo(this);">
						<xsl:value-of select="HitPoints/@maximum"/>
					</td>
					<td/>
					<td colspan="10" rowspan="3" class="data-value" style="text-align:left; padding-left:10px" title="Hit Points" info="{HitPoints/@info}" onclick="showInfo(this);">
						<xsl:value-of select="HitPoints/@current"/>
					</td>
					<td/>
					<td colspan="10" rowspan="3" class="data-value" style="text-align:left; padding-left:10px" title="Hit Points" info="{HitPoints/@info}" onclick="showInfo(this);">
						<xsl:value-of select="HitPoints/@wounds"/>
					</td>
					<td/>
					<td colspan="10" rowspan="3" class="data-value" style="text-align:left; padding-left:10px" title="Hit Points" info="{HitPoints/@info}" onclick="showInfo(this);">
						<xsl:value-of select="HitPoints/@non-lethal"/>
					</td>
					<td/>
					<td/>
					<td/>
					<td/>
					<td colspan="22" rowspan="3" class="data-value">
						<xsl:value-of select="@speed"/>
					</td>
				</tr>
				<tr>
					<td/>
				</tr>
				<tr>
					<td colspan="6" class="subtitle">STRENGTH</td>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td colspan="6" class="subtitle">HIT POINTS</td>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
				</tr>
				<tr>
					<td/><td/><td/><td/><td/><td/><td/><td/><td/><td/>
					<td/><td/><td/><td/><td/><td/><td/><td/><td/><td/>
					<td/><td/><td/><td/><td/><td/><td/><td/><td/><td/>
					<td/><td/><td/><td/><td/><td/><td/><td/><td/><td/>
					<td/><td/><td/><td/><td/><td/><td/><td/><td/><td/>
					<td/><td/><td/><td/><td/><td/><td/><td/><td/><td/>
					<td/><td/><td/><td/><td/><td/><td/><td/><td/><td/>
					<td/><td/><td/><td/><td/><td/><td/><td/><td/><td/>
					<td/><td/><td/><td/><td/><td/><td/><td/><td/><td/>
					<td/><td/><td/><td/><td/><td/><td/><td/>
				</tr>
				<tr>
					<td colspan="6" rowspan="2" class="title">DEX</td>
					<xsl:call-template name="ability">
						<xsl:with-param name="ability" select="AbilityScores/AbilityScore[@type='Dexterity']"/>
					</xsl:call-template>
					<td colspan="6" rowspan="2" class="title">AC</td>
					<td/>
					<td colspan="4" rowspan="3" class="data-value-important">
						<xsl:value-of select="AC/@total"/>
					</td>
					<td rowspan="3" class="equation">=</td>
					<td colspan="3" rowspan="3" class="ac-base-value">10</td>
					<td rowspan="3" class="equation">+</td>
					<td colspan="4" rowspan="3" class="data-value">
						<xsl:value-of select="AC/ACComponent[@type='Armor']/@value"/>
						<xsl:if test="not(AC/ACComponent[@type='Armor']/@value)">+0</xsl:if>
					</td>
					<td rowspan="3" class="equation">+</td>
					<td colspan="4" rowspan="3" class="data-value">
						<xsl:value-of select="AC/ACComponent[@type='Shield']/@value"/>
						<xsl:if test="not(AC/ACComponent[@type='Shield']/@value)">+0</xsl:if>
					</td>
					<td rowspan="3" class="equation">+</td>
					<td colspan="4" rowspan="3" class="data-value">
						<xsl:value-of select="AC/ACComponent[@type='Dexterity']/@value"/>
						<xsl:if test="not(AC/ACComponent[@type='Dexterity']/@value)">+0</xsl:if>
					</td>
					<td rowspan="3" class="equation">+</td>
					<td colspan="4" rowspan="3" class="data-value">
						<xsl:value-of select="AC/ACComponent[@type='Size']/@value"/>
						<xsl:if test="not(AC/ACComponent[@type='Size']/@value)">+0</xsl:if>
					</td>
					<td rowspan="3" class="equation">+</td>
					<td colspan="4" rowspan="3" class="data-value">
						<xsl:value-of select="AC/ACComponent[@type='Natural Armor']/@value"/>
						<xsl:if test="not(AC/ACComponent[@type='Natural Armor']/@value)">+0</xsl:if>
					</td>
					<td rowspan="3" class="equation">+</td>
					<td colspan="4" rowspan="3" class="data-value">
						<xsl:value-of select="AC/ACComponent[@type='Deflection']/@value"/>
						<xsl:if test="not(AC/ACComponent[@type='Deflection']/@value)">+0</xsl:if>
					</td>
					<td rowspan="3" class="equation">+</td>
					<td colspan="4" rowspan="3" class="data-value">
						<xsl:value-of select="AC/ACComponent[@type='Misc']/@value"/>
						<xsl:if test="not(AC/ACComponent[@type='Misc']/@value)">+0</xsl:if>
					</td>
					<td/>
					<td/>
					<td colspan="4" rowspan="3" class="data-value">
						<xsl:value-of select="AC/@armor-check-penalty"/>
					</td>
					<td/>
					<td colspan="13" rowspan="3" class="data-value">
						<xsl:value-of select="@damage-reduction"/>
					</td>
				</tr>
				<tr>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
				</tr>
				<tr>
					<td colspan="6" class="subtitle">
            DEXTERITY
					</td>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td colspan="6" class="subtitle">
            ARMOR CLASS
					</td>
					<td/>
					<td/>
					<td/>
					<td/>
				</tr>
				<tr>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td colspan="4" rowspan="2" class="data-box-desc-total-over">TOTAL</td>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td colspan="4" rowspan="2" class="data-box-desc-under">ARMOR BONUS</td>
					<td/>
					<td colspan="4" rowspan="2" class="data-box-desc-under">SHIELD BONUS</td>
					<td/>
					<td colspan="4" rowspan="2" class="data-box-desc-under">DEX MODIFIER</td>
					<td/>
					<td colspan="4" rowspan="2" class="data-box-desc-under">SIZE MODIFIER</td>
					<td/>
					<td colspan="4" rowspan="2" class="data-box-desc-under">NATURAL ARMOR</td>
					<td/>
					<td colspan="4" rowspan="2" class="data-box-desc-under">DEFLECT MOD</td>
					<td/>
					<td colspan="4" rowspan="2" class="data-box-desc-under">MISC MOD</td>
					<td/>
					<td/>
					<td colspan="4" rowspan="3" class="data-box-desc-under">ARMOR CHECK PENALTY</td>
					<td/>
					<td colspan="13" rowspan="2" class="data-box-desc">DAMAGE REDUCTION</td>
				</tr>
				<tr>
					<td colspan="6" rowspan="2" class="title">CON</td>
					<xsl:call-template name="ability">
						<xsl:with-param name="ability" select="AbilityScores/AbilityScore[@type='Constitution']"/>
					</xsl:call-template>
				</tr>
				<tr>
					<td/>
				</tr>
				<tr>
					<td colspan="6" class="subtitle">
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
				</tr>
				<tr>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td colspan="6" rowspan="2" class="title">TOUCH</td>
					<td/>
					<td colspan="4" rowspan="3" class="data-value-important">
						<xsl:value-of select="AC/@touch"/>
					</td>
					<td/>
					<td colspan="10" rowspan="2" class="title">FLAT-FOOTED</td>
					<td/>
					<td colspan="4" rowspan="3" class="data-value-important">
						<xsl:value-of select="AC/@flat-footed"/>
					</td>
					<td/>
					<td colspan="2" rowspan="4" class="skills-cross-class-heading">
						<div style="transform: translateY(-2.1em) rotate(-90deg); -webkit-transform: translateY(-2.1em) rotate(-90deg);">
							CROSS
						</div>
					</td>
					<td colspan="27" rowspan="2" class="skills-title">SKILLS</td>
					<td colspan="8" rowspan="2" class="skills-max-ranks-heading">MAX RANKS</td>
					<td colspan="5" rowspan="2" class="skills-max-ranks-value">
            9 / 4.5
					</td>
				</tr>
				<tr>
					<td colspan="6" rowspan="2" class="title">INT</td>
					<xsl:call-template name="ability">
						<xsl:with-param name="ability" select="AbilityScores/AbilityScore[@type='Intelligence']"/>
					</xsl:call-template>
					<td/>
					<td/>
					<td/>
					<td/>
				</tr>
				<tr>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td colspan="6" class="subtitle">ARMOR CLASS</td>
					<td/>
					<td/>
					<td colspan="10" class="subtitle">ARMOR CLASS</td>
					<td/>
					<td/>
					<td colspan="16" rowspan="2" class="skills-subhead-main">SKILL NAME</td>
					<td colspan="4" rowspan="2" class="skills-subhead">KEY ABILITY</td>
					<td/>
					<td colspan="4" rowspan="2" class="skills-subhead-main">SKILL MODIFIER</td>
					<td/>
					<td colspan="4" rowspan="2" class="skills-subhead">ABILITY MODIFIER</td>
					<td/>
					<td colspan="4" rowspan="2" class="skills-subhead-main">RANKS</td>
					<td/>
					<td colspan="4" rowspan="2" class="skills-subhead">MISC. MODIFIER</td>
				</tr>
				<tr>
					<td colspan="6" class="subtitle">INTELLIGENCE</td>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
				</tr>
				<tr>
					<td/><td/><td/><td/><td/><td/><td/><td/><td/><td/>
					<td/><td/><td/><td/><td/><td/><td/><td/><td/><td/>
					<td/><td/><td/><td/><td/><td/><td/><td/><td/><td/>
					<td/><td/><td/><td/><td/><td/><td/><td/><td/><td/>
					<td/><td/><td/><td/><td/><td/><td/><td/><td/><td/>
					<td/><td/><td/><td/><td/><td/><td/><td/><td/><td/>
					<td/><td/><td/><td/><td/><td/><td/><td/><td/><td/>
					<td/><td/><td/><td/><td/><td/><td/><td/><td/><td/>
					<td/><td/><td/><td/><td/><td/><td/><td/><td/><td/>
					<td/><td/><td/><td/><td/><td/><td/><td/>
				</tr>
				<tr>
					<td colspan="6" rowspan="2" class="title">WIS</td>
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
				<tr>
					<td/>
				</tr>
				<tr>
					<td colspan="6" class="subtitle">WISDOM</td>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td colspan="11" rowspan="2" class="title">INITIATIVE</td>
					<td/>
					<td colspan="4" rowspan="3" class="data-value-important" title="Initiative" info="{Initiative/@info}" onclick="showInfo(this);">
						<xsl:value-of select="Initiative/@total"/>
					</td>
					<td rowspan="3" class="equation">=</td>
					<td colspan="4" rowspan="3" class="data-value">
						<xsl:call-template name="ability-mod">
							<xsl:with-param name="ability" select="AbilityScores/AbilityScore[@type='Dexterity']"/>
						</xsl:call-template>
					</td>
					<td rowspan="3" class="equation">+</td>
					<td colspan="4" rowspan="3" class="data-value">
						<xsl:value-of select="Initiative/@misc"/>
						<xsl:if test="not(Initiative/@misc)">+0</xsl:if>
					</td>
					<xsl:call-template name="skill">
						<xsl:with-param name="skill" select="Skills/Skill[2]"/>
					</xsl:call-template>
				</tr>
				<tr>
					<td/><td/><td/><td/><td/><td/><td/><td/><td/><td/>
					<td/><td/><td/><td/><td/><td/><td/><td/><td/><td/>
					<td/><td/><td/><td/><td/><td/>
				</tr>
				<tr>
					<td colspan="6" rowspan="2" class="title">CHA</td>
					<xsl:call-template name="ability">
						<xsl:with-param name="ability" select="AbilityScores/AbilityScore[@type='Charisma']"/>
					</xsl:call-template>
					<td colspan="11" class="subtitle">MODIFIER</td>
					<td/>
					<xsl:call-template name="skill">
						<xsl:with-param name="skill" select="Skills/Skill[3]"/>
					</xsl:call-template>
				</tr>
				<tr>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td colspan="4" rowspan="2" class="data-box-desc-total-over">TOTAL</td>
					<td/>
					<td colspan="4" rowspan="2" class="data-box-desc-under">DEX MODIFIER</td>
					<td/>
					<td colspan="4" rowspan="2" class="data-box-desc-under">MISC. MODIFIER</td>
					<td/>
					<td/>
				</tr>
				<tr>
					<td colspan="6" class="subtitle">CHARISMA</td>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
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
				<tr>
					<td/><td/><td/><td/><td/><td/><td/><td/><td/><td/>
					<td/><td/><td/><td/><td/><td/><td/><td/><td/><td/>
					<td/><td/><td/><td/><td/><td/><td/><td/><td/><td/>
					<td/><td/><td/><td/><td/><td/><td/><td/><td/><td/>
					<td/><td/><td/><td/><td/><td/><td/><td/><td/><td/>
					<td/><td/><td/><td/>
				</tr>
				<tr>
					<td colspan="11" rowspan="2" class="data-box-desc-total">SAVING THROWS</td>
					<td/>
					<td colspan="4" rowspan="2" class="data-box-desc-total">TOTAL</td>
					<td/>
					<td colspan="4" rowspan="2" class="data-box-desc">BASE<br /> SAVE</td>
					<td/>
					<td colspan="4" rowspan="2" class="data-box-desc">ABILITY MODIFIER</td>
					<td/>
					<td colspan="4" rowspan="2" class="data-box-desc">MISC. &amp; MAGIC</td>
					<td/>
					<td colspan="4" rowspan="2" class="data-box-desc">TEMP MODIFIER</td>
					<td/>
					<td colspan="17" class="conditional-heading">CONDITIONAL MODIFIERS</td>
					<xsl:call-template name="skill">
						<xsl:with-param name="skill" select="Skills/Skill[5]"/>
					</xsl:call-template>
				</tr>
				<tr>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td colspan="17" rowspan="12" class="conditional-modifiers">
						<xsl:for-each select="Buffs/Buff/Modifier[@condition]">
							<xsl:value-of select="@description"/><br/>
						</xsl:for-each>
					</td>
					<td/>
					<td/>
				</tr>
				<tr>
					<td colspan="11" rowspan="2" class="title">FORTITUDE</td>
					<xsl:call-template name="save">
						<xsl:with-param name="save" select="SavingThrows/Save[@type='Fortitude']"/>
						<xsl:with-param name="ability" select="AbilityScores/AbilityScore[@type='Constitution']"/>
					</xsl:call-template>
					<td rowspan="3"/>
					<xsl:call-template name="skill">
						<xsl:with-param name="skill" select="Skills/Skill[6]"/>
					</xsl:call-template>
				</tr>
				<tr>
					<td/>
					<td/>
					<td/>
				</tr>
				<tr>
					<td colspan="11" class="subtitle">(CONSTITUTION)</td>
					<td/>
					<xsl:call-template name="skill">
						<xsl:with-param name="skill" select="Skills/Skill[7]"/>
					</xsl:call-template>
				</tr>
				<tr>
					<td/><td/><td/><td/><td/><td/><td/><td/><td/><td/>
					<td/><td/><td/><td/><td/><td/><td/><td/><td/><td/>
					<td/><td/><td/><td/><td/><td/><td/><td/><td/><td/>
					<td/><td/><td/><td/><td/><td/><td/>
				</tr>
				<tr>
					<td colspan="11" rowspan="2" class="title">REFLEX</td>
					<xsl:call-template name="save">
						<xsl:with-param name="save" select="SavingThrows/Save[@type='Reflex']"/>
						<xsl:with-param name="ability" select="AbilityScores/AbilityScore[@type='Dexterity']"/>
					</xsl:call-template>
					<td rowspan="3"/>
					<xsl:call-template name="skill">
						<xsl:with-param name="skill" select="Skills/Skill[8]"/>
					</xsl:call-template>
				</tr>
				<tr>
					<td/>
					<td/>
					<td/>
				</tr>
				<tr>
					<td colspan="11" class="subtitle">(DEXTERITY)</td>
					<td/>
					<xsl:call-template name="skill">
						<xsl:with-param name="skill" select="Skills/Skill[9]"/>
					</xsl:call-template>
				</tr>
				<tr>
					<td/><td/><td/><td/><td/><td/><td/><td/><td/><td/>
					<td/><td/><td/><td/><td/><td/><td/><td/><td/><td/>
					<td/><td/><td/><td/><td/><td/><td/><td/><td/><td/>
					<td/><td/><td/><td/><td/><td/><td/>
				</tr>
				<tr>
					<td colspan="11" rowspan="2" class="title">WILL</td>
					<xsl:call-template name="save">
						<xsl:with-param name="save" select="SavingThrows/Save[@type='Will']"/>
						<xsl:with-param name="ability" select="AbilityScores/AbilityScore[@type='Wisdom']"/>
					</xsl:call-template>
					<td rowspan="3"/>
					<xsl:call-template name="skill">
						<xsl:with-param name="skill" select="Skills/Skill[10]"/>
					</xsl:call-template>
				</tr>
				<tr>
					<td/><td/><td/>
				</tr>
				<tr>
					<td colspan="11" class="subtitle">(WISDOM)</td>
					<td/>
					<xsl:call-template name="skill">
						<xsl:with-param name="skill" select="Skills/Skill[11]"/>
					</xsl:call-template>
				</tr>
				<tr>
					<td/><td/><td/><td/><td/><td/><td/><td/><td/><td/>
					<td/><td/><td/><td/><td/><td/><td/><td/><td/><td/>
					<td/><td/><td/><td/><td/><td/><td/><td/><td/><td/>
					<td/><td/><td/><td/><td/><td/><td/><td/><td/><td/>
					<td/><td/><td/><td/><td/><td/><td/><td/><td/><td/>
					<td/><td/><td/><td/>
				</tr>
				<tr>
					<td/><td/><td/><td/><td/><td/><td/><td/><td/><td/>
					<td/><td/><td/><td/><td/><td/><td/><td/><td/><td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td colspan="4" class="data-box-desc">TEMP.</td>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td colspan="12" rowspan="3" class="title-smaller">SPELL RESISTANCE</td>
					<td/>
					<td colspan="4" rowspan="3" class="data-value-important">
						<xsl:value-of select="@spell-resistance"/>
					</td>
					<xsl:call-template name="skill">
						<xsl:with-param name="skill" select="Skills/Skill[12]"/>
					</xsl:call-template>
				</tr>
				<tr>
					<!-- bottom border triggers firefox bug-->
					<td colspan="16" rowspan="3" class="title" style="border-bottom:none">BASE ATTACK BONUS</td>
					<td/>
					<td colspan="9" rowspan="3" class="bab">
						<xsl:value-of select="Attacks/@attacks"/>
					</td>
					<td/>
					<td colspan="4" rowspan="3" class="temp-value">&nbsp;</td>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
				</tr>
				<tr>
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
				<tr>
					<td/><td/><td/><td/><td/><td/><td/><td/><td/><td/>
					<td/><td/><td/><td/><td/><td/><td/><td/><td/><td/>
					<td/><td/><td/><td/><td/>
				</tr>
				<tr>
					<td/><td/><td/><td/><td/><td/><td/><td/><td/><td/>
					<td/><td/><td/><td/><td/><td/><td/><td/><td/><td/>
					<td/><td/><td/><td/><td/><td/><td/><td/><td/><td/>
					<td/><td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td colspan="12" rowspan="3" class="title-smaller">ARCANE SPELL FAILURE *</td>
					<td/>
					<td colspan="4" rowspan="3" class="data-value">
						<xsl:value-of select="@arcane-spell-failure"/>
					</td>
					<xsl:call-template name="skill">
						<xsl:with-param name="skill" select="Skills/Skill[14]"/>
					</xsl:call-template>
				</tr>
				<tr>
					<td colspan="9" rowspan="2" class="title">GRAPPLE</td>
					<td/>
					<td colspan="4" rowspan="3" class="data-value-important">
						<xsl:value-of select="Attacks/Attack[@type='Grapple']/@total"/>
					</td>
					<td rowspan="3" class="equation">=</td>
					<td colspan="4" rowspan="3" class="data-value">
						<xsl:value-of select="Attacks/@base"/>
					</td>
					<td rowspan="3" class="equation">+</td>
					<td colspan="4" rowspan="3" class="data-value">
						<xsl:call-template name="ability-mod">
							<xsl:with-param name="ability" select="AbilityScores/AbilityScore[@type='Strength']"/>
						</xsl:call-template>
					</td>
					<td rowspan="3" class="equation">+</td>
					<td colspan="4" rowspan="3" class="data-value">
						<xsl:value-of select="Attacks/@size-modifier"/>
					</td>
					<td rowspan="3" class="equation">+</td>
					<td colspan="4" rowspan="3" class="data-value">
						<xsl:value-of select="Attacks/Attack[@type='Grapple']/@misc"/>
					</td>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
				</tr>
				<tr>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<xsl:call-template name="skill">
						<xsl:with-param name="skill" select="Skills/Skill[15]"/>
					</xsl:call-template>
				</tr>
				<tr>
					<td colspan="9" class="subtitle">MODIFIER</td>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
				</tr>
				<tr>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td colspan="4" rowspan="2" class="data-box-desc-total-over">TOTAL</td>
					<td/>
					<td colspan="4" rowspan="2" class="data-box-desc-under">BASE ATTACK</td>
					<td/>
					<td colspan="4" rowspan="2" class="data-box-desc-under">STR MODIFIER</td>
					<td/>
					<td colspan="4" rowspan="2" class="data-box-desc-under">SIZE MODIFIER</td>
					<td/>
					<td colspan="4" rowspan="2" class="data-box-desc-under">MISC. MODIFIER</td>
					<td/>
					<td/>
					<td/>
					<td colspan="12" rowspan="3" class="title-smaller">ACTION POINTS</td>
					<td/>
					<td colspan="4" rowspan="3" class="data-value-important">
						<xsl:value-of select="@action-points"/>
					</td>
					<xsl:call-template name="skill">
						<xsl:with-param name="skill" select="Skills/Skill[16]"/>
					</xsl:call-template>
				</tr>
				<tr>
					<td/>
				</tr>
				<tr>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
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
				<tr>
					<td/><td/><td/><td/><td/><td/><td/><td/><td/><td/>
					<td/><td/><td/><td/><td/><td/><td/><td/><td/><td/>
					<td/><td/><td/><td/><td/><td/><td/><td/><td/><td/>
					<td/><td/><td/><td/><td/><td/><td/><td/><td/><td/>
					<td/><td/><td/><td/><td/><td/><td/><td/><td/><td/>
					<td/><td/><td/><td/>
				</tr>
				<tr>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td colspan="12" rowspan="2" class="data-box-desc-total">TOTAL</td>
					<td/>
					<td colspan="9" rowspan="2" class="data-box-desc" style="vertical-align:bottom">BASE ATTACK BONUS</td>
					<td/>
					<td colspan="4" rowspan="2" class="data-box-desc">STR MODIFIER</td>
					<td/>
					<td colspan="4" rowspan="2" class="data-box-desc">SIZE MODIFIER</td>
					<td/>
					<td colspan="4" rowspan="2" class="data-box-desc">MISC. MODIFIER</td>
					<td/>
					<td colspan="4" rowspan="2" class="data-box-desc">TEMP. MODIFIER</td>
					<xsl:call-template name="skill">
						<xsl:with-param name="skill" select="Skills/Skill[18]"/>
					</xsl:call-template>
				</tr>
				<tr>
					<td/>
				</tr>
				<tr>
					<td colspan="11" rowspan="2" class="title">MELEE</td>
					<td/>
					<td colspan="12" rowspan="3" class="total-attack-bonus" title="Melee Attack" info="{Attacks/Attack[@type='Melee']/@info}" onclick="showInfo(this);">
						<xsl:choose>
							<xsl:when test="Attacks/@total_defense = 'true'">
								<s><xsl:value-of select="Attacks/Attack[@type='Melee']/@attacks"/></s>
							</xsl:when>
							<xsl:otherwise>
								<xsl:value-of select="Attacks/Attack[@type='Melee']/@attacks"/>
							</xsl:otherwise>
						</xsl:choose>
					</td>
					<td rowspan="3" class="equation">=</td>
					<td colspan="9" rowspan="3" class="bab">
						<xsl:value-of select="Attacks/@base"/>
					</td>
					<td rowspan="3" class="equation">+</td>
					<td colspan="4" rowspan="3" class="data-value">
						<xsl:call-template name="ability-mod">
							<xsl:with-param name="ability" select="AbilityScores/AbilityScore[@type='Strength']"/>
						</xsl:call-template>
					</td>
					<td rowspan="3" class="equation">+</td>
					<td colspan="4" rowspan="3" class="data-value">
						<xsl:value-of select="Attacks/@size-modifier"/>
					</td>
					<td rowspan="3" class="equation">+</td>
					<td colspan="4" rowspan="3" class="data-value">
						<xsl:value-of select="Attacks/Attack[@type='Melee']/@misc"/>
					</td>
					<td rowspan="3" class="equation">+</td>
					<td colspan="4" rowspan="3" class="temp-value">
						<xsl:value-of select="Attacks/Attack[@type='Melee']/@temp-modifier"/>
					</td>
					<xsl:call-template name="skill">
						<xsl:with-param name="skill" select="Skills/Skill[19]"/>
					</xsl:call-template>
				</tr>
				<tr>
					<td/>
				</tr>
				<tr>
					<td colspan="11" class="subtitle">
            ATTACK BONUS
					</td>
					<td/>
					<xsl:call-template name="skill">
						<xsl:with-param name="skill" select="Skills/Skill[20]"/>
					</xsl:call-template>
				</tr>
				<tr>
					<td/><td/><td/><td/><td/><td/><td/><td/><td/><td/>
					<td/><td/><td/><td/><td/><td/><td/><td/><td/><td/>
					<td/><td/><td/><td/><td/><td/><td/><td/><td/><td/>
					<td/><td/><td/><td/><td/><td/><td/><td/><td/><td/>
					<td/><td/><td/><td/><td/><td/><td/><td/><td/><td/>
					<td/><td/><td/><td/>
				</tr>
				<tr>
					<td colspan="11" rowspan="2" class="title">RANGED</td>
					<td/>
					<td colspan="12" rowspan="3" class="total-attack-bonus" title="Ranged Attack" info="{Attacks/Attack[@type='Ranged']/@info}" onclick="showInfo(this);">
						<xsl:choose>
							<xsl:when test="Attacks/@total_defense = 'true'">
								<s><xsl:value-of select="Attacks/Attack[@type='Ranged']/@attacks"/></s>
							</xsl:when>
							<xsl:otherwise>
								<xsl:value-of select="Attacks/Attack[@type='Ranged']/@attacks"/>
							</xsl:otherwise>
						</xsl:choose>
					</td>
					<td rowspan="3" class="equation">=</td>
					<td colspan="9" rowspan="3" class="bab">
						<xsl:value-of select="Attacks/@base"/>
					</td>
					<td rowspan="3" class="equation">+</td>
					<td colspan="4" rowspan="3" class="data-value">
						<xsl:call-template name="ability-mod">
							<xsl:with-param name="ability" select="AbilityScores/AbilityScore[@type='Dexterity']"/>
						</xsl:call-template>
					</td>
					<td rowspan="3" class="equation">+</td>
					<td colspan="4" rowspan="3" class="data-value">
						<xsl:value-of select="Attacks/@size-modifier"/>
					</td>
					<td rowspan="3" class="equation">+</td>
					<td colspan="4" rowspan="3" class="data-value">
						<xsl:value-of select="Attacks/Attack[@type='Ranged']/@misc"/>
					</td>
					<td rowspan="3" class="equation">+</td>
					<td colspan="4" rowspan="3" class="temp-value">
						<xsl:value-of select="Attacks/Attack[@type='Ranged']/@temp-modifier"/>
					</td>
					<xsl:call-template name="skill">
						<xsl:with-param name="skill" select="Skills/Skill[21]"/>
					</xsl:call-template>
				</tr>
				<tr>
					<td/>
					<td/>
					<td/>
				</tr>
				<tr>
					<td colspan="11" class="subtitle">ATTACK BONUS</td>
					<td/>
					<xsl:call-template name="skill">
						<xsl:with-param name="skill" select="Skills/Skill[22]"/>
					</xsl:call-template>
				</tr>
				<tr>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td colspan="12" rowspan="2" class="data-box-desc-total-over">TOTAL</td>
					<td/>
					<td colspan="9" rowspan="2" class="data-box-desc-under">BASE ATTACK BONUS</td>
					<td/>
					<td colspan="4" rowspan="2" class="data-box-desc-under">DEX MODIFIER</td>
					<td/>
					<td colspan="4" rowspan="2" class="data-box-desc-under">SIZE MODIFIER</td>
					<td/>
					<td colspan="4" rowspan="2" class="data-box-desc-under">MISC. MODIFIER</td>
					<td/>
					<td colspan="4" rowspan="2" class="data-box-desc-under">TEMP. MODIFIER</td>
					<td/>
					<td/>
				</tr>
				<tr>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
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
				<tr>
					<td/>
				</tr>
				<tr>
					<td colspan="54">&nbsp;</td>
					<xsl:call-template name="skill">
						<xsl:with-param name="skill" select="Skills/Skill[24]"/>
					</xsl:call-template>
				</tr>
				<tr>
					<td/>
				</tr>
				<tr>
					<td colspan="17" rowspan="2" class="title">WEAPON</td>
					<td colspan="37"/>
					<xsl:call-template name="skill">
						<xsl:with-param name="skill" select="Skills/Skill[25]"/>
					</xsl:call-template>
				</tr>
				<tr>
					<td colspan="15" class="weapon-subtitle">TOTAL ATTACK BONUS</td>
					<td colspan="13" class="weapon-subtitle">DAMAGE</td>
					<td colspan="9" class="weapon-subtitle">CRITICAL</td>
					<td/>
					<td/>
				</tr>
				<tr>
					<xsl:call-template name="weapon">
						<xsl:with-param name="weapon" select="Attacks/AttackForm[1]"/>
					</xsl:call-template>
					<xsl:call-template name="skill">
						<xsl:with-param name="skill" select="Skills/Skill[26]"/>
					</xsl:call-template>
				</tr>
				<tr>
					<td/>
					<td/>
				</tr>
				<tr>
					<xsl:call-template name="skill">
						<xsl:with-param name="skill" select="Skills/Skill[27]"/>
					</xsl:call-template>
				</tr>
				<tr>
					<td colspan="5" class="weapon-subtitle">RANGE</td>
					<td colspan="5" class="weapon-subtitle">WEIGHT</td>
					<td colspan="9" class="weapon-subtitle">TYPE</td>
					<td colspan="6" class="weapon-subtitle">SIZE</td>
					<td colspan="29" class="weapon-subtitle">SPECIAL PROPERTIES</td>
					<td/>
					<td/>
				</tr>
				<tr>
					<xsl:call-template name="weapon2">
						<xsl:with-param name="weapon" select="Attacks/AttackForm[1]"/>
					</xsl:call-template>
					<xsl:call-template name="skill">
						<xsl:with-param name="skill" select="Skills/Skill[28]"/>
					</xsl:call-template>
				</tr>
				<tr>
					<td style="border-right:2px solid black;"></td>
					<td/>
					<td/>
				</tr>
				<tr>
					<td style="border-right:2px solid black; border-bottom:2px solid black;"></td>
					<xsl:call-template name="skill">
						<xsl:with-param name="skill" select="Skills/Skill[29]"/>
					</xsl:call-template>
				</tr>
				<tr>
					<td colspan="9" rowspan="2" class="weapon-ammo-label">AMMUNITION</td>
					<td colspan="21" rowspan="2" class="weapon-ammo-desc">
						<xsl:value-of select="Attacks/AttackForm[1]/@ammunition"/>
					</td>
					<td/>
					<td rowspan="2" class="checkbox">&#x2610;</td>
					<td rowspan="2" class="checkbox">&#x2610;</td>
					<td rowspan="2" class="checkbox">&#x2610;</td>
					<td rowspan="2" class="checkbox">&#x2610;</td>
					<td rowspan="2" class="checkbox">&#x2610;</td>
					<td/>
					<td rowspan="2" class="checkbox">&#x2610;</td>
					<td rowspan="2" class="checkbox">&#x2610;</td>
					<td rowspan="2" class="checkbox">&#x2610;</td>
					<td rowspan="2" class="checkbox">&#x2610;</td>
					<td rowspan="2" class="checkbox">&#x2610;</td>
					<td/>
					<td rowspan="2" class="checkbox">&#x2610;</td>
					<td rowspan="2" class="checkbox">&#x2610;</td>
					<td rowspan="2" class="checkbox">&#x2610;</td>
					<td rowspan="2" class="checkbox">&#x2610;</td>
					<td rowspan="2" class="checkbox">&#x2610;</td>
					<td/>
					<td rowspan="2" class="checkbox">&#x2610;</td>
					<td rowspan="2" class="checkbox">&#x2610;</td>
					<td rowspan="2" class="checkbox">&#x2610;</td>
					<td rowspan="2" class="checkbox">&#x2610;</td>
					<td rowspan="2" class="checkbox">&#x2610;</td>
				</tr>
				<tr>
					<td/>
					<td/>
					<td/>
					<td/>
					<xsl:call-template name="skill">
						<xsl:with-param name="skill" select="Skills/Skill[30]"/>
					</xsl:call-template>
				</tr>
				<tr>
					<td/>
				</tr>
				<tr>
					<td colspan="17" rowspan="2" class="title">WEAPON</td>
					<td colspan="37"/>
					<xsl:call-template name="skill">
						<xsl:with-param name="skill" select="Skills/Skill[31]"/>
					</xsl:call-template>
				</tr>
				<tr>
					<td colspan="15" class="weapon-subtitle">TOTAL ATTACK BONUS</td>
					<td colspan="13" class="weapon-subtitle">DAMAGE</td>
					<td colspan="9" class="weapon-subtitle">CRITICAL</td>
					<td/>
					<td/>
				</tr>
				<tr>
					<xsl:call-template name="weapon">
						<xsl:with-param name="weapon" select="Attacks/AttackForm[2]"/>
					</xsl:call-template>
					<xsl:call-template name="skill">
						<xsl:with-param name="skill" select="Skills/Skill[32]"/>
					</xsl:call-template>
				</tr>
				<tr>
					<td/>
					<td/>
				</tr>
				<tr>
					<xsl:call-template name="skill">
						<xsl:with-param name="skill" select="Skills/Skill[33]"/>
					</xsl:call-template>
				</tr>
				<tr>
					<td colspan="5" class="weapon-subtitle">RANGE</td>
					<td colspan="5" class="weapon-subtitle">WEIGHT</td>
					<td colspan="9" class="weapon-subtitle">TYPE</td>
					<td colspan="6" class="weapon-subtitle">SIZE</td>
					<td colspan="29" class="weapon-subtitle">SPECIAL PROPERTIES</td>
					<td/>
					<td/>
				</tr>
				<tr>
					<xsl:call-template name="weapon2">
						<xsl:with-param name="weapon" select="Attacks/AttackForm[2]"/>
					</xsl:call-template>
					<xsl:call-template name="skill">
						<xsl:with-param name="skill" select="Skills/Skill[34]"/>
					</xsl:call-template>
				</tr>
				<tr>
					<td style="border-right:2px solid black;"></td>
					<td/>
					<td/>
				</tr>
				<tr>
					<td style="border-right:2px solid black; border-bottom:2px solid black;"></td>
					<xsl:call-template name="skill">
						<xsl:with-param name="skill" select="Skills/Skill[35]"/>
					</xsl:call-template>
				</tr>
				<tr>
					<td colspan="9" rowspan="2" class="weapon-ammo-label">AMMUNITION</td>
					<td colspan="21" rowspan="2" class="weapon-ammo-desc">
						<xsl:value-of select="Attacks/AttackForm[2]/@ammunition"/>
					</td>
					<td/>
					<td rowspan="2" class="checkbox">&#x2610;</td>
					<td rowspan="2" class="checkbox">&#x2610;</td>
					<td rowspan="2" class="checkbox">&#x2610;</td>
					<td rowspan="2" class="checkbox">&#x2610;</td>
					<td rowspan="2" class="checkbox">&#x2610;</td>
					<td/>
					<td rowspan="2" class="checkbox">&#x2610;</td>
					<td rowspan="2" class="checkbox">&#x2610;</td>
					<td rowspan="2" class="checkbox">&#x2610;</td>
					<td rowspan="2" class="checkbox">&#x2610;</td>
					<td rowspan="2" class="checkbox">&#x2610;</td>
					<td/>
					<td rowspan="2" class="checkbox">&#x2610;</td>
					<td rowspan="2" class="checkbox">&#x2610;</td>
					<td rowspan="2" class="checkbox">&#x2610;</td>
					<td rowspan="2" class="checkbox">&#x2610;</td>
					<td rowspan="2" class="checkbox">&#x2610;</td>
					<td/>
					<td rowspan="2" class="checkbox">&#x2610;</td>
					<td rowspan="2" class="checkbox">&#x2610;</td>
					<td rowspan="2" class="checkbox">&#x2610;</td>
					<td rowspan="2" class="checkbox">&#x2610;</td>
					<td rowspan="2" class="checkbox">&#x2610;</td>
					<td/>
					<td/>
				</tr>
				<tr>
					<td/>
					<td/>
					<td/>
					<td/>
					<xsl:call-template name="skill">
						<xsl:with-param name="skill" select="Skills/Skill[36]"/>
					</xsl:call-template>
				</tr>
        <tr>
          <td/>
        </tr>
        <tr>
          <td colspan="17" rowspan="2" class="title">WEAPON</td>
          <td colspan="37"/>
          <xsl:call-template name="skill">
						<xsl:with-param name="skill" select="Skills/Skill[37]"/>
					</xsl:call-template>
				</tr>
				<tr>
					<td colspan="15" class="weapon-subtitle">TOTAL ATTACK BONUS</td>
					<td colspan="13" class="weapon-subtitle">DAMAGE</td>
					<td colspan="9" class="weapon-subtitle">CRITICAL</td>
					<td/>
					<td/>
				</tr>
				<tr>
					<xsl:call-template name="weapon">
						<xsl:with-param name="weapon" select="Attacks/AttackForm[3]"/>
					</xsl:call-template>
					<xsl:call-template name="skill">
						<xsl:with-param name="skill" select="Skills/Skill[38]"/>
					</xsl:call-template>
				</tr>
				<tr>
					<td/>
					<td/>
				</tr>
				<tr>
					<xsl:call-template name="skill">
						<xsl:with-param name="skill" select="Skills/Skill[39]"/>
					</xsl:call-template>
				</tr>
				<tr>
					<td colspan="5" class="weapon-subtitle">RANGE</td>
					<td colspan="5" class="weapon-subtitle">WEIGHT</td>
					<td colspan="9" class="weapon-subtitle">TYPE</td>
					<td colspan="6" class="weapon-subtitle">SIZE</td>
					<td colspan="29" class="weapon-subtitle">SPECIAL PROPERTIES</td>
					<td/>
					<td/>
				</tr>
				<tr>
					<xsl:call-template name="weapon2">
						<xsl:with-param name="weapon" select="Attacks/AttackForm[3]"/>
					</xsl:call-template>
					<xsl:call-template name="skill">
						<xsl:with-param name="skill" select="Skills/Skill[40]"/>
					</xsl:call-template>
				</tr>
				<tr>
					<td style="border-right:2px solid black;"></td>
					<td/>
					<td/>
				</tr>
				<tr>
					<td style="border-right:2px solid black; border-bottom:2px solid black;"></td>
					<xsl:call-template name="skill">
						<xsl:with-param name="skill" select="Skills/Skill[41]"/>
					</xsl:call-template>
				</tr>
				<tr>
					<td colspan="9" rowspan="2" class="weapon-ammo-label">AMMUNITION</td>
					<td colspan="21" rowspan="2" class="weapon-ammo-desc">
						<xsl:value-of select="Attacks/AttackForm[3]/@ammunition"/>
					</td>
					<td/>
					<td rowspan="2" class="checkbox">&#x2610;</td>
					<td rowspan="2" class="checkbox">&#x2610;</td>
					<td rowspan="2" class="checkbox">&#x2610;</td>
					<td rowspan="2" class="checkbox">&#x2610;</td>
					<td rowspan="2" class="checkbox">&#x2610;</td>
					<td/>
					<td rowspan="2" class="checkbox">&#x2610;</td>
					<td rowspan="2" class="checkbox">&#x2610;</td>
					<td rowspan="2" class="checkbox">&#x2610;</td>
					<td rowspan="2" class="checkbox">&#x2610;</td>
					<td rowspan="2" class="checkbox">&#x2610;</td>
					<td/>
					<td rowspan="2" class="checkbox">&#x2610;</td>
					<td rowspan="2" class="checkbox">&#x2610;</td>
					<td rowspan="2" class="checkbox">&#x2610;</td>
					<td rowspan="2" class="checkbox">&#x2610;</td>
					<td rowspan="2" class="checkbox">&#x2610;</td>
					<td/>
					<td rowspan="2" class="checkbox">&#x2610;</td>
					<td rowspan="2" class="checkbox">&#x2610;</td>
					<td rowspan="2" class="checkbox">&#x2610;</td>
					<td rowspan="2" class="checkbox">&#x2610;</td>
					<td rowspan="2" class="checkbox">&#x2610;</td>
					<td/>
					<td/>
				</tr>
				<tr>
					<td/>
					<td/>
					<td/>
					<td/>
					<xsl:call-template name="skill">
						<xsl:with-param name="skill" select="Skills/Skill[42]"/>
					</xsl:call-template>
				</tr>
        <tr>
          <td/>
        </tr>
        <tr>
          <td colspan="17" rowspan="2" class="title">WEAPON</td>
          <td colspan="37"/>
          <xsl:call-template name="skill">
						<xsl:with-param name="skill" select="Skills/Skill[43]"/>
					</xsl:call-template>
				</tr>
				<tr>
					<td colspan="15" class="weapon-subtitle">TOTAL ATTACK BONUS</td>
					<td colspan="13" class="weapon-subtitle">DAMAGE</td>
					<td colspan="9" class="weapon-subtitle">CRITICAL</td>
					<td/>
					<td/>
				</tr>
				<tr>
					<xsl:call-template name="weapon">
						<xsl:with-param name="weapon" select="Attacks/AttackForm[4]"/>
					</xsl:call-template>
					<xsl:call-template name="skill">
						<xsl:with-param name="skill" select="Skills/Skill[44]"/>
					</xsl:call-template>
				</tr>
				<tr>
					<td/>
					<td/>
				</tr>
				<tr>
					<xsl:call-template name="skill">
						<xsl:with-param name="skill" select="Skills/Skill[45]"/>
					</xsl:call-template>
				</tr>
				<tr>
					<td colspan="5" class="weapon-subtitle">RANGE</td>
					<td colspan="5" class="weapon-subtitle">WEIGHT</td>
					<td colspan="9" class="weapon-subtitle">TYPE</td>
					<td colspan="6" class="weapon-subtitle">SIZE</td>
					<td colspan="29" class="weapon-subtitle">SPECIAL PROPERTIES</td>
					<td/>
					<td/>
				</tr>
				<tr>
					<xsl:call-template name="weapon2">
						<xsl:with-param name="weapon" select="Attacks/AttackForm[4]"/>
					</xsl:call-template>
					<xsl:call-template name="skill">
						<xsl:with-param name="skill" select="Skills/Skill[46]"/>
					</xsl:call-template>
				</tr>
				<tr>
					<td style="border-right:2px solid black;"></td>
					<td/>
					<td/>
				</tr>
				<tr>
					<td style="border-right:2px solid black;"></td>
					<xsl:call-template name="skill">
						<xsl:with-param name="skill" select="Skills/Skill[47]"/>
					</xsl:call-template>
				</tr>
				<tr>
					<td colspan="9" rowspan="2" class="weapon-ammo-label">AMMUNITION</td>
					<td colspan="21" rowspan="2" class="weapon-ammo-desc">
						<xsl:value-of select="Attacks/AttackForm[4]/@ammunition"/>
					</td>
					<td/>
					<td rowspan="2" class="checkbox">&#x2610;</td>
					<td rowspan="2" class="checkbox">&#x2610;</td>
					<td rowspan="2" class="checkbox">&#x2610;</td>
					<td rowspan="2" class="checkbox">&#x2610;</td>
					<td rowspan="2" class="checkbox">&#x2610;</td>
					<td/>
					<td rowspan="2" class="checkbox">&#x2610;</td>
					<td rowspan="2" class="checkbox">&#x2610;</td>
					<td rowspan="2" class="checkbox">&#x2610;</td>
					<td rowspan="2" class="checkbox">&#x2610;</td>
					<td rowspan="2" class="checkbox">&#x2610;</td>
					<td/>
					<td rowspan="2" class="checkbox">&#x2610;</td>
					<td rowspan="2" class="checkbox">&#x2610;</td>
					<td rowspan="2" class="checkbox">&#x2610;</td>
					<td rowspan="2" class="checkbox">&#x2610;</td>
					<td rowspan="2" class="checkbox">&#x2610;</td>
					<td/>
					<td rowspan="2" class="checkbox">&#x2610;</td>
					<td rowspan="2" class="checkbox">&#x2610;</td>
					<td rowspan="2" class="checkbox">&#x2610;</td>
					<td rowspan="2" class="checkbox">&#x2610;</td>
					<td rowspan="2" class="checkbox">&#x2610;</td>
					<td/>
					<td/>
				</tr>
				<tr>
					<td/>
					<td/>
					<td/>
					<td/>
					<xsl:call-template name="skill">
						<xsl:with-param name="skill" select="Skills/Skill[48]"/>
					</xsl:call-template>
				</tr>
        <tr>
          <td/>
        </tr>
        <tr>
          <td colspan="17" rowspan="2" class="title">WEAPON</td>
          <td colspan="37"/>
          <xsl:call-template name="skill">
						<xsl:with-param name="skill" select="Skills/Skill[49]"/>
					</xsl:call-template>
				</tr>
				<tr>
					<td colspan="15" class="weapon-subtitle">TOTAL ATTACK BONUS</td>
					<td colspan="13" class="weapon-subtitle">DAMAGE</td>
					<td colspan="9" class="weapon-subtitle">CRITICAL</td>
					<td/>
					<td/>
				</tr>
				<tr>
					<xsl:call-template name="weapon">
						<xsl:with-param name="weapon" select="Attacks/AttackForm[5]"/>
					</xsl:call-template>
					<xsl:call-template name="skill">
						<xsl:with-param name="skill" select="Skills/Skill[50]"/>
					</xsl:call-template>
				</tr>
				<tr>
					<td/>
					<td/>
				</tr>
				<tr>
					<xsl:call-template name="skill">
						<xsl:with-param name="skill" select="Skills/Skill[51]"/>
					</xsl:call-template>
				</tr>
				<tr>
					<td colspan="5" class="weapon-subtitle">RANGE</td>
					<td colspan="5" class="weapon-subtitle">WEIGHT</td>
					<td colspan="9" class="weapon-subtitle">TYPE</td>
					<td colspan="6" class="weapon-subtitle">SIZE</td>
					<td colspan="29" class="weapon-subtitle">SPECIAL PROPERTIES</td>
					<td/>
					<td/>
				</tr>
				<tr>
					<xsl:call-template name="weapon2">
						<xsl:with-param name="weapon" select="Attacks/AttackForm[5]"/>
					</xsl:call-template>
					<xsl:call-template name="skill">
						<xsl:with-param name="skill" select="Skills/Skill[52]"/>
					</xsl:call-template>
				</tr>
				<tr>
					<td style="border-right:2px solid black;"></td>
					<td/>
					<td/>
				</tr>
				<tr>
					<td style="border-right:2px solid black; border-bottom:2px solid black;"></td>
					<xsl:call-template name="skill">
						<xsl:with-param name="skill" select="Skills/Skill[53]"/>
					</xsl:call-template>
				</tr>
				<tr>
					<td colspan="9" rowspan="2" class="weapon-ammo-label">AMMUNITION</td>
					<td colspan="21" rowspan="2" class="weapon-ammo-desc">
						<xsl:value-of select="Attacks/AttackForm[5]/@ammunition"/>
					</td>
					<td/>
					<td rowspan="2" class="checkbox">&#x2610;</td>
					<td rowspan="2" class="checkbox">&#x2610;</td>
					<td rowspan="2" class="checkbox">&#x2610;</td>
					<td rowspan="2" class="checkbox">&#x2610;</td>
					<td rowspan="2" class="checkbox">&#x2610;</td>
					<td/>
					<td rowspan="2" class="checkbox">&#x2610;</td>
					<td rowspan="2" class="checkbox">&#x2610;</td>
					<td rowspan="2" class="checkbox">&#x2610;</td>
					<td rowspan="2" class="checkbox">&#x2610;</td>
					<td rowspan="2" class="checkbox">&#x2610;</td>
					<td/>
					<td rowspan="2" class="checkbox">&#x2610;</td>
					<td rowspan="2" class="checkbox">&#x2610;</td>
					<td rowspan="2" class="checkbox">&#x2610;</td>
					<td rowspan="2" class="checkbox">&#x2610;</td>
					<td rowspan="2" class="checkbox">&#x2610;</td>
					<td/>
					<td rowspan="2" class="checkbox">&#x2610;</td>
					<td rowspan="2" class="checkbox">&#x2610;</td>
					<td rowspan="2" class="checkbox">&#x2610;</td>
					<td rowspan="2" class="checkbox">&#x2610;</td>
					<td rowspan="2" class="checkbox">&#x2610;</td>
					<td/>
					<td/>
				</tr>
				<tr>
					<td/>
					<td/>
					<td/>
					<td/>
					<xsl:call-template name="skill">
						<xsl:with-param name="skill" select="Skills/Skill[54]"/>
					</xsl:call-template>
				</tr>
        <tr>
          <td/>
        </tr>
        <tr>
          <td colspan="17" rowspan="2" class="title">WEAPON</td>
          <td colspan="37"/>
          <xsl:call-template name="skill">
						<xsl:with-param name="skill" select="Skills/Skill[55]"/>
					</xsl:call-template>
				</tr>
				<tr>
					<td colspan="15" class="weapon-subtitle">TOTAL ATTACK BONUS</td>
					<td colspan="13" class="weapon-subtitle">DAMAGE</td>
					<td colspan="9" class="weapon-subtitle">CRITICAL</td>
					<td/>
					<td/>
				</tr>
				<tr>
					<xsl:call-template name="weapon">
						<xsl:with-param name="weapon" select="Attacks/AttackForm[6]"/>
					</xsl:call-template>
					<xsl:call-template name="skill">
						<xsl:with-param name="skill" select="Skills/Skill[56]"/>
					</xsl:call-template>
				</tr>
				<tr>
					<td/>
					<td/>
				</tr>
				<tr>
					<xsl:call-template name="skill">
						<xsl:with-param name="skill" select="Skills/Skill[57]"/>
					</xsl:call-template>
				</tr>
				<tr>
					<td colspan="5" class="weapon-subtitle">RANGE</td>
					<td colspan="5" class="weapon-subtitle">WEIGHT</td>
					<td colspan="9" class="weapon-subtitle">TYPE</td>
					<td colspan="6" class="weapon-subtitle">SIZE</td>
					<td colspan="29" class="weapon-subtitle">SPECIAL PROPERTIES</td>
					<td/>
					<td/>
				</tr>
				<tr>
					<xsl:call-template name="weapon2">
						<xsl:with-param name="weapon" select="Attacks/AttackForm[6]"/>
					</xsl:call-template>
					<td/>
					<td/>
					<td/>
					<td/>
					<td colspan="40" rowspan="5" class="skills-notes">
            Skills marked with <sup>1</sup> can be used normally even if the character has zero ranks.<br />
            Skills marked with <span style="font-size:1.1428em;">&#x2612;</span> are cross skills.<br />
            * ARMOR CHECK PENALTY applies.&nbsp;&nbsp;&nbsp;&nbsp;** Twice ARMOR CHECK PENALTY applies.
					</td>
				</tr>
				<tr>
					<td style="border-right:2px solid black;"></td>
					<td/>
					<td/>
					<td/>
					<td/>
				</tr>
				<tr>
					<td style="border-right:2px solid black; border-bottom:2px solid black;"></td>
					<td/>
					<td/>
					<td/>
					<td/>
				</tr>
				<tr>
					<td colspan="9" rowspan="2" class="weapon-ammo-label">AMMUNITION</td>
					<td colspan="21" rowspan="2" class="weapon-ammo-desc">
						<xsl:value-of select="Attacks/AttackForm[6]/@ammunition"/>
					</td>
					<td/>
					<td rowspan="2" class="checkbox">&#x2610;</td>
					<td rowspan="2" class="checkbox">&#x2610;</td>
					<td rowspan="2" class="checkbox">&#x2610;</td>
					<td rowspan="2" class="checkbox">&#x2610;</td>
					<td rowspan="2" class="checkbox">&#x2610;</td>
					<td/>
					<td rowspan="2" class="checkbox">&#x2610;</td>
					<td rowspan="2" class="checkbox">&#x2610;</td>
					<td rowspan="2" class="checkbox">&#x2610;</td>
					<td rowspan="2" class="checkbox">&#x2610;</td>
					<td rowspan="2" class="checkbox">&#x2610;</td>
					<td/>
					<td rowspan="2" class="checkbox">&#x2610;</td>
					<td rowspan="2" class="checkbox">&#x2610;</td>
					<td rowspan="2" class="checkbox">&#x2610;</td>
					<td rowspan="2" class="checkbox">&#x2610;</td>
					<td rowspan="2" class="checkbox">&#x2610;</td>
					<td/>
					<td rowspan="2" class="checkbox">&#x2610;</td>
					<td rowspan="2" class="checkbox">&#x2610;</td>
					<td rowspan="2" class="checkbox">&#x2610;</td>
					<td rowspan="2" class="checkbox">&#x2610;</td>
					<td rowspan="2" class="checkbox">&#x2610;</td>
					<td/>
					<td/>
					<td/>
					<td/>
				</tr>
				<tr>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
					<td/>
				</tr>
			</table>
		</div>

</xsl:template>
</xsl:stylesheet>
