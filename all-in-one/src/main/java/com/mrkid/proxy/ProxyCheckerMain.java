package com.mrkid.proxy;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mrkid.proxy.cnproxy.CnProxyCrawler;
import com.mrkid.proxy.coobobo.CooboboCrawler;
import com.mrkid.proxy.dto.Proxy;
import com.mrkid.proxy.dto.ProxyCheckResponse;
import com.mrkid.proxy.goubanjia.GoubanjiaCrawler;
import com.mrkid.proxy.haoip.HaoIPCrawler;
import com.mrkid.proxy.ip3366.Ip3366Crawler;
import com.mrkid.proxy.kuaidaili.KuaiDaiLiCrawler;
import com.mrkid.proxy.kxdaili.KxDailiCrawler;
import com.mrkid.proxy.p66ip.P66IPCrawler;
import com.mrkid.proxy.p881free.P881FreeCrawler;
import com.mrkid.proxy.scheduler.ProxyChecker;
import com.mrkid.proxy.utils.Crawl4jUtils;
import edu.uci.ics.crawler4j.crawler.CrawlController;
import edu.uci.ics.crawler4j.crawler.WebCrawler;
import io.reactivex.Flowable;
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
import java.util.concurrent.LinkedBlockingQueue;
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

            BlockingQueue<Proxy> proxies = new LinkedBlockingQueue<>();

            List<Thread> threads = new ArrayList<>();

            // cn-proxy
            final Thread cnProxyCrawlerThread = new Thread(() ->
                    crawl(CnProxyCrawler.STORE_ROOT, CnProxyCrawler.SEED, 0,
                            () -> new CnProxyCrawler(proxies)));

            threads.add(cnProxyCrawlerThread);


            // coobobo
            final Thread cooboboCrawlerThread = new Thread(() ->
                    crawl(CooboboCrawler.STORE_ROOT, CooboboCrawler.SEED, 0,
                            () -> new CooboboCrawler(proxies, false)));
            threads.add(cooboboCrawlerThread);

            // goubanjia
            final Thread goubanjiaCrawlerThread = new Thread(() ->
                    crawl(GoubanjiaCrawler.STORE_ROOT, GoubanjiaCrawler.SEED, 0,
                            () -> new GoubanjiaCrawler(proxies)));
            threads.add(goubanjiaCrawlerThread);

            // ip3366
            final Thread ip3366CrawlerThread = new Thread(() ->
                    crawl(Ip3366Crawler.STORE_ROOT, Ip3366Crawler.SEED, 0,
                            () -> new Ip3366Crawler(proxies)));
            threads.add(ip3366CrawlerThread);

            // kuaidaili is sensitive to high frequency visit.
            final Thread kuaidailiCrawlerThread = new Thread(() ->
                    crawl(KuaiDaiLiCrawler.STORE_ROOT, KuaiDaiLiCrawler.SEED, 2000,
                            () -> new KuaiDaiLiCrawler(proxies, false)));
            threads.add(kuaidailiCrawlerThread);

            // kxdaili
            final Thread kxdailiCrawlerThread = new Thread(() ->
                    crawl(KxDailiCrawler.STORE_ROOT, KxDailiCrawler.SEED, 0,
                            () -> new KxDailiCrawler(proxies)));
            threads.add(kxdailiCrawlerThread);

            // 66ip
            final Thread p66ipCrawlerThread = new Thread(() ->
                    crawl(P66IPCrawler.STORE_ROOT, P66IPCrawler.SEED, 0,
                            () -> new P66IPCrawler(proxies, false)));
            threads.add(p66ipCrawlerThread);

            // 881free
            final Thread p881freeCrawlerThread = new Thread(() -> {
                try {
                    new P881FreeCrawler().fetchProxy().forEach(p -> proxies.add(p));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
            threads.add(p881freeCrawlerThread);

            // haoip
            final Thread haoipCrawlerThread = new Thread(() -> {
                try {
                    new HaoIPCrawler().fetchProxy().forEach(p -> proxies.add(p));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
            threads.add(haoipCrawlerThread);

            threads.forEach(th -> th.start());

            threads.forEach(th -> {
                try {
                    th.join();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            });

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

            final File checkResultFile = new File(historyDirectory, today + ".check");

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
                Flowable<ProxyCheckResponse> flow = Flowable.fromIterable(mergedProxies).flatMap(p -> proxyChecker
                        .getProxyResponse(ip, proxyCheckUrl, p), 400);


                flow.doOnNext(p -> writeInJsonFormat(checkJsonWriter, p));

                final Flowable<ProxyCheckResponse> validFlow = flow.filter(p -> p.isValid());
                validFlow.doOnNext(p -> writeInJsonFormat(validJsonWriter, p));

                final Flowable<ProxyCheckResponse> httpFlow = validFlow.filter(r -> "http".equalsIgnoreCase(r.getProxy
                        ().getSchema()));

                httpFlow.doOnNext(p -> {
                    switch (p.getProxyType()) {
                        case ProxyCheckResponse.TRANSPARENT_PROXY:
                            writeInSquidFormat(transparentSquidWriter, p);
                            break;

                        case ProxyCheckResponse.ANONYMOUS_PROXY:
                            writeInSquidFormat(anonymousSquidWriter, p);
                            break;

                        case ProxyCheckResponse.DISTORTING_PROXY:
                            writeInSquidFormat(distortingSquidWriter, p);
                            break;

                        case ProxyCheckResponse.HIGH_ANONYMITY_PROXY:
                            writeInSquidFormat(highAnonymitySquidWriter, p);
                            break;

                    }
                });

                httpFlow.blockingSubscribe();
            }

            context.close();
        }
    }

    private static void crawl(String storeRoot, String seed, int politenessDelay, CrawlController
            .WebCrawlerFactory<WebCrawler> factory) {
        CrawlController crawlController = null;
        try {
            crawlController = Crawl4jUtils.newCrawlController();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        crawlController.getConfig().setCrawlStorageFolder(storeRoot);
        crawlController.getConfig().setPolitenessDelay(politenessDelay);

        crawlController.addSeed(seed);
        int numberOfCrawlers = 1;


        crawlController.start(factory, numberOfCrawlers);
        crawlController.waitUntilFinish();
        crawlController.shutdown();
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
