package com.mrkid.proxy.model;

import com.mrkid.proxy.dto.AnonymityType;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;
import java.util.Date;

/**
 * User: xudong
 * Date: 31/10/2016
 * Time: 6:16 PM
 */
@Data
@Entity
public class Proxy {

    @EmbeddedId
    private ProxyKey key;

    private boolean valid;

    private int anonymityType = AnonymityType.UNKNOWN.getKey();

    private int recentFailTimes = 0;

    private String location;
    private Date lastCheckSuccess;
    private Date lastCheckFail;

    private String source = "";
}
