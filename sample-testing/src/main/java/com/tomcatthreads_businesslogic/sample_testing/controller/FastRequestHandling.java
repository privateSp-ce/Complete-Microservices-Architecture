package com.tomcatthreads_businesslogic.sample_testing.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class FastRequestHandling {

    @GetMapping("/fast")
    public String fastEndpoint() {
        return "Fast response from Thread: " +
                Thread.currentThread().getName();
    }

}
