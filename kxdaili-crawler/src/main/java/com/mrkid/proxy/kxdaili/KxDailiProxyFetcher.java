package com.mrkid.proxy.kxdaili;

import com.mrkid.proxy.Crawl4jProxyFetcher;
import com.mrkid.proxy.ProxyWebCrawler;
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
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * User: xudong
 * Date: 03/11/2016
 * Time: 12:47 PM
 */

public class KxDailiProxyFetcher extends Crawl4jProxyFetcher {

    public static final String STORE_ROOT = "./crawl/kxdaili/root";
    public static final String SEED = "http://www.kxdaili.com/dailiip.html";

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
        return () -> new KxDailiCrawler("/kxdaili.xsl", "KXDAILI");
    }
}

class KxDailiCrawler extends ProxyWebCrawler {
    public KxDailiCrawler(String xslPath, String source) {
        super(xslPath, source);
    }

    @Override
    public boolean shouldVisit(Page referringPage, WebURL url) {
        return url.getURL().startsWith("http://www.kxdaili.com/dailiip");
    }
}
