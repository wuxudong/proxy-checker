package com.mrkid.proxy.checker;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mrkid.proxy.dto.ProxyCheckResponse;
import com.mrkid.proxy.dto.ProxyDTO;
import com.mrkid.proxy.dto.ProxyType;
import io.reactivex.Flowable;
import io.reactivex.processors.BehaviorProcessor;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.protocol.HttpContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

/**
 * User: xudong
 * Date: 31/10/2016
 * Time: 3:39 PM
 */
@Component
public class ProxyChecker {
    private ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private CloseableHttpAsyncClient httpclient;

    private static final Logger logger = LoggerFactory.getLogger(ProxyChecker.class);

    public Flowable<ProxyCheckResponse> check(ProxyDTO proxyDTO) {

        long start = System.currentTimeMillis();

        final Flowable<ProxyCheckResponse> proxyCheckResponseFlow = getProxyResponse(proxyDTO);

        final Flowable<String> ip138Flow = generalGet("http://1212.ip138.com/ic.asp", proxyDTO);

        return Flowable.zip(proxyCheckResponseFlow, ip138Flow,
                (response, s) -> response)
                .onErrorResumeNext(e -> {
                    return Flowable.just(new ProxyCheckResponse("", "", proxyDTO, false));
                }).doFinally(
                        () -> logger.info("checking {}://{}:{} takes {} ms",
                                proxyDTO.getSchema(), proxyDTO.getHost(), proxyDTO.getPort(),
                                System.currentTimeMillis() - start)
                );
    }


    private Flowable<ProxyCheckResponse> getProxyResponse(ProxyDTO proxy) {
        final Flowable<String> remoteIpFlow = toFlowable(execute(proxy, new HttpGet("http://httpbin.org/ip")));

        final Flowable<String> headersFlow = toFlowable(execute(proxy, new HttpGet("http://httpbin.org/headers")));

        return remoteIpFlow.zipWith(headersFlow, (remoteIpResponse, headersResponse) -> {
            String remoteIp = objectMapper.readTree(remoteIpResponse).get("origin").asText();
            final JsonNode headersNode = objectMapper.readTree(headersResponse).get("headers");

            final Set<String> possibleHeaderNames = new HashSet<>(Arrays.asList("HTTP_VIA", "HTTP_X_FORWARDED_FOR",
                    "HTTP_FORWARDED_FOR", "HTTP_X_FORWARDED", "HTTP_FORWARDED", "HTTP_CLIENT_IP",
                    "HTTP_FORWARDED_FOR_IP", "VIA", "X_FORWARDED_FOR", "FORWARDED_FOR", "X_FORWARDED",
                    "FORWARDED", "CLIENT_IP", "FORWARDED_FOR_IP", "HTTP_PROXY_CONNECTION"));


            boolean highAnonymity = true;
            final Iterator<String> fieldNames = headersNode.fieldNames();
            while (fieldNames.hasNext()) {
                if (possibleHeaderNames.contains(fieldNames.next().toUpperCase())) {
                    highAnonymity = false;
                }
            }

            ProxyCheckResponse proxyCheckResponse = new ProxyCheckResponse();
            proxyCheckResponse.setProxy(proxy);
            proxyCheckResponse.setRemoteIp(remoteIp);
            proxyCheckResponse.setValid(true);
            if (highAnonymity) {
                proxyCheckResponse.setProxyType(ProxyType.HIGH_ANONYMITY_PROXY);
            } else {
                proxyCheckResponse.setProxyType(ProxyType.TRANSPARENT_PROXY);
            }

            return proxyCheckResponse;
        });

    }

    private Flowable<String> generalGet(String targetUrl, ProxyDTO proxy) {
        final HttpGet request = new HttpGet(targetUrl);
        return toFlowable(execute(proxy, request));
    }

    private CompletableFuture<String> execute(ProxyDTO proxy, HttpRequestBase request) {

        CompletableFuture<String> promise = new CompletableFuture<>();

        HttpContext httpContext = HttpClientContext.create();

        logger.info("check proxy: " + proxy + " for url " + request.getURI().toString());

        if (proxy.getSchema().equalsIgnoreCase("socks5") || proxy.getSchema().equalsIgnoreCase("socks4")) {
            httpContext.setAttribute("socks.address", new InetSocketAddress(proxy.getHost(), proxy.getPort()));
        } else if (proxy.getSchema().equalsIgnoreCase("http") || proxy.getSchema().equalsIgnoreCase("https")) {
            RequestConfig config = RequestConfig.custom()
                    .setProxy(new HttpHost(proxy.getHost(), proxy.getPort(), proxy.getSchema().toLowerCase()))
                    .build();

            request.setConfig(config);

        }

        httpclient.execute(request, httpContext, new FutureCallback<HttpResponse>() {
            @Override
            public void completed(HttpResponse httpResponse) {
                logger.info("completed proxy: " + proxy + " for url " + request.getURI().toString());

                if (httpResponse.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
                    final String message = "status code is " + httpResponse.getStatusLine
                            ().getStatusCode();
                    logger.error(message);
                    promise.completeExceptionally(new RuntimeException(message));
                } else {
                    try {
                        final String body = IOUtils.toString(httpResponse.getEntity().getContent(), "utf-8");
                        logger.info("get " + body + " from " + request.getURI() + " through " + proxy);
                        promise.complete(body);
                    } catch (IOException e) {
                        logger.error("unable to parse check response of " + httpResponse.getEntity(), e);

                        promise.completeExceptionally(e);
                    }
                }
            }

            @Override
            public void failed(Exception e) {
                logger.error("failed to visit " + request.getURI().toString() + " through " + proxy + " caused by " + e
                                .getMessage(),
                        e);
                promise.completeExceptionally(e);
            }

            @Override
            public void cancelled() {
                logger.info("cancel proxy: " + proxy + " for url " + request.getURI().toString());

                promise.cancel(false);
            }
        });

        return promise;
    }

    private <T> Flowable<T> toFlowable(CompletableFuture<T> future) {
        final BehaviorProcessor<T> processor = BehaviorProcessor.create();

        future.whenComplete((result, error) -> {
            if (error != null) {
                processor.onError(error);
            } else {
                processor.onNext(result);
                processor.onComplete();
            }
        });


        return processor;


    }
}
