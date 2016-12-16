package com.mrkid.proxy.model;

import com.mrkid.proxy.dto.ProxyType;
import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Table;
import java.util.Date;

/**
 * Archive low quality proxies
 * User: xudong
 * Date: 31/10/2016
 * Time: 6:16 PM
 */
@Data
@Entity
public class LowQualityProxy {
    @Id
    private String host;

    private String schema;
    private int port;

    private boolean valid;

    private int proxyType = ProxyType.UNKNOWN.getKey();

    private int recentFailTimes = 0;

    private String location;
    private Date lastCheckSuccess;
    private Date lastCheckFail;

    private String source = "";
}
