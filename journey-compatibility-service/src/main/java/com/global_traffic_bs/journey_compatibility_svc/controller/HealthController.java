package com.global_traffic_bs.journey_compatibility_svc.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {

    @GetMapping("/")
    public String home() {
        return "Journey Compatibility Service is running";
    }
}