package com.mrkid.proxy.crawler.vender.ip3366;

import com.mrkid.proxy.crawler.crawler4j.Crawl4jProxyFetcher;
import com.mrkid.proxy.crawler.crawler4j.ProxyWebCrawler;
import edu.uci.ics.crawler4j.crawler.CrawlController;
import edu.uci.ics.crawler4j.crawler.Page;
import edu.uci.ics.crawler4j.crawler.WebCrawler;
import edu.uci.ics.crawler4j.url.WebURL;

import java.util.Arrays;
import java.util.List;

/**
 * User: xudong
 * Date: 03/11/2016
 * Time: 12:47 PM
 */
public class Ip3366ProxyFetcher extends Crawl4jProxyFetcher {

    public static final String STORE_ROOT = "./crawl/ip3366/root";
    public static final String SEED = "http://www.ip3366.net/free/";

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
        return () -> new Ip3366Crawler();
    }
}

class Ip3366Crawler extends ProxyWebCrawler {

    public Ip3366Crawler() {
        super("/xsl/ip3366.xsl", "IP3366");
    }

    @Override
    public boolean shouldVisit(Page referringPage, WebURL url) {
        return url.getURL().startsWith("http://www.ip3366.net/free/");
    }
}
