package com.mrkid.proxy.service;

import com.mrkid.proxy.dto.Proxy;
import com.mrkid.proxy.dto.ProxyCheckResponse;
import com.mrkid.proxy.scheduler.ProxyChecker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.Collectors;

/**
 *
 * Check if proxy works.
 *
 * User: xudong
 * Date: 03/11/2016
 * Time: 4:54 PM
 */
@Component
public class ProxyCheckMaster {
    private BlockingQueue<Proxy> heartBeatQueue = new LinkedBlockingQueue<>();

    private BlockingQueue<Proxy> crawlerQueue = new LinkedBlockingQueue<>();

    private BlockingQueue<Proxy> recheckValidProxiesQueue = new LinkedBlockingQueue<>();

    private Map<Proxy, Date> validProxies = new ConcurrentHashMap<>();


    private static long CHECK_INTERVAL = 5 * 60 * 1000;

    @Autowired
    private ProxyChecker proxyChecker;

    @Autowired
    private String originIp;

    @Autowired
    private String proxyCheckUrl;


//    @PostConstruct
//    public void init() {
//        new ProxyCheckWorker(heartBeatQueue).start();
//        new ProxyCheckWorker(crawlerQueue).start();
//        new ProxyCheckWorker(recheckValidProxiesQueue).start();
//        new HealthChecker().start();
//    }

    public void heartbeat(Proxy proxy) {
        heartBeatQueue.add(proxy);
    }

    class HealthChecker extends Thread {
        @Override
        public void run() {
            while (true) {
                try {
                    Date current = new Date();

                    if (recheckValidProxiesQueue.isEmpty()) {
                        final ArrayList<Map.Entry<Proxy, Date>> entries = new ArrayList<>(validProxies.entrySet());
                        Collections.sort(entries, (o1, o2) -> o1.getValue().compareTo(o2.getValue()));
                        entries.stream()
                                .filter(e -> (current.getTime() - e.getValue().getTime()) > CHECK_INTERVAL)
                                .forEach(e -> recheckValidProxiesQueue.offer(e.getKey()));
                    }

                    Thread.sleep(5000l);
                } catch (InterruptedException e) {
                }
            }
        }
    }


    class ProxyCheckWorker extends Thread {
        private BlockingQueue<Proxy> input;

        public ProxyCheckWorker(BlockingQueue<Proxy> input) {
            this.input = input;
        }

        @Override
        public void run() {
            while (true) {
                try {
                    List<Proxy> batch = new ArrayList<>();

                    final int count = input.drainTo(batch, 100);
                    if (count == 0) {
                        Thread.sleep(1000l);
                    } else {
                        final List<ProxyCheckResponse> responses = proxyChecker.check(originIp, proxyCheckUrl,
                                batch.stream().collect(Collectors.toList()));
                        responses.stream().forEach(r -> {
                                    if (r.isValid()) {
                                        validProxies.put(r.getProxy(), new Date());
                                    } else {
                                        validProxies.remove(r.getProxy());
                                    }
                                }
                        );
                    }
                } catch (InterruptedException e) {
                }
            }

        }
    }


}
