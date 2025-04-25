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

//    public String preparePayment(String amount, String orderName) {
//        String accessToken = getAccessToken();
//
//        // Step 2: 결제 준비 요청에 필요한 데이터 생성
//        Map<String, Object> body = new HashMap<>();
//        body.put("amount", amount);  // 결제할 금액
//        body.put("name", orderName);  // 상품명 등 결제 정보
//
//        // Step 3: 결제 준비 API 호출
//        WebClient webClient = WebClient.create("https://api.iamport.kr");
//        return webClient.post()
//                .uri("/payments/prepare")
//                .header("Authorization", "Bearer " + accessToken)  // 발급받은 access token을 헤더에 추가
//                .contentType(MediaType.APPLICATION_JSON)
//                .bodyValue(body)
//                .retrieve()
//                .bodyToMono(PaymentPrepareResponse.class)  // 응답을 받을 클래스
//                .block();
//    }
//
//    // 결제 검증 (포트원 API 호출)
//    public PaymentVerificationResponse verifyPayment(String impUid) {
//        String accessToken = getAccessToken();  // access token 발급
//
//        Map<String, String> body = new HashMap<>();
//        body.put("imp_uid", impUid);
//
//        return webClient.post()
//                .uri("/payments/verify")
//                .header("Authorization", "Bearer " + accessToken)
//                .bodyValue(body)
//                .retrieve()
//                .bodyToMono(PaymentVerificationResponse.class)
//                .block();  // 응답을 기다리고 반환
//    }
//
//    // Access token 발급
//    private String getAccessToken() {
//        Map<String, String> body = new HashMap<>();
//        body.put("imp_key", apiKey);
//        body.put("imp_secret", apiSecret);
//
//        return webClient.post()
//                .uri("/users/getToken")
//                .contentType(MediaType.APPLICATION_JSON)
//                .bodyValue(body)
//                .retrieve()
//                .bodyToMono(PortoneTokenResponse.class)
//                .map(body -> body.response().access_token())
//                .block();
//    }
}
