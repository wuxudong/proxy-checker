package com.mrkid.proxy.haoip;

import com.mrkid.proxy.ProxyFetcher;
import com.mrkid.proxy.dto.ProxyDTO;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * User: xudong
 * Date: 03/11/2016
 * Time: 12:47 PM
 */
public class HaoIPCrawler implements ProxyFetcher {

    @Override
    public List<ProxyDTO> crawl() throws Exception {
        Document doc = Jsoup.connect("http://haoip.cc/tiqu.htm").timeout(30000).get();

        final Elements tables = doc.select("div .row .col-xs-12");

        final List<ProxyDTO> proxies = tables.stream().map(table -> extractProxies(table)).flatMap(l -> l.stream())
                .collect(Collectors.toList());

        return proxies;
    }

    private List<ProxyDTO> extractProxies(Element table) {
        return Arrays.stream(table.text().split("\\s")).map(s -> {
            final String[] token = s.split(":");
            ProxyDTO proxy = new ProxyDTO();
            proxy.setSchema("http");
            proxy.setHost(token[0]);
            proxy.setPort(Integer.valueOf(token[1]));
            proxy.setSource("HAOIP");

            return proxy;

        }).collect(Collectors.toList());
    }

}
