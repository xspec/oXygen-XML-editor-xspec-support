<?xml version="1.0" encoding="UTF-8"?>


  <xsl:stylesheet version="2.0"
                xmlns="http://www.w3.org/1999/XSL/TransformAlias"
                xmlns:pkg="http://expath.org/ns/pkg"
                xmlns:test="http://www.jenitennison.com/xslt/unit-test"
                xmlns:x="http://www.jenitennison.com/xslt/xspec"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                exclude-result-prefixes="#all"
                
                
                xmlns:uuid="java:java.util.UUID"
  				xmlns:local="http://oxygenxml.com/local"
 				xmlns:string="java:java.lang.String"
                >
  
  

  <xsl:import href="../src/compiler/generate-xspec-tests.xsl"/>
  
  <xsl:include href="id-generation.xsl"/>

  
  
  <xsl:template match="x:scenario" as="xs:string" mode="x:generate-id">
    <xsl:value-of select="local:generate-id(.)"/>
  </xsl:template>

 
</xsl:stylesheet>