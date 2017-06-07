<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet version="2.0" 
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xs="http://www.w3.org/2001/XMLSchema"
  xmlns="http://www.w3.org/1999/XSL/TransformAlias"
  xmlns:test="http://www.jenitennison.com/xslt/unit-test"
  exclude-result-prefixes="#default test uuid local string"
  xmlns:x="http://www.jenitennison.com/xslt/xspec"
  xmlns:__x="http://www.w3.org/1999/XSL/TransformAliasAlias"
  xmlns:pkg="http://expath.org/ns/pkg"
  xmlns:impl="urn:x-xspec:compile:xslt:impl"
  
  xmlns:uuid="java:java.util.UUID"
  xmlns:local="http://oxygenxml.com/local"
  xmlns:string="java:java.lang.String"
  >

  <xsl:import href="../src/compiler/generate-xspec-tests.xsl"/>

  
  
  <xd:doc xmlns:xd="http://www.oxygenxml.com/ns/doc/xsl">
    <xd:desc>
      <xd:p>Overriden to keep the source location of a scenario. When a scenario fails,
      we can now open the file containing that particular scenario.</xd:p>
      <xd:p>This template is copied from generate-common-tests.xsl</xd:p>
    </xd:desc>
    <xd:param name="xslt-version"></xd:param>
  </xd:doc>
  <xsl:template match="x:scenario" mode="x:gather-specs">
    <xsl:param name="xslt-version" as="xs:string" tunnel="yes" required="yes"/>
    <!-- 
      
      Oxygen Patch START 
      
      Keep the location of the scenario for backmapping. 
    -->
    <x:scenario xslt-version="{$xslt-version}" source="{base-uri(.)}">
    <!-- Oxygen Patch END -->
      <xsl:copy-of select="@*"/>
      <xsl:apply-templates mode="x:gather-specs"/>
    </x:scenario>
  </xsl:template>
  
  <xd:doc xmlns:xd="http://www.oxygenxml.com/ns/doc/xsl">
    <xd:desc>
      <xd:p>Overriden to control the ID generation.</xd:p>
      <xd:p>This template is copied from <b>generate-common-tests.xsl</b></xd:p>
    </xd:desc>
    <xd:param name="xslt-version"></xd:param>
  </xd:doc>
  <xsl:template match="x:scenario" mode="x:generate-calls">
    <xsl:param name="vars" select="()" tunnel="yes" as="element(var)*"/>
    <xsl:call-template name="x:output-call">
<!-- 
      
      Oxygen Patch START 
      
      Control the ID generation.
-->
      <xsl:with-param name="name" select="local:generate-id(.)"/>
<!-- Oxygen Patch END -->
      <xsl:with-param name="last" select="empty(following-sibling::x:scenario)"/>
      <xsl:with-param name="params" as="element(param)*">
        <xsl:for-each select="$vars">
          <param name="{ @name }" select="${ @name }"/>
        </xsl:for-each>
      </xsl:with-param>
    </xsl:call-template>
  </xsl:template>
  

  <xd:doc xmlns:xd="http://www.oxygenxml.com/ns/doc/xsl">
    <xd:desc>
      <xd:p>Overriden to keep the source location of a scenario. When a scenario fails,
        we can now open the file containing that particular scenario.</xd:p>
      <xd:p>This template is copied from generate-xspec-tests.xsl</xd:p>
    </xd:desc>
    <xd:param name="xslt-version"></xd:param>
  </xd:doc>
  <xsl:template name="x:output-scenario">
    <xsl:param name="pending"   select="()" tunnel="yes" as="node()?"/>
    <xsl:param name="apply"     select="()" tunnel="yes" as="element(x:apply)?"/>
    <xsl:param name="call"      select="()" tunnel="yes" as="element(x:call)?"/>
    <xsl:param name="context"   select="()" tunnel="yes" as="element(x:context)?"/>
    <xsl:param name="variables" as="element(x:variable)*"/>
    <xsl:param name="params"    as="element(param)*"/>
    <xsl:variable name="pending-p" select="exists($pending) and empty(ancestor-or-self::*/@focus)"/>
    <!-- We have to create these error messages at this stage because before now
       we didn't have merged versions of the environment -->
    <xsl:if test="$context/@href and ($context/node() except $context/x:param)">
      <xsl:message terminate="yes">
        <xsl:text>ERROR in scenario "</xsl:text>
        <xsl:value-of select="x:label(.)" />
        <xsl:text>": can't set the context document using both the href</xsl:text>
        <xsl:text> attribute and the content of &lt;context&gt;</xsl:text>
      </xsl:message>
    </xsl:if>
    <xsl:if test="$call/@template and $call/@function">
      <xsl:message terminate="yes">
        <xsl:text>ERROR in scenario "</xsl:text>
        <xsl:value-of select="x:label(.)" />
        <xsl:text>": can't call a function and a template at the same time</xsl:text>
      </xsl:message>
    </xsl:if>
    <xsl:if test="$apply and $context">
      <xsl:message terminate="yes">
        <xsl:text>ERROR in scenario "</xsl:text>
        <xsl:value-of select="x:label(.)" />
        <xsl:text>": can't use apply and set a context at the same time</xsl:text>
      </xsl:message>
    </xsl:if>
    <xsl:if test="$apply and $call">
      <xsl:message terminate="yes">
        <xsl:text>ERROR in scenario "</xsl:text>
        <xsl:value-of select="x:label(.)" />
        <xsl:text>": can't use apply and call at the same time</xsl:text>
      </xsl:message>
    </xsl:if>
    <xsl:if test="$context and $call/@function">
      <xsl:message terminate="yes">
        <xsl:text>ERROR in scenario "</xsl:text>
        <xsl:value-of select="x:label(.)" />
        <xsl:text>": can't set a context and call a function at the same time</xsl:text>
      </xsl:message>
    </xsl:if>
    <xsl:if test="x:expect and not($call) and not($apply) and not($context)">
      <xsl:message terminate="yes">
        <xsl:text>ERROR in scenario "</xsl:text>
        <xsl:value-of select="x:label(.)" />
        <xsl:text>": there are tests in this scenario but no call, or apply or context has been given</xsl:text>
      </xsl:message>
    </xsl:if>
    
    <!-- 
        
        Oxygen Patch START 
        
        Use local:generate-id() to control the generated IDs.
      
      -->

    
    <template name="x:{local:generate-id(.)}">
      
      <!-- Oxygen Patch END -->
      
      <xsl:for-each select="$params">
        <param name="{ @name }" required="yes"/>
      </xsl:for-each>
      <message>
        <xsl:if test="$pending-p">
          <xsl:text>PENDING: </xsl:text>
          <xsl:if test="$pending != ''">
            <xsl:text>(</xsl:text>
            <xsl:value-of select="normalize-space($pending)"/>
            <xsl:text>) </xsl:text>
          </xsl:if>
        </xsl:if>
        <xsl:if test="parent::x:scenario">
          <xsl:text>..</xsl:text>
        </xsl:if>
        <xsl:value-of select="normalize-space(x:label(.))"/>
      </message>
      <!-- 
        
        Oxygen Patch START 
        
        Keep the location of the scenario for backmapping. 
      
      -->
      <x:scenario  source="{@source}">
      <!-- Oxygen Patch END -->
        <xsl:if test="$pending-p">
          <xsl:attribute name="pending" select="$pending" />
        </xsl:if>
        <xsl:sequence select="x:label(.)" />
        <xsl:apply-templates select="x:apply | x:call | x:context" mode="x:report" />
        <xsl:apply-templates select="$variables" mode="x:generate-declarations"/>
        <xsl:if test="not($pending-p) and x:expect">
          <variable name="x:result" as="item()*">
            <xsl:choose>
              <xsl:when test="$call/@template">
                <!-- Set up variables containing the parameter values -->
                <xsl:apply-templates select="$call/x:param[1]" mode="x:compile" />
                <!-- Create the template call -->
                <xsl:variable name="template-call">
                  <call-template name="{$call/@template}">
                    <xsl:for-each select="$call/x:param">
                      <with-param name="{@name}" select="${@name}">
                        <xsl:copy-of select="@tunnel, @as" />
                      </with-param>
                    </xsl:for-each>
                  </call-template>
                </xsl:variable>
                <xsl:choose>
                  <xsl:when test="$context">
                    <!-- Set up the $context variable -->
                    <xsl:apply-templates select="$context" mode="x:setup-context"/>
                    <!-- Switch to the context and call the template -->
                    <for-each select="$impl:context">
                      <xsl:copy-of select="$template-call" />
                    </for-each>
                  </xsl:when>
                  <xsl:otherwise>
                    <xsl:copy-of select="$template-call" />
                  </xsl:otherwise>
                </xsl:choose>
              </xsl:when>
              <xsl:when test="$call/@function">
                <!-- Set up variables containing the parameter values -->
                <xsl:apply-templates select="$call/x:param[1]" mode="x:compile" />
                <!-- Create the function call -->
                <sequence>
                  <xsl:attribute name="select">
                    <xsl:value-of select="$call/@function" />
                    <xsl:text>(</xsl:text>
                    <xsl:for-each select="$call/x:param">
                      <xsl:sort select="xs:integer(@position)" />
                      <xsl:text>$</xsl:text>
                      <xsl:value-of select="if (@name) then @name else generate-id()" />
                      <xsl:if test="position() != last()">, </xsl:if>
                    </xsl:for-each>
                    <xsl:text>)</xsl:text>
                  </xsl:attribute>
                </sequence>
              </xsl:when>
              <xsl:when test="$apply">
                <!-- TODO: FIXME: ... -->
                <xsl:message terminate="yes">
                  <xsl:text>The instruction t:apply is not supported yet!</xsl:text>
                </xsl:message>
                <!-- Set up variables containing the parameter values -->
                <xsl:apply-templates select="$apply/x:param[1]" mode="x:compile"/>
                <!-- Create the apply templates instruction -->
                <apply-templates>
                  <xsl:copy-of select="$apply/@select | $apply/@mode"/>
                  <xsl:for-each select="$apply/x:param">
                    <with-param name="{ @name }" select="${ @name }">
                      <xsl:copy-of select="@tunnel"/>
                    </with-param>
                  </xsl:for-each>
                </apply-templates>
              </xsl:when>
              <xsl:when test="$context">
                <!-- Set up the $context variable -->
                <xsl:apply-templates select="$context" mode="x:setup-context"/>
                <!-- Set up variables containing the parameter values -->
                <xsl:apply-templates select="$context/x:param[1]" mode="x:compile"/>
                <!-- Create the template call -->
                <apply-templates select="$impl:context">
                  <xsl:sequence select="$context/@mode" />
                  <xsl:for-each select="$context/x:param">
                    <with-param name="{@name}" select="${@name}">
                      <xsl:copy-of select="@tunnel, @as" />
                    </with-param>
                  </xsl:for-each>
                </apply-templates>
              </xsl:when>
              <xsl:otherwise>
                <!-- TODO: Adapt to a new error reporting facility (above usages too). -->
                <xsl:message terminate="yes">Error: cannot happen.</xsl:message>
              </xsl:otherwise>
            </xsl:choose>      
          </variable>
          <call-template name="test:report-value">
            <with-param name="value" select="$x:result" />
            <with-param name="wrapper-name" select="'x:result'" />
            <with-param name="wrapper-ns" select="'{ $xspec-ns }'"/>
          </call-template>
        </xsl:if>
        <xsl:call-template name="x:call-scenarios"/>
      </x:scenario>
    </template>
    <xsl:call-template name="x:compile-scenarios"/>
  </xsl:template>
  
  
 
  
  
  <!--
       OXYGEN PATCH START
       
       This method generates the same ID based on the label of the scenario. It is good enough...chances are that
       there are multiple scenarios with the same label....
   -->
  
  <xsl:function name="local:generate-id" as="xs:string">
    <xsl:param name="context"/>
    <xsl:variable name="seed" select="if($context/@label) then $context/@label else $context/x:label/text()" as="xs:string"/>
    
    <!--<xsl:value-of select="concat('x', uuid:toString(uuid:nameUUIDFromBytes(string:getBytes($seed))))"/>-->
    
    <xsl:value-of
      xmlns:ox="http://www.oxygenxml.com./xslt/xspec"
      select="ox:generate-id($seed)"/>
    
  </xsl:function>
  
  <!--
       OXYGEN PATCH END
   -->
 
</xsl:stylesheet>