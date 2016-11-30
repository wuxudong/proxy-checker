package com.mrkid.proxy.p881free;

import com.mrkid.proxy.dto.Proxy;
import com.mrkid.proxy.dto.Source;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * User: xudong
 * Date: 03/11/2016
 * Time: 12:47 PM
 */
public class P881FreeCrawler {

    public List<Proxy> fetchProxy() throws IOException {
        Document doc = Jsoup.connect("http://881free.com/").timeout(30000).get();


        final Elements tables = doc.select("article table");

        final List<Proxy> proxies = tables.stream().map(table -> extractProxies(table)).flatMap(l -> l.stream())
                .collect(Collectors.toList());

        return proxies;
    }

    private List<Proxy> extractProxies(Element table) {
        final Elements header = table.select("thead tr th");
        final Elements rows = table.select("tbody tr");

        return rows.stream().map(row -> {
            final int size = header.size();

            String host = "";
            int port = 0;

            String location = null;
            Date lastCheckSuccess = null;

            Elements cells = row.select("td");

            for (int i = 0; i < size; i++) {
                String headerText = header.get(i).text();
                switch (headerText) {
                    case "IP地址":
                        host = cells.get(i).text();
                        break;
                    case "端口":
                        port = Integer.valueOf(cells.get(i).text());
                        break;
                    case "国家":
                        location = cells.get(i).text();
                        break;
                    default:
                }

            }

            if (StringUtils.isBlank(host) || port == 0) {
                return null;
            }

            Proxy proxy = new Proxy("http", host, port);
            proxy.setLocation(location);
            proxy.setSource(Source.P881FREE.name());

            return proxy;
        }).filter(p -> p != null).collect(Collectors.toList());
    }

}
