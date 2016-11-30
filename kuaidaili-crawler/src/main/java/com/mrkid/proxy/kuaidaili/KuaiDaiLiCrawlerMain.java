package com.mrkid.proxy.kuaidaili;

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
public class KuaiDaiLiCrawlerMain {
    public static void main(String[] args) throws Exception {
        BlockingQueue<Proxy> proxies = new LinkedBlockingQueue<>();

        CrawlController crawlController = Crawl4jUtils.newCrawlController(KuaiDaiLiCrawler.STORE_ROOT);
        crawlController.addSeed(KuaiDaiLiCrawler.SEED);


        crawlController.getConfig().setPolitenessDelay(2000); //  kuaidaili is sensitive to high frequency visit.

        int numberOfCrawlers = 1;
        crawlController.start(() -> new KuaiDaiLiCrawler(proxies, false), numberOfCrawlers);
        crawlController.waitUntilFinish();
        crawlController.shutdown();

        System.out.println("crawl kuaidaili finish, proxy count:" + proxies.size());
        System.out.println(proxies);
    }
}
