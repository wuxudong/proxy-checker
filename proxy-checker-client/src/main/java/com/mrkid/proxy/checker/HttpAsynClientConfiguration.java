package com.mrkid.proxy.checker;

import org.apache.http.client.config.RequestConfig;
import org.apache.http.config.ConnectionConfig;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.apache.http.impl.nio.reactor.IOReactorConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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

        final int TIMEOUT = 10 * 1000;
        // reactor config
        IOReactorConfig reactorConfig = IOReactorConfig.custom()
                .setConnectTimeout(TIMEOUT)
                .setSoTimeout(TIMEOUT).build();

        HttpAsyncClientBuilder asyncClientBuilder = HttpAsyncClientBuilder.create();
        asyncClientBuilder.setDefaultIOReactorConfig(reactorConfig);

        asyncClientBuilder.setMaxConnPerRoute(1000).setMaxConnTotal(1000);

        final CloseableHttpAsyncClient httpAsyncClient = asyncClientBuilder.build();
        httpAsyncClient.start();
        return httpAsyncClient;
    }
}
