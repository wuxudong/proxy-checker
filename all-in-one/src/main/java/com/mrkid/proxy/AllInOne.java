package com.mrkid.proxy;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mrkid.proxy.cnproxy.Crawler4jControllerHelper;
import com.mrkid.proxy.dto.Proxy;
import com.mrkid.proxy.scheduler.Main;
import com.mrkid.proxy.scheduler.ProxyChecker;
import com.mrkid.proxy.utils.AddressUtils;
import edu.uci.ics.crawler4j.crawler.CrawlController;
import org.apache.commons.io.FileUtils;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.Collectors;

/**
 * User: xudong
 * Date: 14/11/2016
 * Time: 2:08 PM
 */
@SpringBootApplication
public class AllInOne {
    public static void main(String[] args) throws Exception {
        if (args.length != 1) {
            System.out.printf("Usage: <output>");
            return;
        }

        String output = args[0];

        // crawl proxy from cn-proxy


        CrawlController controller = Crawler4jControllerHelper.getCrawlController();

        final BlockingQueue<Proxy> queue = new LinkedBlockingQueue<>();

        CrawlController.WebCrawlerFactory factory = Crawler4jControllerHelper.getWebCrawlerFactory(queue);

        int numberOfCrawlers = 1;
        controller.start(factory, numberOfCrawlers);
        controller.waitUntilFinish();


        // start web
        final ConfigurableApplicationContext context = SpringApplication.run(Main.class);

        // check every proxy and output

        List<Proxy> proxies = new ArrayList<>(queue);

        String ip = AddressUtils.getMyIp();

        final ProxyChecker proxyChecker = context.getBean(ProxyChecker.class);

        try (final PrintWriter writer = new PrintWriter(new FileWriter(output))) {
            proxyChecker.check(ip, "http://" + ip + ":8080//proxy-check",
                    proxies).stream().forEach(line -> writer.println(line));
        }

        context.close();
    }
}
