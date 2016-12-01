package com.mrkid.proxy;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mrkid.proxy.checker.ProxyChecker;
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
import com.mrkid.proxy.utils.AddressUtils;
import com.mrkid.proxy.utils.Crawl4jUtils;
import edu.uci.ics.crawler4j.crawler.CrawlController;
import edu.uci.ics.crawler4j.crawler.WebCrawler;
import io.reactivex.Flowable;
import org.apache.commons.cli.*;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import java.io.*;
import java.util.*;
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

    private static final Logger logger = LoggerFactory.getLogger(ProxyCheckerMain.class);

    public static void main(String[] args) throws Exception {
        {
            Options options = new Options();

            options.addOption("a", "action", true,
                    "[crawl|check|all] -> only crawl proxies? only check already crawled proxies? or do both?");

            options.addOption("s", "server", true,
                    "you checker api url, something like http://serverip:8080/proxy-check. " +
                            "required when action = check|all");


            CommandLineParser parser = new BasicParser();
            CommandLine cmd = parser.parse(options, args);
            HelpFormatter formatter = new HelpFormatter();


            if (cmd.hasOption("action")) {
                final String action = cmd.getOptionValue("action");

                final ConfigurableApplicationContext context = SpringApplication.run(ProxyCheckerMain.class);

                final ProxyChecker proxyChecker = context.getBean(ProxyChecker.class);

                String ip = AddressUtils.getMyPublicIp();
                String proxyCheckUrl = cmd.getOptionValue("server");

                File dataDirectory = new File("data");
                if (!dataDirectory.exists()) {
                    dataDirectory.mkdir();
                }

                File historyDirectory = new File(dataDirectory, "history");
                if (!historyDirectory.exists()) {
                    historyDirectory.mkdir();
                }

                final String today = DateFormatUtils.format(new Date(), "yyyy-MM-dd");
                File backupDirectory = new File(historyDirectory, today);
                if (!backupDirectory.exists()) {
                    backupDirectory.mkdir();
                }

                switch (action) {
                    case "crawl":
                        saveLatestCrawl(new HashSet<>(crawl()), historyDirectory, backupDirectory);
                        break;
                    case "check":
                        // load lastest crawl
                        final Set<Proxy> latestCrawl = loadLatestCrawl(historyDirectory);
                        // merge history
                        checkAndSave(dataDirectory, mergeHistory(latestCrawl, dataDirectory), ip, proxyCheckUrl,
                                proxyChecker);
                        break;
                    case "all":
                        Set<Proxy> crawledProxies = new HashSet<>(crawl());
                        // save data
                        saveLatestCrawl(crawledProxies, historyDirectory, backupDirectory);
                        checkAndSave(dataDirectory, mergeHistory(crawledProxies, dataDirectory), ip, proxyCheckUrl,
                                proxyChecker);

                        break;
                    default:
                        formatter.printHelp("java -jar all-in-one.jar", options);
                }

                context.close();

            } else {
                formatter.printHelp("java -jar all-in-one.jar", options);
                return;
            }
        }
    }

    private static Set<Proxy> mergeHistory(Set<Proxy> crawledProxies, File dataDirectory) throws IOException {
        File validProxiesFile = new File(dataDirectory, "proxy.valid");
        Set<Proxy> validProxies = validProxiesFile.exists() ? loadLatestValid(validProxiesFile) : new HashSet<>();


        Set<Proxy> checking = new HashSet<>();
        checking.addAll(crawledProxies);
        checking.addAll(validProxies);
        return checking;
    }

    private static void saveLatestCrawl(Set<Proxy> crawledProxies, File historyDirectory, File backupDirectory)
            throws IOException {
        final File latestCrawlFile = new File(historyDirectory, "latest.crawl");
        saveCrawledProxies(crawledProxies, latestCrawlFile);
        FileUtils.copyFile(latestCrawlFile, new File(backupDirectory, latestCrawlFile.getName()));
    }

    private static Set<Proxy> loadLatestCrawl(File historyDirectory) throws
            IOException {
        final File latestCrawlFile = new File(historyDirectory, "latest.crawl");
        try (final BufferedReader crawlReader = new BufferedReader(new FileReader(latestCrawlFile))) {
            return crawlReader.lines().map(l -> {
                try {
                    return objectMapper.readValue(l, Proxy.class);
                } catch (IOException e) {
                    e.printStackTrace();
                    return null;
                }
            }).filter(p -> p != null).collect(Collectors.toSet());
        }
    }


    private static void checkAndSave(File dataDirectory, Set<Proxy> checking, String ip, String proxyCheckUrl,
                                     ProxyChecker proxyChecker) throws IOException {

        logger.info(checking.size() + " proxies need to be checked :" + checking);

        final File validProgressingFile = new File(dataDirectory, "proxy.valid.progress");

        final File checkResultFile = new File(dataDirectory, "proxy.check.progress");

        final File transparentFile = new File(dataDirectory, "transparent_proxy.squid.progress");

        final File anonymousFile = new File(dataDirectory, "anonymous_proxy.squid.progress");

        final File distortingFile = new File(dataDirectory, "distorting_proxy.squid.progress");

        final File highAnonymityFile = new File(dataDirectory, "high_anonymity_proxy.squid.progress");


        try (final PrintWriter checkJsonWriter = new PrintWriter(new FileWriter(checkResultFile));
             final PrintWriter validJsonWriter = new PrintWriter(new FileWriter(validProgressingFile));
             final PrintWriter transparentSquidWriter = new PrintWriter(new FileWriter(transparentFile));
             final PrintWriter anonymousSquidWriter = new PrintWriter(new FileWriter(anonymousFile));
             final PrintWriter distortingSquidWriter = new PrintWriter(new FileWriter(distortingFile));
             final PrintWriter highAnonymitySquidWriter = new PrintWriter(new FileWriter(highAnonymityFile))
        ) {
            Flowable.fromIterable(checking)
                    .flatMap(p -> proxyChecker.getProxyResponse(ip, proxyCheckUrl, p), 1000)
                    .doOnNext(p -> writeInJsonFormat(checkJsonWriter, p))
                    .filter(p -> p.isValid())
                    .doOnNext(p -> writeInJsonFormat(validJsonWriter, p))
                    .filter(r -> "http".equalsIgnoreCase(r.getProxy().getSchema()))
                    .doOnNext(p -> {
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
                    }).blockingSubscribe();
        }

        // remove .progress postfix
        Arrays.asList(validProgressingFile, checkResultFile, transparentFile, anonymousFile, distortingFile,
                highAnonymityFile)
                .forEach(f -> {
                            final String name = f.getName();
                            final String newName = name.substring(0, name.indexOf(".progress"));
                            final File destFile = new File(f.getParent(), newName);
                            if (destFile.exists()) {
                                destFile.delete();
                            }
                            f.renameTo(destFile);
                        }
                );
    }

    private static void saveCrawledProxies(Collection<Proxy> proxies, File target) throws
            IOException {
        try (final PrintWriter crawlWriter = new PrintWriter(new FileWriter(target))) {
            proxies.forEach(p -> writeInJsonFormat(crawlWriter, p));
        }
    }

    private static BlockingQueue<Proxy> crawl() {
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

        logger.info(proxies.size() + " proxies crawled");
        return proxies;
    }

    private static void crawl(String storeRoot, String seed, int politenessDelay, CrawlController
            .WebCrawlerFactory<WebCrawler> factory) {
        CrawlController crawlController = null;
        try {
            crawlController = Crawl4jUtils.newCrawlController(storeRoot);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        crawlController.getConfig().setPolitenessDelay(politenessDelay);

        crawlController.addSeed(seed);
        int numberOfCrawlers = 1;


        crawlController.start(factory, numberOfCrawlers);
        crawlController.waitUntilFinish();
        crawlController.shutdown();
    }

    private static Set<Proxy> loadLatestValid(File validProxiesFile) throws IOException {
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
