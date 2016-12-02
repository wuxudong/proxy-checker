package com.mrkid.proxy.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Arrays;

/**
 * User: xudong
 * Date: 02/12/2016
 * Time: 8:45 AM
 */


public enum ProxyType {
    UNKNOWN(0),
    TRANSPARENT_PROXY(1),
    ANONYMOUS_PROXY(2),
    DISTORTING_PROXY(3),
    HIGH_ANONYMITY_PROXY(4);

    private int key;

    ProxyType(int key) {
        this.key = key;
    }


    @JsonValue
    public String getKey() {
        return String.valueOf(key);
    }

    @JsonCreator
    public static ProxyType create(String i) {
        return Arrays.stream(ProxyType.values()).filter(t -> t.key == Integer.valueOf(i)).findFirst().orElseGet(() ->
                UNKNOWN);
    }
}
