package com.mrkid.proxy.kxdaili;

import com.mrkid.proxy.dto.Proxy;
import com.mrkid.proxy.utils.Crawl4jUtils;
import edu.uci.ics.crawler4j.crawler.CrawlController;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * User: xudong
 * Date: 30/11/2016
 * Time: 9:51 AM
 */
public class KxDailiCrawlerMain {
    public static void main(String[] args) throws Exception {
        BlockingQueue<Proxy> proxies = new LinkedBlockingQueue<>();

        CrawlController crawlController = Crawl4jUtils.newCrawlController(KxDailiCrawler.STORE_ROOT);
        crawlController.addSeed(KxDailiCrawler.SEED);

        int numberOfCrawlers = 1;
        crawlController.start(() -> new KxDailiCrawler(proxies), numberOfCrawlers);
        crawlController.waitUntilFinish();
        crawlController.shutdown();

        System.out.println("crawl kxDaili finish, proxy count:" + proxies.size());
        System.out.println(proxies);
    }
}
