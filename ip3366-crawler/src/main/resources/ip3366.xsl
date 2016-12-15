<?xml version="1.0" encoding="ISO-8859-1"?>

<xsl:stylesheet
        xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
        version="1.0">

    <xsl:output method="xml" indent="yes"/>

    <xsl:template match="/">
        <proxies>
            <xsl:for-each select="//table/tbody/tr">
                <proxy>
                    <host>
                        <xsl:value-of select="td[1]"/>
                    </host>
                    <port>
                        <xsl:value-of select="td[2]"/>
                    </port>
                    <location>
                        <xsl:value-of select="td[5]"/>
                    </location>
                </proxy>
            </xsl:for-each>
        </proxies>
    </xsl:template>
</xsl:stylesheet>


