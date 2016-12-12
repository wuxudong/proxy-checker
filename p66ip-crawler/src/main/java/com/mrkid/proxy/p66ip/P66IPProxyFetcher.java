package com.mrkid.proxy.p66ip;

import com.mrkid.proxy.Crawl4jProxyFetcher;
import com.mrkid.proxy.dto.ProxyDTO;
import com.mrkid.proxy.dto.Source;
import edu.uci.ics.crawler4j.crawler.CrawlController;
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * User: xudong
 * Date: 03/11/2016
 * Time: 12:47 PM
 */
public class P66IPProxyFetcher extends Crawl4jProxyFetcher {

    public static final String STORE_ROOT = "./crawl/p66ip/root";
    public static final String SEED = "http://www.66ip.cn/1.html";

    private boolean crawlHistory = false;

    @Override
    protected String getStoreRoot() {
        return STORE_ROOT;
    }

    @Override
    protected List<String> getSeeds() {
        return Arrays.asList(SEED);
    }

    @Override
    protected int getPolitenessDelay() {
        return 0;
    }

    @Override
    protected CrawlController.WebCrawlerFactory<WebCrawler> getWebCrawlerFactory() {
        return () -> new P66IPCrawler(crawlHistory);
    }

    public P66IPProxyFetcher(boolean crawlHistory) {
        this.crawlHistory = crawlHistory;
    }
}


class P66IPCrawler extends WebCrawler {


    private List<ProxyDTO> result = new ArrayList<>();

    private boolean crawlHistory = false;

    private final Pattern pagePattern = Pattern.compile("http://www.66ip.cn/(\\d+).html");

    public P66IPCrawler(boolean crawlHistory) {
        this.crawlHistory = crawlHistory;
    }

    @Override
    public boolean shouldVisit(Page referringPage, WebURL url) {
        final Matcher matcher = pagePattern.matcher(url.getURL());
        if (!matcher.matches()) {
            return false;
        } else {

            if (crawlHistory) {
                return true;
            } else {
                int page = Integer.valueOf(matcher.group(1));

                return page < 10;
            }
        }
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

            final List<ProxyDTO> proxies = tables.stream().map(table -> extractProxies(table)).flatMap(l -> l.stream())
                    .collect(Collectors.toList());

            proxies.forEach(p -> result.add(p));
        }
    }

    private List<ProxyDTO> extractProxies(Element table) {
        final Elements header = table.select("tr:eq(0) td");
        final Elements rows = table.select("tr:gt(0)");

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
                    case "ip":
                        host = cells.get(i).text();
                        break;
                    case "端口号":
                        port = Integer.valueOf(cells.get(i).text());
                        break;
                    case "代理位置":
                        location = cells.get(i).text();
                        break;
                    case "验证时间":
                        try {
                            lastCheckSuccess = DateUtils.parseDate(cells.get(i).text().split("\\s+")[0],
                                    "yyyy年MM月dd日HH时");
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        break;
                    default:
                }

            }

            if (StringUtils.isBlank(host) || port == 0) {
                return null;
            }

            ProxyDTO proxy = new ProxyDTO("http", host, port);
            proxy.setLocation(location);
            proxy.setLastCheckSuccess(lastCheckSuccess);

            proxy.setSource(Source.P66IP.name());

            return proxy;
        }).filter(p -> p != null).collect(Collectors.toList());
    }

    @Override
    public List<ProxyDTO> getMyLocalData() {
        return result;
    }
}
