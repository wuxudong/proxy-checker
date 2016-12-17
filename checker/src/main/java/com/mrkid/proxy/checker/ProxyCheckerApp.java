package com.mrkid.proxy.checker;

import com.mrkid.proxy.dto.ProxyDTO;
import com.mrkid.proxy.model.Proxy;
import com.mrkid.proxy.service.ProxyService;
import io.reactivex.Flowable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.TimeUnit;
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
public class ProxyCheckerApp {
    @Autowired
    private ProxyChecker proxyChecker;

    @Autowired
    private ProxyService proxyService;

    @Autowired
    private Integer maxConcurrency;

    @Autowired
    private List<ProxyCheckResponseWriter> proxyCheckResponseWriters;

    private static final Logger logger = LoggerFactory.getLogger(ProxyCheckerApp.class);

    private Flowable<ProxyDTO> proxyGenerator() {
        AtomicInteger page = new AtomicInteger(0);
        int size = 1000;

        return Flowable.<List<Proxy>>generate(e -> {
            final List<Proxy> checkableProxies = proxyService.getCheckableProxies(page.getAndIncrement(), size);
            if (checkableProxies.isEmpty()) {
                e.onComplete();
            } else {
                e.onNext(checkableProxies);
            }
        })
                .concatMapIterable(l -> l)
                .map(p -> {
                    ProxyDTO proxyDTO = new ProxyDTO();
                    proxyDTO.setSchema(p.getKey().getSchema());
                    proxyDTO.setHost(p.getKey().getHost());
                    proxyDTO.setPort(p.getKey().getPort());
                    proxyDTO.setSource(p.getSource());
                    return proxyDTO;
                });
    }

    private void check() {
        AtomicInteger concurrency = new AtomicInteger(0);

        AtomicInteger dispatchedCount = new AtomicInteger(0);

        Flowable.interval(1, 1, TimeUnit.SECONDS).subscribe(l ->
                System.out.println("dispatchedCount " + dispatchedCount.get() + " concurrency " + concurrency.get())
        );

        proxyGenerator()
                .doOnNext(p -> System.out.println("check " + p))
                .doOnNext(p -> concurrency.incrementAndGet())
                .doOnNext(p -> dispatchedCount.incrementAndGet())
                .flatMap(p -> proxyChecker.check(p), maxConcurrency)
                .doOnNext(p -> concurrency.decrementAndGet())
                .doOnNext(p -> proxyService.saveProxyCheckResponse(p))
                .doOnNext(p -> proxyCheckResponseWriters.forEach(writer -> {
                    if (writer.shouldWrite(p)) writer.write(p);
                })).blockingSubscribe();

    }


    public static void main(String[] args) throws Exception {
        final ConfigurableApplicationContext context = SpringApplication.run(ProxyCheckerApp.class);

        final ProxyCheckerApp app = context.getBean(ProxyCheckerApp.class);
        app.check();

        context.close();
    }

}
