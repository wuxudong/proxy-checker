package com.mrkid.proxy.crawler.vender.p881free;

import com.mrkid.proxy.crawler.ProxyFetcher;
import com.mrkid.proxy.dto.ProxyDTO;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * User: xudong
 * Date: 03/11/2016
 * Time: 12:47 PM
 */
public class P881FreeCrawler implements ProxyFetcher {

    private Logger logger = LoggerFactory.getLogger(P881FreeCrawler.class);

    @Override
    public List<ProxyDTO> crawl() throws Exception {
        Document doc = Jsoup.connect("http://881free.com/").timeout(30000).get();

        final Element body = doc.body();

        final List<ProxyDTO> proxies = Arrays.stream(body.text().split("\\s"))
                .filter(s -> StringUtils.isNotBlank(s))
                .map(s -> {

                    List<String> schemas = Arrays.asList("http://", "https://");


                    for (String schema : schemas) {
                        if (s.indexOf(schema) >= 0) {
                            s = s.substring(s.indexOf(schema) + schema.length());
                        }
                    }

                    final String[] token = s.split(":");

                    if (token.length != 2) {
                        logger.error("invalid address:" + s);
                        return null;
                    }

                    ProxyDTO proxy = new ProxyDTO();
                    proxy.setType("http");
                    proxy.setHost(token[0]);
                    proxy.setPort(Integer.valueOf(token[1]));
                    proxy.setSource("P881FREE");

                    return proxy;

                }).filter(p -> p != null).collect(Collectors.toList());

        return proxies;
    }
}
