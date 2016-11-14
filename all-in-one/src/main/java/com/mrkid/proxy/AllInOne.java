package com.mrkid.proxy;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mrkid.proxy.cnproxy.Crawler4jControllerHelper;
import com.mrkid.proxy.dto.Proxy;
import com.mrkid.proxy.dto.ProxyCheckResponse;
import com.mrkid.proxy.scheduler.Main;
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
import java.util.concurrent.LinkedBlockingQueue;

/**
 * User: xudong
 * Date: 14/11/2016
 * Time: 2:08 PM
 */
@SpringBootApplication
public class AllInOne {
    public static void main(String[] args) throws Exception {
        if (args.length != 1) {
            System.out.printf("Usage: <output_directory>");
            return;
        }

        File outputDirectory = new File(args[0]);

        if (outputDirectory.exists()) {
            if (outputDirectory.isDirectory()) {
                System.out.printf("<output_directory> should be a directory");
                return;
            }
        } else {
            outputDirectory.mkdirs();
        }

        // crawl proxy from cn-proxy


        CrawlController controller = Crawler4jControllerHelper.getCrawlController();

        final BlockingQueue<Proxy> queue = new LinkedBlockingQueue<>();

        CrawlController.WebCrawlerFactory factory = Crawler4jControllerHelper.getWebCrawlerFactory(queue);

        int numberOfCrawlers = 1;
        controller.start(factory, numberOfCrawlers);
        controller.waitUntilFinish();

        System.out.println("crawl cn-proxy finish");

        // start web
        final ConfigurableApplicationContext context = SpringApplication.run(Main.class);

        // try wait a little longer until web is started
        Thread.sleep(5000l);

        System.out.println("web start finish");


        // check every proxy and output

        List<Proxy> proxies = new ArrayList<>(queue);

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
                    if (line.getProxyType() == ProxyCheckResponse.HIGH_ANONYMITY_PROXY &&
                            "http".equalsIgnoreCase(line.getProxy().getSchema())) {
                        squidWriter.println(
                                String.format("cache_peer %s parent %d 0 round-robin no-query",
                                        line.getProxy().getHost(), line.getProxy().getPort()));

                    }
                } catch (JsonProcessingException e) {
                }
            });
        }

//        context.close();
    }
}
