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

  
  
  <xd:doc xmlns:xd="http://www.oxygenxml.com/ns/doc/xsl">
    <xd:desc>
      <xd:p>Overriden to keep the source location of a scenario. When a scenario fails,
      we can now open the file containing that particular scenario.</xd:p>
      <xd:p>This template is copied from generate-common-tests.xsl</xd:p>
    </xd:desc>
    <xd:param name="xslt-version"></xd:param>
  </xd:doc>
  <xsl:template match="x:scenario" as="element(x:scenario)" mode="x:gather-specs">
      <xsl:param name="xslt-version" as="xs:decimal" tunnel="yes" required="yes"/>
      <xsl:copy>
      
      <!-- 
      
      Oxygen Patch START 
      
      Keep the location of the scenario for backmapping. 
    -->
    <xsl:if test="not(@source) or @source = ''">
        <xsl:attribute name="source" select="base-uri(.)"></xsl:attribute>
      </xsl:if>
    <!-- Oxygen Patch END -->
    
         <xsl:attribute name="xslt-version" select="$xslt-version" />
         <xsl:copy-of select="@*"/>
         <xsl:apply-templates mode="#current"/>
      </xsl:copy>
   </xsl:template>
  
  <xd:doc xmlns:xd="http://www.oxygenxml.com/ns/doc/xsl">
    <xd:desc>
      <xd:p>Overriden to control the ID generation.</xd:p>
      <xd:p>This template is copied from <b>generate-common-tests.xsl</b></xd:p>
    </xd:desc>
    <xd:param name="xslt-version"></xd:param>
  </xd:doc>
  <xsl:template match="x:scenario" mode="x:generate-calls">
      <xsl:param name="vars" select="()" tunnel="yes" as="element(x:var)*"/>
      <xsl:call-template name="x:output-call">
      
  <!-- 
      
      Oxygen Patch START 
      
      Control the ID generation.
-->    
         <!--  <xsl:with-param name="local-name" select="generate-id()"/>   -->
         
          <xsl:with-param name="local-name" select="local:generate-id(.)"/>
          
<!-- Oxygen Patch END -->

         
         <xsl:with-param name="last" select="empty(following-sibling::x:scenario)"/>
         <xsl:with-param name="params" as="element(param)*">
            <xsl:for-each select="x:distinct-variable-names($vars)">
               <param name="{ @name }" select="${ @name }">
                    <xsl:sequence select="x:copy-namespaces(.)"/>
               </param>
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
  <xsl:template name="x:output-scenario" as="element(xsl:template)+">
  <xsl:context-item as="element(x:scenario)" use="required"
    use-when="element-available('xsl:context-item')" />

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
    
    <!-- <template name="{x:xspec-name(.,generate-id())}"> -->
      
      <!-- Oxygen Patch END -->
  
  
    <xsl:sequence select="x:copy-namespaces(.)"/>
    <xsl:for-each select="$params">
      <param name="{ @name }" required="yes">
        <xsl:sequence select="x:copy-namespaces(.)"/>
      </param>
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

    <xsl:element name="{x:xspec-name(.,'scenario')}" namespace="{$xspec-namespace}">
    
          <!-- 
        
        Oxygen Patch START 
        
        Keep the location of the scenario for backmapping. 

        Keep the template ID and source so we can run just this scenario later on.
      
      -->
      <xsl:attribute name="source" select="./@source"/>
      <xsl:attribute name="template-id" select="local:generate-id(.)"/>
    
          <!-- Oxygen Patch END -->
          
      <!-- Create @pending generator -->
      <xsl:if test="$pending-p">
        <xsl:sequence select="x:create-pending-attr-generator($pending)" />
      </xsl:if>

      <!-- Create x:label directly -->
      <xsl:sequence select="x:label(.)" />

      <!-- Handle variables and apply/call/context in document order,
           instead of apply/call/context first and variables second. -->
      <xsl:for-each select="$variables | x:apply | x:call | x:context">
        <xsl:choose>
          <xsl:when test="self::x:apply or self::x:call or self::x:context">
            <!-- Create report generator -->
            <xsl:apply-templates select="." mode="x:report" />
          </xsl:when>
          <xsl:otherwise>
            <xsl:apply-templates select="." mode="x:generate-declarations"/>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:for-each>
      <xsl:if test="not($pending-p) and x:expect">
        <variable name="{x:xspec-name(.,'result')}" as="item()*">
          <xsl:choose>
            <xsl:when test="$call/@template">
              <!-- Set up variables containing the parameter values -->
              <xsl:apply-templates select="$call/x:param[1]" mode="x:compile" />
              <!-- Create the template call -->
              <xsl:variable name="template-call">
                <xsl:call-template name="x:enter-sut">
                  <xsl:with-param name="instruction" as="element(xsl:call-template)">
                    <call-template name="{$call/@template}">
                      <xsl:sequence select="x:copy-namespaces($call)" />
                      <xsl:for-each select="$call/x:param">
                        <with-param name="{@name}" select="${@name}">
                          <xsl:copy-of select="@tunnel, @as" />
                        </with-param>
                      </xsl:for-each>
                    </call-template>
                  </xsl:with-param>
                </xsl:call-template>
              </xsl:variable>
              <xsl:choose>
                <xsl:when test="$context">
                  <!-- Set up the $impl:context variable -->
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
              <xsl:call-template name="x:enter-sut">
                <xsl:with-param name="instruction" as="element(xsl:sequence)">
                  <sequence>
                    <xsl:sequence select="x:copy-namespaces($call)"/>
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
                </xsl:with-param>
              </xsl:call-template>
            </xsl:when>
            <xsl:when test="$apply">
               <!-- TODO: FIXME: ... -->
               <xsl:message terminate="yes">
                  <xsl:text>The instruction t:apply is not supported yet!</xsl:text>
               </xsl:message>
               <!-- Set up variables containing the parameter values -->
               <xsl:apply-templates select="$apply/x:param[1]" mode="x:compile"/>
               <!-- Create the apply templates instruction.
                 This code path, particularly with @catch, has not been tested. -->
               <xsl:call-template name="x:enter-sut">
                 <xsl:with-param name="instruction" as="element(xsl:apply-templates)">
                   <apply-templates>
                     <xsl:sequence select="x:copy-namespaces($apply)" /><!--TODO: Check that this line works after x:apply is implemented.-->
                     <xsl:copy-of select="$apply/@select | $apply/@mode"/>
                     <xsl:for-each select="$apply/x:param">
                       <with-param name="{ @name }" select="${ @name }">
                         <xsl:copy-of select="@tunnel"/>
                       </with-param>
                     </xsl:for-each>
                   </apply-templates>
                 </xsl:with-param>
               </xsl:call-template>
            </xsl:when>
            <xsl:when test="$context">
              <!-- Set up the $impl:context variable -->
              <xsl:apply-templates select="$context" mode="x:setup-context"/>
              <!-- Set up variables containing the parameter values -->
              <xsl:apply-templates select="$context/x:param[1]" mode="x:compile"/>
              <!-- Create the template call -->
              <xsl:call-template name="x:enter-sut">
                <xsl:with-param name="instruction" as="element(xsl:apply-templates)">
                  <apply-templates select="$impl:context">
                    <xsl:sequence select="x:copy-namespaces($context)" />
                    <xsl:sequence select="$context/@mode" />
                    <xsl:for-each select="$context/x:param">
                      <with-param name="{@name}" select="${@name}">
                        <xsl:copy-of select="@tunnel, @as" />
                      </with-param>
                    </xsl:for-each>
                  </apply-templates>
                </xsl:with-param>
              </xsl:call-template>
            </xsl:when>
            <xsl:otherwise>
               <!-- TODO: Adapt to a new error reporting facility (above usages too). -->
               <xsl:message terminate="yes">Error: cannot happen.</xsl:message>
            </xsl:otherwise>
          </xsl:choose>      
        </variable>
        <call-template name="test:report-sequence">
          <with-param name="sequence" select="${x:xspec-name(.,'result')}" />
          <with-param name="wrapper-name" as="xs:string">
            <xsl:value-of select="x:xspec-name(.,'result')" />
          </with-param>
        </call-template>
      </xsl:if>
      <xsl:call-template name="x:call-scenarios"/>
    </xsl:element>
  </template>
  <xsl:call-template name="x:compile-scenarios"/>
</xsl:template>
  
  
  
 
 
</xsl:stylesheet>