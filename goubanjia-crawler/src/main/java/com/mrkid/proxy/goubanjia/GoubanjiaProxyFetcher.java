package com.mrkid.proxy.goubanjia;

import com.mrkid.proxy.Crawl4jProxyFetcher;
import com.mrkid.proxy.dto.ProxyDTO;
import edu.uci.ics.crawler4j.crawler.CrawlController;
import edu.uci.ics.crawler4j.crawler.Page;
import edu.uci.ics.crawler4j.crawler.WebCrawler;
import edu.uci.ics.crawler4j.parser.HtmlParseData;
import edu.uci.ics.crawler4j.url.WebURL;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * User: xudong
 * Date: 03/11/2016
 * Time: 12:47 PM
 */
public class GoubanjiaProxyFetcher extends Crawl4jProxyFetcher {

    public static final String STORE_ROOT = "./crawl/goubanjia/root";
    public static final String SEED = "http://www.goubanjia.com/free/index.shtml";

    @Override
    protected String getStoreRoot() {
        return STORE_ROOT;
    }

    @Override
    protected List<String> getSeeds() {
        return Arrays.asList(SEED);
    }

    @Override
    protected int getPolitenessDelay() {
        return 0;
    }

    @Override
    protected CrawlController.WebCrawlerFactory<WebCrawler> getWebCrawlerFactory() {
        return () -> new GoubanjiaCrawler();
    }
}

class GoubanjiaCrawler extends WebCrawler {

    private List<ProxyDTO> result = new ArrayList<>();

    @Override
    public boolean shouldVisit(Page referringPage, WebURL url) {
        return url.getURL().startsWith("http://www.goubanjia.com/free/");
    }

    /**
     * This function is called when a page is fetched and ready
     * to be processed by your program.
     */
    @Override
    public void visit(Page page) {
        String url = page.getWebURL().getURL();
        System.out.println("URL: " + url);

        if (page.getParseData() instanceof HtmlParseData) {
            HtmlParseData htmlParseData = (HtmlParseData) page.getParseData();
            String html = htmlParseData.getHtml();

            final Document doc = Jsoup.parse(html);
            final Elements tables = doc.select("#list > table");

            final List<ProxyDTO> proxies = tables.stream().map(table -> extractProxies(table)).flatMap(l -> l.stream())
                    .collect(Collectors.toList());

            proxies.forEach(p->result.add(p));
        }
    }

    private List<ProxyDTO> extractProxies(Element table) {
        final Elements header = table.select("thead tr th");
        final Elements rows = table.select("tbody tr");

        return rows.stream().map(row -> {
            final int size = header.size();

            String host = "";
            int port = 0;

            String location = null;

            Elements cells = row.select("td");

            for (int i = 0; i < size; i++) {
                String headerName = header.get(i).text();
                switch (headerName) {
                    case "IP:PORT":
                        final Element address = cells.get(i);

                        final String before = address.toString();
                        address.select("*[style~=display:\\s*none]").remove();

                        final String[] token = address.text().replaceAll("\\s", "").split(":");
                        host = token[0];
                        port = Integer.valueOf(token[1]);

                        logger.debug("extract " + host + ":" + port + " from " + before);
                        break;
                    case "IP归属地":
                        location = cells.get(i).text();
                        break;
                    default:
                }

            }

            if (StringUtils.isBlank(host) || port == 0) {
                return null;
            }

            ProxyDTO proxy = new ProxyDTO();
            proxy.setHost(host);
            proxy.setPort(port);
            proxy.setSchema("http");
            proxy.setLocation(location);

            proxy.setSource("GOUBANJIA");


            return proxy;
        }).filter(p -> p != null).collect(Collectors.toList());
    }
}
