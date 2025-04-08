package com.example.ililbooks.domain.book.service;

import com.example.ililbooks.client.BookClient;
import com.example.ililbooks.client.dto.BookApiResponse;
import com.example.ililbooks.domain.book.dto.request.BookCreateRequest;
import com.example.ililbooks.domain.book.dto.request.BookUpdateRequest;
import com.example.ililbooks.domain.book.dto.response.BookResponse;
import com.example.ililbooks.domain.book.dto.response.BookWithImagesResponse;
import com.example.ililbooks.domain.book.entity.Book;
import com.example.ililbooks.domain.book.repository.BookRepository;
import com.example.ililbooks.domain.review.dto.response.ReviewWithImagesResponse;
import com.example.ililbooks.domain.user.entity.Users;
import com.example.ililbooks.domain.review.service.ReviewFindService;
import com.example.ililbooks.domain.user.service.UserService;
import com.example.ililbooks.global.dto.AuthUser;
import com.example.ililbooks.global.exception.BadRequestException;
import com.example.ililbooks.global.exception.NotFoundException;
import com.example.ililbooks.domain.book.entity.BookImage;
import com.example.ililbooks.domain.book.repository.ImageBookRepository;
import com.example.ililbooks.global.image.service.S3ImageService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Random;

import static com.example.ililbooks.global.exception.ErrorMessage.*;
import static com.example.ililbooks.global.image.dto.response.ImageResponse.ofBookImageList;

@Service
@RequiredArgsConstructor
public class BookService {
    private final BookRepository bookRepository;
    private final ImageBookRepository imageBookRepository;
    private final UserService userService;
    private final BookClient bookClient;
    private final ReviewFindService reviewFindService;
    private final S3ImageService s3ImageService;

    @Transactional
    public BookResponse createBook(AuthUser authUser, BookCreateRequest bookCreateRequest) {

        //이미 등록된 책인 경우 (책 고유 번호로 판별)
        if(bookRepository.existsByIsbn(bookCreateRequest.getIsbn())) {
            throw new BadRequestException(DUPLICATE_BOOK.getMessage());
        }

        Users findUsers = userService.findByIdOrElseThrow(authUser.getUserId());

        Book createBook = Book.of(findUsers, bookCreateRequest);
        Book savedBook = bookRepository.save(createBook);

        return BookResponse.of(savedBook);
    }

    @Transactional
    public void createBookByOpenApi(AuthUser authUser, Integer pageNum, Integer pageSize, String kwd) {

        //open api를 통해 책 리스트 가져오기
        BookApiResponse[] books = bookClient.getBooks(kwd, pageNum, pageSize);

        //랜덤 가격 및 재고 생성을 위한 Random객체 선언
        Random random = new Random();

        for (BookApiResponse book : books) {
            //랜덤 가격 (Min: 5000, Max: 45000)
            long randomPrice = 5000 + random.nextLong(40000);
            BigDecimal price = BigDecimal.valueOf(Math.round(randomPrice / 1000.0) * 1000);

            //랜덤 재고 (Min: 1, Max:101)
            int randomStock = 1 +  random.nextInt(100);

            //책 고유번호가 없는 경우
            if (!StringUtils.hasText(book.getIsbn())) {
                continue;
            }

            //이미 등록된 책인 경우 저장하지 않음
            if (bookRepository.existsByIsbn(book.getIsbn())) {
                continue;
            }

            Users findUsers = userService.findByIdOrElseThrow(authUser.getUserId());
            Book savedBook = Book.of(findUsers, book, price, randomStock);

            bookRepository.save(savedBook);
        }
    }

    @Transactional
    public void uploadBookImage(Long bookId, String imageUrl) {
        Book findBook = findBookByIdOrElseThrow(bookId);
        BookImage bookImage = BookImage.of(findBook, imageUrl);

        //등록된 이미지의 개수가 5개를 넘는 경우
        if(imageBookRepository.countByBookId(bookImage.getBook().getId()) >= 5) {
            throw new BadRequestException(IMAGE_UPLOAD_LIMIT_OVER.getMessage());
        }

        imageBookRepository.save(bookImage);
    }

    @Transactional
    public void deleteBookImage(AuthUser authUser, Long imageId) {
        //이미지가 존재하지 않는 경우
        BookImage findBookImage = imageBookRepository.findImageById(imageId)
                .orElseThrow(()-> new NotFoundException(NOT_FOUND_IMAGE.getMessage()));

        //자신이 등록한 책 이미지가 아닌 경우
        if (!authUser.getUserId().equals(findBookImage.getBook().getUsers().getId())) {
            throw new BadRequestException(CANNOT_DELETE_OTHERS_IMAGE.getMessage());
        }

        s3ImageService.deleteImage(findBookImage.getFileName());
        imageBookRepository.delete(findBookImage);
    }

    @Transactional(readOnly = true)
    public BookWithImagesResponse getBookResponse(Long bookId, int pageNum, int pageSize) {
        Book findBook = findBookByIdOrElseThrow(bookId);

        Page<ReviewWithImagesResponse> reviews = reviewFindService.getReviews(findBook.getId(), pageNum, pageSize);
        List<BookImage> findBookImage = imageBookRepository.findAllByBookId(findBook.getId());

        return BookWithImagesResponse.of(findBook, reviews, ofBookImageList(findBookImage));
    }

    @Transactional(readOnly = true)
    public Page<BookResponse> getBooks(int pageNum, int pageSize) {
        Pageable pageable = PageRequest.of(pageNum - 1, pageSize);

        Page<Book> findBooks = bookRepository.findAll(pageable);

        return findBooks
                .map(book ->
                {
                    List<BookImage> findBookImage = imageBookRepository.findAllByBookId(book.getId());

                    //대표 이미지 하나를 뽑아서 응답
                    if (findBookImage.isEmpty()) {
                        return BookResponse.of(book, null);
                    }
                    return BookResponse.of(book, findBookImage.get(0).getImageUrl());
                });
    }

    @Transactional
    public void updateBook(Long bookId, BookUpdateRequest bookUpdateRequest) {
        Book findBook = findBookByIdOrElseThrow(bookId);

        findBook.updateBook(bookUpdateRequest);
    }

    @Transactional
    public void deleteBook(Long bookId) {
        Book findBook = findBookByIdOrElseThrow(bookId);

        bookRepository.delete(findBook);
    }

    public Book findBookByIdOrElseThrow(Long bookId) {
        return bookRepository.findById(bookId).orElseThrow(() -> new NotFoundException(NOT_FOUND_BOOK.getMessage()));
    }
}
