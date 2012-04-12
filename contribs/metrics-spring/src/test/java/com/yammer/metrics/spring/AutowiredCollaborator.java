package com.yammer.metrics.spring;

import org.springframework.beans.factory.annotation.Autowired;

public class AutowiredCollaborator {

    @Autowired
    private ProxyTargetClass dependency;

    public ProxyTargetClass getDependency() {
        return dependency;
    }

}