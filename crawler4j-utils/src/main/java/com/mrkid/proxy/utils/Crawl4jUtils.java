package com.mrkid.proxy.utils;

import edu.uci.ics.crawler4j.crawler.CrawlConfig;
import edu.uci.ics.crawler4j.crawler.CrawlController;
import edu.uci.ics.crawler4j.fetcher.PageFetcher;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtConfig;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtServer;

/**
 * User: xudong
 * Date: 30/11/2016
 * Time: 2:05 PM
 */
public class Crawl4jUtils {
    public static CrawlController newCrawlController(String storeRoot) throws Exception {
        CrawlConfig config = new CrawlConfig();
        config.setCrawlStorageFolder(storeRoot);

        PageFetcher pageFetcher = new PageFetcher(config);
        RobotstxtConfig robotstxtConfig = new RobotstxtConfig();
        RobotstxtServer robotstxtServer = new RobotstxtServer(robotstxtConfig, pageFetcher);
        CrawlController controller = new CrawlController(config, pageFetcher, robotstxtServer);

        return controller;
    }

}
