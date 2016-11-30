package com.mrkid.proxy.goubanjia;

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
public class GoubanjiaCrawlerMain {
    public static void main(String[] args) throws Exception {
        BlockingQueue<Proxy> proxies = new LinkedBlockingQueue<>();

        CrawlController crawlController = Crawl4jUtils.newCrawlController();
        crawlController.getConfig().setCrawlStorageFolder(GoubanjiaCrawler.STORE_ROOT);
        crawlController.addSeed(GoubanjiaCrawler.SEED);

        int numberOfCrawlers = 1;
        crawlController.start(() -> new GoubanjiaCrawler(proxies), numberOfCrawlers);
        crawlController.waitUntilFinish();
        crawlController.shutdown();

        System.out.println("crawl goubanjia finish, proxy count:" + proxies.size());
        System.out.println(proxies);
    }
}
