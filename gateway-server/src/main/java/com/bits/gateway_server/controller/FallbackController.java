package com.bits.gateway_server.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/fallback")
public class FallbackController {

    @GetMapping("/delivery")
    public ResponseEntity<Map<String, Object>> deliveryFallback() {
        Map<String, Object> res = new HashMap<>();
        res.put("message", "⚠️ Delivery service temporarily unavailable. Please try again later.");
        return ResponseEntity.ok(res);
    }
}
