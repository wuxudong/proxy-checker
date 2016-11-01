package com.mrkid.proxy;

import org.apache.catalina.servlet4preview.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

/**
 * User: xudong
 * Date: 31/10/2016
 * Time: 3:01 PM
 */
@Controller
public class ProxyCheckerController {
    @RequestMapping(path = "/proxy-check")
    @ResponseBody
    public ProxyCheckResponse checkProxy(@RequestParam("originIp") String originIp, @RequestBody
            Proxy proxy, @RequestHeader(value = "X-FORWARDED-FOR", required = false) String xForwardedFor,
                                         HttpServletRequest request) {
        String remoteIp = request.getRemoteAddr();
        return new ProxyCheckResponse(originIp, remoteIp, xForwardedFor, proxy, true);
    }
}
