package com.example.ililbooks.domain.limitedevent.entity;

import com.example.ililbooks.domain.limitedevent.enums.LimitedEventStatus;
import com.example.ililbooks.global.entity.TimeStamped;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "limited_events")
public class LimitedEvent extends TimeStamped {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long limitedEventId;

    private Long bookId;

    private String title;

    @Enumerated(EnumType.STRING)
    private LimitedEventStatus status;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private String contents;

    private int bookQuantity;

    /*
     * LimitedEvent 생성자
     */
    @Builder
    public LimitedEvent(Long bookId, String title, LocalDateTime startTime, LocalDateTime endTime, String contents, int bookQuantity) {
        this.bookId = bookId;
        this.title = title;
        this.status = LimitedEventStatus.INACTIVE;
        this.startTime = startTime;
        this.endTime = endTime;
        this.contents = contents;
        this.bookQuantity = bookQuantity;
    }

    /*
     * 행사 상태 활성화
     */
    public void activate() {
        this.status = LimitedEventStatus.ACTIVE;
    }

    /*
     * 행사 수정
     */
    public void update(String title, LocalDateTime startTime, LocalDateTime endTime, String contents, int bookQuantity) {
        this.title = title;
        this.startTime = startTime;
        this.endTime = endTime;
        this.contents = contents;
        this.bookQuantity = bookQuantity;
    }
}
