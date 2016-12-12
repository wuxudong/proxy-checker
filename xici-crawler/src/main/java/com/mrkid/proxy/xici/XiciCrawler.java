package com.mrkid.proxy.xici;

import com.mrkid.proxy.ProxyFetcher;
import com.mrkid.proxy.dto.ProxyDTO;
import com.mrkid.proxy.dto.Source;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * User: xudong
 * Date: 03/11/2016
 * Time: 12:47 PM
 */
public class XiciCrawler implements ProxyFetcher {

    @Override
    public List<ProxyDTO> crawl() throws Exception {
        Document doc = Jsoup.connect("http://api.xicidaili.com/free2016.txt").timeout(30000).get();


        final Element body = doc.body();

        final List<ProxyDTO> proxies = extractProxies(body);

        return proxies;

    }

    private List<ProxyDTO> extractProxies(Element table) {
        return Arrays.stream(table.text().split("\\s")).map(s -> {
            final String[] token = s.split(":");
            ProxyDTO proxy = new ProxyDTO("http", token[0], Integer.valueOf(token[1]));
            proxy.setSource(Source.XICI.name());

            return proxy;

        }).collect(Collectors.toList());
    }

}
