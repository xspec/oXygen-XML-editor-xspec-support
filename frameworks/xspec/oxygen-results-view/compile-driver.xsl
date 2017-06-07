<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns="http://www.w3.org/1999/XSL/TransformAlias"
    xmlns:x="http://www.jenitennison.com/xslt/xspec">

    <xsl:output indent="yes"/>

    <xsl:param name="xspec.template.name.entrypoint" as="xs:string*"/>

    <xsl:param name="compiled-stylesheet-uri" as="xs:string*"/>

    <xsl:namespace-alias stylesheet-prefix="" result-prefix="xsl"/>

    <xsl:template match="/">
        <xsl:apply-templates select="x:description"/>
    </xsl:template>


    <xsl:template match="x:description">
        <!-- The compiled stylesheet element. -->
        <stylesheet version="{( @xslt-version, '2.0' )[1]}">
            <import href="{$compiled-stylesheet-uri}"/>
            
            <xsl:if test="string-length($xspec.template.name.entrypoint) > 0">
                <!-- Override the main compiled template if we must execute specific scenarios. -->
                <template name="x:main">

                    <result-document format="x:report">

                        <x:report>
                            <!-- Generate calls to the compiled top-level scenarios. -->
                            <xsl:for-each select="tokenize($xspec.template.name.entrypoint, ' ')">
                                <call-template name="x:{.}"/>
                            </xsl:for-each>
                        </x:report>
                    </result-document>
                </template>
            </xsl:if>
        </stylesheet>
    </xsl:template>


</xsl:stylesheet>
