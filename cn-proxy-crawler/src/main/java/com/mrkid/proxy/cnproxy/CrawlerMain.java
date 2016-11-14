package com.mrkid.proxy.cnproxy;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mrkid.proxy.dto.Proxy;
import edu.uci.ics.crawler4j.crawler.CrawlController;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * User: xudong
 * Date: 14/11/2016
 * Time: 2:10 PM
 */
public class CrawlerMain {
    public static void main(String[] args) throws Exception {
        final PrintWriter output = new PrintWriter(new FileWriter(args[0]));

        if (args.length != 1) {
            System.out.println("usage: <output>");
        }
        final AtomicBoolean finished = new AtomicBoolean(false);
        final ObjectMapper objectMapper = new ObjectMapper();


        CrawlController controller = Crawler4jControllerHelper.getCrawlController();

        final BlockingQueue<Proxy> queue = new LinkedBlockingQueue<>();

        CrawlController.WebCrawlerFactory factory = Crawler4jControllerHelper.getWebCrawlerFactory(queue);

        Thread outputAppender = new Thread(() -> {
            while (!finished.get()) {
                try {
                    final Proxy proxy = queue.poll(1, TimeUnit.SECONDS);
                    if (proxy != null) {
                        // TODO
                        try {
                            output.println(objectMapper.writeValueAsString(proxy));
                        } catch (JsonProcessingException e) {
                        }

                    }
                } catch (InterruptedException e) {
                }
            }

        });

        outputAppender.start();

        int numberOfCrawlers = 1;
        controller.start(factory, numberOfCrawlers);
        controller.waitUntilFinish();

        finished.set(true);
        outputAppender.join();

        output.close();
    }
}
