package com.mrkid.proxy.crawler;

import com.mrkid.proxy.crawler.vender.cnproxy.CnProxyFetcher;
import com.mrkid.proxy.crawler.vender.coobobo.CooboboProxyFetcher;
import com.mrkid.proxy.crawler.vender.haoip.HaoIPCrawler;
import com.mrkid.proxy.crawler.vender.ip3366.Ip3366ProxyFetcher;
import com.mrkid.proxy.crawler.vender.kxdaili.KxDailiProxyFetcher;
import com.mrkid.proxy.crawler.vender.p66ip.P66IPProxyFetcher;
import com.mrkid.proxy.crawler.vender.p881free.P881FreeCrawler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.List;

/**
 * User: xudong
 * Date: 12/12/2016
 * Time: 12:15 PM
 */
@Configuration
public class ProxyCrawlerConfiguration {

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
    public List<ProxyFetcher> proxyFetchers(ProxyFetcher... fetchers) {
        return Arrays.asList(fetchers);
    }

}
