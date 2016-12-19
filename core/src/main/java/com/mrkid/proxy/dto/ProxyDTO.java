package com.mrkid.proxy.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.StringUtils;

import java.util.Date;

/**
 * User: xudong
 * Date: 31/10/2016
 * Time: 6:16 PM
 */
@Data
@EqualsAndHashCode(of = {"type", "host"})
public class ProxyDTO {
    private String type = "http";
    private String host;
    private int port;

    private String location;
    private Date lastCheckSuccess;
    private Date lastCheckFail;

    private String source = "";

    public void setLocation(String location) {
        this.location = StringUtils.isNotBlank(location) ? location.trim() : "";
    }
}
