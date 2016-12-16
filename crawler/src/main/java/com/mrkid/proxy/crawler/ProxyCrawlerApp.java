package com.mrkid.proxy.crawler;

import com.mrkid.proxy.dto.ProxyDTO;
import com.mrkid.proxy.service.ProxyService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.Collectors;

/**
 * User: xudong
 * Date: 31/10/2016
 * Time: 3:17 PM
 */
@SpringBootApplication(scanBasePackages = "com.mrkid.proxy")
@EnableJpaRepositories("com.mrkid.proxy")
@EntityScan("com.mrkid.proxy")
@Component
public class ProxyCrawlerApp {

    @Autowired
    private ProxyService proxyService;

    @Autowired
    private List<ProxyFetcher> proxyFetchers;

    private static final Logger logger = LoggerFactory.getLogger(ProxyCrawlerApp.class);

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
        new HashSet<>(proxies).stream()
                .filter(p -> StringUtils.isNotBlank(p.getHost()))
                .forEach(p -> proxyService.saveProxy(p));
    }

    public static void main(String[] args) throws Exception {
        final ConfigurableApplicationContext context = SpringApplication.run(ProxyCrawlerApp.class);


        final ProxyCrawlerApp app = context.getBean(ProxyCrawlerApp.class);
        app.crawl();

        context.close();

    }

}
