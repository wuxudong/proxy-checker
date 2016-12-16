package com.mrkid.proxy.crawler.vender.p66ip;

import com.mrkid.proxy.crawler.vender.XsltTestUtils;
import org.apache.tika.io.IOUtils;
import org.jsoup.Jsoup;
import org.jsoup.helper.W3CDom;
import org.junit.Test;
import org.w3c.dom.Document;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.InputStream;

/**
 * User: xudong
 * Date: 15/12/2016
 * Time: 1:24 PM
 */
public class P66IPXsltTest {

    @Test
    public void test() throws Exception {
        XsltTestUtils.transform("/html/p66ip.html", "/xsl/p66ip.xsl", "gb2312");
    }

}
