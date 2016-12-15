package com.mrkid.proxy.kuaidaili;

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
import java.io.StringWriter;
import java.io.Writer;

/**
 * User: xudong
 * Date: 15/12/2016
 * Time: 1:24 PM
 */
public class KuaidailiXsltTest {
    @Test
    public void test() throws Exception {
        try (InputStream htmlStream = getClass().getResourceAsStream("/kuaidaili.html");
             InputStream xslStream = getClass().getResourceAsStream("/kuaidaili.xsl");
        ) {
            Document document = new W3CDom().fromJsoup(Jsoup.parse(IOUtils.toString(htmlStream, "utf-8")));

            Transformer transformer = TransformerFactory.newInstance().newTransformer(new StreamSource(xslStream));

            transformer.transform(new DOMSource(document), new StreamResult(System.out));
        }
    }

}
