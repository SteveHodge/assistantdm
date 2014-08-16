<?xml version="1.0" encoding="ISO-8859-1"?>

<!DOCTYPE stylesheet [
<!ENTITY nbsp  "&#160;" >
]>

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	<xsl:output method="html" encoding="utf-8" indent="yes" />

<xsl:template name="turning-block">
    <table style="width:38.30em; position:relative; left:-1px; top:-1px">
      <tr class='double-row'>
        <td class='title' colspan='26'><xsl:choose><xsl:when test="not(Turn/@type = 'rebuke')">TURN</xsl:when><xsl:otherwise>REBUKE</xsl:otherwise></xsl:choose> ATTEMPTS</td>
      </tr>
      <tr class='double-row'>
      </tr>
      <tr style="height:5.4em;">
		  <td colspan='9' class='turn-subhead'><xsl:choose><xsl:when test="not(Turn/@type = 'rebuke')">Turning</xsl:when><xsl:otherwise>Rebuking</xsl:otherwise></xsl:choose> Check Modifier</td>
		  <td colspan='4' class='data-value'><xsl:value-of select="Turn/@modifier"/></td>
		  <td colspan='9' class='turn-subhead'>Times/Day</td>
		  <td colspan='4' class='data-value'><xsl:value-of select="Turn/@per-day"/></td>
      </tr>
      <tr class='double-row'>
      	<td/><td/><td/><td/><td/><td/><td/><td/><td/><td/>
      	<td/><td/><td/><td/><td/><td/><td/><td/><td/><td/>
      	<td/><td/><td/><td/><td/><td/>
      </tr>
      <tr class='double-row'>
      	<td/><td/><td/><td/><td/><td/><td/><td/><td/><td/>
      	<td/><td/><td/><td/>
		  <td colspan='8' rowspan='2' class='turn-subhead'>Used</td>
		  <td colspan='4' rowspan='2' class='data-value'><xsl:value-of select="Turn/@used"/></td>
      </tr>
      <tr class='double-row'>
		  <td colspan='4' class='turn-table-colhead T L'><xsl:choose><xsl:when test="not(Turn/@type = 'rebuke')">Turing</xsl:when><xsl:otherwise>Rebuking</xsl:otherwise></xsl:choose> Check</td>
		  <td colspan='9' class='turn-table-colhead T R'>Most Powerful Undead Affected (Max HD)</td>
		  <td/>
      </tr>
      <tr class='double-row'>
		  <td colspan='4' class='turn-table-cell L'>Up to 0</td>
		  <td colspan='9' class='turn-table-cell R'>
				<xsl:choose>
					<xsl:when test="Turn/@max-hd > 7">
						<xsl:value-of select="Turn/@max-hd - 8"/>
					</xsl:when>
					<xsl:otherwise>0</xsl:otherwise>
				</xsl:choose>
		  </td>
		  <td/>
		  <td/>
		  <td/><td/><td/><td/><td/><td/><td/><td/><td/><td/><td/>
      </tr>
      <tr class='double-row'>
		  <td colspan='4' class='turn-table-cell L'>1-3</td>
		  <td colspan='9' class='turn-table-cell R'>
				<xsl:choose>
					<xsl:when test="Turn/@max-hd > 6">
						<xsl:value-of select="Turn/@max-hd - 7"/>
					</xsl:when>
					<xsl:otherwise>0</xsl:otherwise>
				</xsl:choose>
		  </td>
		  <td/>
		  <td/>
		  <td colspan='11' class='turn-hd-subhead'># of HD <xsl:choose><xsl:when test="not(Turn/@type = 'rebuke')">Turned</xsl:when><xsl:otherwise>Rebuked</xsl:otherwise></xsl:choose></td>
      </tr>
      <tr class='double-row'>
		  <td colspan='4' class='turn-table-cell L'>4-6</td>
		  <td colspan='9' class='turn-table-cell R'>
				<xsl:choose>
					<xsl:when test="Turn/@max-hd > 5">
						<xsl:value-of select="Turn/@max-hd - 6"/>
					</xsl:when>
					<xsl:otherwise>0</xsl:otherwise>
				</xsl:choose>
		  </td>
		  <td/>
		  <td/>
		  <td colspan='11' rowspan='2' class='turn-total-hd'><xsl:value-of select="Turn/@total-hd"/></td>
      </tr>
      <tr class='double-row'>
		  <td colspan='4' class='turn-table-cell L'>7-9</td>
		  <td colspan='9' class='turn-table-cell R'>
				<xsl:choose>
					<xsl:when test="Turn/@max-hd > 4">
						<xsl:value-of select="Turn/@max-hd - 5"/>
					</xsl:when>
					<xsl:otherwise>0</xsl:otherwise>
				</xsl:choose>
		  </td>
		  <td/>
		  <td/>
      </tr>
      <tr class='double-row'>
		  <td colspan='4' class='turn-table-cell L'>10-12</td>
		  <td colspan='9' class='turn-table-cell R'>
				<xsl:choose>
					<xsl:when test="Turn/@max-hd > 3">
						<xsl:value-of select="Turn/@max-hd - 4"/>
					</xsl:when>
					<xsl:otherwise>0</xsl:otherwise>
				</xsl:choose>
		  </td>
		  <td/>
		  <td/>
		  <td colspan='11' rowspan='6' class='turn-note'>If your cleric level is double the HD of the undead or more, the undead are
		  	 <xsl:choose><xsl:when test="not(Turn/@type = 'rebuke')">destroyed</xsl:when><xsl:otherwise>commanded</xsl:otherwise></xsl:choose>
		  	 rather than <xsl:choose><xsl:when test="not(Turn/@type = 'rebuke')">turned</xsl:when><xsl:otherwise>rebuked</xsl:otherwise></xsl:choose>.
		  	 Dispelling <xsl:choose><xsl:when test="not(Turn/@type = 'rebuke')">rebuking</xsl:when><xsl:otherwise>turning</xsl:otherwise></xsl:choose>
		  	 works like <xsl:choose><xsl:when test="not(Turn/@type = 'rebuke')">turning</xsl:when><xsl:otherwise>rebuking</xsl:otherwise></xsl:choose>,
		  	 but you must equal or exceed the check result of the cleric who <xsl:choose><xsl:when test="not(Turn/@type = 'rebuke')">rebuked</xsl:when><xsl:otherwise>turned</xsl:otherwise></xsl:choose>.</td>
      </tr>
      <tr class='double-row'>
		  <td colspan='4' class='turn-table-cell L'>13-15</td>
		  <td colspan='9' class='turn-table-cell R'>
				<xsl:choose>
					<xsl:when test="Turn/@max-hd > 2">
						<xsl:value-of select="Turn/@max-hd - 3"/>
					</xsl:when>
					<xsl:otherwise>0</xsl:otherwise>
				</xsl:choose>
		  </td>
		  <td/>
		  <td/>
      </tr>
      <tr class='double-row'>
		  <td colspan='4' class='turn-table-cell L'>16-18</td>
		  <td colspan='9' class='turn-table-cell R'>
				<xsl:choose>
					<xsl:when test="Turn/@max-hd > 1">
						<xsl:value-of select="Turn/@max-hd - 2"/>
					</xsl:when>
					<xsl:otherwise>0</xsl:otherwise>
				</xsl:choose>
		  </td>
		  <td/>
		  <td/>
      </tr>
      <tr class='double-row'>
        <td colspan='4' class='turn-table-cell L'>19-21</td>
		  <td colspan='9' class='turn-table-cell R'>
				<xsl:choose>
					<xsl:when test="Turn/@max-hd > 0">
						<xsl:value-of select="Turn/@max-hd - 1"/>
					</xsl:when>
					<xsl:otherwise>0</xsl:otherwise>
				</xsl:choose>
		  </td>
		  <td/>
		  <td/>
      </tr>
      <tr class='double-row'>
		  <td colspan='4' class='turn-table-cell L B'>22+</td>
		  <td colspan='9' class='turn-table-cell R B'><xsl:value-of select="Turn/@max-hd"/></td>
		  <td/>
		  <td/>
      </tr>
      <tr class='double-row'>
		  <td colspan='13'></td>
		  <td/>
		  <td/>
      </tr>
    </table>
</xsl:template>

<xsl:template match="Character">
<div class="character-table sheet2">

<table cellspacing='0'>
 <col style='width:0'/>
 <col span='97' style='width:1.1667em;'/>
 <tr style='height:0'>
  <td/>
  <td class='col-size'></td>
  <td class='col-size'></td>
  <td class='col-size'></td>
  <td class='col-size'></td>
  <td class='col-size'></td>
  <td class='col-size'></td>
  <td class='col-size'></td>
  <td class='col-size'></td>
  <td class='col-size'></td>
  <td class='col-size'></td>
  <td class='col-size'></td>
  <td class='col-size'></td>
  <td class='col-size'></td>
  <td class='col-size'></td>
  <td class='col-size'></td>
  <td class='col-size'></td>
  <td class='col-size'></td>
  <td class='col-size'></td>
  <td class='col-size'></td>
  <td class='col-size'></td>
  <td class='col-size'></td>
  <td class='col-size'></td>
  <td class='col-size'></td>
  <td class='col-size'></td>
  <td class='col-size'></td>
  <td class='col-size'></td>
  <td class='col-size'></td>
  <td class='col-size'></td>
  <td class='col-size'></td>
  <td class='col-size'></td>
  <td class='col-size'></td>
  <td class='col-size'></td>
  <td class='col-size'></td>
  <td class='col-size'></td>
  <td class='col-size'></td>
  <td class='col-size'></td>
  <td class='col-size'></td>
  <td class='col-size'></td>
  <td class='col-size'></td>
  <td class='col-size'></td>
  <td class='col-size'></td>
  <td class='col-size'></td>
  <td class='col-size'></td>
  <td class='col-size'></td>
  <td class='col-size'></td>
  <td class='col-size'></td>
  <td class='col-size'></td>
  <td class='col-size'></td>
  <td class='col-size'></td>
  <td class='col-size'></td>
  <td class='col-size'></td>
  <td class='col-size'></td>
  <td class='col-size'></td>
  <td class='col-size'></td>
  <td class='col-size'></td>
  <td class='col-size'></td>
  <td class='col-size'></td>
  <td class='col-size'></td>
  <td class='col-size'></td>
  <td class='col-size'></td>
  <td class='col-size'></td>
  <td class='col-size'></td>
  <td class='col-size'></td>
  <td class='col-size'></td>
  <td class='col-size'></td>
  <td class='col-size'></td>
  <td class='col-size'></td>
  <td class='col-size'></td>
  <td class='col-size'></td>
  <td class='col-size'></td>
  <td class='col-size'></td>
  <td class='col-size'></td>
  <td class='col-size'></td>
  <td class='col-size'></td>
  <td class='col-size'></td>
  <td class='col-size'></td>
  <td class='col-size'></td>
  <td class='col-size'></td>
  <td class='col-size'></td>
  <td class='col-size'></td>
  <td class='col-size'></td>
  <td class='col-size'></td>
  <td class='col-size'></td>
  <td class='col-size'></td>
  <td class='col-size'></td>
  <td class='col-size'></td>
  <td class='col-size'></td>
  <td class='col-size'></td>
  <td class='col-size'></td>
  <td class='col-size'></td>
  <td class='col-size'></td>
  <td class='col-size'></td>
  <td class='col-size'></td>
  <td class='col-size'></td>
  <td class='col-size'></td>
  <td class='col-size'></td>
  <td class='col-size'></td>
  <td class='col-size'></td>
 </tr>
 <tr class='double-row'>
  <td/>
  <td colspan='27' rowspan='3' class='campaign-name'>
		<xsl:value-of select="@campaign"/>
  </td>
  <td colspan='27' rowspan='3' class='campaign-name'>
		<xsl:value-of select="@xp"/> / <xsl:value-of select="@xp-req"/>
  </td>
  <td/>
  <td colspan='43' class='title' >SPECIAL ABILITIES</td>
 </tr>
 <tr class='double-row'>
  <td/>
  <td/>
  <td colspan='43' rowspan='64' class='special-abilities'>
  	<xsl:if test="SpecialAbilities/Ability[@type='racial']">
	RACE ABILITIES
  	<ul>
  		<xsl:for-each select="SpecialAbilities/Ability[@type='racial']">
  		<li><xsl:value-of select="."/></li>
  		</xsl:for-each>
  	</ul>
  	</xsl:if>
  	<xsl:if test="SpecialAbilities/Ability[@type='class']">
   CLASS ABILITIES
   <ul>
  		<xsl:for-each select="SpecialAbilities/Ability[@type='class']">
  		<li><xsl:value-of select="."/></li>
  		</xsl:for-each>
  	</ul>
  	</xsl:if>
  	<xsl:if test="SpecialAbilities/Ability[@type='feat']">
	FEATS
	<ul>
  		<xsl:for-each select="SpecialAbilities/Ability[@type='feat']">
  		<li><xsl:value-of select="."/></li>
  		</xsl:for-each>
  	</ul>
  	</xsl:if>
    </td>
 </tr>
 <tr class='double-row'>
  <td/>
  <td/>
 </tr>
 <tr class='single-row'>
  <td/>
  <td class='campaign-subhead' colspan='27'>CAMPAIGN</td>
  <td class='campaign-subhead' colspan='27'>EXPERIENCE POINTS</td>
  <td/>
 </tr>
 <tr class='single-row'>
  <td/>
 </tr>
 <tr class='double-row'>
  <td/>
  <td class='left-title' colspan='54'>GEAR</td>
  <td/>
 </tr>
 <tr class='single-row'>
  <td/>
 </tr>
 <tr class='single-row'>
  <td/>
  <td colspan='17' rowspan='2' class='title' >ARMOR/PROTECTIVE ITEM</td>
 </tr>
 <tr class='single-row'>
  <td/>
  <td colspan='11' class='armor-subhead'>TYPE</td>
  <td colspan='13' class='armor-subhead'>ARMOR BONUS</td>
  <td colspan='13' class='armor-subhead'>MAX DEX BONUS</td>
  <td/>
 </tr>
 <tr class='double-row'>
  <td/>
  <td colspan='17' rowspan='2' class='weapon-name'><xsl:value-of select="Armor/@name"/></td>
  <td colspan='11' rowspan='2' class='weapon-name'><xsl:value-of select="Armor/@type"/></td>
  <td colspan='13' rowspan='2' class='weapon-name'><xsl:value-of select="Armor/@bonus"/></td>
  <td colspan='13' rowspan='2' class='weapon-name'><xsl:value-of select="Armor/@max-dex"/></td>
  <td/>
 </tr>
 <tr class='single-row'>
  <td/>
 </tr>
 <tr class='single-row'>
  <td/>
  <td colspan='7' class='armor-subhead'>ACP</td>
  <td colspan='7' class='armor-subhead'>SPELL FAILURE</td>
  <td colspan='5' class='armor-subhead'>SPEED</td>
  <td colspan='5' class='armor-subhead'>WEIGHT</td>
  <td colspan='30' class='armor-subhead'>SPECIAL PROPERTIES</td>
  <td/>
 </tr>
 <tr class='double-row'>
  <td/>
  <td colspan='7' rowspan='2' class='weapon-name' ><xsl:value-of select="Armor/@acp"/></td>
  <td colspan='7' rowspan='2' class='weapon-name' ><xsl:value-of select="Armor/@spell-failure"/></td>
  <td colspan='5' rowspan='2' class='weapon-name' ><xsl:value-of select="Armor/@speed"/></td>
  <td colspan='5' rowspan='2' class='weapon-name' ><xsl:value-of select="Armor/@weight"/></td>
  <td colspan='30' rowspan='2' class='weapon-name' ><xsl:value-of select="Armor/@properties"/></td>
  <td/>
 </tr>
 <tr class='single-row'>
  <td/>
 </tr>
 <tr class='single-row'>
  <td/>
 </tr>
 <tr class='single-row'>
  <td/>
  <td colspan='17' rowspan='2' class='title'>SHIELD/PROTECTIVE ITEM</td>
  <td/>
  <td/>
  <td/>
  <td/>
  <td/>
  <td/>
  <td/>
  <td/>
  <td/>
  <td/>
  <td/>
  <td/>
  <td/>
  <td/>
  <td/>
  <td/>
  <td/>
  <td/>
  <td/>
  <td/>
  <td/>
  <td/>
  <td/>
  <td/>
  <td/>
  <td/>
  <td/>
  <td/>
  <td/>
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
 <tr class='single-row'>
  <td/>
  <td colspan='15' class='armor-subhead'>ARMOR BONUS</td>
  <td colspan='6' class='armor-subhead'>WEIGHT</td>
  <td colspan='8' class='armor-subhead'>CHECK PENALTY</td>
  <td colspan='8' class='armor-subhead'>SPELL FAILURE</td>
  <td/>
 </tr>
 <tr class='single-row'>
  <td/>
  <td colspan='17' rowspan='2' class='weapon-name'><xsl:value-of select="Shield/@name"/></td>
  <td colspan='15' rowspan='2' class='weapon-name'><xsl:value-of select="Shield/@bonus"/></td>
  <td colspan='6' rowspan='2' class='weapon-name'><xsl:value-of select="Shield/@weight"/></td>
  <td colspan='8' rowspan='2' class='weapon-name'><xsl:value-of select="Shield/@acp"/></td>
  <td colspan='8' rowspan='2' class='weapon-name'><xsl:value-of select="Shield/@spell-failure"/></td>
  <td/>
 </tr>
 <tr class='double-row'>
  <td/>
 </tr>
 <tr class='single-row'>
  <td/>
  <td colspan='54' class='armor-subhead'>SPECIAL PROPERTIES</td>
  <td/>
 </tr>
 <tr class='single-row'>
  <td/>
  <td colspan='54' rowspan='2' class='weapon-name' ><xsl:value-of select="Shield/@properties"/></td>
  <td/>
 </tr>
 <tr class='double-row'>
  <td/>
 </tr>
 <tr class='single-row'>
  <td/>
 </tr>
 <tr class='double-row'>
  <td/>
  <td colspan='54' class='title'>OTHER POSSESSIONS</td>
  <td/>
 </tr>
 <tr class='single-row'>
  <td/>
  <td colspan='23' class='possessions-colhead L'>ITEM</td>
  <td colspan='4' class='possessions-colhead'>Wgt</td>
  <td colspan='23' class='possessions-colhead'>ITEM</td>
  <td colspan='4' class='possessions-colhead R'>Wgt</td>
  <td/>
 </tr>
 <tr class='double-row'>
  <td/>
  <td colspan='23' class='possessions-item L'></td>
  <td colspan='4' class='possessions-weight'></td>
  <td colspan='23' class='possessions-item'></td>
  <td colspan='4' class='possessions-weight R'></td>
  <td/>
 </tr>
 <tr class='double-row'>
  <td/>
  <td colspan='23' class='possessions-item L'></td>
  <td colspan='4' class='possessions-weight'></td>
  <td colspan='23' class='possessions-item'></td>
  <td colspan='4' class='possessions-weight R'></td>
  <td/>
 </tr>
 <tr class='double-row'>
  <td/>
  <td colspan='23' class='possessions-item L'></td>
  <td colspan='4' class='possessions-weight'></td>
  <td colspan='23' class='possessions-item'></td>
  <td colspan='4' class='possessions-weight R'></td>
  <td/>
 </tr>
 <tr class='double-row'>
  <td/>
  <td colspan='23' class='possessions-item L'></td>
  <td colspan='4' class='possessions-weight'></td>
  <td colspan='23' class='possessions-item'></td>
  <td colspan='4' class='possessions-weight R'></td>
  <td/>
 </tr>
 <tr class='double-row'>
  <td/>
  <td colspan='23' class='possessions-item L'></td>
  <td colspan='4' class='possessions-weight'></td>
  <td colspan='23' class='possessions-item'></td>
  <td colspan='4' class='possessions-weight R'></td>
  <td/>
 </tr>
 <tr class='double-row'>
  <td/>
  <td colspan='23' class='possessions-item L'></td>
  <td colspan='4' class='possessions-weight'></td>
  <td colspan='23' class='possessions-item'></td>
  <td colspan='4' class='possessions-weight R'></td>
  <td/>
 </tr>
 <tr class='double-row'>
  <td/>
  <td colspan='23' class='possessions-item L'></td>
  <td colspan='4' class='possessions-weight'></td>
  <td colspan='23' class='possessions-item'></td>
  <td colspan='4' class='possessions-weight R'></td>
  <td/>
 </tr>
 <tr class='double-row'>
  <td/>
  <td colspan='23' class='possessions-item L'></td>
  <td colspan='4' class='possessions-weight'></td>
  <td colspan='23' class='possessions-item'></td>
  <td colspan='4' class='possessions-weight R'></td>
  <td/>
 </tr>
 <tr class='double-row'>
  <td/>
  <td colspan='23' class='possessions-item L'></td>
  <td colspan='4' class='possessions-weight'></td>
  <td colspan='23' class='possessions-item'></td>
  <td colspan='4' class='possessions-weight R'></td>
  <td/>
 </tr>
 <tr class='double-row'>
  <td/>
  <td colspan='23' class='possessions-item L'></td>
  <td colspan='4' class='possessions-weight'></td>
  <td colspan='23' class='possessions-item'></td>
  <td colspan='4' class='possessions-weight R'></td>
  <td/>
 </tr>
 <tr class='double-row'>
  <td/>
  <td colspan='23' class='possessions-item L'></td>
  <td colspan='4' class='possessions-weight'></td>
  <td colspan='27' class='slot-heading R'>Magic Items Equipped by Slot</td>
  <td/>
 </tr>
 <tr class='double-row'>
  <td/>
  <td colspan='23' class='possessions-item L'></td>
  <td colspan='4' class='possessions-weight'></td>
  <td colspan='27' class='slot-subhead R'>Ring Slot (RH)</td>
  <td/>
 </tr>
 <tr class='double-row'>
  <td/>
  <td colspan='23' class='possessions-item L'></td>
  <td colspan='4' class='possessions-weight'></td>
  <td colspan='23' class='slot-item'>(none) (0 GP)</td>
  <td colspan='4' class='slot-weight R'>0</td>
  <td/>
 </tr>
 <tr class='double-row'>
  <td/>
  <td colspan='23' class='possessions-item L'></td>
  <td colspan='4' class='possessions-weight'></td>
  <td colspan='27' class='slot-subhead R'>Ring Slot (LH)</td>
  <td/>
 </tr>
 <tr class='double-row'>
  <td/>
  <td colspan='23' class='possessions-item L'></td>
  <td colspan='4' class='possessions-weight'></td>
  <td colspan='23' class='slot-item'>(none) (0 GP)</td>
  <td colspan='4' class='slot-weight R'>0</td>
  <td/>
 </tr>
 <tr class='double-row'>
  <td/>
  <td colspan='23' class='possessions-item L'></td>
  <td colspan='4' class='possessions-weight'></td>
  <td colspan='27' class='slot-subhead R'>Hand Slot</td>
  <td/>
 </tr>
 <tr class='double-row'>
  <td/>
  <td colspan='23' class='possessions-item L'></td>
  <td colspan='4' class='possessions-weight'></td>
  <td colspan='23' class='slot-item'>(none) (0 GP)</td>
  <td colspan='4' class='slot-weight R'>0</td>
  <td/>
 </tr>
 <tr class='double-row'>
  <td/>
  <td colspan='23' class='possessions-item L'></td>
  <td colspan='4' class='possessions-weight'></td>
  <td colspan='27' class='slot-subhead R'>Arm Slot</td>
  <td/>
 </tr>
 <tr class='double-row'>
  <td/>
  <td colspan='23' class='possessions-item L'></td>
  <td colspan='4' class='possessions-weight'></td>
  <td colspan='23' class='slot-item'>(none) (0 GP)</td>
  <td colspan='4' class='slot-weight R'>0</td>
  <td/>
 </tr>
 <tr class='double-row'>
  <td/>
  <td colspan='23' class='possessions-item L'></td>
  <td colspan='4' class='possessions-weight'></td>
  <td colspan='27' class='slot-subhead R'>Head Slot</td>
  <td/>
 </tr>
 <tr class='double-row'>
  <td/>
  <td colspan='23' class='possessions-item L'></td>
  <td colspan='4' class='possessions-weight'></td>
  <td colspan='23' class='slot-item'>(none) (0 GP)</td>
  <td colspan='4' class='slot-weight R'>0</td>
  <td/>
 </tr>
 <tr class='double-row'>
  <td/>
  <td colspan='23' class='possessions-item L'></td>
  <td colspan='4' class='possessions-weight'></td>
  <td colspan='27' class='slot-subhead R'>Face Slot</td>
  <td/>
 </tr>
 <tr class='double-row'>
  <td/>
  <td colspan='23' class='possessions-item L'></td>
  <td colspan='4' class='possessions-weight'></td>
  <td colspan='23' class='slot-item'>(none) (0 GP)</td>
  <td colspan='4' class='slot-weight R'>0</td>
  <td/>
 </tr>
 <tr class='double-row'>
  <td/>
  <td colspan='23' class='possessions-item L'></td>
  <td colspan='4' class='possessions-weight'></td>
  <td colspan='27' class='slot-subhead R'>Shoulder Slot</td>
  <td/>
 </tr>
 <tr class='double-row'>
  <td/>
  <td colspan='23' class='possessions-item L'></td>
  <td colspan='4' class='possessions-weight'></td>
  <td colspan='23' class='slot-item'>(none) (0 GP)</td>
  <td colspan='4' class='slot-weight R'>0</td>
  <td/>
 </tr>
 <tr class='double-row'>
  <td/>
  <td colspan='23' class='possessions-item L'></td>
  <td colspan='4' class='possessions-weight'></td>
  <td colspan='27' class='slot-subhead R'>Neck Slot</td>
  <td/>
 </tr>
 <tr class='double-row'>
  <td/>
  <td colspan='23' class='possessions-item L'></td>
  <td colspan='4' class='possessions-weight'></td>
  <td colspan='23' class='slot-item'>(none) (0 GP)</td>
  <td colspan='4' class='slot-weight R'>0</td>
  <td/>
 </tr>
 <tr class='double-row'>
  <td/>
  <td colspan='23' class='possessions-item L'></td>
  <td colspan='4' class='possessions-weight'></td>
  <td colspan='27' class='slot-subhead R'>Body Slot</td>
  <td/>
 </tr>
 <tr class='double-row'>
  <td/>
  <td colspan='23' class='possessions-item L'></td>
  <td colspan='4' class='possessions-weight'></td>
  <td colspan='23' class='slot-item'>(none) (0 GP)</td>
  <td colspan='4' class='slot-weight R'>0</td>
  <td/>
 </tr>
 <tr class='double-row'>
  <td/>
  <td colspan='23' class='possessions-item L'></td>
  <td colspan='4' class='possessions-weight'></td>
  <td colspan='27' class='slot-subhead R'>Torso Slot</td>
  <td/>
 </tr>
 <tr class='double-row'>
  <td/>
  <td colspan='23' class='possessions-item L'></td>
  <td colspan='4' class='possessions-weight'></td>
  <td colspan='23' class='slot-item'>(none) (0 GP)</td>
  <td colspan='4' class='slot-weight R'>0</td>
  <td/>
 </tr>
 <tr class='double-row'>
  <td/>
  <td colspan='23' class='possessions-item L'></td>
  <td colspan='4' class='possessions-weight'></td>
  <td colspan='27' class='slot-subhead R'>Waist Slot</td>
  <td/>
 </tr>
 <tr class='double-row'>
  <td/>
  <td colspan='23' class='possessions-item L'></td>
  <td colspan='4' class='possessions-weight'></td>
  <td colspan='23' class='slot-item'>(none) (0 GP)</td>
  <td colspan='4' class='slot-weight R'>0</td>
  <td/>
 </tr>
 <tr class='double-row'>
  <td/>
  <td colspan='23' class='possessions-item L'></td>
  <td colspan='4' class='possessions-weight'></td>
  <td colspan='27' class='slot-subhead R'>Feet Slot</td>
  <td/>
 </tr>
 <tr class='double-row'>
  <td/>
  <td colspan='23' class='possessions-item L'></td>
  <td colspan='4' class='possessions-weight'></td>
  <td colspan='23' class='slot-item'>(none) (0 GP)</td>
  <td colspan='4' class='slot-weight R'>0</td>
  <td/>
 </tr>
 <tr class='double-row'>
  <td/>
  <td colspan='23' class='possessions-item B L'></td>
  <td colspan='4' class='possessions-weight B'></td>
  <td colspan='23' class='slot-subhead B'>TOTAL WEIGHT CARRIED</td>
  <td colspan='4' class='total-weight B R'>0</td>
  <td/>
 </tr>
 <tr class='single-row'>
  <td/>
 </tr>
 <tr class='double-row'>
  <td/>
  <td colspan='37' class='title'>NOTES</td>
  <td/>
  <td colspan='16' class='title'>LANGUAGES</td>
  <td/>
 </tr>
 <tr class='double-row'>
  <td/>
  <td colspan='18' class='underlined-text'></td>
  <td/>
  <td colspan='18' class='underlined-text'></td>
  <td/>
  <td class='L'></td>
  <td colspan='14' class='underlined-text'></td>
  <td class='R'></td>
  <td/>
 </tr>
 <tr class='double-row'>
  <td/>
  <td colspan='18' class='underlined-text'></td>
  <td/>
  <td colspan='18' class='underlined-text'></td>
  <td/>
  <td class='L'></td>
  <td colspan='14' class='underlined-text'></td>
  <td class='R'></td>
  <td/>
 </tr>
 <tr class='double-row'>
  <td/>
  <td colspan='18' class='underlined-text'></td>
  <td/>
  <td colspan='18' class='underlined-text'></td>
  <td/>
  <td class='L'></td>
  <td colspan='14' class='underlined-text'></td>
  <td class='R'></td>
 </tr>
 <tr class='double-row'>
  <td/>
  <td colspan='18' class='underlined-text'></td>
  <td/>
  <td colspan='18' class='underlined-text'></td>
  <td/>
  <td class='L'></td>
  <td colspan='14' class='underlined-text'></td>
  <td class='R'></td>
  <td/>
  <td colspan='16' class='title'>CARRYING INFO</td>
  <td/>
  <td colspan='26' rowspan='17'>
		<xsl:call-template name="turning-block"/>
  </td>
 </tr>
 <tr class='double-row'>
  <td/>
  <td colspan='18' class='underlined-text'></td>
  <td/>
  <td colspan='18' class='underlined-text'></td>
  <td/>
  <td class='L'></td>
  <td colspan='14' class='underlined-text'></td>
  <td class='R'></td>
  <td/>
 </tr>
 <tr class='double-row'>
  <td/>
  <td colspan='18' class='underlined-text'></td>
  <td/>
  <td colspan='18' class='underlined-text'></td>
  <td/>
  <td class='L'></td>
  <td colspan='14' class='underlined-text'></td>
  <td class='R'></td>
  <td/>
  <td colspan='4' rowspan='2' class='data-value'><xsl:value-of select="Weight/@light"/></td>
  <td/>
  <td/>
  <td colspan='4' rowspan='2' class='data-value'><xsl:value-of select="Weight/@medium"/></td>
  <td/>
  <td/>
  <td colspan='4' rowspan='2' class='data-value'><xsl:value-of select="Weight/@heavy"/></td>
  <td/>
 </tr>
 <tr class='double-row'>
  <td/>
  <td colspan='18' class='underlined-text'></td>
  <td/>
  <td colspan='18' class='underlined-text'></td>
  <td/>
  <td class='L'></td>
  <td colspan='14' class='underlined-text'></td>
  <td class='R'></td>
 </tr>
 <tr class='double-row'>
  <td/>
  <td colspan='18' class='underlined-text'></td>
  <td/>
  <td colspan='18' class='underlined-text'></td>
  <td/>
  <td class='L'></td>
  <td colspan='14' class='underlined-text'></td>
  <td class='R'></td>
  <td/>
  <td colspan='4' class='carry-subhead'>LIGHT LOAD</td>
  <td/>
  <td/>
  <td colspan='4' class='carry-subhead'>MED LOAD</td>
  <td/>
  <td/>
  <td colspan='4' class='carry-subhead'>HEAVY LOAD</td>
 </tr>
 <tr class='double-row'>
  <td/>
  <td colspan='18' class='underlined-text'></td>
  <td/>
  <td colspan='18' class='underlined-text'></td>
  <td/>
  <td class='L'></td>
  <td colspan='14' class='underlined-text'></td>
  <td class='R'></td>
  <td/>
  <td/>
  <td/>
  <td/>
  <td/>
  <td/>
  <td/>
  <td/>
  <td/>
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
 <tr class='double-row'>
  <td/>
  <td colspan='18' class='underlined-text'></td>
  <td/>
  <td colspan='18' class='underlined-text'></td>
  <td/>
  <td class='L'></td>
  <td colspan='14' class='underlined-text'></td>
  <td class='R'></td>
  <td/>
  <td colspan='4' rowspan='2' class='data-value'><xsl:value-of select="Weight/@over-head"/></td>
  <td/>
  <td/>
  <td colspan='4' rowspan='2' class='data-value'><xsl:value-of select="Weight/@off-ground"/></td>
  <td/>
  <td/>
  <td colspan='4' rowspan='2' class='data-value'><xsl:value-of select="Weight/@drag"/></td>
  <td/>
 </tr>
 <tr class='double-row'>
  <td/>
  <td colspan='18' class='underlined-text'></td>
  <td/>
  <td colspan='18' class='underlined-text'></td>
  <td/>
  <td class='L'></td>
  <td colspan='14' class='underlined-text'></td>
  <td class='R'></td>
  <td/>
  <td/>
  <td/>
  <td/>
  <td/>
  <td/>
 </tr>
 <tr class='double-row'>
  <td/>
  <td colspan='18' class='underlined-text'></td>
  <td/>
  <td colspan='18' class='underlined-text'></td>
  <td/>
  <td class='L'></td>
  <td colspan='14' class='underlined-text'></td>
  <td class='R'></td>
  <td/>
  <td colspan='4' class='carry-subhead'>LIFT OVER HEAD</td>
  <td/>
  <td/>
  <td colspan='4' class='carry-subhead'>LIFT OFF GROUND</td>
  <td/>
  <td/>
  <td colspan='4' class='carry-subhead'>PUSH DRAG</td>
  <td/>
 </tr>
 <tr class='double-row'>
  <td/>
  <td colspan='18' class='underlined-text'></td>
  <td/>
  <td colspan='18' class='underlined-text'></td>
  <td/>
  <td class='L'></td>
  <td colspan='14' class='underlined-text'></td>
  <td class='R'></td>
  <td/>
  <td colspan='16' class='title'>MONEY</td>
  <td/>
 </tr>
 <tr class='double-row'>
  <td/>
  <td colspan='18' class='underlined-text'></td>
  <td/>
  <td colspan='18' class='underlined-text'></td>
  <td/>
  <td class='L'></td>
  <td colspan='14' class='underlined-text'></td>
  <td class='R'></td>
  <td/>
  <td colspan='5' class='money-table-cell L'>PP</td>
  <td colspan='11' class='money-table-cell R'></td>
  <td/>
 </tr>
 <tr class='double-row'>
  <td/>
  <td colspan='18' class='underlined-text'></td>
  <td/>
  <td colspan='18' class='underlined-text'></td>
  <td/>
  <td class='L'></td>
  <td colspan='14' class='underlined-text'></td>
  <td class='R'></td>
  <td/>
  <td colspan='5' class='money-table-cell L'>GP</td>
  <td colspan='11' class='money-table-cell R'></td>
  <td/>
 </tr>
 <tr class='double-row'>
  <td/>
  <td colspan='18' class='underlined-text'></td>
  <td/>
  <td colspan='18' class='underlined-text'></td>
  <td/>
  <td class='L'></td>
  <td colspan='14' class='underlined-text'></td>
  <td class='R'></td>
  <td/>
  <td colspan='5' class='money-table-cell L'>SP</td>
  <td colspan='11' class='money-table-cell R'></td>
  <td/>
 </tr>
 <tr class='double-row'>
  <td/>
  <td colspan='18' class='underlined-text'></td>
  <td/>
  <td colspan='18' class='underlined-text'></td>
  <td/>
  <td class='L'></td>
  <td colspan='14' class='underlined-text'></td>
  <td class='R'></td>
  <td/>
  <td colspan='5' class='money-table-cell L'>CP</td>
  <td colspan='11' class='money-table-cell R'></td>
  <td/>
 </tr>
 <tr class='double-row'>
  <td/>
  <td colspan='18' class='underlined-text'></td>
  <td/>
  <td colspan='18' class='underlined-text'></td>
  <td/>
  <td class='L'></td>
  <td colspan='14' class='underlined-text'></td>
  <td class='R'></td>
  <td/>
  <td colspan='5' class='money-table-cell L'>Art</td>
  <td colspan='11' class='money-table-cell R'></td>
  <td/>
 </tr>
 <tr class='double-row'>
  <td/>
  <td colspan='18' class='underlined-text'></td>
  <td/>
  <td colspan='18' class='underlined-text'></td>
  <td/>
  <td class='L'></td>
  <td colspan='14' class='underlined-text'></td>
  <td class='R'></td>
  <td/>
  <td colspan='5' class='money-table-cell L'>Gems</td>
  <td colspan='11' class='money-table-cell R'></td>
  <td/>
 </tr>
 <tr class='double-row'>
  <td/>
  <td colspan='18' class='underlined-text'></td>
  <td/>
  <td colspan='18' class='underlined-text'></td>
  <td/>
  <td class='B L'></td>
  <td colspan='14' class='underlined-text B'></td>
  <td class='R B'></td>
  <td/>
  <td colspan='5' class='money-table-cell L B'>Other (GP)</td>
  <td colspan='11' class='money-table-cell R B'></td>
  <td/>
 </tr>
</table>

</div>

</xsl:template>
</xsl:stylesheet>