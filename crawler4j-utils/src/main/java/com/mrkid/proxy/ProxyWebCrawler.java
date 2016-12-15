package com.mrkid.proxy;

import com.mrkid.proxy.dto.ProxyDTO;
import com.mrkid.proxy.dto.ProxyListDTO;
import edu.uci.ics.crawler4j.crawler.Page;
import edu.uci.ics.crawler4j.crawler.WebCrawler;
import edu.uci.ics.crawler4j.parser.HtmlParseData;
import org.apache.commons.lang3.StringEscapeUtils;
import org.jsoup.Jsoup;
import org.jsoup.helper.W3CDom;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

/**
 * User: xudong
 * Date: 15/12/2016
 * Time: 11:47 AM
 */
public class ProxyWebCrawler extends WebCrawler {
    private List<ProxyDTO> result = new ArrayList<>();

    private String xslPath;

    private String source;

    private Logger logger = LoggerFactory.getLogger(ProxyWebCrawler.class);

    public ProxyWebCrawler(String xslPath, String source) {
        this.xslPath = xslPath;
        this.source = source;
    }

    /**
     * This function is called when a page is fetched and ready
     * to be processed by your program.
     */
    @Override
    public void visit(Page page) {
        String url = page.getWebURL().getURL();
        System.out.println("URL: " + url);

        try {
            if (page.getParseData() instanceof HtmlParseData) {
                HtmlParseData htmlParseData = (HtmlParseData) page.getParseData();

                final Document document = Jsoup.parse(htmlParseData.getHtml());

                Transformer transformer = TransformerFactory.newInstance()
                        .newTransformer(new StreamSource(getClass().getResourceAsStream(xslPath)));

                Writer write = new StringWriter();
                transformer.transform(new DOMSource(new W3CDom().fromJsoup(document)), new StreamResult(write));

                String xml = write.toString();

                // XML 1.1
                // [#x1-#xD7FF] | [#xE000-#xFFFD] | [#x10000-#x10FFFF]
                String xml11pattern = "[^"
                        + "\u0001-\uD7FF"
                        + "\uE000-\uFFFD"
                        + "\ud800\udc00-\udbff\udfff"
                        + "]+";

                // remove invalid character in xml
                xml = xml.replaceAll(xml11pattern, "");

                JAXBContext jc = JAXBContext.newInstance(ProxyListDTO.class);

                Unmarshaller unmarshaller = jc.createUnmarshaller();
                ProxyListDTO data = (ProxyListDTO) unmarshaller.unmarshal(new StringReader(xml));
                data.getProxies().forEach(p -> p.setSource(source));


                result.addAll(data.getProxies());
            }
        } catch (Exception e) {
            logger.error("fail to visit " + url, e);
        }
    }


    @Override
    public List<ProxyDTO> getMyLocalData() {
        return result;
    }

}
