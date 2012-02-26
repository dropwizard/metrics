package com.yammer.metrics.spring;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 *
 */
@Component
public class AutowiredCollaborator {

    @Autowired
    private ProxyTargetClass dependency;
}
