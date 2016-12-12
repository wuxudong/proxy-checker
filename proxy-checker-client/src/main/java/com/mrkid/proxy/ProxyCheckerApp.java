package com.mrkid.proxy;

import com.mrkid.proxy.checker.ProxyChecker;
import com.mrkid.proxy.dto.ProxyDTO;
import com.mrkid.proxy.model.Proxy;
import com.mrkid.proxy.service.ProxyService;
import io.reactivex.Flowable;
import org.apache.commons.cli.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * User: xudong
 * Date: 31/10/2016
 * Time: 3:17 PM
 */
@SpringBootApplication
@Component
public class ProxyCheckerApp {
    @Autowired
    private ProxyChecker proxyChecker;

    @Autowired
    private ProxyService proxyService;

    @Autowired
    private List<ProxyFetcher> proxyFetchers;

    @Autowired
    private List<ProxyCheckResponseWriter> proxyCheckResponseWriters;

    private static final Logger logger = LoggerFactory.getLogger(ProxyCheckerApp.class);

    private Flowable<ProxyDTO> proxyGenerator() {
        AtomicInteger page = new AtomicInteger(0);
        int size = 100;

        return Flowable.<List<Proxy>>generate(e -> {
            final List<Proxy> checkableProxies = proxyService.getCheckableProxies(page.getAndIncrement(), size);
            if (checkableProxies.isEmpty()) {
                e.onComplete();
            } else {
                e.onNext(checkableProxies);
            }
        })
                .concatMapIterable(l -> l)
                .map(p -> new ProxyDTO(p.getSchema(), p.getHost(), p.getPort()));
    }

    private void check() {
        proxyGenerator()
                .flatMap(p -> proxyChecker.check(p), 5000)
                .doOnNext(p -> proxyService.saveProxyCheckResponse(p))
                .doOnNext(p -> proxyCheckResponseWriters.forEach(writer -> {
                    if (writer.shouldWrite(p)) writer.write(p);
                })).blockingSubscribe();

    }


    private void crawl() {
        BlockingQueue<ProxyDTO> proxies = new LinkedBlockingQueue<>();

        List<Thread> threads = proxyFetchers.stream().map(fetcher -> new Thread(() -> {
            try {
                proxies.addAll(fetcher.crawl());
            } catch (Exception e) {
                e.printStackTrace();
            }
        })).collect(Collectors.toList());


        threads.forEach(th -> th.start());

        threads.forEach(th -> {
            try {
                th.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });

        logger.info(proxies.size() + " proxies crawled");
        new HashSet<>(proxies).forEach(p -> proxyService.saveProxy(p));
    }

    public static void main(String[] args) throws Exception {
        {
            Options options = new Options();

            options.addOption("a", "action", true,
                    "[crawl|check|all] -> only crawl proxies? only check already crawled proxies? or do both?");

            CommandLineParser parser = new BasicParser();
            CommandLine cmd = parser.parse(options, args);
            HelpFormatter formatter = new HelpFormatter();


            if (!cmd.hasOption("action")) {
                formatter.printHelp("java -jar proxy-checker-client.jar", options);
                return;

            }

            final String action = cmd.getOptionValue("action");

            final ConfigurableApplicationContext context = SpringApplication.run(ProxyCheckerApp.class);


            final ProxyCheckerApp app = context.getBean(ProxyCheckerApp.class);
            switch (action) {
                case "crawl":
                    app.crawl();
                    break;

                case "check":
                    app.check();
                    break;

                case "all":
                    app.crawl();
                    app.check();
                    break;

                default:
                    formatter.printHelp("java -jar proxy-checker-client.jar", options);
            }

            context.close();

        }
    }

}
