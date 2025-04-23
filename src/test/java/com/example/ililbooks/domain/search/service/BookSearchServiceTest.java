package com.example.ililbooks.domain.search.service;

import com.example.ililbooks.domain.book.entity.Book;
import com.example.ililbooks.domain.book.repository.BookRepository;
import com.example.ililbooks.domain.search.dto.BookSearchResponse;
import com.example.ililbooks.domain.search.entity.BookDocument;
import com.example.ililbooks.domain.search.repository.BookSearchRepository;
import com.example.ililbooks.domain.user.entity.Users;
import com.example.ililbooks.domain.user.enums.LoginType;
import com.example.ililbooks.domain.user.enums.UserRole;
import com.example.ililbooks.global.exception.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static com.example.ililbooks.domain.book.enums.LimitedType.REGULAR;
import static com.example.ililbooks.domain.book.enums.SaleStatus.ON_SALE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookSearchServiceTest {

    @Mock
    private BookSearchRepository bookSearchRepository;

    @Mock
    private BookRepository bookRepository;

    @Mock
    private TrendingKeywordService trendingKeywordService;

    @InjectMocks
    private BookSearchService bookSearchService;

    Users user;
    Book book;

    @BeforeEach
    void setUp() {
        user = Users.builder()
                .id(1L)
                .email("admin@email.com")
                .password("password1234")
                .nickname("admin")
                .contactNumber("010-1234-1234")
                .loginType(LoginType.EMAIL)
                .userRole(UserRole.ROLE_ADMIN)
                .roadAddress("도로명주소")
                .detailedAddress("상세주소")
                .zipCode("12345")
                .build();

        book = Book.builder()
                .id(1L)
                .title("자바 스프링")
                .author("김영한")
                .publisher("길벗")
                .users(user)
                .price(BigDecimal.valueOf(10000))
                .stock(10)
                .saleStatus(ON_SALE)
                .limitedType(REGULAR)
                .isbn("9999999")
                .category("IT")
                .build();

    }

    @Test
    void BookDocument_단건_저장_성공() {
        bookSearchService.saveBookDocumentFromBook(book);

        ArgumentCaptor<BookDocument> captor = ArgumentCaptor.forClass(BookDocument.class);

        verify(bookSearchRepository).save(captor.capture());
    }

    @Test
    void BookDocument_단건_저장_실패_nullBook() {
        assertThatThrownBy(() -> bookSearchService.saveBookDocumentFromBook(null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void BookDocument_다건_저장_성공() {
        BookDocument documentA = BookDocument.toDocument(book);

        BookDocument documentB = BookDocument.toDocument(book);

        List<BookDocument> bookDocuments = List.of(documentA, documentB);

        bookSearchService.saveAll(bookDocuments);
    }

    @Test
    void BookDocument_다건_저장_실패_null() {
        assertThrows(NullPointerException.class, () -> bookSearchService.saveAll(null));
    }

    @Test
    void searchBooksV2_검색_성공() {
        Pageable pageable = PageRequest.of(1, 10);
        String keyword = "검색";
        BookDocument documentA = BookDocument.toDocument(book);
        BookDocument documentB = BookDocument.toDocument(book);

        PageImpl<BookDocument> bookDocuments = new PageImpl<>(List.of(documentA, documentB));

        when(bookSearchRepository.findByMultiMatch(pageable, keyword)).thenReturn(bookDocuments);

        Page<BookSearchResponse> bookSearchResponses = bookSearchService.searchBooksV2(keyword, pageable);

        assertThat(bookSearchResponses.getTotalElements()).isEqualTo(2);

    }

    @Test
    void searchBooksV2_검색_실패_결과없음() {
        Pageable pageable = PageRequest.of(1, 10);
        String keyword = "실패";

        when(bookSearchRepository.findByMultiMatch(pageable, keyword))
                .thenReturn(Page.empty());

        Page<BookSearchResponse> result = bookSearchService.searchBooksV2(keyword, pageable);

        assertThat(result.getContent()).isEmpty();
    }

    @Test
    void searchBooksV1_검색_성공() {
        Pageable pageable = PageRequest.of(1, 10);
        String keyword = "검색";

        PageImpl<Book> books = new PageImpl<>(List.of(book, book));

        when(bookRepository.findBooksByKeyword(keyword, pageable)).thenReturn(books);

        assertThat(bookSearchService.searchBooksV1(keyword, pageable)).hasSize(2);

    }

    @Test
    void searchBooksV1_검색_실패_결과없음() {
        Pageable pageable = PageRequest.of(1, 10);
        String keyword = "실패";

        when(bookRepository.findBooksByKeyword(keyword, pageable))
                .thenReturn(Page.empty());

        Page<BookSearchResponse> bookSearchResponses = bookSearchService.searchBooksV1(keyword, pageable);

        assertThat(bookSearchResponses.getContent()).isEmpty();
    }

    @Test
    void BookDocument_업데이트_성공() {
        BookDocument document = BookDocument.toDocument(book);

        String updatedTitle = "제목수정";

        when(bookSearchRepository.findByIsbnOnSale(book.getIsbn())).thenReturn(Optional.of(document));

        book.updateBook(updatedTitle, "김영한", BigDecimal.valueOf(10000), "IT" , 100, ON_SALE.name(), REGULAR.name());

        bookSearchService.updateBookDocument(book);

        assertThat(document.getTitle()).isEqualTo(updatedTitle);
    }


    @Test
    void BookDocument_삭제_성공() {
        BookDocument document = BookDocument.toDocument(book);

        when(bookSearchRepository.findByIsbnOnSale(book.getIsbn())).thenReturn(Optional.of(document));

        bookSearchService.deleteBookDocument(book);

        assertThat(document.isDeleted()).isTrue();
    }


    @Test
    void BookDocument_업데이트_실패_BookDocument없음() {
        when(bookSearchRepository.findByIsbnOnSale(book.getIsbn()))
                .thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> {
            bookSearchService.updateBookDocument(book);
        });
    }

    @Test
    void BookDocument_삭제_실패_BookDocument없음() {
        when(bookSearchRepository.findByIsbnOnSale(book.getIsbn()))
                .thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> {
            bookSearchService.deleteBookDocument(book);
        });
    }

}