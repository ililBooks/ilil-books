package com.example.ililbooks.global.notification.service;

import com.example.ililbooks.domain.bestseller.dto.response.BestSellerChartResponse;
import com.example.ililbooks.domain.bestseller.enums.PeriodType;
import com.example.ililbooks.domain.bestseller.service.BestSellerService;
import com.example.ililbooks.global.dto.AuthUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static com.example.ililbooks.global.exception.ErrorMessage.FAILED_SEND_MAIL;

@Slf4j
@Service
@RequiredArgsConstructor
public class AsyncNotificationService {
    private final JavaMailSender javaMailSender;
    private final BestSellerService bestSellerService;

    @Async("emailTaskExecutor")
    public void sendOrderMail(AuthUser authUser, String orderNumber, BigDecimal totalPrice) {
        //텍스트로 메일 보내기
        SimpleMailMessage simpleMailMessage = new SimpleMailMessage();

        try {
            //메일을 받을 수신자 설정
            simpleMailMessage.setTo(authUser.getEmail());

            //메일 제목
            simpleMailMessage.setSubject("주문이 완료되었습니다.");

            //메일 내용
            simpleMailMessage.setText(
                    "주문 정보\n" +
                    "-------------------------\n" +
                    "닉네임: " + authUser.getNickname() + "\n" +
                    "주문 번호: " + orderNumber + "\n" +
                    "총 가격: " + totalPrice + "\n"
            );

            //메일 전송
            javaMailSender.send(simpleMailMessage);

        } catch (Exception e) {
            //메일 전송이 실패하더라도 주문은 성공
            log.error(FAILED_SEND_MAIL.getMessage(), e);
        }
    }

    @Async("emailTaskExecutor")
    public void sendPromotionEmail(String email, String nickname) {
        List<BestSellerChartResponse> bestSellerChart = bestSellerService.getBestSellerChart(PeriodType.MONTHLY, LocalDate.now().toString());
        //텍스트로 메일 보내기
        SimpleMailMessage simpleMailMessage = new SimpleMailMessage();

        try {
            // 메일을 받을 수신자 설정
            simpleMailMessage.setTo(email);

            // 메일 제목
            simpleMailMessage.setSubject(nickname + "님, 이달의 베스트셀러를 만나보세요!");

            // 메일 내용
            StringBuilder emailContent = new StringBuilder();
            emailContent.append(nickname).append("님, 안녕하세요!\n\n");
            emailContent.append("이번 달 인기 도서를 소개합니다. 지금 확인해보세요!\n\n");
            emailContent.append("=== 이달의 베스트셀러 ===\n");

            // 베스트셀러 목록
            for (BestSellerChartResponse book : bestSellerChart) {
                emailContent.append("#").append(book.rank()).append(": ")
                        .append(book.title()).append("\n")
                        .append("  저자: ").append(book.author()).append("\n")
                        .append("  카테고리: ").append(book.category()).append("\n")
                        .append("  출판사: ").append(book.publisher()).append("\n\n");
            }

            // 구매 유도 문구
            emailContent.append("=== 특별 할인 ===\n");
            emailContent.append("지금 구매 시 10% 할인 코드 [BEST10]을 사용하세요!\n");
            emailContent.append("쇼핑하러 가기: https://ililbooks.click\n\n");
            emailContent.append("즐거운 독서 되세요,\n");
            emailContent.append("FROM ililbooks\n");

            simpleMailMessage.setText(emailContent.toString());


            javaMailSender.send(simpleMailMessage);
        } catch (Exception e) {
            log.error(FAILED_SEND_MAIL.getMessage(), e);
        }
    }
}
