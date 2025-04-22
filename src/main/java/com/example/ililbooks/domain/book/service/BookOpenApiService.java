package com.example.ililbooks.domain.book.service;

import com.example.ililbooks.client.book.BookClient;
import com.example.ililbooks.client.book.dto.BookApiResponse;
import com.example.ililbooks.domain.book.entity.Book;
import com.example.ililbooks.domain.book.repository.BookRepository;
import com.example.ililbooks.domain.search.entity.BookDocument;
import com.example.ililbooks.domain.search.service.BookSearchService;
import com.example.ililbooks.domain.user.entity.Users;
import com.example.ililbooks.domain.user.service.UserService;
import com.example.ililbooks.global.dto.AuthUser;
import com.example.ililbooks.global.exception.BadRequestException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static com.example.ililbooks.global.exception.ErrorMessage.DUPLICATE_BOOK;

@Service
@RequiredArgsConstructor
public class BookOpenApiService {
    private final BookRepository bookRepository;
    private final UserService userService;
    private final BookClient bookClient;
    private final BookSearchService bookSearchService;

    /**
     *  대량 데이터를 저장하기 위해 keywords 배열에 키워드를 담아서 로직 마지막에 한 번에 save 하는 방식
     *  추후 배포할 때 키워드를 하나씩 입력하는 방식으로 바꿀 예정
     *  키워드는 슬랙에 따로 공유하겠습니다
     */
    @Transactional
    public void createBookByOpenApi(AuthUser authUser, Pageable pageable) {

        String[] keywords = {
                "육아일기"
        };

        //랜덤 가격 및 재고 생성을 위한 Random 객체 선언
        Random random = new Random();
        Users users = userService.findByIdOrElseThrow(authUser.getUserId());

        //open api를 통해 책 리스트 가져오기
        BookApiResponse[] books;

        List<Book> booksToSave = new ArrayList<>();
        List<BookDocument> bookDocumentsToSave = new ArrayList<>();

        for (String kwd : keywords) {

            try {
                books = bookClient.findBooks(kwd, pageable);
            }catch (Exception e) {
                continue;
            }

            // 책 데이터가 없거나 null 인 경우 처리
            if (ObjectUtils.isEmpty(books)) {
                continue;
            }

            for (BookApiResponse bookApiResponse : books) {

                // isbn 존재하지 않음 or 이미 등록된 책 or 제목,저자,isbn 중 255bytes 가 초과 일 때 다음 루프
                if (!isValidBookApiResponse(bookApiResponse)) continue;

                //랜덤 가격 (Min: 5000, Max: 45000)
                long randomPrice = 5000 + random.nextLong(40000);
                BigDecimal price = BigDecimal.valueOf(Math.round(randomPrice / 1000.0) * 1000);

                //랜덤 재고 (Min: 1, Max:101)
                int randomStock = 1 + random.nextInt(100);

                try {
                    Book book = Book.of(
                            users,
                            bookApiResponse.title().replaceAll("<[^>]*>", ""),
                            bookApiResponse.author().replaceAll("<[^>]*>", ""),
                            bookApiResponse.publisher().replaceAll("<[^>]*>", ""),
                            bookApiResponse.category(),
                            bookApiResponse.isbn(),
                            price,
                            randomStock
                    );

                    BookDocument document = BookDocument.toDocument(book);

                    for (Book bookToSave : booksToSave) {
                        if (bookToSave.getIsbn().equals(book.getIsbn())) {
                            throw new BadRequestException(DUPLICATE_BOOK.getMessage());
                        }
                    }

                    booksToSave.add(book);
                    bookDocumentsToSave.add(document);
                } catch (Exception e) {
                    System.out.println("책 저장 실패 (ISBN: " + bookApiResponse.isbn() + "): " + e.getMessage());
                }
            }
        }
        bookRepository.saveAll(booksToSave);
        bookSearchService.saveAll(bookDocumentsToSave);
    }

    private boolean isValidBookApiResponse(BookApiResponse bookApiResponse) {
        //책 고유번호가 없는 경우
        if (!StringUtils.hasText(bookApiResponse.isbn())) {
            return false;
        }

        //이미 등록된 책인 경우 저장하지 않음
        if (bookRepository.existsByIsbn(bookApiResponse.isbn())) {
            return false;
        }

        // 각 컬럼이 255byte 초과할 경우
        if (bookApiResponse.isbn().getBytes(StandardCharsets.UTF_8).length > 255 ||
                bookApiResponse.author().getBytes(StandardCharsets.UTF_8).length > 255 ||
                bookApiResponse.title() != null && bookApiResponse.title().getBytes(StandardCharsets.UTF_8).length > 255
        ) {
            return false;
        }
        return true;
    }
}
