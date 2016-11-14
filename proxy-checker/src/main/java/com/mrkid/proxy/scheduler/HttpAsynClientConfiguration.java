package com.mrkid.proxy.scheduler;

import org.apache.http.client.config.RequestConfig;
import org.apache.http.config.ConnectionConfig;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.apache.http.impl.nio.reactor.IOReactorConfig;
import org.springframework.beans.factory.annotation.Autowire;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

import java.nio.charset.CodingErrorAction;

/**
 * User: xudong
 * Date: 03/11/2016
 * Time: 4:35 PM
 */
@Configuration
public class HttpAsynClientConfiguration {
    @Bean
    public CloseableHttpAsyncClient httpAsyncClient() {

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
        final CloseableHttpAsyncClient httpAsyncClient = asyncClientBuilder.build();
        httpAsyncClient.start();
        return httpAsyncClient;
    }
}
