package com.mrkid.proxy;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mrkid.proxy.dto.Proxy;
import com.mrkid.proxy.dto.ProxyCheckResponse;
import com.mrkid.proxy.scheduler.ProxyChecker;
import edu.uci.ics.crawler4j.crawler.CrawlController;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.stream.Collectors;

/**
 * User: xudong
 * Date: 31/10/2016
 * Time: 3:17 PM
 */
@SpringBootApplication
public class ProxyCheckerMain {
    private final static ObjectMapper objectMapper = new ObjectMapper();

    public static void main(String[] args) throws Exception {
        {
            final ConfigurableApplicationContext context = SpringApplication.run(ProxyCheckerMain.class);

            // crawl proxy from cn-proxy

            CrawlController crawlController = context.getBean(CrawlController.class);

            int numberOfCrawlers = 1;
            crawlController.start(context.getBean(CrawlController.WebCrawlerFactory.class), numberOfCrawlers);
            crawlController.waitUntilFinish();
            crawlController.shutdown();

            System.out.println("crawl cn-proxy finish");


            // merge history
            File dataDirectory = new File("data");
            if (!dataDirectory.exists()) {
                dataDirectory.mkdir();
            }

            File historyDirectory = new File(dataDirectory, "history");
            if (!historyDirectory.exists()) {
                historyDirectory.mkdir();
            }

            final String today = DateFormatUtils.format(new Date(), "yyyy-MM-dd");
            File crawlHistoryFile = new File(historyDirectory, today + ".crawl");

            List<Proxy> proxies = new ArrayList<>(context.getBean("cnProxyQueue", BlockingQueue.class));

            try (final PrintWriter crawlWriter = new PrintWriter(new FileWriter(crawlHistoryFile))) {
                proxies.forEach(p -> writeInJsonFormat(crawlWriter, p));
            }

            File validProxiesFile = new File(dataDirectory, "proxy.valid");
            if (!validProxiesFile.exists()) {
                validProxiesFile.createNewFile();
            }
            Set<Proxy> mergedProxies = getValidProxies(validProxiesFile);

            mergedProxies.addAll(proxies);

            // check proxies
            String ip = context.getBean("originIp", String.class);
            String proxyCheckUrl = context.getBean("proxyCheckUrl", String.class);

            final ProxyChecker proxyChecker = context.getBean(ProxyChecker.class);

            final File checkResultFile = new File(historyDirectory, today +
                    ".check");

            final File transparentFile = new File(dataDirectory,
                    "transparent_proxy.squid");

            final File anonymousFile = new File(dataDirectory,
                    "anonymous_proxy.squid");

            final File distortingFile = new File(dataDirectory,
                    "distorting_proxy.squid");

            final File highAnonymityFile = new File(dataDirectory,
                    "high_anonymity_proxy.squid");


            try (final PrintWriter checkJsonWriter = new PrintWriter(new FileWriter(checkResultFile));
                 final PrintWriter validJsonWriter = new PrintWriter(new FileWriter(validProxiesFile));
                 final PrintWriter transparentSquidWriter = new PrintWriter(new FileWriter(transparentFile));
                 final PrintWriter anonymousSquidWriter = new PrintWriter(new FileWriter(anonymousFile));
                 final PrintWriter distortingSquidWriter = new PrintWriter(new FileWriter(distortingFile));
                 final PrintWriter highAnonymitySquidWriter = new PrintWriter(new FileWriter(highAnonymityFile))
            ) {
                final List<ProxyCheckResponse> checkResponses = proxyChecker.check(ip, proxyCheckUrl,
                        new ArrayList<>(mergedProxies));

                checkResponses.forEach(p -> writeInJsonFormat(checkJsonWriter, p));

                checkResponses.stream().filter(p -> p.isValid()).forEach(p -> writeInJsonFormat(validJsonWriter, p));

                checkResponses.stream().filter(r -> r.isValid())
                        .filter(r -> "http".equalsIgnoreCase(r.getProxy().getSchema()))
                        .collect(Collectors.toSet()).stream()
                        .forEach(line -> {
                            switch (line.getProxyType()) {
                                case ProxyCheckResponse.TRANSPARENT_PROXY:
                                    writeInSquidFormat(transparentSquidWriter, line);
                                    break;

                                case ProxyCheckResponse.ANONYMOUS_PROXY:
                                    writeInSquidFormat(anonymousSquidWriter, line);
                                    break;

                                case ProxyCheckResponse.DISTORTING_PROXY:
                                    writeInSquidFormat(distortingSquidWriter, line);
                                    break;

                                case ProxyCheckResponse.HIGH_ANONYMITY_PROXY:
                                    writeInSquidFormat(highAnonymitySquidWriter, line);
                                    break;

                            }
                        });
            }

            context.close();
        }
    }

    private static Set<Proxy> getValidProxies(File validProxiesFile) throws IOException {
        return FileUtils.readLines(validProxiesFile).stream().map(l -> {
            try {
                return objectMapper
                        .readValue(l, ProxyCheckResponse.class);
            } catch (IOException e) {
                return null;
            }
        }).filter(p -> p != null).map(r -> r.getProxy()).collect(Collectors.toSet());
    }

    private static void writeInJsonFormat(PrintWriter writer, Object o) {
        try {
            writer.println(objectMapper.writeValueAsString(o));
        } catch (JsonProcessingException e) {
        }
    }

    private static void writeInSquidFormat(PrintWriter writer, ProxyCheckResponse line) {
        writer.println(
                String.format(
                        "cache_peer %s parent %d 0 round-robin no-query connect-fail-limit=1",
                        line.getProxy().getHost(), line.getProxy().getPort()));
    }

}
