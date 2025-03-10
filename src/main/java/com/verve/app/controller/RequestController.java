package com.verve.app.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@RestController
public class RequestController
{
    private static final Logger logger = LoggerFactory.getLogger(RequestController.class);
    private final Set<Integer> uniqueIds = Collections.newSetFromMap(new ConcurrentHashMap<>());
    private final RestTemplate restTemplate = new RestTemplate();

    @GetMapping("/api/verve/accept")
    public ResponseEntity<String> acceptRequest(@RequestParam int id, @RequestParam(required = false) String endpoint)
    {
        try
        {
            uniqueIds.add(id);
            if (endpoint != null)
            {
                sendHttpRequest(endpoint);
            }
            return ResponseEntity.ok("ok");
        }
        catch (Exception e)
        {
            logger.error("Error processing request: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("failed");
        }
    }

    private void sendHttpRequest(String endpoint)
    {
        String url = endpoint + "?count=" + uniqueIds.size();
        try
        {
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class); // Can be a post request with a request body.
            logger.info("HTTP GET request to {} responded with status: {}", url, response.getStatusCode());
        }
        catch (Exception e)
        {
            logger.error("Failed to send HTTP request to {}", endpoint, e);
        }
    }

    @Scheduled(fixedRate = 60000)
    public void logUniqueRequestCount()
    {
        int count = uniqueIds.size();
        logger.info("Unique requests in the last minute: {}", count); //Can be sent as Kafka message
        uniqueIds.clear();
    }
}
