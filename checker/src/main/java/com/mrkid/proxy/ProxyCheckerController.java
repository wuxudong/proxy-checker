package com.mrkid.proxy;

import org.apache.catalina.servlet4preview.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * User: xudong
 * Date: 31/10/2016
 * Time: 3:01 PM
 */
@Controller
public class ProxyCheckerController {
    @RequestMapping(path = "/proxy-check")
    @ResponseBody
    public ProxyCheckerResponse checkProxy(@RequestParam("originIp") String originIp, @RequestParam("proxyIp")
            String proxyIp,  @RequestHeader
            (value = "X-FORWARDED-FOR", required = false) String xForwardedFor, HttpServletRequest request) {
        String remoteIp = request.getRemoteAddr();
        return new ProxyCheckerResponse(originIp, proxyIp, remoteIp,xForwardedFor);
    }
}
