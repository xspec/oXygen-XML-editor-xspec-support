<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xs="http://www.w3.org/2001/XMLSchema"
    exclude-result-prefixes="xs"
    xmlns:math="math"
    version="2.0">
    
    <xsl:function name="math:sum">
        <xsl:param name="p1" as="xs:integer"/>
        <xsl:param name="p2" as="xs:integer"/>
        
        <xsl:value-of select="$p1 + $p2"/>
    </xsl:function>
</xsl:stylesheet>