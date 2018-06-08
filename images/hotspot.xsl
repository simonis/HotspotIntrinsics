<?xml version='1.0'?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0">

<!--
<xsl:param name="base-url">https://openjdk.dev.java.net/source/browse/openjdk/jdk/trunk</xsl:param>
-->
<xsl:param name="base-url">http://hg.openjdk.java.net/jdk7/jdk7</xsl:param>
<xsl:param name="url-suffix"></xsl:param>
<xsl:param name="hg-tag">tip</xsl:param>

<xsl:output method="xml" omit-xml-declaration="no"/>
<xsl:preserve-space elements="*"/>

<xsl:template match="system|file">
  <span class="{name()}">
    <xsl:apply-templates/>
  </span>
</xsl:template>

<xsl:template match="jdkfile">
  <a href="{$base-url}/{substring-before(normalize-space(child::text()), '/')}{'/raw-file/'}{$hg-tag}{'/'}{substring-after(normalize-space(child::text()), '/')}{$url-suffix}" 
     class="{name()}">
    <xsl:apply-templates/>
  </a>
</xsl:template>

<!-- 
  We eed a way to write a &nbsp; in the XML soure file. Notice that
  we can't just write &nbsp; in the XML file because &nbsp; is not a
  valid XML entity. With this match rule we can now use <nbsp/> in the
  XML file and this will be converted into &nbsp; in the generated
  output. 
-->
<xsl:template match="nbsp">
  <xsl:text disable-output-escaping="yes">&amp;nbsp;</xsl:text>
</xsl:template>

<xsl:template match="abstract|warning|mh1|mh2|mh3|mh4">
  <div class="{name()}">
    <xsl:apply-templates/>
  </div>
</xsl:template>

<xsl:template match="calling_hirarchy">
  <table class="calling_hirarchy"><xsl:text>&#x0a;</xsl:text>
    <xsl:variable name="maxIndent" select="max(call/@level)"/>
    <xsl:for-each select="call">
      <xsl:variable name="type" select="@type"/>
      <xsl:choose>
        <xsl:when test="position() = 1 or number(@level) &lt;= number(preceding-sibling::call[1]/@level)">
          <xsl:text>  </xsl:text>
            <tr>
          <xsl:text>&#x0a;</xsl:text>
            <xsl:text>    </xsl:text>
              <td colspan="{$maxIndent + 1 - @level}" class="{$type} indent_level_{@level}">
            <xsl:text>&#x0a;</xsl:text>
              <xsl:text>      </xsl:text>
                <xsl:apply-templates/>
              <xsl:text>&#x0a;</xsl:text>
            <xsl:text>    </xsl:text>
              </td>
            <xsl:text>&#x0a;</xsl:text>
          <xsl:text>  </xsl:text>
            </tr>
          <xsl:text>&#x0a;</xsl:text>
        </xsl:when>
        <xsl:otherwise>
	  <xsl:variable name="following_siblings_with_smaller_level" 
                        select="following-sibling::call[number(@level) &lt; number(current()/@level)]"/>
          <xsl:choose>
            <xsl:when test="number(count($following_siblings_with_smaller_level)) &gt; number(0)">
              <xsl:text>  </xsl:text>
                <tr>
              <xsl:text>&#x0a;</xsl:text>
                <xsl:text>    </xsl:text>
	          <td rowspan="{last() - position() - count($following_siblings_with_smaller_level[1]/following-sibling::call)}">
                  <xsl:text disable-output-escaping="yes">&amp;nbsp;</xsl:text>
                  </td>	  
                <xsl:text>&#x0a;</xsl:text>
                <xsl:text>    </xsl:text>
                  <td colspan="{$maxIndent + 1 - @level}" class="{$type} indent_level_{@level}">
                <xsl:text>&#x0a;</xsl:text>
                  <xsl:text>      </xsl:text>
                      <xsl:apply-templates/>
                  <xsl:text>&#x0a;</xsl:text>
                <xsl:text>    </xsl:text>
                  </td>
                <xsl:text>&#x0a;</xsl:text>
              <xsl:text>  </xsl:text>
                </tr>
              <xsl:text>&#x0a;</xsl:text>
            </xsl:when>
            <xsl:otherwise>
              <xsl:text>  </xsl:text>
                <tr>
              <xsl:text>&#x0a;</xsl:text>
                <xsl:text>    </xsl:text>
	          <td rowspan="{last() - position() + 1}">
                    <xsl:text disable-output-escaping="yes">&amp;nbsp;</xsl:text>
                  </td>	  
                <xsl:text>&#x0a;</xsl:text>
                <xsl:text>    </xsl:text>
                  <td colspan="{$maxIndent + 1 - @level}" class="{$type} indent_level_{@level}">
                <xsl:text>&#x0a;</xsl:text>
                  <xsl:text>    </xsl:text>
                    <xsl:apply-templates/>
                  <xsl:text>&#x0a;</xsl:text>
                <xsl:text>    </xsl:text>
                  </td>
                <xsl:text>&#x0a;</xsl:text>
              <xsl:text>  </xsl:text>
                </tr>
              <xsl:text>&#x0a;</xsl:text>
            </xsl:otherwise>
          </xsl:choose>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:for-each>
  </table>
</xsl:template>

<xsl:template match="*">
  <xsl:copy>
    <xsl:copy-of select="@*"/>
    <xsl:apply-templates/>
  </xsl:copy>
</xsl:template>

</xsl:stylesheet>
