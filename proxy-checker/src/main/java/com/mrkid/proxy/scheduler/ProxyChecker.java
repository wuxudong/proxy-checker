package com.mrkid.proxy.scheduler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mrkid.proxy.dto.Proxy;
import com.mrkid.proxy.dto.ProxyCheckResponse;
import io.reactivex.Flowable;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.protocol.HttpContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.InetSocketAddress;
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

    public Flowable<ProxyCheckResponse> getProxyResponse(String originIp,
                                                         String proxyCheckerUrl, Proxy proxy) {
        return toFlowable(asyncCheck(originIp, proxyCheckerUrl, proxy));
    }

    private CompletableFuture<ProxyCheckResponse> asyncCheck(String originIp,
                                                             String proxyCheckerUrl, Proxy proxy) {

        CompletableFuture<ProxyCheckResponse> promise = new CompletableFuture<>();

        final ProxyCheckResponse errorResponse = new ProxyCheckResponse("", "", "", proxy, false);

        final HttpPost request = new HttpPost(proxyCheckerUrl + "?originIp=" + originIp);

        HttpContext httpContext = HttpClientContext.create();


        logger.info("check proxy: " + proxy);

        if (proxy.getSchema().equalsIgnoreCase("socks5") || proxy.getSchema().equalsIgnoreCase("socks4")) {
            httpContext.setAttribute("socks.address", new InetSocketAddress(proxy.getHost(), proxy.getPort()));
        } else if (proxy.getSchema().equalsIgnoreCase("http") || proxy.getSchema().equalsIgnoreCase("https")) {
            RequestConfig config = RequestConfig.custom()
                    .setProxy(new HttpHost(proxy.getHost(), proxy.getPort(), proxy.getSchema().toLowerCase()))
                    .build();

            request.setConfig(config);
        }


        try {
            request.setEntity(new StringEntity(objectMapper.writeValueAsString(proxy), ContentType.APPLICATION_JSON));
        } catch (JsonProcessingException e) {
            logger.error("unable to write json", e);
        }

        httpclient.execute(request, httpContext, new FutureCallback<HttpResponse>() {
            @Override
            public void completed(HttpResponse httpResponse) {
                if (httpResponse.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
                    promise.complete(errorResponse);
                } else {
                    try {
                        final String value = IOUtils.toString(httpResponse.getEntity().getContent(), "utf-8");
                        promise.complete(objectMapper.readValue(value, ProxyCheckResponse.class));
                    } catch (IOException e) {
                        logger.error("unable to parse check response of " + request.getEntity(), e);

                        promise.complete(errorResponse);
                    }
                }

            }

            @Override
            public void failed(Exception e) {
                logger.error("failure of  " + request.getEntity(), e);
                promise.complete(errorResponse);
            }

            @Override
            public void cancelled() {
                promise.cancel(false);
            }
        });

        return promise;
    }

    private <T> Flowable<T> toFlowable(CompletableFuture<T> future) {
        return Flowable.<T>generate(emitter ->
                future.whenComplete((result, error) -> {
                    if (error != null) {
                        emitter.onError(error);
                    } else {
                        logger.info("check result: " + result);

                        emitter.onNext(result);
                        emitter.onComplete();
                    }
                })).onExceptionResumeNext(Flowable.empty());
    }
}
