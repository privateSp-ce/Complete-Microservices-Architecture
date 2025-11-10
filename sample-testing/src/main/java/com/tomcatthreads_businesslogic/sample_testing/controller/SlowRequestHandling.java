package com.tomcatthreads_businesslogic.sample_testing.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SlowRequestHandling {

    @GetMapping("/slow")
    public String slowEndpoint() throws InterruptedException {
        System.out.println("Request received by Thread: " +
                Thread.currentThread().getName());

        // 30 seconds sleep - thread block chesthundi
        Thread.sleep(30000);

        return "Response after 30 seconds!";
    }

}
