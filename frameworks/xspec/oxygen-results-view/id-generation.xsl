<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xs="http://www.w3.org/2001/XMLSchema"
    exclude-result-prefixes="xs"
    xmlns:local="http://oxygenxml.com/local"
    xmlns:x="http://www.jenitennison.com/xslt/xspec"
    version="2.0">
    
    
    <!--
       OXYGEN PATCH START
       
       This method generates the same ID based on the label of the scenario. It is good enough...chances are that
       there are multiple scenarios with the same label....
   -->
    
    <xsl:function name="local:generate-id" as="xs:string">
        <xsl:param name="context"/>
        <!--<xsl:variable name="seed" select="if($context/@label) then $context/@label else $context/x:label/text()" as="xs:string"/>-->
        
        <xsl:variable name="seed" select="local:collect-labels($context)" as="xs:string"/>
        
        <xsl:value-of
            xmlns:ox="http://www.oxygenxml.com./xslt/xspec"
            select="ox:generate-id($seed)"/>
        
    </xsl:function>
    
    <!-- 
    Collects all labels from the current scenario and all of its ancestors.
    -->
    <xsl:function name="local:collect-labels" as="xs:string">
        <xsl:param name="scenario" as="node()?"/>
        
        <!-- This counter should us generate an unique label from each scenario -->
        <xsl:variable name="index" select="count($scenario/preceding-sibling::x:scenario)"/>
        <xsl:variable name="l">
            <xsl:choose>
                <xsl:when test="$scenario/@label">
                    <xsl:value-of select="$scenario/@label"/>
                </xsl:when>
                <xsl:when test="$scenario/x:label">
                    <xsl:value-of select="$scenario/x:label"/>
                </xsl:when>
                <xsl:otherwise> </xsl:otherwise>
            </xsl:choose>
            <xsl:value-of select="concat('(', $index, ')')"/>
        </xsl:variable>
        
        <xsl:variable name="ancestorLabel">
            <xsl:variable name="ancestor" select="$scenario/ancestor::*[@label or x:label][1]"/>
            <xsl:if test="$ancestor">
                <xsl:value-of select="local:collect-labels($ancestor)"/>
            </xsl:if>
        </xsl:variable>
        
        
        <xsl:value-of select="if (string-length($ancestorLabel) > 0) then concat($ancestorLabel, ' / ', $l) else $l"/>
        
    </xsl:function>
    
    
    <!--
       OXYGEN PATCH END
   -->
</xsl:stylesheet>