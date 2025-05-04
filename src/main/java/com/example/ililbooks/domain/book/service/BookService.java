package com.example.ililbooks.domain.book.service;

import com.example.ililbooks.domain.book.dto.request.BookCreateRequest;
import com.example.ililbooks.domain.book.dto.request.BookUpdateRequest;
import com.example.ililbooks.domain.book.dto.response.BookResponse;
import com.example.ililbooks.domain.book.entity.Book;
import com.example.ililbooks.domain.book.entity.BookImage;
import com.example.ililbooks.domain.book.enums.LimitedType;
import com.example.ililbooks.domain.book.repository.BookRepository;
import com.example.ililbooks.domain.book.repository.ImageBookRepository;
import com.example.ililbooks.domain.review.service.ReviewDeleteService;
import com.example.ililbooks.domain.user.entity.Users;
import com.example.ililbooks.domain.user.service.UserService;
import com.example.ililbooks.global.dto.AuthUser;
import com.example.ililbooks.global.exception.BadRequestException;
import com.example.ililbooks.global.exception.ForbiddenException;
import com.example.ililbooks.global.exception.NotFoundException;
import com.example.ililbooks.global.image.dto.request.ImageRequest;
import com.example.ililbooks.global.image.service.S3ImageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static com.example.ililbooks.global.exception.ErrorMessage.*;

@Service
@RequiredArgsConstructor
public class BookService {
    private final BookRepository bookRepository;
    private final UserService userService;
    private final ReviewDeleteService reviewDeleteService;
    private final ImageBookRepository imageBookRepository;
    private final S3ImageService s3ImageService;

    @Transactional
    public BookResponse createBook(AuthUser authUser, BookCreateRequest bookCreateRequest) {

        //이미 등록된 책인 경우 (책 고유 번호로 판별)
        if(bookRepository.existsByIsbn(bookCreateRequest.isbn())) {
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
                bookCreateRequest.publisher(),
                LimitedType.valueOf(bookCreateRequest.limitedType())
        );

        Book savedBook = bookRepository.save(book);

        return BookResponse.of(savedBook);
    }

    @Transactional
    public void uploadBookImage(AuthUser authUser, Long bookId, ImageRequest imageRequest) {
        Book book = findBookByIdOrElseThrow(bookId);

        // publisher는 자신이 등록한 책에 대해서만 이미지 등록이 가능
        if (!book.getUsers().getId().equals(authUser.getUserId())) {
            throw new ForbiddenException(CANNOT_UPLOAD_OTHERS_BOOK_IMAGE.getMessage());
        }

        //해당 순서의 이미지가 이미 존재하는 경우
        if (imageBookRepository.existsByBookIdAndPositionIndex(book.getId(), imageRequest.positionIndex())) {
            throw new BadRequestException(DUPLICATE_POSITION_INDEX.getMessage());
        }

        BookImage bookImage = BookImage.of(book, imageRequest.imageUrl(), imageRequest.fileName(), imageRequest.extension(), imageRequest.positionIndex());

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
        // BookDocument 수정
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

    public List<Long> findInvalidBookIds(List<Long> ids) {
        return ids.stream()
                .filter(id -> !existsOnSaleRegularBookById(id))
                .toList();
    }

    public Optional<BookImage> findFirstByBookId(Long bookId) {
        return imageBookRepository.findFirstByBookId(bookId);
    }
}
