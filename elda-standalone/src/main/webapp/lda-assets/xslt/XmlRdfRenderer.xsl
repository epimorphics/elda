<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xs="http://www.w3.org/2001/XMLSchema">
	<xsl:output method="html" version="1.0" encoding="UTF-8" indent="yes"/>

	<xsl:template match="/result">
		<xsl:apply-templates select="*"/>
	</xsl:template>
	<xsl:template match="*[./child::*]" priority="-1">
		<p>
			<b><xsl:value-of select="name()"/></b>
			<xsl:if test="@href">
				: <a href="{ ./@href }"><xsl:value-of select="./@href"/></a>
			</xsl:if>
			<div style="margin-left: 30px;">
					<xsl:if test="name[@lang]">
						<table cellpadding="0" cellspacing="0">
							<tbody>
								<tr>
									<th colspan="2" align="left">name</th>
								</tr>
								<xsl:apply-templates select="name[@lang]" mode="lang_grid"/>
							</tbody>
						</table>
						<br/>
					</xsl:if>
					<xsl:if test="label[@lang]">
						<table cellpadding="0" cellspacing="0">
							<tbody>
								<tr>
									<th colspan="2" align="left">label</th>
								</tr>
								<xsl:apply-templates select="label[@lang]" mode="lang_grid"/>
							</tbody>
						</table>
						<br/>
					</xsl:if>
					<xsl:apply-templates select="*"/>
			</div>
		</p>
	</xsl:template>
 	<xsl:template match="*[@href and count(./*) = 0]" priority="-2">
		<b><xsl:value-of select="name()"/>: </b>
		<a href="{ ./@href }"><xsl:value-of select="./@href"/></a>
		<br/>
	</xsl:template> 
	<xsl:template match="comment[child::*] | name[child::*]">
		<table cellpadding="0" cellspacing="0">
			<tbody>
				<tr>
					<th colspan="2" align="left"><xsl:value-of select="name()"/></th>
				</tr>
				<xsl:apply-templates select="item" mode="lang_grid"/>
			</tbody>
		</table>
		<br/>
	</xsl:template>
	<xsl:template match="item | name | label" mode="lang_grid">
		<tr>
			<td style="padding: 0 20px 0 30px"><i><xsl:value-of select="@lang"/></i></td>
			<td><xsl:value-of select="text()"/></td>
		</tr>
	</xsl:template>
	<xsl:template match="notation">
		<b><xsl:value-of select="name()"/>: </b>
		<xsl:value-of select="concat(@datatype, ' - ', text())"/>
		<br/>
	</xsl:template>
	<xsl:template match="*" priority="-100"/>
</xsl:stylesheet>