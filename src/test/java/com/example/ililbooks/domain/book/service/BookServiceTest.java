package com.example.ililbooks.domain.book.service;

import com.example.ililbooks.domain.book.dto.request.BookCreateRequest;
import com.example.ililbooks.domain.book.dto.request.BookUpdateRequest;
import com.example.ililbooks.domain.book.dto.response.BookResponse;
import com.example.ililbooks.domain.book.entity.Book;
import com.example.ililbooks.domain.book.entity.BookImage;
import com.example.ililbooks.domain.book.repository.BookRepository;
import com.example.ililbooks.domain.book.repository.ImageBookRepository;
import com.example.ililbooks.domain.review.service.ReviewDeleteService;
import com.example.ililbooks.domain.search.service.BookSearchService;
import com.example.ililbooks.domain.user.service.UserService;
import com.example.ililbooks.global.dto.AuthUser;
import com.example.ililbooks.global.exception.BadRequestException;
import com.example.ililbooks.global.exception.ForbiddenException;
import com.example.ililbooks.global.exception.NotFoundException;
import com.example.ililbooks.global.image.dto.request.ImageRequest;
import com.example.ililbooks.global.image.service.S3ImageService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.Optional;

import static com.example.ililbooks.domain.book.service.BookReadServiceTest.*;
import static com.example.ililbooks.global.exception.ErrorMessage.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class BookServiceTest {
    @Mock
    private BookRepository bookRepository;

    @Mock
    private UserService userService;

    @Mock
    private ReviewDeleteService reviewDeleteService;

    @Mock
    private BookSearchService bookSearchService;

    @Mock
    private ImageBookRepository imageBookRepository;

    @Mock
    private S3ImageService s3ImageService;

    @InjectMocks
    private BookService bookService;

    //request
    public static final BookCreateRequest TEST_BOOK_CREATE_REQUEST = new BookCreateRequest(
            "자바 ORM 표준 JPA 프로그래밍",
            "김영한",
            BigDecimal.valueOf(36000),
            "프로그래밍",
            50,
            "isbn",
            "문예 한국사"
    );

    public static final BookUpdateRequest BOOK_UPDATE_REQUEST = new BookUpdateRequest(
            "자바 ORM 표준 JPA 프로그래밍",
            "김영한",
            BigDecimal.valueOf(36000),
            "프로그래밍",
            50,
            "SOLD_OUT",
            "REGULAR"
    );

    public static final ImageRequest IMAGE_REQUEST = new ImageRequest(
            "imageUrl.png",
            "imageUrl",
            "png",
            5
    );

    //users
    public static final Long TEST_ADMIN_USER_ID1 = 1L;
    public static final Long TEST_ADMIN_USER_ID2 = 2L;
    public static final AuthUser TEST_ADMIN_AUTH_USER = AuthUser.builder()
            .userId(TEST_ADMIN_USER_ID1)
            .email(TEST_EMAIL_ADMIN_USERS.getEmail())
            .nickname(TEST_EMAIL_ADMIN_USERS.getNickname())
            .role(TEST_EMAIL_ADMIN_USERS.getUserRole())
            .build();

    //book
    public static final Long TEST_BOOK_IMAGE_ID = 1L;
    public static final BookImage TEST_BOOK_IMAGE = BookImage.of(
            TEST_BOOK,
            "imageUrl.png",
            "imageUrl",
            "png",
            5
    );

    @Test
    void 이미_등록된_책이_존재하여_등록_실패() {
        //given
        given(bookRepository.existsByIsbn(anyString())).willReturn(true);

        //when & then
        assertThrows(BadRequestException.class,
                () -> bookService.createBook(TEST_ADMIN_AUTH_USER, TEST_BOOK_CREATE_REQUEST),
                DUPLICATE_BOOK.getMessage()
                );
    }

    @Test
    void 책_단건_등록_성공() {
        //given
        ReflectionTestUtils.setField(TEST_EMAIL_ADMIN_USERS, "id", TEST_ADMIN_USER_ID1);
        ReflectionTestUtils.setField(TEST_BOOK, "id", TEST_BOOK_ID);

        given(bookRepository.existsByIsbn(anyString())).willReturn(false);
        given(userService.findByIdOrElseThrow(anyLong())).willReturn(TEST_EMAIL_ADMIN_USERS);
        given(bookRepository.save(any(Book.class))).willReturn(TEST_BOOK);

        BookResponse bookResponse = BookResponse.of(TEST_BOOK);

        //when
        BookResponse result = bookService.createBook(TEST_ADMIN_AUTH_USER, TEST_BOOK_CREATE_REQUEST);

        //then
        assertEquals(bookResponse, result);
        verify(bookRepository).save(any(Book.class));
        verify(bookSearchService).saveBookDocumentFromBook(any(Book.class));
    }

    @Test
    void 책이_존재하지_않아_책_이미지_업로드_실패() {
        //given
        given(bookRepository.findBookById(anyLong())).willThrow(new NotFoundException());

        //when & then
        assertThrows(NotFoundException.class,
                () -> bookService.uploadBookImage(TEST_ADMIN_AUTH_USER, TEST_BOOK_ID, IMAGE_REQUEST),
                NOT_FOUND_BOOK.getMessage()
        );
    }

    @Test
    void 자신이_등록한_책이_아니라서_이미지_업로드_실패 () {
        //given
        setInvalidBookAndUserId();

        given(bookRepository.findBookById(anyLong())).willReturn(Optional.of(TEST_BOOK));

        //when & then
        assertThrows(ForbiddenException.class,
                () -> bookService.uploadBookImage(TEST_ADMIN_AUTH_USER, TEST_BOOK_ID, IMAGE_REQUEST),
                CANNOT_UPLOAD_OTHERS_BOOK_IMAGE.getMessage()
        );
    }

    @Test
    void 입력된_위치에_이미_이미지가_존재하여_이미지_업로드_실패 () {
        //given
        setBookAndUserId();

        given(bookRepository.findBookById(anyLong())).willReturn(Optional.of(TEST_BOOK));
        given(imageBookRepository.existsByBookIdAndPositionIndex(anyLong(), anyInt())).willReturn(true);

        //when & then
        assertThrows(BadRequestException.class,
                () -> bookService.uploadBookImage(TEST_ADMIN_AUTH_USER, TEST_BOOK_ID, IMAGE_REQUEST),
                DUPLICATE_POSITION_INDEX.getMessage()
        );
    }

    @Test
    void 등록된_이미지의_개수_5개_초과로_이미지_업로드_실패() {
        //given
        setBookAndUserId();

        given(bookRepository.findBookById(anyLong())).willReturn(Optional.of(TEST_BOOK));
        given(imageBookRepository.countByBookId(anyLong())).willReturn(5);

        //when & then
        assertThrows(BadRequestException.class,
                () -> bookService.uploadBookImage(TEST_ADMIN_AUTH_USER, TEST_BOOK_ID, IMAGE_REQUEST),
                IMAGE_UPLOAD_LIMIT_OVER.getMessage()
        );
    }

    @Test
    void 책_이미지_등록_성공 () {
        //given
        setBookAndUserId();

        given(bookRepository.findBookById(anyLong())).willReturn(Optional.of(TEST_BOOK));

        //when
        bookService.uploadBookImage(TEST_ADMIN_AUTH_USER, TEST_BOOK_ID, IMAGE_REQUEST);

        //then
        verify(imageBookRepository).save(any(BookImage.class));
    }

    @Test
    void 이미지가_존재하지_않아_이미지_삭제_실패() {
        //given
        given(imageBookRepository.findImageById(anyLong())).willThrow(new NotFoundException());

        //when & then
        assertThrows(NotFoundException.class,
                () -> bookService.deleteBookImage(TEST_ADMIN_AUTH_USER, TEST_BOOK_IMAGE_ID),
                NOT_FOUND_IMAGE.getMessage()
        );
    }

    @Test
    void 자신이_등록한_책이_아니어서_이미지_삭제_실패() {
        //given
        setInvalidBookAndUserId();

        given(imageBookRepository.findImageById(anyLong())).willReturn(Optional.of(TEST_BOOK_IMAGE));

        //when & then
        assertThrows(ForbiddenException.class,
                () -> bookService.deleteBookImage(TEST_ADMIN_AUTH_USER, TEST_BOOK_IMAGE_ID),
                CANNOT_DELETE_OTHERS_IMAGE.getMessage()
        );
    }

    @Test
    void 책_이미지_삭제_성공 () {
        //given
        setBookAndUserId();

        given(imageBookRepository.findImageById(anyLong())).willReturn(Optional.of(TEST_BOOK_IMAGE));

        //when
        bookService.deleteBookImage(TEST_ADMIN_AUTH_USER, TEST_BOOK_IMAGE_ID);

        //then
        verify(s3ImageService).deleteImage(anyString());
        verify(imageBookRepository).delete(any(BookImage.class));
    }

    @Test
    void 책이_존재하지_않아_수정_실패() {
        //given
        given(bookRepository.findBookById(anyLong())).willThrow(new NotFoundException());

        //when & then
        assertThrows(NotFoundException.class,
                () -> bookService.updateBook(TEST_ADMIN_AUTH_USER, TEST_BOOK_ID, BOOK_UPDATE_REQUEST),
                NOT_FOUND_BOOK.getMessage()
        );
    }

    @Test
    void 자신이_등록한_책이_아니어서_수정_실패() {
        //given
        setInvalidBookAndUserId();

        given(bookRepository.findBookById(anyLong())).willReturn(Optional.of(TEST_BOOK));

        //when & then
        assertThrows(ForbiddenException.class,
                () -> bookService.updateBook(TEST_ADMIN_AUTH_USER, TEST_BOOK_ID, BOOK_UPDATE_REQUEST),
                CANNOT_UPDATE_OTHERS_BOOK.getMessage()
        );
    }

    @Test
    void 책_수정_성공() {
        //given
        setBookAndUserId();

        given(bookRepository.findBookById(anyLong())).willReturn(Optional.of(TEST_BOOK));

        //when
        bookService.updateBook(TEST_ADMIN_AUTH_USER, TEST_BOOK_ID, BOOK_UPDATE_REQUEST);

        //then
        verify(bookSearchService).updateBookDocument(any(Book.class));
    }

    @Test
    void 책이_존재하지_않아_삭제_실패() {
        //given
        given(bookRepository.findBookById(anyLong())).willThrow(new NotFoundException());


        //when & then
        assertThrows(NotFoundException.class,
                () -> bookService.deleteBook(TEST_ADMIN_AUTH_USER, TEST_BOOK_ID),
                NOT_FOUND_BOOK.getMessage()
        );
    }

    @Test
    void 자신이_등록한_책이_아니어서_삭제_실패() {
        //given
        setInvalidBookAndUserId();

        given(bookRepository.findBookById(anyLong())).willReturn(Optional.of(TEST_BOOK));

        //when & then
        assertThrows(ForbiddenException.class,
                () -> bookService.deleteBook(TEST_ADMIN_AUTH_USER, TEST_BOOK_ID),
                CANNOT_UPDATE_OTHERS_BOOK.getMessage()
        );
    }

    @Test
    void 이미지_삭제_성공 () {
        //given
        setBookAndUserId();

        given(bookRepository.findBookById(anyLong())).willReturn(Optional.of(TEST_BOOK));
        given(imageBookRepository.findAllByBookId(TEST_BOOK_ID)).willReturn(TEST_LIST_BOOK_IMAGE);

        //when
        bookService.deleteBook(TEST_ADMIN_AUTH_USER, TEST_BOOK_ID);

        //then
        verify(s3ImageService, times(TEST_LIST_BOOK_IMAGE.size())).deleteImage(anyString());
        verify(imageBookRepository).deleteAllByBookId(anyLong());
        verify(reviewDeleteService).deleteAllReviewByBookId(anyLong());
        verify(bookSearchService).deleteBookDocument(any(Book.class));
    }

    private void setBookAndUserId() {
        ReflectionTestUtils.setField(TEST_BOOK, "id", TEST_BOOK_ID);
        ReflectionTestUtils.setField(TEST_BOOK_IMAGE, "id", TEST_BOOK_IMAGE_ID);
        ReflectionTestUtils.setField(TEST_ADMIN_AUTH_USER, "userId", TEST_ADMIN_USER_ID1);
        ReflectionTestUtils.setField(TEST_EMAIL_ADMIN_USERS, "id", TEST_ADMIN_USER_ID1);
    }

    private void setInvalidBookAndUserId() {
        ReflectionTestUtils.setField(TEST_ADMIN_AUTH_USER, "userId", TEST_ADMIN_USER_ID1);
        ReflectionTestUtils.setField(TEST_BOOK, "users", TEST_EMAIL_ADMIN_USERS);
        ReflectionTestUtils.setField(TEST_EMAIL_ADMIN_USERS, "id", TEST_ADMIN_USER_ID2);
    }
}