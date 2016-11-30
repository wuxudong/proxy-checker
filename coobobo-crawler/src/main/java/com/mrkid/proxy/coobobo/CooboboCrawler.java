package com.mrkid.proxy.coobobo;

import com.mrkid.proxy.dto.Proxy;
import com.mrkid.proxy.dto.Source;
import edu.uci.ics.crawler4j.crawler.Page;
import edu.uci.ics.crawler4j.crawler.WebCrawler;
import edu.uci.ics.crawler4j.parser.HtmlParseData;
import edu.uci.ics.crawler4j.url.WebURL;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.text.ParseException;
import java.util.Date;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * User: xudong
 * Date: 03/11/2016
 * Time: 12:47 PM
 */
public class CooboboCrawler extends WebCrawler {

    public static final String STORE_ROOT = "./crawl/coobobo/root";
    public static final String SEED = "http://www.coobobo.com/free-http-proxy/";

    private BlockingQueue<Proxy> outputQueue;

    private boolean crawlHistory = false;

    private static final String HISTROY_LIST_URL = "http://www.coobobo.com/free-http-proxy-everyday";

    private static final Pattern HISTROY_URL_PATTERN = Pattern.compile(
            "http://www.coobobo.com/free-http-proxy/(\\d){4}-(\\d){2}-(\\d){2}");

    private static final Pattern LATEST_URL_PATTERN = Pattern.compile(
            "http://www.coobobo.com/free-http-proxy/(\\d)+");


    public CooboboCrawler(BlockingQueue<Proxy> outputQueue, boolean crawlHistory) {
        this.outputQueue = outputQueue;
        this.crawlHistory = crawlHistory;
    }

    @Override
    public boolean shouldVisit(Page referringPage, WebURL url) {
        if (url.getURL().equals(HISTROY_LIST_URL)) {
            return true;
        }

        if (LATEST_URL_PATTERN.matcher(url.getURL()).matches()) {
            return true;
        }


        if (crawlHistory) {
            return HISTROY_URL_PATTERN.matcher(url.getURL()).matches();
        }

        return false;
    }

    /**
     * This function is called when a page is fetched and ready
     * to be processed by your program.
     */
    @Override
    public void visit(Page page) {
        String url = page.getWebURL().getURL();
        System.out.println("URL: " + url);

        if (page.getParseData() instanceof HtmlParseData) {
            HtmlParseData htmlParseData = (HtmlParseData) page.getParseData();
            String html = htmlParseData.getHtml();

            final Document doc = Jsoup.parse(html);
            final Elements tables = doc.select("table");

            final List<Proxy> proxies = tables.stream().map(table -> extractProxies(table)).flatMap(l -> l.stream())
                    .collect(Collectors.toList());

            proxies.forEach(p -> outputQueue.offer(p));
        }
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
                String headerName = header.get(i).text();
                switch (headerName) {
                    case "IP":
                        host = cells.get(i).text();
                        break;
                    case "端口":
                        port = Integer.valueOf(cells.get(i).text());
                        break;
                    case "位置":
                        location = cells.get(i).text();
                        break;
                    case "最后验证时间":
                        try {
                            lastCheckSuccess = DateUtils.parseDate(cells.get(i).text(), "yyyy-MM-dd HH:MM:ss");
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                        break;
                    default:
                }

            }

            if (StringUtils.isBlank(host) || port == 0) {
                return null;
            }

            Proxy proxy = new Proxy("http", host, port);
            proxy.setLocation(location);
            proxy.setLastCheckSuccess(lastCheckSuccess);

            proxy.setSource(Source.COOBOBO.name());

            return proxy;
        }).filter(p -> p != null).collect(Collectors.toList());
    }

}
