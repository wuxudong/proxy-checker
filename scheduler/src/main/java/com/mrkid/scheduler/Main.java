package com.mrkid.scheduler;

import com.mrkid.proxy.Proxy;
import org.apache.commons.io.FileUtils;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.config.ConnectionConfig;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.apache.http.impl.nio.reactor.IOReactorConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.CodingErrorAction;
import java.util.List;
import java.util.stream.Collectors;

/**
 * User: xudong
 * Date: 31/10/2016
 * Time: 3:37 PM
 */
@SpringBootApplication
public class Main {
    public static void main(String[] args) throws IOException {
        if (args.length != 4) {
            System.out.printf("Usage: <originIp> <proxyFile> <proxyCheckerUrl> <output>");
        }

        String originIp = args[0];
        String proxyFile = args[1];
        String proxyCheckerUrl = args[2];
        String output = args[3];

        final ConfigurableApplicationContext context = SpringApplication.run(Main.class);
        final CloseableHttpAsyncClient httpAsyncClient = context.getBean(CloseableHttpAsyncClient.class);
        final Scheduler scheduler = context.getBean(Scheduler.class);

        httpAsyncClient.start();

        try (final PrintWriter writer = new PrintWriter(new FileWriter(output))) {
            final List<Proxy> proxies = FileUtils.readLines(new File(proxyFile), "utf-8")
                    .stream().map(line
                            -> {
                        final String[] tokens = line.split("\t");
                        String host = tokens[0];
                        int port = Integer.valueOf(tokens[1]);
                        String schema = tokens[2];
                        return new Proxy(schema, host, port);
                    }).collect(Collectors.toList());

            scheduler.check(originIp, proxyCheckerUrl,
                    proxies).stream().forEach(line -> writer.println(line));
        }

        httpAsyncClient.close();
        context.close();
    }

    @Bean
    public CloseableHttpAsyncClient getHttpAsyncClient() {

        final int TIMEOUT = 30 * 1000;
        // reactor config
        IOReactorConfig reactorConfig = IOReactorConfig.custom()
                .setConnectTimeout(TIMEOUT)
                .setSoTimeout(TIMEOUT).build();

        HttpAsyncClientBuilder asyncClientBuilder = HttpAsyncClientBuilder.create();
        asyncClientBuilder.setDefaultIOReactorConfig(reactorConfig);

        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectTimeout(TIMEOUT)
                .setConnectionRequestTimeout(TIMEOUT)
                .setSocketTimeout(TIMEOUT).build();
        asyncClientBuilder.setDefaultRequestConfig(requestConfig);

        ConnectionConfig connectionConfig = ConnectionConfig.custom()
                .setMalformedInputAction(CodingErrorAction.IGNORE)
                .setUnmappableInputAction(CodingErrorAction.IGNORE)
                .build();
        asyncClientBuilder.setDefaultConnectionConfig(connectionConfig);
        return asyncClientBuilder.build();
    }
}
