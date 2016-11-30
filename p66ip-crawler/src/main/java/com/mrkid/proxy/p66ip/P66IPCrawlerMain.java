package com.mrkid.proxy.p66ip;

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
public class P66IPCrawlerMain {
    public static void main(String[] args) throws Exception {
        BlockingQueue<Proxy> proxies = new LinkedBlockingQueue<>();

        CrawlController crawlController = Crawl4jUtils.newCrawlController();
        crawlController.getConfig().setCrawlStorageFolder(P66IPCrawler.STORE_ROOT);
        crawlController.addSeed(P66IPCrawler.SEED);

        int numberOfCrawlers = 1;
        crawlController.start(() -> new P66IPCrawler(proxies, false), numberOfCrawlers);
        crawlController.waitUntilFinish();
        crawlController.shutdown();

        System.out.println("crawl 66ip finish, proxy count:" + proxies.size());
        System.out.println(proxies);
    }
}
