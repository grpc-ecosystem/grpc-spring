package net.devh.boot.grpc.client.config;

import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.HashMap;
import java.util.Map;

/**
 * @Title method config
 * @Description MethodConfig
 * @Program spring-cloud-tcl-starter
 * @Author wushengju
 * @Version 1.0
 * @Date 2021-08-10 10:55
 * @Copyright Copyright (c) 2021 TCL Inc. All rights reserved
 **/
@ToString
@EqualsAndHashCode
public class NameConfig {
    /**
     * the service name which defined in xx.proto
     */
    private String service;
    /**
     * the method name which you will call
     */
    private String method;

    public Map<String, Object> buildMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("service", this.getService());
        map.put("method", this.getMethod());
        return map;
    }

    public void copyDefaultsFrom(final NameConfig config) {
        if (this == config) {
            return;
        }
        if (this.service == null) {
            this.service = config.service;
        }
        if (this.method == null) {
            this.method = config.method;
        }
    }
    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }
}
