package com.example.distributed_kv_store.controller;

import com.example.distributed_kv_store.response.HealthResponse;
import com.example.distributed_kv_store.service.NodeMetricsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/nodeHealth")
public class HealthController {
    private final NodeMetricsService nodeMetricsService;

    public HealthController(NodeMetricsService nodeMetricsService) {
        this.nodeMetricsService = nodeMetricsService;
    }

    @GetMapping("/actuator/health")
    public ResponseEntity<HealthResponse> health() {
        // Service generates the data, Response just holds it
        HealthResponse response = nodeMetricsService.generateHealthResponse();
        return ResponseEntity.ok(response);
    }
}
