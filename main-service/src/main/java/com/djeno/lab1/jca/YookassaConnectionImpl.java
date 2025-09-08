package com.djeno.lab1.jca;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.resource.ResourceException;
import jakarta.resource.cci.ConnectionMetaData;
import jakarta.resource.cci.Interaction;
import jakarta.resource.cci.LocalTransaction;
import jakarta.resource.cci.ResultSetInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.UUID;

@Slf4j
@Service
public class YookassaConnectionImpl implements YookassaConnection{
    @Value("${yookassa.shopId}")
    private String shopId;

    @Value("${yookassa.apiKey}")
    private String apiKey;


    @Override
    public String createPayment(Long amount, Long orderId) {
        try {
            var json = """
                {
                  "amount": {
                    "value": "%s",
                    "currency": "RUB"
                  },
                  "confirmation": {
                    "type": "redirect",
                    "return_url": "%s"
                  },
                  "metadata": {
                    "order_id":"%s"
                  },
                  "capture": true,
                  "description": "%s"
                }
                """.formatted(amount.toString(), "https://se.ifmo.ru/",orderId.toString(), "Test");
            log.info("Created Payment {}", json);
            log.info(json);

            String auth=encodeBasicAuth(shopId, apiKey);

            log.info("Encoded basic auth {}", auth);

            var request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.yookassa.ru/v3/payments"))
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json")
                    .header("Idempotence-Key", UUID.randomUUID().toString())
                    .header("Authorization", "Basic " + encodeBasicAuth(shopId, apiKey))
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            log.info("Request {}", request);

            var client = HttpClient.newHttpClient();
            var response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode()>=300){
                throw new RuntimeException("Error creating payment");
            }

            String body = response.body();

            ObjectMapper mapper = new ObjectMapper();
            JsonNode node = mapper.readTree(body);
            return node.path("confirmation").path("confirmation_url").asText();

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private String encodeBasicAuth(String shopId, String apiKey) {
        var credentials = shopId + ":" + apiKey;
        return java.util.Base64.getEncoder().encodeToString(credentials.getBytes());
    }

    @Override
    public Interaction createInteraction() throws ResourceException {
        return null;
    }

    @Override
    public LocalTransaction getLocalTransaction() throws ResourceException {
        return null;
    }

    @Override
    public ConnectionMetaData getMetaData() throws ResourceException {
        return null;
    }

    @Override
    public ResultSetInfo getResultSetInfo() throws ResourceException {
        return null;
    }

    @Override
    public void close() throws ResourceException {

    }
}