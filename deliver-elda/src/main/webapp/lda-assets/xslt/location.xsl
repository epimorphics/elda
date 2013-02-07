<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

<xsl:import href="result-trimmed-osod.xsl" />

<xsl:template match="result" mode="extension">
        <script type="text/javascript" src="{$_resourceRoot}scripts/staging.js"></script>
</xsl:template>

</xsl:stylesheet>