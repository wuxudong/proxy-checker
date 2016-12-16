<?xml version="1.0" encoding="ISO-8859-1"?>

<xsl:stylesheet
        version="2.0"
        xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
        xmlns:xhtml="http://www.w3.org/1999/xhtml">

    <xsl:output method="xml" indent="yes"/>

    <xsl:template match="/">
        <proxies>
            <xsl:for-each select="(//xhtml:table)[3]/xhtml:tbody/xhtml:tr[position()>1]">
                <proxy>
                    <host>
                        <xsl:value-of select="xhtml:td[1]"/>
                    </host>
                    <port>
                        <xsl:value-of select="xhtml:td[2]"/>
                    </port>
                    <location>
                        <xsl:value-of select="xhtml:td[3]"/>
                    </location>
                </proxy>
            </xsl:for-each>
        </proxies>

    </xsl:template>
</xsl:stylesheet>


