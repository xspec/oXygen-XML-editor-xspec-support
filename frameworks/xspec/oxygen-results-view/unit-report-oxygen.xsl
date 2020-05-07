<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet version="3.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:x="http://www.jenitennison.com/xslt/xspec"
                xmlns:test="http://www.jenitennison.com/xslt/unit-test"
                xmlns:pkg="http://expath.org/ns/pkg"
                xmlns:xhtml="http://www.w3.org/1999/xhtml"
                xmlns:fn="http://www.w3.org/2005/xpath-functions"
                xmlns:saxon="http://saxon.sf.net/"
                exclude-result-prefixes="x xs test pkg xhtml fn xsl saxon">
    
    <xsl:param name="report-css-uri" select="
        resolve-uri('test-report.css')"/>
    
    <xsl:param name="report-js-uri" select="
        resolve-uri('test-report.js')"/>
        
    <xsl:output name="escaped" method="xml" omit-xml-declaration="yes" indent="yes"/>
    
    <xsl:output  method="html" omit-xml-declaration="yes" indent="yes" />
    
    
    <xsl:import href="../src/reporter/format-utils.xsl"/>
    
    <xsl:import href="../src/common/xspec-utils.xsl"/>
    
    
    <!-- 
      
      Oxygen Patch START
      
      Exclude all prefixes.
      
      TODO Which prefixes to we actually need excluding????
  
  -->
    
    <!-- Copied from format-utils.xsl -->
    <xsl:output name="x:report" exclude-result-prefixes="#all" method="xml" indent="yes"/>
    <!-- Oxygen Patch END -->

    <xsl:template match="x:report">
        <html>
            <head>
                <link rel="stylesheet" type="text/css" href="{ $report-css-uri }"/>
                <script type="text/javascript" src="{ $report-js-uri }"/>
                
            </head>
            <body>
            	<xsl:apply-templates select="x:scenario"/>
            </body>
        </html>
    </xsl:template>
    
    <xsl:template match="x:scenario">
        <div class="testsuite">
            <xsl:attribute name="data-name" select="x:label"/>
            <xsl:attribute name="id" select="@id"/>
            <xsl:attribute name="data-xspec" select="@xspec"/>
            <xsl:attribute name="data-tests" select="count(.//x:test)"/>
            <xsl:attribute name="data-failures" select="count(.//x:test[@successful='false'])"/>
            <p style="margin:0px;">
            <span><xsl:value-of select="x:label"/></span>
            <span>&#160;</span>
            <a class="button" onclick="runScenario(this)" >Run</a>
            </p>
            <xsl:apply-templates select="x:test"/>
            <xsl:apply-templates select="x:scenario" />
        </div>
    </xsl:template>

    <xsl:template match="x:scenario" mode="nested">
        <xsl:param name="prefix" select="''"/>
        <xsl:variable name="prefixed-label" select="concat($prefix, x:label, ' ')"/>
        <xsl:apply-templates select="x:test">
            <xsl:with-param name="prefix" select="$prefixed-label"/>
        </xsl:apply-templates>
        <xsl:apply-templates select="x:scenario" mode="nested">
            <xsl:with-param name="prefix" select="$prefixed-label"/>
        </xsl:apply-templates>
    </xsl:template>
    
    <xsl:template match="x:test">
        <xsl:param name="prefix"/>
        
        <xsl:variable name="id" select="generate-id()"/>
        <div class="testcase" data-name="{xs:string(x:label/text())}">
            <xsl:variable name="status">
                <xsl:choose>
                    <xsl:when test="@pending">skipped</xsl:when>
                    <xsl:when test="@successful='true'">passed</xsl:when>
                    <xsl:otherwise>failed</xsl:otherwise>
                </xsl:choose>
            </xsl:variable>
            <p class="{$status}">
                <span class="test-{$status}" onclick="toggleResult(this)"><xsl:value-of select="concat($prefix, x:label)"/></span>
                <span>&#160;</span>
                <a class="button" onclick="showTest(this)">Show</a>
                <xsl:if test="@successful='false'">
                    <span>&#160;</span>
                    <a class="button" onclick="toggleResult(this)">Q-Diff</a>
                    <span>&#160;</span>
                    <a class="button" onclick="showDiff(this)">Diff</a>
                </xsl:if>

            </p>
            <xsl:choose>
                
                <xsl:when test="@successful='false'">
                    <div class="failure" id="{$id}" style="display:none;">
                        <xsl:call-template name="diff"/>
                        
                        <!--
                        <xsl:apply-templates select="x:expect"/>
                        -->
                        
                    </div>
                    <xsl:call-template name="embedDiff"/>
                </xsl:when>
            </xsl:choose>
        </div>
    </xsl:template>
    
    
    <xsl:template name="diff">
        <xsl:variable name="result" as="element(x:result)"
            select="if (x:result) then x:result else ../x:result" />
        <table class="xspecResult">
            <thead>
                <tr>
                    <th style="font-size:14px;">Result</th>
                    <th style="font-size:14px;">
                        <xsl:choose>
                            <xsl:when test="x:result">Expecting</xsl:when>
                            <xsl:otherwise>Expected</xsl:otherwise>
                        </xsl:choose>
                    </th>
                </tr>
            </thead>
            <tbody>
                <tr>
                    <td>
                        <xsl:apply-templates select="$result" mode="x:format-result">
                            <xsl:with-param name="result-to-compare-with" select="x:expect" />
                        </xsl:apply-templates>
                    </td>
                    <td>
                        <xsl:choose>
                            <xsl:when test="not(x:result) and x:expect/@test">
                                <pre><xsl:value-of select="@test" /></pre>
                            </xsl:when>
                            <xsl:otherwise>
                                <xsl:apply-templates select="x:expect" mode="x:format-result">
                                    <xsl:with-param name="result-to-compare-with" select="$result" />
                                </xsl:apply-templates>
                            </xsl:otherwise>
                        </xsl:choose>
                    </td>
                </tr>
            </tbody>
        </table>
    </xsl:template>
    
    <xsl:template name="embedDiff">
        <xsl:variable name="result" as="element(x:result)"
            select="if (x:result) then x:result else ../x:result" />
        
        <pre class="embeded.diff.data" style="display:none;">
            <div class="embeded.diff.result" style="white-space:pre;">
                <xsl:apply-templates select="$result" mode="serialize-result-for-diff"/>
            </div>
            
            <div class="embeded.diff.expected" style="white-space:pre;">
                <xsl:apply-templates select="x:expect" mode="serialize-result-for-diff"/>
            </div>
        </pre>
    </xsl:template>
    
    
    

    <!-- Serializes the expected/actual results for Oxugen's Diff view.  -->
    <xsl:template match="x:expect | x:result" mode="serialize-result-for-diff">
        <xsl:variable name="tmp">
            <xsl:choose>
                <xsl:when test="@select and node()">
                    <!-- Applies the XPath filter to get the subset of interest. -->
                    <xsl:variable name="filtered">
                        <xsl:variable name="expr" select="if (normalize-space(@select) = '/') then concat('.', @select, 'node()') else concat('.', @select)"/>
                        <xsl:evaluate xpath="$expr" context-item="."/>
                    </xsl:variable>
                    <!-- Convert to a version ready to be serialized. -->
                    <xsl:apply-templates select="$filtered" mode="copy">
                        <xsl:with-param name="level" select="0"/>
                    </xsl:apply-templates>   
                </xsl:when>
                <xsl:when test="node()">
                    <xsl:apply-templates select="node()" mode="copy">
                        <xsl:with-param name="level" select="0"/>
                    </xsl:apply-templates>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:value-of select="@select"/>
                </xsl:otherwise>
            </xsl:choose>
        </xsl:variable>
        
        <xsl:variable name="params">
            <output:serialization-parameters 
                xmlns:output="http://www.w3.org/2010/xslt-xquery-serialization">
                <output:omit-xml-declaration value="yes"/>
            </output:serialization-parameters>            
        </xsl:variable> 
        
        <xsl:value-of select="serialize($tmp, $params/*:serialization-parameters)" />
    </xsl:template>
    
    
    <xsl:template match="node() | @*" mode="serialize-result-for-diff">
        <xsl:copy>
            <xsl:apply-templates select="node() | @*"/>
        </xsl:copy>
    </xsl:template>
    
    <!-- Wrapper for significant spaces. -->
    <xsl:template match="test:ws" mode="copy">
      <xsl:analyze-string select="." regex="\s">
        <xsl:matching-substring>
          <xsl:choose>
            <xsl:when test=". = '&#xA;'">\n</xsl:when>
            <xsl:when test=". = '&#xD;'">\r</xsl:when>
            <xsl:when test=". = '&#x9;'">\t</xsl:when>
            <xsl:when test=". = ' '">.</xsl:when>
          </xsl:choose>
        </xsl:matching-substring>
      </xsl:analyze-string>
</xsl:template>
    
    <xsl:template match="node() | @*" mode="copy" >
        <xsl:param name="level" as="xs:integer"/>
        <xsl:message>Copy|<xsl:value-of select="node-name(.)"/>|<xsl:value-of select="."/>|</xsl:message>
        <xsl:copy copy-namespaces="no">
            <xsl:apply-templates select="node() | @*" mode="copy">
                <xsl:with-param name="level" select="$level + 1"></xsl:with-param>
            </xsl:apply-templates>
        </xsl:copy>
    </xsl:template>
    
    <xsl:template match="text()[not(normalize-space())]" mode="copy">
        <xsl:param name="level" as="xs:integer"  />
        <xsl:value-of select="concat('&#xA;', substring(., string-length(.) - 3*$level + 1))" />
    </xsl:template> 
    
    <!-- If the indent node is last we consider the indent level smaller -->
    <xsl:template match="text()[not(normalize-space())][empty(following-sibling::*)]" mode="copy">
        <xsl:param name="level" as="xs:integer"  />
        <xsl:value-of select="concat('&#xA;', substring(., string-length(.) - 3*($level - 1) + 1))" />
    </xsl:template> 
    
    
    
    
    <xd:doc xmlns:xd="http://www.oxygenxml.com/ns/doc/xsl">
        <xd:desc>
            <xd:p> Copied from format-xspec-report.xsl to comment the XPath block.</xd:p>
            <xd:p>This template is copied from format-xspec-report.xsl</xd:p>
        </xd:desc>
        <xd:param name="xslt-version"></xd:param>
    </xd:doc>
    <!-- Formats the Actual Result or the Expected Result in HTML -->
    <xsl:template match="element()" as="element()+" mode="x:format-result">
        <xsl:param name="result-to-compare-with" as="element()?" required="yes" />
        
        <!-- True if this element represents Expected Result -->
        <xsl:variable name="expected" as="xs:boolean" select=". instance of element(x:expect)" />
        
        <xsl:choose>
            <xsl:when test="@href or node() or (@select eq '/self::document-node()')">
                <xsl:if test="@select">
                    <!--
                    <p>
                        <xsl:text>XPath </xsl:text>
                        <code>
                            <xsl:if test="exists($result-to-compare-with)">
                                <xsl:attribute name="class" select="
                                    test:comparison-html-class(
                                    @select,
                                    $result-to-compare-with/@select,
                                    $expected,
                                    false())" />
                            </xsl:if>
                            <xsl:value-of select="@select" />
                        </code>
                        <xsl:text> from:</xsl:text>
                    </p>
                    -->
                </xsl:if>
                
                <xsl:choose>
                    <xsl:when test="@href">
                        <p><a href="{@href}"><xsl:value-of select="x:format-uri(@href)" /></a></p>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:variable name="indentation"
                            select="string-length(substring-after(text()[1], '&#xA;'))" />
                        <pre>
            <xsl:choose>
              <!-- Serialize the result while performing comparison -->
              <xsl:when test="exists($result-to-compare-with)">
                <xsl:variable name="nodes-to-compare-with" as="node()*"
                  select="if ($result-to-compare-with/@href)
                          then document($result-to-compare-with/@href)/node()
                          else $result-to-compare-with/node()" />
                <xsl:for-each select="node()">
                  <xsl:variable name="significant-pos" as="xs:integer?" select="test:significant-position(.)" />
                  <xsl:apply-templates select="." mode="test:serialize">
                    <xsl:with-param name="indentation" select="$indentation" tunnel="yes" />
                    <xsl:with-param name="perform-comparison" select="true()" tunnel="yes" />
                    <xsl:with-param name="node-to-compare-with" select="$nodes-to-compare-with[test:significant-position(.) eq $significant-pos]" />
                    <xsl:with-param name="expected" select="$expected" />
                  </xsl:apply-templates>
                </xsl:for-each>
              </xsl:when>

              <!-- Serialize the result without performing comparison -->
              <xsl:otherwise>
                <xsl:apply-templates select="node()" mode="test:serialize">
                  <xsl:with-param name="indentation" select="$indentation" tunnel="yes" />
                </xsl:apply-templates>
              </xsl:otherwise>
            </xsl:choose>
          </pre>
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:when>
            <xsl:otherwise>
                <pre><xsl:value-of select="@select" /></pre>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
    
    
<!--    
    
    <xsl:template match="x:expect[@select]">
        <xsl:text>Expected: </xsl:text><xsl:value-of select="x:expect/@select"/>
    </xsl:template>
    
    <xsl:template match="x:expect">
        <xsl:variable as="element(output:serialization-parameters)" name="serialization-parameters"
            xmlns:output="http://www.w3.org/2010/xslt-xquery-serialization">
            <output:serialization-parameters>
                <output:omit-xml-declaration value="yes"/>
            </output:serialization-parameters>
        </xsl:variable>
        <xsl:value-of select="fn:serialize(., $serialization-parameters)"></xsl:value-of>
    </xsl:template>
    
    -->




</xsl:stylesheet>
