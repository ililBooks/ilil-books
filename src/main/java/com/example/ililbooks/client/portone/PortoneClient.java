package com.example.ililbooks.client.portone;

import com.example.ililbooks.client.portone.dto.PortoneTokenResponse;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.HashMap;
import java.util.Map;

@Component
public class PortoneClient {

    private final WebClient webClient = WebClient.create("https://api.iamport.kr");
    @Value("${portone.api-key}")
    private String apiKey;

    @Value("${portone.api-secret}")
    private String apiSecret;

    /**
     * 아임포트 access_token 발급
     */
    public String getAccessToken() {
        Map<String, String> body = new HashMap<>();
        body.put("imp_key", apiKey);
        body.put("imp_secret", apiSecret);

        return webClient.post()
                .uri("/users/getToken")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .retrieve()
                .bodyToMono(PortoneTokenResponse.class)
                .map(response -> response.getResponse().getAccessToken())
                .block();
    }

//    /**
//     * 결제 정보 조회
//     */
//    public PaymentInfoResponse getPaymentByImpUid(String impUid) {
//        String token = getAccessToken();
//
//        return webClient.get()
//                .uri("/payments/{impUid}", impUid)
//                .header(HttpHeaders.AUTHORIZATION, token)
//                .retrieve()
//                .bodyToMono(PaymentInfoResponse.class)
//                .block();
//    }
}
