package com.mrkid.proxy.archive;

import com.mrkid.proxy.model.Proxy;
import com.mrkid.proxy.service.ProxyService;
import io.reactivex.Flowable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * User: xudong
 * Date: 31/10/2016
 * Time: 3:17 PM
 */
@SpringBootApplication(scanBasePackages = "com.mrkid.proxy")
@EnableJpaRepositories("com.mrkid.proxy")
@EntityScan("com.mrkid.proxy")
@Component
public class ArchiveApp {
    @Autowired
    private ProxyService proxyService;

    public static void main(String[] args) throws Exception {
        final ConfigurableApplicationContext context = SpringApplication.run(ArchiveApp.class);

        context.getBean(ArchiveApp.class).archiveLowQualityProxies();

        context.close();

    }

    private void archiveLowQualityProxies() {
        AtomicInteger page = new AtomicInteger(0);
        int size = 1000;

        Flowable.<List<Proxy>>generate(e -> {
            final List<Proxy> inactiveProxies = proxyService.getInactiveProxies(page.getAndIncrement(), size);
            if (inactiveProxies.isEmpty()) {
                e.onComplete();
            } else {
                e.onNext(inactiveProxies);
            }
        })
                .concatMapIterable(l -> l)
                .doOnNext(p -> proxyService.archive(p))
                .blockingSubscribe();

    }
}
