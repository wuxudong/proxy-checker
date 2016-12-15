package com.mrkid.proxy.coobobo;

import com.mrkid.proxy.Crawl4jProxyFetcher;
import com.mrkid.proxy.ProxyWebCrawler;
import edu.uci.ics.crawler4j.crawler.CrawlController;
import edu.uci.ics.crawler4j.crawler.Page;
import edu.uci.ics.crawler4j.crawler.WebCrawler;
import edu.uci.ics.crawler4j.url.WebURL;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

/**
 * User: xudong
 * Date: 03/11/2016
 * Time: 12:47 PM
 */
public class CooboboProxyFetcher extends Crawl4jProxyFetcher {

    public static final String STORE_ROOT = "./crawl/coobobo/root";
    public static final String SEED = "http://www.coobobo.com/free-http-proxy/";

    private boolean crawlHistory = false;

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
        return () -> new CooboboCrawler(crawlHistory);
    }

    public CooboboProxyFetcher(boolean crawlHistory) {
        this.crawlHistory = crawlHistory;
    }
}

class CooboboCrawler extends ProxyWebCrawler {

    private boolean crawlHistory = false;

    private static final String HISTROY_LIST_URL = "http://www.coobobo.com/free-http-proxy-everyday";

    private static final Pattern HISTROY_URL_PATTERN = Pattern.compile(
            "http://www.coobobo.com/free-http-proxy/(\\d){4}-(\\d){2}-(\\d){2}");

    private static final Pattern LATEST_URL_PATTERN = Pattern.compile(
            "http://www.coobobo.com/free-http-proxy/(\\d)+");


    public CooboboCrawler(boolean crawlHistory) {
        super("/coobobo.xsl", "COOBOBO");
        this.crawlHistory = crawlHistory;
    }

    @Override
    public boolean shouldVisit(Page referringPage, WebURL url) {
        if (url.getURL().equals(HISTROY_LIST_URL)) {
            return true;
        }

        if (LATEST_URL_PATTERN.matcher(url.getURL()).matches()) {
            return true;
        }


        if (crawlHistory) {
            return HISTROY_URL_PATTERN.matcher(url.getURL()).matches();
        }

        return false;
    }
}
