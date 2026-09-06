package com.backend.jobfetch;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Slf4j
@Component
public class ApiClient {
    
    private final RestTemplate restTemplate;
    
    public ApiClient() {
        this.restTemplate = new RestTemplate();  // ✅ Create RestTemplate directly
    }
    
    public String get(String url, Map<String, String> headers) {
        try {
            HttpHeaders httpHeaders = new HttpHeaders();
            if (headers != null) {
                headers.forEach(httpHeaders::set);
            }
            HttpEntity<?> entity = new HttpEntity<>(httpHeaders);
            
            ResponseEntity<String> response = restTemplate.exchange(
                url, HttpMethod.GET, entity, String.class
            );
            
            if (response.getStatusCode().is2xxSuccessful()) {
                return response.getBody();
            } else {
                log.warn("API returned non-2xx status: {} for URL: {}", response.getStatusCode(), url);
                return null;
            }
        } catch (HttpClientErrorException e) {
            log.error("HTTP Client Error: {} - {}", e.getStatusCode(), e.getMessage());
            return null;
        } catch (ResourceAccessException e) {
            log.error("Network Error: {}", e.getMessage());
            return null;
        } catch (Exception e) {
            log.error("Error calling API: {}", e.getMessage());
            return null;
        }
    }
    
    public String get(String url) {
        return get(url, null);
    }
}