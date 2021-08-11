package net.devh.boot.grpc.client.config;

import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

/**
 * @Title 方法配置信息实体, 请参考如下链接:
 * https://github.com/grpc/proposal/blob/37e658b12f1684f29b3acca04f0167b84d502876/A6-client-retries.md#grpc-retry-design
 * https://github.com/grpc/grpc/blob/master/doc/service_config.md
 * @Description MethodConfig
 * @Program spring-cloud-tcl-starter
 * @Author wushengju
 * @Version 1.0
 * @Date 2021-08-10 13:38
 * @Copyright Copyright (c) 2021 TCL Inc. All rights reserved
 **/
@ToString
@EqualsAndHashCode
public class MethodConfig {
    /**
     * retry policy config
     */
    private RetryPolicyConfig retryPolicy;
    /**
     * name for list
     */
    private List<NameConfig> name;

    public RetryPolicyConfig getRetryPolicy() {
        return retryPolicy;
    }

    public void setRetryPolicy(RetryPolicyConfig retryPolicy) {
        this.retryPolicy = retryPolicy;
    }

    public List<NameConfig> getName() {
        return name;
    }

    public void setName(List<NameConfig> name) {
        this.name = name;
    }

    public void copyDefaultsFrom(final MethodConfig config) {
        if (this == config) {
            return;
        }
        this.retryPolicy.copyDefaultsFrom(config.retryPolicy);
        if (this.name == null || this.name.isEmpty()) {
            if (config.getName() != null && !config.getName().isEmpty()) {
                this.name = new ArrayList<>();
                config.getName().forEach(nameConfig -> {
                    NameConfig newConfig = new NameConfig();
                    newConfig.copyDefaultsFrom(nameConfig);
                    this.name.add(newConfig);
                });
            }
        }
    }
}
