<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="3.0"
                xmlns:local="http://oxygenxml.com/local"
                xmlns:x="http://www.jenitennison.com/xslt/xspec"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                exclude-result-prefixes="#all"
                >

  <xsl:import href="../src/compiler/compile-xquery-tests.xsl"/>

  <xsl:include href="id-generation.xsl"/>

  <xsl:template match="x:scenario" as="xs:string" mode="x:generate-id">
    <xsl:value-of select="local:generate-id(.)"/>
  </xsl:template>

</xsl:stylesheet>