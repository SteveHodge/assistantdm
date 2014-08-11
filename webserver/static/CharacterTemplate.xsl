<?xml version="1.0" encoding="ISO-8859-1"?>

<!DOCTYPE stylesheet [
<!ENTITY nbsp  "&#160;" >
]>

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	<xsl:output method="html" encoding="utf-8" indent="yes" />


<xsl:template name="weapon">
	<xsl:param name="weapon"/>

	<table style="width:100%;height:100%;">
		<tr>
			<td colspan="17" rowspan="2" class="title">WEAPON</td>
			<td colspan="37"/>
		</tr>
		<tr>
			<td colspan="15" class="weapon-subtitle">TOTAL ATTACK BONUS</td>
			<td colspan="13" class="weapon-subtitle">DAMAGE</td>
			<td colspan="9" class="weapon-subtitle">CRITICAL</td>
		</tr>
		<tr style="height:3.84em;">
			<td colspan="17" class="weapon-name">
				<xsl:value-of select="$weapon/@name"/>
			</td>
			<td colspan="15" class="data-value-important" title="{$weapon/@name}" info="{$weapon/@info}" onclick="showInfo(this);">
				<xsl:choose>
					<xsl:when test="Attacks/@total_defense = 'true'">
						<s><xsl:value-of select="$weapon/@attacks"/></s>
					</xsl:when>
					<xsl:otherwise>
						<xsl:value-of select="$weapon/@attacks"/>
					</xsl:otherwise>
				</xsl:choose>
			</td>
			<td colspan="13" class="data-value" title="{$weapon/@name}" info="{$weapon/@damage_info}" onclick="showInfo(this);">
					<xsl:value-of select="$weapon/@damage"/>
			</td>
			<td colspan="9" class="data-value">
				<xsl:value-of select="$weapon/@critical"/>
			</td>
		</tr>
		<tr>
			<td colspan="5" class="weapon-subtitle">RANGE</td>
			<td colspan="5" class="weapon-subtitle">WEIGHT</td>
			<td colspan="9" class="weapon-subtitle">TYPE</td>
			<td colspan="6" class="weapon-subtitle">SIZE</td>
			<td colspan="29" class="weapon-subtitle">SPECIAL PROPERTIES</td>
		</tr>
		<tr style="height:3.84em;">
			<td colspan="5" class="data-value">
				<xsl:if test="not($weapon/@range)">0</xsl:if>
				<xsl:value-of select="$weapon/@range"/>
				ft
			</td>
			<td colspan="5" class="data-value">
				<xsl:if test="not($weapon/@weight)">0</xsl:if>
				<xsl:value-of select="$weapon/@weight"/>
				lb
			</td>
			<td colspan="9" class="data-value">
				<xsl:value-of select="$weapon/@type"/>
			</td>
			<td colspan="6" class="data-value">
				<xsl:value-of select="$weapon/@size"/>
			</td>
			<td colspan="29" class="data-value">
				<xsl:value-of select="$weapon/@properties"/>
			</td>
		</tr>
		<tr style="height:2.56em;">
			<td colspan="9" class="weapon-ammo-label">AMMUNITION</td>
			<td colspan="21" class="weapon-ammo-desc">
			<xsl:value-of select="Attacks/AttackForm[6]/@ammunition"/>
			</td>
			<td/>
			<td class="symbol">&#x2610;</td>
			<td class="symbol">&#x2610;</td>
			<td class="symbol">&#x2610;</td>
			<td class="symbol">&#x2610;</td>
			<td class="symbol">&#x2610;</td>
			<td/>
			<td class="symbol">&#x2610;</td>
			<td class="symbol">&#x2610;</td>
			<td class="symbol">&#x2610;</td>
			<td class="symbol">&#x2610;</td>
			<td class="symbol">&#x2610;</td>
			<td/>
			<td class="symbol">&#x2610;</td>
			<td class="symbol">&#x2610;</td>
			<td class="symbol">&#x2610;</td>
			<td class="symbol">&#x2610;</td>
			<td class="symbol">&#x2610;</td>
			<td/>
			<td class="symbol">&#x2610;</td>
			<td class="symbol">&#x2610;</td>
			<td class="symbol">&#x2610;</td>
			<td class="symbol">&#x2610;</td>
			<td class="symbol">&#x2610;</td>
		</tr>
	</table>
</xsl:template>

<xsl:template name="ability">
	<xsl:param name="ability"/>
	<xsl:param name="abbr"/>
	<xsl:param name="fullname"/>

	<tr style="height:2.56em;">
		<td class="title"><xsl:value-of select="$abbr"/></td>
		<td/>
		<td rowspan="2" class="data-value-important" title="{$ability/@type}" info="{$ability/@info}" onclick="showInfo(this);">
			<xsl:value-of select="$ability/@total"/>
		</td>
		<td/>
		<td rowspan="2" class="data-value-important"><xsl:value-of select="$ability/@modifier"/></td>
		<td/>
		<td rowspan="2" class="temp-value"><xsl:value-of select="$ability/@temp"/></td>
		<td/>
		<td rowspan="2" class="temp-value"><xsl:value-of select="$ability/@temp-modifier"/></td>
	</tr>
	<tr>
		<td class="subtitle"><xsl:value-of select="$fullname"/></td>
		<td/>
		<td/>
		<td/>
		<td/>
	</tr>
</xsl:template>

<xsl:template name="abilities">
	<table style="width:100%;height:100%;">
		<col style="width:8.64em;"/><col style="width:1.44em;"/>
		<col style="width:5.76em;"/><col style="width:1.44em;"/>
		<col style="width:5.76em;"/><col style="width:1.44em;"/>
		<col style="width:5.76em;"/><col style="width:1.44em;"/>
		<col style="width:5.76em;"/>

		<tr style="height:2.56em;">
			<td class="label">ABILITY NAME</td>
			<td/>
			<td class="label">ABILITY SCORE</td>
			<td/>
			<td class="label">ABILITY MODIFIER</td>
			<td/>
			<td class="label">TEMP. SCORE</td>
			<td/>
			<td class="label">TEMP. MODIFIER</td>
		</tr>
		<xsl:call-template name="ability">
			<xsl:with-param name="ability" select="AbilityScores/AbilityScore[@type='Strength']"/>
			<xsl:with-param name="abbr" select="'STR'"/>
			<xsl:with-param name="fullname" select="'STRENGTH'"/>
		</xsl:call-template>
		<tr><td/></tr>
		<xsl:call-template name="ability">
			<xsl:with-param name="ability" select="AbilityScores/AbilityScore[@type='Dexterity']"/>
			<xsl:with-param name="abbr" select="'DEX'"/>
			<xsl:with-param name="fullname" select="'DEXTERITY'"/>
		</xsl:call-template>
		<tr><td/></tr>
		<xsl:call-template name="ability">
			<xsl:with-param name="ability" select="AbilityScores/AbilityScore[@type='Constitution']"/>
			<xsl:with-param name="abbr" select="'CON'"/>
			<xsl:with-param name="fullname" select="'CONSTITUTION'"/>
		</xsl:call-template>
		<tr><td/></tr>
		<xsl:call-template name="ability">
			<xsl:with-param name="ability" select="AbilityScores/AbilityScore[@type='Intelligence']"/>
			<xsl:with-param name="abbr" select="'INT'"/>
			<xsl:with-param name="fullname" select="'INTELLIGENCE'"/>
		</xsl:call-template>
		<tr><td/></tr>
		<xsl:call-template name="ability">
			<xsl:with-param name="ability" select="AbilityScores/AbilityScore[@type='Wisdom']"/>
			<xsl:with-param name="abbr" select="'WIS'"/>
			<xsl:with-param name="fullname" select="'WISDOM'"/>
		</xsl:call-template>
		<tr><td/></tr>
		<xsl:call-template name="ability">
			<xsl:with-param name="ability" select="AbilityScores/AbilityScore[@type='Charisma']"/>
			<xsl:with-param name="abbr" select="'CHA'"/>
			<xsl:with-param name="fullname" select="'CHARISMA'"/>
		</xsl:call-template>
	</table>
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

	<td colspan="4" rowspan="2" class="data-value-important" title="{$save/@type}" info="{$save/@info}" onclick="showInfo(this);">
		<xsl:value-of select="$save/@total"/>
	</td>
	<td rowspan="2" class="symbol">=</td>
	<td colspan="4" rowspan="2" class="data-value">
		<xsl:value-of select="$save/@base"/>
	</td>
	<td rowspan="2" class="symbol">+</td>
	<td colspan="4" rowspan="2" class="data-value">
		<xsl:call-template name="ability-mod">
			<xsl:with-param name="ability" select="$ability"/>
		</xsl:call-template>
	</td>
	<td rowspan="2" class="symbol">+</td>
	<td colspan="4" rowspan="2" class="data-value">
		<xsl:value-of select="$save/@mods"/>
	</td>
	<td rowspan="2" class="symbol">+</td>
	<td colspan="4" rowspan="2" class="temp-value">
		<xsl:value-of select="$save/@misc"/>
	</td>
</xsl:template>

<xsl:template name="saving-throws">
<table style="width:100%;height:100%;">
	<tr style="height:2.56em;">
		<td colspan="11" class="label-total">SAVING THROWS</td>
		<td/>
		<td colspan="4" class="label-total">TOTAL</td>
		<td/>
		<td colspan="4" class="label">BASE<br /> SAVE</td>
		<td/>
		<td colspan="4" class="label">ABILITY MODIFIER</td>
		<td/>
		<td colspan="4" class="label">MISC. &amp; MAGIC</td>
		<td/>
		<td colspan="4" class="label">TEMP MODIFIER</td>
	</tr>
	<tr style="height:2.56em;">
		<td colspan="11" class="title">FORTITUDE</td>
		<td/>
		<xsl:call-template name="save">
			<xsl:with-param name="save" select="SavingThrows/Save[@type='Fortitude']"/>
			<xsl:with-param name="ability" select="AbilityScores/AbilityScore[@type='Constitution']"/>
		</xsl:call-template>
	</tr>
	<tr>
		<td colspan="11" class="subtitle">(CONSTITUTION)</td>
	</tr>
	<tr>
		<td/><td/><td/><td/><td/><td/><td/><td/><td/><td/>
		<td/><td/><td/><td/><td/><td/><td/><td/><td/><td/>
		<td/><td/><td/><td/><td/><td/><td/><td/><td/><td/>
		<td/><td/><td/><td/><td/><td/>
	</tr>
	<tr style="height:2.56em;">
		<td colspan="11" class="title">REFLEX</td>
		<td/>
		<xsl:call-template name="save">
			<xsl:with-param name="save" select="SavingThrows/Save[@type='Reflex']"/>
			<xsl:with-param name="ability" select="AbilityScores/AbilityScore[@type='Dexterity']"/>
		</xsl:call-template>
	</tr>
	<tr>
		<td colspan="11" class="subtitle">(DEXTERITY)</td>
	</tr>
	<tr>
		<td/><td/><td/><td/><td/><td/><td/><td/><td/><td/>
		<td/><td/><td/><td/><td/><td/><td/><td/><td/><td/>
		<td/><td/><td/><td/><td/><td/><td/><td/><td/><td/>
		<td/><td/><td/><td/><td/><td/>
	</tr>
	<tr style="height:2.56em;">
		<td colspan="11" class="title">WILL</td>
		<td/>
		<xsl:call-template name="save">
			<xsl:with-param name="save" select="SavingThrows/Save[@type='Will']"/>
			<xsl:with-param name="ability" select="AbilityScores/AbilityScore[@type='Wisdom']"/>
		</xsl:call-template>
	</tr>
	<tr>
		<td colspan="11" class="subtitle">(WISDOM)</td>
	</tr>
</table>
</xsl:template>

<xsl:template name="skill">
	<xsl:param name="skill"/>

	<tr style="height:2.56em;">
		<td colspan="2" class="symbol">
			<xsl:choose>
				<xsl:when test="$skill/@cross">&#x2612;</xsl:when>
				<xsl:when test="$skill/@type">&#x2610;</xsl:when>
				<xsl:otherwise></xsl:otherwise>
			</xsl:choose>
		</td>
		<td colspan="16" class="skills-name">
			<xsl:value-of select="$skill/@type"/>
			<xsl:if test="$skill/@untrained = 'true'"><span style="font-size:1.05em;"><sup>1</sup></span></xsl:if>
		</td>
		<td colspan="4" class="skills-ability"><xsl:value-of select="$skill/@ability"/></td>
		<td />
		<td colspan="4" class="skills-total B" title="{$skill/@type}" info="{$skill/@info}" onclick="showInfo(this);"><xsl:value-of select="$skill/@total"/></td>
		<td class="symbol">=</td>
		<td colspan="4" class="skills-data"><xsl:value-of select="$skill/@ability-modifier"/></td>
		<td class="symbol">+</td>
		<td colspan="4" class="skills-data"><xsl:value-of select="$skill/@ranks"/></td>
		<td class="symbol">+</td>
		<xsl:choose>
			<xsl:when test="$skill/@misc != 0">
				<td colspan="4" class="skills-data"><xsl:value-of select="$skill/@misc"/></td>
			</xsl:when><xsl:otherwise>
				<td colspan="4" class="skills-data" style="color:white;">0</td>
			</xsl:otherwise>
		</xsl:choose>
	</tr>
</xsl:template>

<xsl:template name="skillrows">
	<xsl:param name="pStart"/>
	<xsl:param name="pEnd"/>

	<xsl:if test="not($pStart > $pEnd)">
		<xsl:choose>
			<xsl:when test="$pStart = $pEnd">
				<xsl:call-template name="skill">
					<xsl:with-param name="skill" select="Skills/Skill[$pStart]"/>
				</xsl:call-template>
			</xsl:when>
			<xsl:otherwise>
				<xsl:variable name="vMid" select="floor(($pStart + $pEnd) div 2)"/>
				<xsl:call-template name="skillrows">
					<xsl:with-param name="pStart" select="$pStart"/>
					<xsl:with-param name="pEnd" select="$vMid"/>
				</xsl:call-template>
				<xsl:call-template name="skillrows">
					<xsl:with-param name="pStart" select="$vMid+1"/>
					<xsl:with-param name="pEnd" select="$pEnd"/>
				</xsl:call-template>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:if>
</xsl:template>

<xsl:template name="skills">
	<table style="width:100%;height:100%;">
		<tr style="height:2.56em;">
			<td colspan="2" rowspan="2" class="skills-cross-class-heading">
				<div style="transform: translateY(-2.1em) rotate(-90deg); -webkit-transform: translateY(-2.1em) rotate(-90deg);">
					CROSS-CLASS
				</div>
			</td>
			<td colspan="27" class="skills-title">SKILLS</td>
			<td colspan="8" class="skills-max-ranks-heading">MAX RANKS</td>
			<td colspan="5" class="skills-max-ranks-value">9 / 4.5</td>
		</tr>
		<tr style="height:2.56em;">
			<td colspan="16" class="skills-subhead-main">SKILL NAME</td>
			<td colspan="4" class="skills-subhead">KEY ABILITY</td>
			<td/>
			<td colspan="4" class="skills-subhead-main">SKILL MODIFIER</td>
			<td/>
			<td colspan="4" class="skills-subhead">ABILITY MODIFIER</td>
			<td/>
			<td colspan="4" class="skills-subhead-main">RANKS</td>
			<td/>
			<td colspan="4" class="skills-subhead">MISC. MODIFIER</td>
		</tr>
		<tr>
			<td/><td/><td/><td/><td/><td/><td/><td/><td/><td/>
			<td/><td/>
		</tr>
		<xsl:call-template name="skillrows">
			<xsl:with-param name="pStart" select="1"/>
			<xsl:with-param name="pEnd" select="57"/>
		</xsl:call-template>
		<tr style="height:6.4em;">
			<td colspan="42" class="skills-notes">
      Skills marked with <sup>1</sup> can be used normally even if the character has zero ranks.<br />
      Skills marked with <span style="font-size:1.1428em;">&#x2612;</span> are cross skills.<br />
      * ARMOR CHECK PENALTY applies.&nbsp;&nbsp;&nbsp;&nbsp;** Twice ARMOR CHECK PENALTY applies.
			</td>
		</tr>
	</table>
</xsl:template>

<xsl:template name="basic-info">
	<table style="width:100%;height:100%;">
		<tr style="height:2.56em;">
			<td colspan="31" class="info-value-important">
				<xsl:value-of select="@name"/>
			</td>
			<td/>
			<td colspan="22" class="info-value-important">
				<xsl:value-of select="@player"/>
			</td>
			<td/>
			<td colspan="15" class="info-value">
				<xsl:value-of select="@region"/>
			</td>
		</tr>
		<tr>
			<td colspan="31">CHARACTER NAME</td>
			<td/>
			<td colspan="22">PLAYER</td>
			<td/>
			<td colspan="15">REGION</td>
		</tr>
		<tr style="height:2.56em;">
			<td colspan="15" class="info-value-small">
				<xsl:value-of select="Level/@class"/>
			</td>
			<td/>
			<td colspan="15" class="info-value">
				<xsl:value-of select="@race"/>
			</td>
			<td/>
			<td colspan="6" class="info-value">
				<xsl:value-of select="@gender"/>
			</td>
			<td/>
			<td colspan="15" class="info-value">
				<xsl:value-of select="@alignment"/>
			</td>
			<td/>
			<td colspan="15" class="info-value">
				<xsl:value-of select="@deity"/>
			</td>
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
		</tr>
		<tr style="height:2.56em;">
			<td colspan="7" class="info-value-small">
				<xsl:value-of select="Level/@level"/>
			</td>
			<td/>
			<td colspan="7" class="info-value-small">
				<xsl:value-of select="@size"/>
			</td>
			<td/>
			<td colspan="15" class="info-value">
				<xsl:value-of select="@type"/>
			</td>
			<td/>
			<td colspan="6" class="info-value">
				<xsl:value-of select="@age"/>
			</td>
			<td/>
			<td colspan="7" class="info-value">
				<xsl:value-of select="@height"/>
			</td>
			<td/>
			<td colspan="7" class="info-value">
				<xsl:value-of select="@weight"/>
			</td>
			<td/>
			<td colspan="7" class="info-value">
				<xsl:value-of select="@eye-colour"/>
			</td>
			<td/>
			<td colspan="7" class="info-value">
				<xsl:value-of select="@hair-colour"/>
			</td>
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
		</tr>
	</table>
</xsl:template>

<xsl:template name="hp-and-ac">
	<table style="width:100%;height:100%;">
		<tr style="height:2.56em;">
			<td/>
			<td/>
			<td/>
			<td/>
			<td/>
			<td/>
			<td/>
			<td colspan="4" class="label-total">TOTAL</td>
			<td/>
			<td colspan="10" class="label">CURRENT</td>
			<td/>
			<td colspan="10" class="label">WOUNDS</td>
			<td/>
			<td colspan="10" class="label">NONLETHAL DAMAGE</td>
			<td/>
			<td/>
			<td/>
			<td/>
			<td colspan="22" class="label">SPEED</td>
		</tr>
		<tr style="height:2.56em;">
			<td colspan="6" class="title">HP</td>
			<td/>
			<td colspan="4" rowspan="2" class="data-value-important" title="Hit Points" info="{HitPoints/@info}" onclick="showInfo(this);">
				<xsl:value-of select="HitPoints/@maximum"/>
			</td>
			<td/>
			<td colspan="10" rowspan="2" class="data-value" style="text-align:left; padding-left:10px" title="Hit Points" info="{HitPoints/@info}" onclick="showInfo(this);">
				<xsl:value-of select="HitPoints/@current"/>
			</td>
			<td/>
			<td colspan="10" rowspan="2" class="data-value" style="text-align:left; padding-left:10px" title="Hit Points" info="{HitPoints/@info}" onclick="showInfo(this);">
				<xsl:value-of select="HitPoints/@wounds"/>
			</td>
			<td/>
			<td colspan="10" rowspan="2" class="data-value" style="text-align:left; padding-left:10px" title="Hit Points" info="{HitPoints/@info}" onclick="showInfo(this);">
				<xsl:value-of select="HitPoints/@non-lethal"/>
			</td>
			<td/>
			<td/>
			<td/>
			<td/>
			<td colspan="22" rowspan="2" class="data-value">
				<xsl:value-of select="@speed"/>
			</td>
		</tr>
		<tr>
			<td colspan="6" class="subtitle">HIT POINTS</td>
		</tr>
		<tr>
			<td/><td/><td/><td/><td/><td/><td/><td/><td/><td/>
			<td/><td/><td/><td/><td/><td/><td/><td/><td/><td/>
			<td/><td/><td/><td/><td/><td/><td/><td/><td/><td/>
			<td/><td/><td/><td/><td/><td/><td/><td/><td/><td/>
			<td/><td/><td/><td/><td/><td/><td/><td/><td/><td/>
			<td/><td/><td/><td/><td/><td/><td/><td/><td/><td/>
			<td/><td/><td/><td/><td/><td/><td/><td/><td/><td/>
		</tr>
		<tr style="height:2.56em;">
			<td colspan="6" class="title">AC</td>
			<td/>
			<td colspan="4" rowspan="2" class="data-value-important">
				<xsl:value-of select="AC/@total"/>
			</td>
			<td rowspan="2" class="symbol">=</td>
			<td colspan="3" rowspan="2" class="ac-base-value">10</td>
			<td rowspan="2" class="symbol">+</td>
			<td colspan="4" rowspan="2" class="data-value">
				<xsl:value-of select="AC/ACComponent[@type='Armor']/@value"/>
				<xsl:if test="not(AC/ACComponent[@type='Armor']/@value)">+0</xsl:if>
			</td>
			<td rowspan="2" class="symbol">+</td>
			<td colspan="4" rowspan="2" class="data-value">
				<xsl:value-of select="AC/ACComponent[@type='Shield']/@value"/>
				<xsl:if test="not(AC/ACComponent[@type='Shield']/@value)">+0</xsl:if>
			</td>
			<td rowspan="2" class="symbol">+</td>
			<td colspan="4" rowspan="2" class="data-value">
				<xsl:value-of select="AC/ACComponent[@type='Dexterity']/@value"/>
				<xsl:if test="not(AC/ACComponent[@type='Dexterity']/@value)">+0</xsl:if>
			</td>
			<td rowspan="2" class="symbol">+</td>
			<td colspan="4" rowspan="2" class="data-value">
				<xsl:value-of select="AC/ACComponent[@type='Size']/@value"/>
				<xsl:if test="not(AC/ACComponent[@type='Size']/@value)">+0</xsl:if>
			</td>
			<td rowspan="2" class="symbol">+</td>
			<td colspan="4" rowspan="2" class="data-value">
				<xsl:value-of select="AC/ACComponent[@type='Natural Armor']/@value"/>
				<xsl:if test="not(AC/ACComponent[@type='Natural Armor']/@value)">+0</xsl:if>
			</td>
			<td rowspan="2" class="symbol">+</td>
			<td colspan="4" rowspan="2" class="data-value">
				<xsl:value-of select="AC/ACComponent[@type='Deflection']/@value"/>
				<xsl:if test="not(AC/ACComponent[@type='Deflection']/@value)">+0</xsl:if>
			</td>
			<td rowspan="2" class="symbol">+</td>
			<td colspan="4" rowspan="2" class="data-value">
				<xsl:value-of select="AC/ACComponent[@type='Misc']/@value"/>
				<xsl:if test="not(AC/ACComponent[@type='Misc']/@value)">+0</xsl:if>
			</td>
			<td/>
			<td/>
			<td colspan="4" rowspan="2" class="data-value">
				<xsl:value-of select="AC/@armor-check-penalty"/>
			</td>
			<td/>
			<td colspan="13" rowspan="2" class="data-value">
				<xsl:value-of select="@damage-reduction"/>
			</td>
		</tr>
		<tr>
			<td colspan="6" class="subtitle">ARMOR CLASS</td>
		</tr>
		<tr style="height:2.56em;">
			<td/>
			<td/>
			<td/>
			<td/>
			<td/>
			<td/>
			<td/>
			<td colspan="4" class="label-total-under">TOTAL</td>
			<td/>
			<td/>
			<td/>
			<td/>
			<td/>
			<td colspan="4" class="label-under">ARMOR BONUS</td>
			<td/>
			<td colspan="4" class="label-under">SHIELD BONUS</td>
			<td/>
			<td colspan="4" class="label-under">DEX MODIFIER</td>
			<td/>
			<td colspan="4" class="label-under">SIZE MODIFIER</td>
			<td/>
			<td colspan="4" class="label-under">NATURAL ARMOR</td>
			<td/>
			<td colspan="4" class="label-under">DEFLECT MOD</td>
			<td/>
			<td colspan="4" class="label-under">MISC MOD</td>
			<td/>
			<td/>
			<td colspan="4" rowspan="2" class="label-under">ARMOR CHECK PENALTY</td>
			<td/>
			<td colspan="13" class="label-under">DAMAGE REDUCTION</td>
		</tr>
		<tr>
		</tr>
	</table>
</xsl:template>

<xsl:template name="ac-initiative">
	<table style="width:100%;height:100%;">
		<tr style="height:2.56em;">
			<td colspan="6" class="title">TOUCH</td>
			<td/>
			<td colspan="4" rowspan="2" class="data-value-important">
				<xsl:value-of select="AC/@touch"/>
			</td>
			<td/>
			<td colspan="10" class="title">FLAT-FOOTED</td>
			<td/>
			<td colspan="4" rowspan="2" class="data-value-important">
				<xsl:value-of select="AC/@flat-footed"/>
			</td>
		</tr>
		<tr>
			<td colspan="6" class="subtitle">ARMOR CLASS</td>
			<td/>
			<td/>
			<td colspan="10" class="subtitle">ARMOR CLASS</td>
			<td/>
		</tr>
		<tr>
			<td/><td/><td/><td/><td/><td/><td/><td/><td/><td/>
			<td/><td/><td/><td/><td/><td/><td/><td/><td/><td/>
			<td/><td/><td/><td/><td/><td/><td/>
		</tr>
		<tr>
			<td/><td/><td/><td/><td/><td/><td/><td/><td/><td/>
			<td/><td/><td/><td/><td/><td/><td/><td/><td/><td/>
			<td/><td/><td/><td/><td/><td/><td/>
		</tr>
		<tr>
			<td/><td/><td/><td/><td/><td/><td/><td/><td/><td/>
			<td/><td/><td/><td/><td/><td/><td/><td/><td/><td/>
			<td/><td/><td/><td/><td/><td/><td/>
		</tr>
		<tr>
			<td/><td/><td/><td/><td/><td/><td/><td/><td/><td/>
			<td/><td/><td/><td/><td/><td/><td/><td/><td/><td/>
			<td/><td/><td/><td/><td/><td/><td/>
		</tr>
		<tr style="height:2.56em;">
			<td colspan="11" class="title">INITIATIVE</td>
			<td/>
			<td colspan="4" rowspan="2" class="data-value-important" title="Initiative" info="{Initiative/@info}" onclick="showInfo(this);">
				<xsl:value-of select="Initiative/@total"/>
			</td>
			<td rowspan="2" class="symbol">=</td>
			<td colspan="4" rowspan="2" class="data-value">
				<xsl:call-template name="ability-mod">
					<xsl:with-param name="ability" select="AbilityScores/AbilityScore[@type='Dexterity']"/>
				</xsl:call-template>
			</td>
			<td rowspan="2" class="symbol">+</td>
			<td colspan="4" rowspan="2" class="data-value">
				<xsl:value-of select="Initiative/@misc"/>
				<xsl:if test="not(Initiative/@misc)">+0</xsl:if>
			</td>
			<td/>
		</tr>
		<tr>
			<td colspan="11" class="subtitle">MODIFIER</td>
			<td/>
		</tr>
		<tr style="height:2.56em;">
			<td/><td/><td/><td/><td/><td/><td/><td/><td/><td/>
			<td/><td/>
			<td colspan="4" class="label-total-under">TOTAL</td>
			<td/>
			<td colspan="4" class="label-under">DEX MODIFIER</td>
			<td/>
			<td colspan="4" class="label-under">MISC. MODIFIER</td>
			<td/>
		</tr>
	</table>
</xsl:template>

<xsl:template name="special-info">
	<table style="width:100%;height:100%;">
		<col style="width:17.28em;"/>
		<col style="width:1.44em;"/>
		<col style="width:5.76em;"/>
		<tr>
			<td colspan="3" class="conditional-heading">CONDITIONAL MODIFIERS</td>
		</tr>
		<tr style="height:15.36em;">
			<td colspan="3" class="conditional-modifiers">
				<xsl:for-each select="Buffs/Buff/Modifier[@condition]">
					<xsl:value-of select="@description"/><br/>
				</xsl:for-each>
			</td>
		</tr>
		<tr><td/><td/><td/></tr>
		<tr style="height:3.84em;">
			<td class="title-smaller">SPELL RESISTANCE</td>
			<td/>
			<td class="data-value-important">
				<xsl:value-of select="@spell-resistance"/>
			</td>
		</tr>
		<tr><td/><td/><td/></tr>
		<tr style="height:3.84em;">
			<td class="title-smaller">ARCANE SPELL FAILURE *</td>
			<td/>
			<td class="data-value">
				<xsl:value-of select="@arcane-spell-failure"/>
			</td>
		</tr>
		<tr><td/><td/><td/></tr>
		<tr style="height:3.84em;">
			<td class="title-smaller">ACTION POINTS</td>
			<td/>
			<td class="data-value-important">
				<xsl:value-of select="@action-points"/>
			</td>
		</tr>
	</table>
</xsl:template>

<xsl:template name="bab-grapple">
	<table style="width:100%;height:100%;">
		<tr>
			<td/><td/><td/><td/><td/><td/><td/><td/><td/><td/>
			<td/><td/><td/><td/><td/><td/><td/><td/><td/><td/>
			<td/><td/><td/><td/><td/><td/><td/>
			<td colspan="4" class="label">TEMP.</td>
			<td/>
			<td/>
			<td/>
			<td/>
		</tr>
		<tr style="height:3.84em;">
			<td colspan="16" class="title">BASE ATTACK BONUS</td>
			<td/>
			<td colspan="9" class="bab">
				<xsl:value-of select="Attacks/@normal-attacks"/>
			</td>
			<td/>
			<td colspan="4" class="temp-value">
				<xsl:value-of select="Attacks/@temp"/>
			</td>
			<td/>
			<td/>
			<td/>
			<td/>
		</tr>
		<tr>
			<td/><td/><td/><td/><td/><td/><td/><td/><td/><td/>
			<td/><td/><td/><td/><td/><td/><td/><td/><td/><td/>
			<td/><td/><td/><td/><td/><td/><td/><td/><td/><td/>
			<td/><td/><td/><td/><td/><td/>
		</tr>
		<tr style="height:2.56em;">
			<td colspan="9" class="title">GRAPPLE</td>
			<td/>
			<td colspan="4" rowspan="2" class="data-value-important">
				<xsl:value-of select="Attacks/Attack[@type='Grapple']/@total"/>
			</td>
			<td rowspan="2" class="symbol">=</td>
			<td colspan="4" rowspan="2" class="data-value">
				<xsl:value-of select="Attacks/@base"/>
			</td>
			<td rowspan="2" class="symbol">+</td>
			<td colspan="4" rowspan="2" class="data-value">
				<xsl:call-template name="ability-mod">
					<xsl:with-param name="ability" select="AbilityScores/AbilityScore[@type='Strength']"/>
				</xsl:call-template>
			</td>
			<td rowspan="2" class="symbol">+</td>
			<td colspan="4" rowspan="2" class="data-value">
				<xsl:value-of select="Attacks/@size-modifier"/>
			</td>
			<td rowspan="2" class="symbol">+</td>
			<td colspan="4" rowspan="2" class="data-value">
				<xsl:value-of select="Attacks/Attack[@type='Grapple']/@misc"/>
			</td>
			<td/>
			<td/>
		</tr>
		<tr>
			<td colspan="9" class="subtitle">MODIFIER</td>
			<td/><td/>
		</tr>
		<tr style="height:2.56em;">
			<td/><td/><td/><td/><td/><td/><td/><td/><td/><td/>
			<td colspan="4" class="label-total-under">TOTAL</td>
			<td/>
			<td colspan="4" class="label-under">BASE ATTACK</td>
			<td/>
			<td colspan="4" class="label-under">STR MODIFIER</td>
			<td/>
			<td colspan="4" class="label-under">SIZE MODIFIER</td>
			<td/>
			<td colspan="4" class="label-under">MISC. MODIFIER</td>
			<td/>
			<td/>
		</tr>
	</table>
</xsl:template>

<xsl:template name="basic-attacks">
	<table style="width:100%;height:100%;">
		<tr style="height:2.56em;">
			<td/><td/><td/><td/><td/><td/><td/><td/><td/><td/>
			<td/><td/>
			<td colspan="12" class="label-total">TOTAL</td>
			<td/>
			<td colspan="9" class="label">BASE ATTACK BONUS</td>
			<td/>
			<td colspan="4" class="label">STR MODIFIER</td>
			<td/>
			<td colspan="4" class="label">SIZE MODIFIER</td>
			<td/>
			<td colspan="4" class="label">MISC. MODIFIER</td>
			<td/>
			<td colspan="4" class="label">TEMP. MODIFIER</td>
		</tr>
		<tr style="height:2.56em;">
			<td colspan="11" class="title">MELEE</td>
			<td/>
			<td colspan="12" rowspan="2" class="data-value-important" title="Melee Attack" info="{Attacks/Attack[@type='Melee']/@info}" onclick="showInfo(this);">
				<xsl:choose>
					<xsl:when test="Attacks/@total_defense = 'true'">
						<s><xsl:value-of select="Attacks/Attack[@type='Melee']/@attacks"/></s>
					</xsl:when>
					<xsl:otherwise>
						<xsl:value-of select="Attacks/Attack[@type='Melee']/@attacks"/>
					</xsl:otherwise>
				</xsl:choose>
			</td>
			<td rowspan="2" class="symbol">=</td>
			<td colspan="9" rowspan="2" class="bab">
				<xsl:value-of select="Attacks/@attacks"/>
			</td>
			<td rowspan="2" class="symbol">+</td>
			<td colspan="4" rowspan="2" class="data-value">
				<xsl:call-template name="ability-mod">
					<xsl:with-param name="ability" select="AbilityScores/AbilityScore[@type='Strength']"/>
				</xsl:call-template>
			</td>
			<td rowspan="2" class="symbol">+</td>
			<td colspan="4" rowspan="2" class="data-value">
				<xsl:value-of select="Attacks/@size-modifier"/>
			</td>
			<td rowspan="2" class="symbol">+</td>
			<td colspan="4" rowspan="2" class="data-value">
				<xsl:value-of select="Attacks/Attack[@type='Melee']/@misc"/>
			</td>
			<td rowspan="2" class="symbol">+</td>
			<td colspan="4" rowspan="2" class="temp-value">
				<xsl:value-of select="Attacks/Attack[@type='Melee']/@temp-modifier"/>
			</td>
		</tr>
		<tr>
			<td colspan="11" class="subtitle">ATTACK BONUS</td>
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
			<td colspan="11" class="title">RANGED</td>
			<td/>
			<td colspan="12" rowspan="2" class="data-value-important" title="Ranged Attack" info="{Attacks/Attack[@type='Ranged']/@info}" onclick="showInfo(this);">
				<xsl:choose>
					<xsl:when test="Attacks/@total_defense = 'true'">
						<s><xsl:value-of select="Attacks/Attack[@type='Ranged']/@attacks"/></s>
					</xsl:when>
					<xsl:otherwise>
						<xsl:value-of select="Attacks/Attack[@type='Ranged']/@attacks"/>
					</xsl:otherwise>
				</xsl:choose>
			</td>
			<td rowspan="2" class="symbol">=</td>
			<td colspan="9" rowspan="2" class="bab">
				<xsl:value-of select="Attacks/@attacks"/>
			</td>
			<td rowspan="2" class="symbol">+</td>
			<td colspan="4" rowspan="2" class="data-value">
				<xsl:call-template name="ability-mod">
					<xsl:with-param name="ability" select="AbilityScores/AbilityScore[@type='Dexterity']"/>
				</xsl:call-template>
			</td>
			<td rowspan="2" class="symbol">+</td>
			<td colspan="4" rowspan="2" class="data-value">
				<xsl:value-of select="Attacks/@size-modifier"/>
			</td>
			<td rowspan="2" class="symbol">+</td>
			<td colspan="4" rowspan="2" class="data-value">
				<xsl:value-of select="Attacks/Attack[@type='Ranged']/@misc"/>
			</td>
			<td rowspan="2" class="symbol">+</td>
			<td colspan="4" rowspan="2" class="temp-value">
				<xsl:value-of select="Attacks/Attack[@type='Ranged']/@temp-modifier"/>
			</td>
		</tr>
		<tr>
			<td colspan="11" class="subtitle">ATTACK BONUS</td>
		</tr>
		<tr style="height:2.56em;">
			<td/><td/><td/><td/><td/><td/><td/><td/><td/><td/>
			<td/><td/>
			<td colspan="12" class="label-total-under">TOTAL</td>
			<td/>
			<td colspan="9" class="label-under">BASE ATTACK BONUS</td>
			<td/>
			<td colspan="4" class="label-under">DEX MODIFIER</td>
			<td/>
			<td colspan="4" class="label-under">SIZE MODIFIER</td>
			<td/>
			<td colspan="4" class="label-under">MISC. MODIFIER</td>
			<td/>
			<td colspan="4" class="label-under">TEMP. MODIFIER</td>
		</tr>
	</table>
</xsl:template>

<xsl:template match="Character">
<div class="character-table sheet1">
	<div id="cs1_basic_info"><xsl:call-template name="basic-info"/></div>
	<div id="cs1_abilities"><xsl:call-template name="abilities"/></div>
	<div id="cs1_hp_and_ac"><xsl:call-template name="hp-and-ac"/></div>
	<div id="cs1_ac_initiative"><xsl:call-template name="ac-initiative"/></div>
	<div id="cs1_saving_throws"><xsl:call-template name="saving-throws"/></div>
	<div id="cs1_special_info"><xsl:call-template name="special-info"/></div>
	<div id="cs1_bab_grapple"><xsl:call-template name="bab-grapple"/></div>
	<div id="cs1_basic_attacks"><xsl:call-template name="basic-attacks"/></div>
	<div id="cs1_skills"><xsl:call-template name="skills"/></div>

	<div id="cs1_weapon1">
		<xsl:call-template name="weapon">
			<xsl:with-param name="weapon" select="Attacks/AttackForm[1]"/>
		</xsl:call-template>
	</div>

	<div id="cs1_weapon2">
		<xsl:call-template name="weapon">
			<xsl:with-param name="weapon" select="Attacks/AttackForm[2]"/>
		</xsl:call-template>
	</div>

	<div id="cs1_weapon3">
		<xsl:call-template name="weapon">
			<xsl:with-param name="weapon" select="Attacks/AttackForm[3]"/>
		</xsl:call-template>
	</div>

	<div id="cs1_weapon4">
		<xsl:call-template name="weapon">
			<xsl:with-param name="weapon" select="Attacks/AttackForm[4]"/>
		</xsl:call-template>
	</div>

	<div id="cs1_weapon5">
		<xsl:call-template name="weapon">
			<xsl:with-param name="weapon" select="Attacks/AttackForm[5]"/>
		</xsl:call-template>
	</div>

	<div id="cs1_weapon6">
		<xsl:call-template name="weapon">
			<xsl:with-param name="weapon" select="Attacks/AttackForm[6]"/>
		</xsl:call-template>
	</div>

	<div id="cs1_logo">
		<img style="width:100%; height:100%" src="/assistantdm/static/dndlogo.png" alt="DandDLogo" />
		<div class="sheet-title">CHARACTER RECORD SHEET</div>
	</div>
</div>

</xsl:template>
</xsl:stylesheet>
