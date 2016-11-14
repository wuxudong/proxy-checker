package com.mrkid.proxy;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mrkid.proxy.dto.Proxy;
import com.mrkid.proxy.scheduler.ProxyChecker;
import com.mrkid.proxy.utils.AddressUtils;
import edu.uci.ics.crawler4j.crawler.CrawlController;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;

/**
 * User: xudong
 * Date: 31/10/2016
 * Time: 3:17 PM
 */
@SpringBootApplication
public class ProxyCheckerMain {
    public static void main(String[] args) throws Exception {
        {
            if (args.length != 1) {
                System.out.printf("Usage: <output_directory>");
                return;
            }

            File outputDirectory = new File(args[0]);

            if (outputDirectory.exists()) {
                outputDirectory.delete();
            }
            outputDirectory.mkdirs();


            // start
            final ConfigurableApplicationContext context = SpringApplication.run(ProxyCheckerMain.class);


            // crawl proxy from cn-proxy


            CrawlController crawlController = context.getBean(CrawlController.class);

            int numberOfCrawlers = 1;
            crawlController.start(context.getBean(CrawlController.WebCrawlerFactory.class), numberOfCrawlers);
            crawlController.waitUntilFinish();
            crawlController.shutdown();

            System.out.println("crawl cn-proxy finish");


            // check every proxy and output

            List<Proxy> proxies = new ArrayList<>(context.getBean("cnProxyQueue", BlockingQueue.class));

            String ip = AddressUtils.getMyIp();

            final ProxyChecker proxyChecker = context.getBean(ProxyChecker.class);

            final File allProxyInJsonFormat = new File(outputDirectory, "all_proxy" +
                    ".json");
            final File highAnonymityInSquidFormat = new File(outputDirectory,
                    "high_anonymity_proxy.squid");

            final ObjectMapper objectMapper = new ObjectMapper();
            try (final PrintWriter jsonWriter = new PrintWriter(new FileWriter(allProxyInJsonFormat));
                 final PrintWriter squidWriter = new PrintWriter(new FileWriter(highAnonymityInSquidFormat))) {
                proxyChecker.check(ip, String.format("http://%s:8080/proxy-check", ip), proxies)
                        .stream().forEach(line -> {
                    try {
                        jsonWriter.println(objectMapper.writeValueAsString(line));
                        if ("http".equalsIgnoreCase(line.getProxy().getSchema())) {
                            squidWriter.println(
                                    String.format("cache_peer %s parent %d 0 round-robin no-query",
                                            line.getProxy().getHost(), line.getProxy().getPort()));

                        }
                    } catch (JsonProcessingException e) {
                    }
                });
            }

            context.close();
        }
    }

}
