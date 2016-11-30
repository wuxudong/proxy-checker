package com.mrkid.proxy.ip3366;

import com.mrkid.proxy.dto.Proxy;
import com.mrkid.proxy.utils.Crawl4jUtils;
import edu.uci.ics.crawler4j.crawler.CrawlController;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * User: xudong
 * Date: 30/11/2016
 * Time: 9:51 AM
 */
public class Ip3366CrawlerMain {
    public static void main(String[] args) throws Exception {
        BlockingQueue<Proxy> proxies = new LinkedBlockingQueue<>();

        CrawlController crawlController = Crawl4jUtils.newCrawlController();
        crawlController.getConfig().setCrawlStorageFolder(Ip3366Crawler.STORE_ROOT);
        crawlController.addSeed(Ip3366Crawler.SEED);

        int numberOfCrawlers = 1;
        crawlController.start(() -> new Ip3366Crawler(proxies), numberOfCrawlers);
        crawlController.waitUntilFinish();
        crawlController.shutdown();

        System.out.println("crawl ip3366 finish, proxy count:" + proxies.size());
        System.out.println(proxies);
    }
}
