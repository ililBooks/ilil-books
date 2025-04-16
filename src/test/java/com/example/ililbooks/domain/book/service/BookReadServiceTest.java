package com.example.ililbooks.domain.book.service;

import com.example.ililbooks.domain.book.dto.response.BookListResponse;
import com.example.ililbooks.domain.book.dto.response.BookWithImagesResponse;
import com.example.ililbooks.domain.book.entity.Book;
import com.example.ililbooks.domain.book.entity.BookImage;
import com.example.ililbooks.domain.book.repository.BookRepository;
import com.example.ililbooks.domain.book.repository.ImageBookRepository;
import com.example.ililbooks.domain.review.dto.response.ReviewWithImagesResponse;
import com.example.ililbooks.domain.review.entity.Review;
import com.example.ililbooks.domain.review.service.ReviewFindService;
import com.example.ililbooks.domain.user.entity.Users;
import com.example.ililbooks.global.exception.NotFoundException;
import com.example.ililbooks.global.image.dto.response.ImageListResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import static com.example.ililbooks.domain.user.enums.LoginType.EMAIL;
import static com.example.ililbooks.global.exception.ErrorMessage.NOT_FOUND_BOOK;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.util.AssertionErrors.assertNull;

@ExtendWith(MockitoExtension.class)
public class BookReadServiceTest {
    @Mock
    private BookRepository bookRepository;

    @Mock
    private ImageBookRepository imageBookRepository;

    @Mock
    private ReviewFindService reviewFindService;

    @Mock
    private BookService bookService;

    @InjectMocks
    private BookReadService bookReadService;

    //request
    public static final Pageable TEST_PAGEALBE = PageRequest.of(0, 10);

    //users
    public static final Users TEST_EMAIL_USERS = Users.of(
            "example@mail.com",
            "닉네임",
            "Password1234",
            "ROLE_USER",
            EMAIL
    );

    public static final Users TEST_EMAIL_ADMIN_USERS = Users.of(
            "example@mail.com",
            "닉네임",
            "Password1234",
            "ROLE_ADMIN",
            EMAIL
    );

    //book
    public static final Long TEST_BOOK_ID = 1L;
    public static final Book TEST_BOOK = Book.builder()
            .users(TEST_EMAIL_ADMIN_USERS)
            .title("예제 도서 제목")
            .author("홍길동")
            .price(BigDecimal.valueOf(15000))
            .category("프로그래밍")
            .stock(100)
            .isbn("9788912345670")
            .publisher("예제출판사")
            .build();

    public static final List<BookImage> TEST_LIST_BOOK_IMAGE = List.of(
            BookImage.builder()
                    .imageUrl("imageUrl1.png")
                    .fileName("imageUrl1")
                    .extension("png")
                    .build(),
            BookImage.builder()
                    .imageUrl("imageUrl2.png")
                    .fileName("imageUrl2")
                    .extension("png")
                    .build()
    );

    public static final Page<Book> TEST_PAGE_BOOK = new PageImpl<>(
            List.of(TEST_BOOK),
            TEST_PAGEALBE,
            1
    );

    //review
    public static final Long TEST_REVIEW_ID = 1L;
    public static final Review TEST_REVIEW = Review.builder()
            .id(TEST_REVIEW_ID)
            .users(TEST_EMAIL_USERS)
            .book(TEST_BOOK)
            .rating(5)
            .comments("리뷰")
            .build();

    //response
    public static final List<ImageListResponse> TEST_BOOK_IMAGE_LIST_RESPONSE = List.of(
            new ImageListResponse("imageUrl1.png"),
            new ImageListResponse("imageUrl2.png")
    );

    public static final List<ImageListResponse> TEST_REVIEW_IMAGE_LIST_RESPONSE = List.of(
            new ImageListResponse("imageUrl1"),
            new ImageListResponse("imageUrl2")
    );

    public static final ReviewWithImagesResponse REVIEW_WITH_IMAGES_RESPONSE = ReviewWithImagesResponse.of(
            TEST_REVIEW,
            TEST_REVIEW_IMAGE_LIST_RESPONSE
    );

    public static final Page<ReviewWithImagesResponse> PAGE_REVIEW_WITH_IMAGES_RESPONSE = new PageImpl<>(
            List.of(REVIEW_WITH_IMAGES_RESPONSE),
            TEST_PAGEALBE,
            List.of(REVIEW_WITH_IMAGES_RESPONSE).size()
    );

    public static final BookWithImagesResponse BOOK_WITH_IMAGES_RESPONSE = BookWithImagesResponse.of(
            TEST_BOOK,
            PAGE_REVIEW_WITH_IMAGES_RESPONSE,
            TEST_BOOK_IMAGE_LIST_RESPONSE
    );

    @Test
    void 책이_존재하지_않아_단건_조회_실패() {
        //given
        given(bookService.findBookByIdOrElseThrow(anyLong())).willThrow(new NotFoundException());

        //when & then
        assertThrows(NotFoundException.class,
                () -> bookReadService.findBookResponse(TEST_BOOK_ID, TEST_PAGEALBE),
                NOT_FOUND_BOOK.getMessage()
        );
    }

    @Test
    void 책_단건_조회_성공() {
        //given
        ReflectionTestUtils.setField(TEST_BOOK, "id", TEST_BOOK_ID);

        given(bookService.findBookByIdOrElseThrow(anyLong())).willReturn(TEST_BOOK);
        given(reviewFindService.getReviews(anyLong(), any(Pageable.class))).willReturn(PAGE_REVIEW_WITH_IMAGES_RESPONSE);
        given(bookService.getAllByBookId(TEST_BOOK)).willReturn(TEST_LIST_BOOK_IMAGE);

        //when
        BookWithImagesResponse result = bookReadService.findBookResponse(TEST_BOOK_ID, TEST_PAGEALBE);

        //then
        assertEquals(BOOK_WITH_IMAGES_RESPONSE.userId(), result.userId());
        assertEquals(BOOK_WITH_IMAGES_RESPONSE.title(), result.title());
        assertEquals(BOOK_WITH_IMAGES_RESPONSE.author(), result.author());
        assertEquals(BOOK_WITH_IMAGES_RESPONSE.reviews(), result.reviews());
        assertEquals(BOOK_WITH_IMAGES_RESPONSE.imageUrl(), result.imageUrl());
    }

    @Test
    void 책_다건_조회_성공 () {
        //given
        ReflectionTestUtils.setField(TEST_BOOK, "id", TEST_BOOK_ID);

        given(bookRepository.findAllNotDeleted(any(Pageable.class))).willReturn(TEST_PAGE_BOOK);
        given(imageBookRepository.findAllByBookId(anyLong())).willReturn(TEST_LIST_BOOK_IMAGE);

        //when
        Page<BookListResponse> result = bookReadService.getBooks(TEST_PAGEALBE);

        //then
        assertEquals(TEST_PAGE_BOOK.getTotalElements(), result.getTotalElements());
    }

    @Test
    void 책_이미지가_존재하지_않아_이미지를_제외한_다건_조회_성공 () {
        //given
        ReflectionTestUtils.setField(TEST_BOOK, "id", TEST_BOOK_ID);

        given(bookRepository.findAllNotDeleted(any(Pageable.class))).willReturn(TEST_PAGE_BOOK);
        given(imageBookRepository.findAllByBookId(anyLong())).willReturn(Collections.emptyList());

        //when
        Page<BookListResponse> result = bookReadService.getBooks(TEST_PAGEALBE);

        //then
        BookListResponse response = result.getContent().get(0);

        assertEquals(1, result.getTotalElements());
        assertNull(response.imageUrl(), null);
    }
}