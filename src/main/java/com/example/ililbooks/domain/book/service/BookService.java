package com.example.ililbooks.domain.book.service;

import com.example.ililbooks.client.book.BookClient;
import com.example.ililbooks.client.book.dto.BookApiResponse;
import com.example.ililbooks.domain.book.dto.request.BookCreateRequest;
import com.example.ililbooks.domain.book.dto.request.BookUpdateRequest;
import com.example.ililbooks.domain.book.dto.response.BookListResponse;
import com.example.ililbooks.domain.book.dto.response.BookResponse;
import com.example.ililbooks.domain.book.dto.response.BookWithImagesResponse;
import com.example.ililbooks.domain.book.entity.Book;
import com.example.ililbooks.domain.book.entity.BookImage;
import com.example.ililbooks.domain.book.repository.BookRepository;
import com.example.ililbooks.domain.book.repository.ImageBookRepository;
import com.example.ililbooks.domain.review.dto.response.ReviewWithImagesResponse;
import com.example.ililbooks.domain.review.service.ReviewDeleteService;
import com.example.ililbooks.domain.review.service.ReviewFindService;
import com.example.ililbooks.domain.search.entity.BookDocument;
import com.example.ililbooks.domain.search.service.BookSearchService;
import com.example.ililbooks.domain.user.entity.Users;
import com.example.ililbooks.domain.user.service.UserService;
import com.example.ililbooks.global.dto.AuthUser;
import com.example.ililbooks.global.exception.BadRequestException;
import com.example.ililbooks.global.exception.ForbiddenException;
import com.example.ililbooks.global.exception.NotFoundException;
import com.example.ililbooks.global.image.dto.request.ImageRequest;
import com.example.ililbooks.global.image.service.S3ImageService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static com.example.ililbooks.global.exception.ErrorMessage.*;
import static com.example.ililbooks.global.image.dto.response.ImageListResponse.ofBookImageList;

@Service
@RequiredArgsConstructor
public class BookService {
    private final BookRepository bookRepository;
    private final ImageBookRepository imageBookRepository;
    private final UserService userService;
    private final BookClient bookClient;
    private final ReviewFindService reviewFindService;
    private final S3ImageService s3ImageService;
    private final ReviewDeleteService reviewDeleteService;
    private final BookSearchService bookSearchService;

    @Transactional
    public BookResponse createBook(AuthUser authUser, BookCreateRequest bookCreateRequest) {

        //이미 등록된 책인 경우 (책 고유 번호로 판별)
        if (bookRepository.existsByIsbn(bookCreateRequest.isbn())) {
            throw new BadRequestException(DUPLICATE_BOOK.getMessage());
        }

        Users users = userService.findByIdOrElseThrow(authUser.getUserId());

        Book book = Book.of(
                users,
                bookCreateRequest.title(),
                bookCreateRequest.author(),
                bookCreateRequest.price(),
                bookCreateRequest.category(),
                bookCreateRequest.stock(),
                bookCreateRequest.isbn(),
                bookCreateRequest.publisher()
        );

        Book savedBook = bookRepository.save(book);

        bookSearchService.saveBookDocumentFromBook(book);

        return BookResponse.of(savedBook);
    }

    /**
     *  대량 데이터를 저장하기 위해 keywords 배열에 키워드를 담아서 로직 마지막에 한 번에 save 하는 방식
     *  추후 배포할 때 키워드를 하나씩 입력하는 방식으로 바꿀 예정
     *  키워드는 슬랙에 따로 공유하겠습니다
     */
    @Transactional
    public void createBookByOpenApi(AuthUser authUser, Integer pageNum, Integer pageSize) {

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
                books = bookClient.findBooks(kwd, pageNum, pageSize);
            }catch (Exception e) {
                continue;
            }

            // 책 데이터가 없거나 null 인 경우 처리
            if (books == null || books.length == 0) {
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
                    Book book = Book.of(users, bookApiResponse, price, randomStock);
                    BookDocument document = BookDocument.toDocument(book);

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

    @Transactional
    public void uploadBookImage(AuthUser authUser, Long bookId, ImageRequest imageRequest) {
        Book book = findBookByIdOrElseThrow(bookId);

        // publisher는 자신이 등록한 책에 대해서만 이미지 등록이 가능
        if (!book.getUsers().getId().equals(authUser.getUserId())) {
            throw new ForbiddenException(CANNOT_UPLOAD_OTHERS_BOOK_IMAGE.getMessage());
        }

        BookImage bookImage = BookImage.of(book, imageRequest.imageUrl(), imageRequest.fileName(), imageRequest.extension());

        //등록된 이미지의 개수가 5개를 넘는 경우
        if (imageBookRepository.countByBookId(bookImage.getBook().getId()) >= 5) {
            throw new BadRequestException(IMAGE_UPLOAD_LIMIT_OVER.getMessage());
        }

        imageBookRepository.save(bookImage);
    }

    @Transactional
    public void deleteBookImage(AuthUser authUser, Long imageId) {
        //이미지가 존재하지 않는 경우
        BookImage bookImage = findBookImage(imageId);

        //자신이 등록한 책 이미지가 아닌 경우
        if (!authUser.getUserId().equals(bookImage.getBook().getUsers().getId())) {
            throw new ForbiddenException(CANNOT_DELETE_OTHERS_IMAGE.getMessage());
        }

        s3ImageService.deleteImage(bookImage.getFileName());
        imageBookRepository.delete(bookImage);
    }

    @Transactional(readOnly = true)
    public BookWithImagesResponse findBookResponse(Long bookId, int pageNum, int pageSize) {
        Book book = findBookByIdOrElseThrow(bookId);

        Page<ReviewWithImagesResponse> reviews = reviewFindService.getReviews(book.getId(), pageNum, pageSize);
        List<BookImage> bookImage = getAllByBookId(book);

        return BookWithImagesResponse.of(book, reviews, ofBookImageList(bookImage));
    }

    @Transactional(readOnly = true)
    public Page<BookListResponse> getBooks(int pageNum, int pageSize) {
        Pageable pageable = PageRequest.of(pageNum - 1, pageSize);

        Page<Book> books = bookRepository.findAllNotDeleted(pageable);

        return books
                .map(book ->
                {
                    List<BookImage> bookImages = imageBookRepository.findAllByBookId(book.getId());
                    //대표 이미지 하나를 뽑아서 응답
                    if (bookImages.isEmpty()) {
                        return BookListResponse.of(book);
                    }
                    return BookListResponse.of(book, bookImages.get(0).getImageUrl());
                });
    }

    @Transactional
    public void updateBook(AuthUser authUser, Long bookId, BookUpdateRequest bookUpdateRequest) {
        Book book = findBookByIdOrElseThrow(bookId);

        if (!book.getUsers().getId().equals(authUser.getUserId())) {
            throw new ForbiddenException(CANNOT_UPDATE_OTHERS_BOOK.getMessage());
        }

        book.updateBook(
                bookUpdateRequest.title(),
                bookUpdateRequest.author(),
                bookUpdateRequest.price(),
                bookUpdateRequest.category(),
                bookUpdateRequest.stock(),
                bookUpdateRequest.saleStatus(),
                bookUpdateRequest.limitedType()
                );
    }

    @Transactional
    public void deleteBook(AuthUser authUser, Long bookId) {
        Book book = findBookByIdOrElseThrow(bookId);

        if (!book.getUsers().getId().equals(authUser.getUserId())) {
            throw new ForbiddenException(CANNOT_UPDATE_OTHERS_BOOK.getMessage());
        }

        book.deleteBook();
        List<BookImage> bookImages = imageBookRepository.findAllByBookId(bookId);

        bookImages.forEach(bookImage ->
                s3ImageService.deleteImage(bookImage.getFileName())
        );

        imageBookRepository.deleteAllByBookId(bookId);
        reviewDeleteService.deleteAllReviewByBookId(bookId);
    }

    public Book findBookByIdOrElseThrow(Long bookId) {
        return bookRepository.findBookById(bookId)
                .orElseThrow(() -> new NotFoundException(NOT_FOUND_BOOK.getMessage()));
    }

    public BookImage findBookImage(Long imageId) {
        return imageBookRepository.findImageById(imageId)
                .orElseThrow(() -> new NotFoundException(NOT_FOUND_IMAGE.getMessage()));
    }

    public List<BookImage> getAllByBookId(Book book) {
        return imageBookRepository.findAllByBookId(book.getId());
    }

    public boolean existsOnSaleRegularBookById(Long bookId) {
        return bookRepository.existsOnSaleRegularBookById(bookId);
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
