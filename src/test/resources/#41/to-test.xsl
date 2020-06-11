<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xs="http://www.w3.org/2001/XMLSchema"
    exclude-result-prefixes="xs"
    version="2.0">
    <xsl:template name="code-to-code" as="attribute()+">
        <xsl:param name="value" as="attribute(value)" select="."/>
        <xsl:param name="codeMap" as="element()*"/>
        
        <xsl:variable name="out" as="element()">
            <xsl:choose>
                <xsl:when test="$codeMap[@inValue = $value]">
                    <xsl:copy-of select="$codeMap[@inValue = $value]"/>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:copy-of select="."/>
                </xsl:otherwise>
            </xsl:choose>
        </xsl:variable>
        
        <xsl:attribute name="code" select="$out/@code"/>
        <xsl:attribute name="codeSystem" select="$out/@codeSystem"/>
        <xsl:if test="$out/@displayName">
            <xsl:attribute name="displayName" select="$out/@displayName"/>
        </xsl:if>
    </xsl:template>
</xsl:stylesheet>