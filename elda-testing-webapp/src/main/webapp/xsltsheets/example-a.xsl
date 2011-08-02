<xsl:stylesheet version="1.0"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

<xsl:output method="text"/>

<xsl:param name="establishmentNumber"/>
<xsl:param name="namespaces"/>

<xsl:template match="/">
	<xsl:apply-templates select="result" />
</xsl:template>

<xsl:template match="result">[
	<xsl:value-of select="$namespaces"/>
	PINKLE <xsl:value-of select="$establishmentNumber"/> PONKLE.
]</xsl:template>

</xsl:stylesheet>
        