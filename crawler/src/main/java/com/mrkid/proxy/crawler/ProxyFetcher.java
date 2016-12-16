package com.mrkid.proxy.crawler;

import com.mrkid.proxy.dto.ProxyDTO;

import java.util.List;

/**
 * User: xudong
 * Date: 11/12/2016
 * Time: 3:56 PM
 */
public interface ProxyFetcher {
    List<ProxyDTO> crawl() throws Exception;
}
