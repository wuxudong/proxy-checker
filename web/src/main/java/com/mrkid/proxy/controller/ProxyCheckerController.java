package com.mrkid.proxy.controller;

import com.mrkid.proxy.service.ProxyCheckMaster;
import com.mrkid.proxy.dto.Proxy;
import com.mrkid.proxy.dto.ProxyCheckResponse;
import org.apache.catalina.servlet4preview.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

/**
 * User: xudong
 * Date: 31/10/2016
 * Time: 3:01 PM
 */
@Controller
public class ProxyCheckerController {

    @Autowired
    private ProxyCheckMaster scheduler;

    @RequestMapping(path = "/proxy-check")
    @ResponseBody
    public ProxyCheckResponse checkProxy(@RequestParam("originIp") String originIp, @RequestBody
            Proxy proxy, @RequestHeader(value = "X-FORWARDED-FOR", required = false) String xForwardedFor,
                                         HttpServletRequest request) {
        String remoteIp = request.getRemoteAddr();
        return new ProxyCheckResponse(originIp, remoteIp, xForwardedFor, proxy, true).calculateProxyType();
    }

    @RequestMapping(path = "/heartbeat")
    public void heartbeat(@RequestBody Proxy proxy, @RequestHeader(value = "X-FORWARDED-FOR", required = false)
            String xForwardedFor,
                          HttpServletRequest request) {
        String remoteIp = request.getRemoteAddr();

        scheduler.heartbeat(proxy);
        return;
    }

}
