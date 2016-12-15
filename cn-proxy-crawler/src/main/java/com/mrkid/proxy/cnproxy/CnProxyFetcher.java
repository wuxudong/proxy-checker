package com.mrkid.proxy.cnproxy;

import com.mrkid.proxy.Crawl4jProxyFetcher;
import com.mrkid.proxy.ProxyWebCrawler;
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
public class CnProxyFetcher extends Crawl4jProxyFetcher {

    public static final String SEED = "http://cn-proxy.com/";
    public static final String STORE_ROOT = "./crawl/cnproxy/root";

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
        return () -> new CnProxyCrawler();
    }
}

class CnProxyCrawler extends ProxyWebCrawler {
    public CnProxyCrawler() {
        super("/cn-proxy.xsl", "CNPROXY");
    }

    @Override
    public boolean shouldVisit(Page referringPage, WebURL url) {
        String anchor = url.getAnchor();
        return anchor != null && anchor.equals("全球范围代理服务器");
    }
}
