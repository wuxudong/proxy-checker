package com.mrkid.proxy.checker;

import com.mrkid.proxy.dto.ProxyDTO;
import com.mrkid.proxy.model.Proxy;
import com.mrkid.proxy.service.ProxyService;
import io.reactivex.Flowable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;
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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
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
            System.out.println("process page " + page.get());
            if (checkableProxies.isEmpty()) {
                e.onComplete();
            } else {
                e.onNext(checkableProxies);
            }
        })
                .concatMapIterable(l -> l, 20000)
                .map(p -> {
                    ProxyDTO proxyDTO = new ProxyDTO();
                    proxyDTO.setType(p.getKey().getType());
                    proxyDTO.setHost(p.getKey().getHost());
                    proxyDTO.setPort(p.getKey().getPort());
                    proxyDTO.setSource(p.getSource());
                    return proxyDTO;
                });
    }

    private void check() {
        AtomicInteger concurrency = new AtomicInteger(0);

        AtomicInteger dispatchedCount = new AtomicInteger(0);

        Disposable audit = Flowable.interval(1, 1, TimeUnit.SECONDS).subscribe(l ->
                System.out.println("dispatchedCount " + dispatchedCount.get() + " concurrency " + concurrency.get())
        );

        ExecutorService checkerExecutor = Executors.newFixedThreadPool(10,
                new BasicThreadFactory.Builder().namingPattern("ProxyChecker-Scheduler-%d").build());

        ExecutorService saverExecutor = Executors.newFixedThreadPool(10,
                new BasicThreadFactory.Builder().namingPattern("ProxySaver-Scheduler-%d").build());


        proxyGenerator()
                .doOnNext(p -> concurrency.incrementAndGet())
                .doOnNext(p -> dispatchedCount.incrementAndGet())
                .flatMap(p ->
                                Flowable.defer(() -> proxyChecker.check(p))
                                        .subscribeOn(Schedulers.from(checkerExecutor))
                        , maxConcurrency)
                .doOnNext(p -> concurrency.decrementAndGet()).
                doOnNext(p -> proxyCheckResponseWriters.forEach(writer -> {
                    if (writer.shouldWrite(p)) writer.write(p);
                }))
                .flatMap(p ->
                        Flowable.defer(() -> Flowable.just(proxyService.saveProxyCheckResponse(p)))
                                .subscribeOn(Schedulers.from(saverExecutor)))
                .blockingSubscribe();

        audit.dispose();

        checkerExecutor.shutdown();
        saverExecutor.shutdown();

    }


    public static void main(String[] args) throws Exception {
        final ConfigurableApplicationContext context = SpringApplication.run(ProxyCheckerApp.class);

        final ProxyCheckerApp app = context.getBean(ProxyCheckerApp.class);
        app.check();

        context.close();
    }

}
