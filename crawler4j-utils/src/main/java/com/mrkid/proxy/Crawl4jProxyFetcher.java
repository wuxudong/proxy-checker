package com.mrkid.proxy;

import com.mrkid.proxy.dto.ProxyDTO;
import edu.uci.ics.crawler4j.crawler.CrawlConfig;
import edu.uci.ics.crawler4j.crawler.CrawlController;
import edu.uci.ics.crawler4j.crawler.WebCrawler;
import edu.uci.ics.crawler4j.fetcher.PageFetcher;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtConfig;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtServer;

import java.util.ArrayList;
import java.util.List;

/**
 * User: xudong
 * Date: 12/12/2016
 * Time: 8:42 AM
 */
public abstract class Crawl4jProxyFetcher implements ProxyFetcher {
    protected abstract String getStoreRoot();

    protected abstract List<String> getSeeds();

    protected abstract int getPolitenessDelay();

    protected abstract CrawlController.WebCrawlerFactory<WebCrawler> getWebCrawlerFactory();

    public List<ProxyDTO> crawl() throws Exception {

        CrawlConfig config = new CrawlConfig();
        config.setCrawlStorageFolder(getStoreRoot());
        config.setPolitenessDelay(getPolitenessDelay());

        PageFetcher pageFetcher = new PageFetcher(config);
        RobotstxtConfig robotstxtConfig = new RobotstxtConfig();
        RobotstxtServer robotstxtServer = new RobotstxtServer(robotstxtConfig, pageFetcher);
        CrawlController crawlController = new CrawlController(config, pageFetcher, robotstxtServer);

        getSeeds().forEach(seed -> crawlController.addSeed(seed));

        int numberOfCrawlers = 1;

        crawlController.start(getWebCrawlerFactory(), numberOfCrawlers);
        crawlController.waitUntilFinish();
        crawlController.shutdown();

        List<ProxyDTO> result = new ArrayList<>();

        crawlController.getCrawlersLocalData().forEach(l -> ((List<ProxyDTO>) l).forEach(p -> result.add(p)));

        return result;

    }

}
