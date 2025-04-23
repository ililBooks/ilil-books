package com.example.ililbooks.domain.limitedevent.entity;

import com.example.ililbooks.domain.book.entity.Book;
import com.example.ililbooks.domain.limitedevent.dto.request.LimitedEventUpdateRequest;
import com.example.ililbooks.domain.limitedevent.enums.LimitedEventStatus;
import com.example.ililbooks.global.entity.TimeStamped;
import com.example.ililbooks.global.exception.BadRequestException;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

import static com.example.ililbooks.global.exception.ErrorMessage.OUT_OF_STOCK;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "limited_events")
public class LimitedEvent extends TimeStamped {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    private String title;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "VARCHAR(50)")
    private LimitedEventStatus status;

    @Column(nullable = false)
    private Instant startTime;

    @Column(nullable = false)
    private Instant endTime;

    private String contents;

    private int bookQuantity;

    private boolean isDeleted;

    /*
     * 정적 팩토리 메서드
     */
    public static LimitedEvent of(Book book, String title, Instant startTime, Instant endTime, String contents, int bookQuantity) {
        LimitedEvent event = new LimitedEvent();
        event.book = book;
        event.title = title;
        event.status = LimitedEventStatus.INACTIVE;
        event.startTime = startTime;
        event.endTime = endTime;
        event.contents = contents;
        event.bookQuantity = bookQuantity;
        event.isDeleted = false;
        return event;
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
    public void update(String title, Instant startTime, Instant endTime, String contents, int bookQuantity) {
        this.title = title;
        this.startTime = startTime;
        this.endTime = endTime;
        this.contents = contents;
        this.bookQuantity = bookQuantity;
    }

    /*
     * 행사 수정(행사 시작 후)
     */
    public void updateAfterStart(LimitedEventUpdateRequest request) {
        this.endTime = request.endTime();
        this.contents = request.contents();
        this.bookQuantity = request.bookQuantity();
    }

    /*
     * soft delete 처리 여부 확인
     */
    public boolean isDeleted() {
        return this.isDeleted;
    }

    /*
     * soft delete 처리
     */
    public void softDelete() {
        this.isDeleted = true;
    }

    /*
     * 예약 가능 여부 확인
     */
    public boolean canAcceptReservation(Long reservedCount) {
        return reservedCount < this.bookQuantity;
    }

    /*
     * 한정 수량 차감 메서드
     */
    public void decreaseBookQuantity(int quantity) {
        if (this.bookQuantity < quantity) {
            throw new BadRequestException(OUT_OF_STOCK.getMessage());
        }
        this.bookQuantity -= quantity;
    }

    public void updateStatus(LimitedEventStatus newStatus) {
        this.status = newStatus;
    }
}
