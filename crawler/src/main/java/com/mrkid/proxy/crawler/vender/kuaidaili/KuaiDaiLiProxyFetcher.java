package com.mrkid.proxy.crawler.vender.kuaidaili;

import com.mrkid.proxy.crawler.crawler4j.Crawl4jProxyFetcher;
import com.mrkid.proxy.crawler.crawler4j.ProxyWebCrawler;
import edu.uci.ics.crawler4j.crawler.CrawlController;
import edu.uci.ics.crawler4j.crawler.Page;
import edu.uci.ics.crawler4j.crawler.WebCrawler;
import edu.uci.ics.crawler4j.url.WebURL;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * User: xudong
 * Date: 03/11/2016
 * Time: 12:47 PM
 */
public class KuaiDaiLiProxyFetcher extends Crawl4jProxyFetcher {

    public static final String STORE_ROOT = "./crawl/kuaidaili/root";
    public static final String SEED = "http://www.kuaidaili.com/free/";

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
        return 2000;
    }

    @Override
    protected CrawlController.WebCrawlerFactory<WebCrawler> getWebCrawlerFactory() {
        return () -> new KuaiDaiLiCrawler(false);
    }
}

class KuaiDaiLiCrawler extends ProxyWebCrawler {


    private boolean crawlHistory = false;

    private final Pattern pagePattern = Pattern.compile("http://www.kuaidaili.com/free/[^/]+/(\\d+)/");

    public KuaiDaiLiCrawler(boolean crawlHistory) {
        super("/xsl/kuaidaili.xsl", "KUAIDAILI");
        this.crawlHistory = crawlHistory;
    }

    @Override
    public boolean shouldVisit(Page referringPage, WebURL url) {
        final Matcher matcher = pagePattern.matcher(url.getURL());
        if (!matcher.matches()) {
            return false;
        } else {
            if (crawlHistory) {
                return true;
            } else {
                int page = Integer.valueOf(matcher.group(1));

                return page < 10;
            }
        }
    }
}
