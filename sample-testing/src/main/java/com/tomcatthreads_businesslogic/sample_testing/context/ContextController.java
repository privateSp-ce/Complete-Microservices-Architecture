package com.tomcatthreads_businesslogic.sample_testing.context;

import jakarta.servlet.ServletContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ContextController {

    @Autowired
    private ApplicationContext applicationContext; // Spring context

    @Autowired
    private ServletContext servletContext; // Tomcat context [web:217]

    @GetMapping("/contexts")
    public String getContexts() {
        // Access Spring beans
        String[] beans = applicationContext.getBeanDefinitionNames();

        // Access ServletContext attributes [web:217]
        String contextPath = servletContext.getServerInfo();

        return "Spring Beans: " + beans.length +
                ", Context Path: " + contextPath;
    }

}
