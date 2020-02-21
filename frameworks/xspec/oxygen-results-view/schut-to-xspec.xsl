<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0"
                xmlns:x="http://www.jenitennison.com/xslt/xspec" 
                xmlns:xs="http://www.w3.org/2001/XMLSchema" 
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                exclude-result-prefixes="#all">

	<xsl:import href="../src/schematron/schut-to-xspec.xsl"/>    
    

   <xd:doc xmlns:xd="http://www.oxygenxml.com/ns/doc/xsl">
    <xd:desc>
      <xd:p>Created to embed the source location of a scenario before the SCH are merged.
       When a scenario fails, we can now open the file containing that particular scenario.</xd:p>
       <xd:p>It is basically overriding the template following template from ../src/schematron/schut-to-xspec.xsl</xd:p>
       
       <![CDATA[
            
    <xsl:template match="@* | node() | document-node()" as="node()" priority="-2">
        <xsl:call-template name="x:identity" />
    </xsl:template>
       
       ]]>
    </xd:desc>
  </xd:doc>
    <xsl:template match="x:scenario" priority="-2">
        <xsl:param name="source" tunnel="yes"></xsl:param>
        <xsl:copy>
            <!--
        
        			Oxygen Patch Start 
        
			        Keep the original location of the module.
        
        	-->
            <xsl:attribute name="source" select="if ($source and $source != '') then ($source) else (base-uri(.))"></xsl:attribute>
    		<!-- Oxygen Patch END -->
            <xsl:apply-templates select="@* | node()"/>
        </xsl:copy>
    </xsl:template>
    
       <xd:doc xmlns:xd="http://www.oxygenxml.com/ns/doc/xsl">
	    <xd:desc>
	      <xd:p>Overridden to keep the source location of a scenario. When a scenario fails,
	      we can now open the file containing that particular scenario.</xd:p>
	      <xd:p>This template is copied from ../src/schematron/schut-to-xspec.xsl</xd:p>
	    </xd:desc>
	    <xd:param name="xslt-version"></xd:param>
	  </xd:doc>
    <xsl:template match="x:import">
        <xsl:variable name="href" select="resolve-uri(@href, base-uri())"/>
        <xsl:choose>
            <xsl:when test="doc($href)//*[ 
                self::x:expect-assert | self::x:expect-not-assert | 
                self::x:expect-report | self::x:expect-not-report |
                self::x:expect-valid | self::x:description[@schematron] ]">
                <xsl:comment>BEGIN IMPORT "<xsl:value-of select="@href"/>"</xsl:comment>
                <xsl:apply-templates select="doc($href)/x:description/node()">
                    <!--
        
        Oxygen Patch Start 
        
        Keep the original location of the module.
        
        -->
                    <xsl:with-param name="source" select="resolve-uri($href, base-uri(.))" tunnel="yes"></xsl:with-param>
                    
                    <!-- Oxygen patch END -->
                </xsl:apply-templates>
                <xsl:comment>END IMPORT "<xsl:value-of select="@href"/>"</xsl:comment>
            </xsl:when>
            <xsl:otherwise>
                <xsl:next-match/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
        
</xsl:stylesheet>
