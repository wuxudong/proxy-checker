package com.mrkid.proxy;

import com.mrkid.proxy.cnproxy.CnProxyFetcher;
import com.mrkid.proxy.coobobo.CooboboProxyFetcher;
import com.mrkid.proxy.dto.ProxyCheckResponse;
import com.mrkid.proxy.haoip.HaoIPCrawler;
import com.mrkid.proxy.ip3366.Ip3366ProxyFetcher;
import com.mrkid.proxy.kxdaili.KxDailiProxyFetcher;
import com.mrkid.proxy.p66ip.P66IPProxyFetcher;
import com.mrkid.proxy.p881free.P881FreeCrawler;
import com.mrkid.proxy.utils.AddressUtils;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.apache.http.impl.nio.reactor.IOReactorConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;

/**
 * User: xudong
 * Date: 12/12/2016
 * Time: 12:15 PM
 */
@Configuration
public class CrawlerConfiguration {

    @Bean
    public ProxyFetcher cnProxyFetcher() {
        return new CnProxyFetcher();
    }

    @Bean
    public ProxyFetcher cooboboProxyFetcher() {
        return new CooboboProxyFetcher(false);
    }

    @Bean
    public ProxyFetcher ip3366ProxyFetcher() {
        return new Ip3366ProxyFetcher();
    }

    @Bean
    public ProxyFetcher kxdailiProxyFetcher() {
        return new KxDailiProxyFetcher();
    }

    @Bean
    public ProxyFetcher p66ipProxyFetcher() {
        return new P66IPProxyFetcher(false);
    }

    @Bean
    public ProxyFetcher p881freeProxyFetcher() {
        return new P881FreeCrawler();
    }

    @Bean
    public ProxyFetcher haoipProxyFetcher() {
        return new HaoIPCrawler();
    }

    // goubanjia's proxy quality is too bad, always unavailable
//    @Bean
//    public ProxyFetcher goubanjiaProxyFetcher() {
//        return new GoubanjiaProxyFetcher();
//    }


    // kuaidaili is sensitive to high frequency visit. proxy quality is too bad, always unavailable
//    @Bean
//    public ProxyFetcher kuaidailiProxyFetcher() {
//        return new KuaiDaiLiProxyFetcher();
//    }

    @Bean
    public CloseableHttpAsyncClient httpAsyncClient() {

        final int TIMEOUT = 30 * 1000;
        // reactor config
        IOReactorConfig reactorConfig = IOReactorConfig.custom()
                .setConnectTimeout(TIMEOUT)
                .setSoTimeout(TIMEOUT).build();

        HttpAsyncClientBuilder asyncClientBuilder = HttpAsyncClientBuilder.create();
        asyncClientBuilder.setDefaultIOReactorConfig(reactorConfig);

        RequestConfig config = RequestConfig.custom()
                .setConnectTimeout(TIMEOUT)
                .setConnectionRequestTimeout(TIMEOUT)
                .setSocketTimeout(TIMEOUT).build();
        asyncClientBuilder.setDefaultRequestConfig(config);

        asyncClientBuilder.setMaxConnPerRoute(3000).setMaxConnTotal(3000);

        asyncClientBuilder.setUserAgent("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_12_1) AppleWebKit/537.36 (KHTML, " +
                "like Gecko) Chrome/54.0.2840.98 Safari/537.36");

        final CloseableHttpAsyncClient httpAsyncClient = asyncClientBuilder.build();
        httpAsyncClient.start();
        return httpAsyncClient;
    }


    @Bean
    public File dataDirectory() {
        File dataDirectory = new File("data");
        if (!dataDirectory.exists()) {
            dataDirectory.mkdir();
        }

        return dataDirectory;
    }

    @Bean
    public List<ProxyFetcher> proxyFetchers(ProxyFetcher... fetchers) {
        return Arrays.asList(fetchers);
    }

    @Bean
    public ProxyCheckResponseWriter squidFormatWriter(File dataDirectory) throws IOException {
        return new ProxyCheckResponseWriter() {
            final PrintWriter writer = new PrintWriter(new FileWriter(new File(dataDirectory,
                    "proxy.squid")));

            @Override
            public boolean shouldWrite(ProxyCheckResponse response) {
                return response.isValid() && "http".equalsIgnoreCase(response.getProxy().getSchema());
            }

            @Override
            public void write(ProxyCheckResponse response) {
                writer.println(
                        String.format(
                                "cache_peer %s parent %d 0 round-robin no-query connect-fail-limit=1 #%s",
                                response.getProxy().getHost(),
                                response.getProxy().getPort(),
                                response.getProxyType().name()));

            }

            @Override
            public void close() throws IOException {
                writer.close();

            }
        };
    }

    @Bean
    public ProxyCheckResponseWriter plainFormatWriter(File dataDirectory) throws IOException {
        return new ProxyCheckResponseWriter() {
            final PrintWriter writer = new PrintWriter(new FileWriter(new File(dataDirectory,
                    "proxy.plain")));

            @Override
            public boolean shouldWrite(ProxyCheckResponse response) {
                return response.isValid() && "http".equalsIgnoreCase(response.getProxy().getSchema());
            }

            @Override
            public void write(ProxyCheckResponse response) {
                writer.println(response.getProxy().getHost() + ":" + response.getProxy().getPort());

            }

            @Override
            public void close() throws IOException {
                writer.close();
            }
        };
    }

    @Bean
    public List<ProxyCheckResponseWriter> proxyCheckResponseWriters(ProxyCheckResponseWriter... writers) {
        return Arrays.asList(writers);
    }


    @Bean
    public String ip() throws IOException {
        return AddressUtils.getMyPublicIp();
    }


}
