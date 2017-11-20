package com.mrkid.proxy.crawler;

import java.util.HashSet;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.Collectors;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

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

import com.mrkid.proxy.dto.ProxyDTO;
import com.mrkid.proxy.service.ProxyService;

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
        ScriptEngineManager factory = new ScriptEngineManager();
        ScriptEngine engine = factory.getEngineByName("JavaScript");

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
                .forEach(p -> {
                    this.evalHostIfNeeded(engine, p);
                    proxyService.saveProxy(p);
                });
    }
    
    /**
     * Extra post-processing to handle JS "document.write" case
     */
    private void evalHostIfNeeded(ScriptEngine engine, ProxyDTO proxy) {
        if (proxy.getHost().indexOf("document.write") != -1) {
            String result;
            try {
                result = (String) engine.eval("var document = { write: function(s) {return s;} }; " + proxy.getHost());
                proxy.setHost(result);
            } catch (ScriptException e) {
                logger.warn("Failed evaling javascript on host name of proxy: " + proxy);
            }
        }
    }

    public static void main(String[] args) throws Exception {
        final ConfigurableApplicationContext context = SpringApplication.run(ProxyCrawlerApp.class);


        final ProxyCrawlerApp app = context.getBean(ProxyCrawlerApp.class);
        app.crawl();

        context.close();

    }

}
