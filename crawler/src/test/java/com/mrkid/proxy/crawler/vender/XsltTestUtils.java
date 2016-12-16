package com.mrkid.proxy.crawler.vender;

import org.apache.tika.io.IOUtils;
import org.jsoup.Jsoup;
import org.jsoup.helper.W3CDom;
import org.junit.Test;
import org.w3c.dom.Document;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.IOException;
import java.io.InputStream;

/**
 * User: xudong
 * Date: 16/12/2016
 * Time: 3:05 PM
 */
public class XsltTestUtils {

     public static void transform(String htmlPath, String xslPath, String htmEncoding) throws IOException,
             TransformerException {
        try (InputStream htmlStream = XsltTestUtils.class.getResourceAsStream(htmlPath);
             InputStream xslStream = XsltTestUtils.class.getResourceAsStream(xslPath);
        ) {
            Document document = new W3CDom().fromJsoup(Jsoup.parse(IOUtils.toString(htmlStream, htmEncoding)));

            Transformer transformer = TransformerFactory.newInstance().newTransformer(new StreamSource(xslStream));

            transformer.transform(new DOMSource(document), new StreamResult(System.out));
        }
    }
}
