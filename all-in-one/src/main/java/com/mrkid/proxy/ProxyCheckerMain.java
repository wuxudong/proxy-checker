package com.mrkid.proxy;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mrkid.proxy.dto.Proxy;
import com.mrkid.proxy.dto.ProxyCheckResponse;
import com.mrkid.proxy.scheduler.ProxyChecker;
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
import java.util.stream.Collectors;

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

            String ip = context.getBean("originIp", String.class);
            String proxyCheckUrl = context.getBean("proxyCheckUrl", String.class);

            final ProxyChecker proxyChecker = context.getBean(ProxyChecker.class);

            final File allProxyInJsonFormat = new File(outputDirectory, "all_proxy" +
                    ".json");
            final File highAnonymityInSquidFormat = new File(outputDirectory,
                    "high_anonymity_proxy.squid");

            final ObjectMapper objectMapper = new ObjectMapper();
            try (final PrintWriter jsonWriter = new PrintWriter(new FileWriter(allProxyInJsonFormat));
                 final PrintWriter squidWriter = new PrintWriter(new FileWriter(highAnonymityInSquidFormat))) {
                final List<ProxyCheckResponse> list = proxyChecker.check(ip, proxyCheckUrl, proxies);
                list.forEach(line -> {
                    try {
                        jsonWriter.println(objectMapper.writeValueAsString(line));
                    } catch (JsonProcessingException e) {
                    }
                });

                list.stream().filter(r -> r.isValid())
                        .filter(line -> "http".equalsIgnoreCase(line.getProxy().getSchema()))
                        .collect(Collectors.toSet())
                        .forEach(line ->
                                squidWriter.println(
                                        String.format("cache_peer %s parent %d 0 round-robin no-query", line.getProxy
                                                ().getHost(), line.getProxy().getPort())));


            }

            context.close();
        }
    }

}
