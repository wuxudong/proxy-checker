package com.mrkid.proxy.cnproxy;

import com.mrkid.proxy.dto.Proxy;
import edu.uci.ics.crawler4j.crawler.CrawlConfig;
import edu.uci.ics.crawler4j.crawler.CrawlController;
import edu.uci.ics.crawler4j.fetcher.PageFetcher;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtConfig;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtServer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * User: xudong
 * Date: 03/11/2016
 * Time: 1:05 PM
 */
@Configuration
public class CnProxyCrawlerConfiguration {

    @Bean
    public CrawlController crawlController() throws Exception {
        String crawlStorageFolder = "./crawl/root";

        CrawlConfig config = new CrawlConfig();

        config.setCrawlStorageFolder(crawlStorageFolder);

        PageFetcher pageFetcher = new PageFetcher(config);
        RobotstxtConfig robotstxtConfig = new RobotstxtConfig();
        RobotstxtServer robotstxtServer = new RobotstxtServer(robotstxtConfig, pageFetcher);
        CrawlController controller = new CrawlController(config, pageFetcher, robotstxtServer);

        controller.addSeed("http://cn-proxy.com/");

        return controller;
    }

    @Bean
    public BlockingQueue<Proxy> cnProxyQueue() {
        return new LinkedBlockingQueue<>();
    }


    @Bean
    public CrawlController.WebCrawlerFactory webCrawlerFactory(BlockingQueue<Proxy> cnProxyQueue) {
        return () -> new CnProxyCrawler(cnProxyQueue);
    }
}