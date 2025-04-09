package com.example.ililbooks.domain.limitedevent.entity;

import com.example.ililbooks.domain.book.entity.Book;
import com.example.ililbooks.domain.limitedevent.dto.request.LimitedEventUpdateRequest;
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
//TODO @SQL 로 delete 때릴때마다 delete관련 컬럼 update되게 설정
public class LimitedEvent extends TimeStamped {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    private String title;

    @Enumerated(EnumType.STRING)
    //TODO Column어노테이션 안에 있음 컬럼데피니션 찾아보고 varchar(50)으로 설정
    private LimitedEventStatus status;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private String contents;

    private int bookQuantity;

    //TODO nullable 표시 명확히 할 것
    //null 은 안티패턴이니, Boolean으로 관리 isDeleted = true||false
    private LocalDateTime deletedAt;
    //TODO LocalDateTime -> Instant 차이점 많음

    /*
     * LimitedEvent 생성자
     */
    @Builder
    public LimitedEvent(Book book, String title, LocalDateTime startTime, LocalDateTime endTime, String contents, int bookQuantity) {
        this.book = book;
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

    /*
     * 행사 수정(행사 시작 후)
     */
    public void updateAfterStart(LimitedEventUpdateRequest request) {
        this.endTime = request.getEndTime();
        this.contents = request.getContents();
        this.bookQuantity = request.getBookQuantity();
    }

    /*
     * soft delete 처리 여부 확인
     */
    public boolean isDeleted() {
        return this.deletedAt != null;
    }

    /*
     * soft delete 처리
     */
    public void softDelete() {
        this.deletedAt = LocalDateTime.now();
    }

    public boolean canAcceptReservation(Long reservedCount) {
        return reservedCount < this.bookQuantity;
    }
}
