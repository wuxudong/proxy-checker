package com.mrkid.scheduler;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.config.ConnectionConfig;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.apache.http.impl.nio.reactor.IOReactorConfig;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.CodingErrorAction;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * User: xudong
 * Date: 31/10/2016
 * Time: 3:39 PM
 */
@Component
public class Scheduler {
    private ObjectMapper objectMapper = new ObjectMapper();

    public List<String> check(String originIp, String proxyCheckerUrl, List<ProxyInput>
            proxyInputs) throws IOException {

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

        try (CloseableHttpAsyncClient httpclient = asyncClientBuilder.build()) {
            httpclient.start();

            final List<CompletableFuture<String>> futures = proxyInputs.stream().map(proxyInput -> {

                RequestConfig config = RequestConfig.custom()
                        .setProxy(new HttpHost(proxyInput.getHost(), proxyInput.getPort()))
                        .build();

                HttpGet request = new HttpGet(proxyCheckerUrl + "?" +
                        "originIp=" + originIp +
                        "&proxyIp=" + proxyInput.getHost() + ":" + proxyInput.getPort());
                request.setConfig(config);

                return getProxyResponse(httpclient, request, proxyInput);
            }).collect(Collectors.toList());

            return CompletableFuture.allOf(futures.toArray(new CompletableFuture[futures.size()]))
                    .thenApply(
                            v ->
                                    futures.stream().map(CompletableFuture::join)
                                            .filter(s -> StringUtils.isNotBlank(s))
                                            .collect(Collectors.toList()))
                    .join();
        }


    }

    private CompletableFuture<String> getProxyResponse(CloseableHttpAsyncClient httpclient, HttpGet request,
                                                       ProxyInput proxyInput) {
        CompletableFuture<String> promise = new CompletableFuture<>();


        httpclient.execute(request, new FutureCallback<HttpResponse>() {
            @Override
            public void completed(HttpResponse httpResponse) {
                if (httpResponse.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
                    promise.complete(null);
                } else {
                    try {
                        final String value = IOUtils.toString(httpResponse.getEntity().getContent(), "utf-8");
                        promise.complete(objectMapper.writeValueAsString(objectMapper.readTree(value)));
                    } catch (IOException e) {
                        e.printStackTrace();
                        promise.complete(null);
                    }
                }

            }

            @Override
            public void failed(Exception e) {
                e.printStackTrace();
                promise.complete(null);
            }

            @Override
            public void cancelled() {
                promise.cancel(false);
            }
        });

        return promise;
    }

    private synchronized void append(PrintWriter writer, String content) {
        writer.println(content);
    }
}
