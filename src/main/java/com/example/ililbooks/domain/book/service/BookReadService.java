package com.example.ililbooks.domain.book.service;

import com.example.ililbooks.domain.book.dto.response.BookListResponse;
import com.example.ililbooks.domain.book.dto.response.BookWithImagesResponse;
import com.example.ililbooks.domain.book.entity.Book;
import com.example.ililbooks.domain.book.entity.BookImage;
import com.example.ililbooks.domain.book.repository.BookRepository;
import com.example.ililbooks.domain.book.repository.ImageBookRepository;
import com.example.ililbooks.domain.review.dto.response.ReviewWithImagesResponse;
import com.example.ililbooks.domain.review.service.ReviewFindService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.example.ililbooks.global.image.dto.response.ImageListResponse.ofBookImageList;

@Service
@RequiredArgsConstructor
public class BookReadService {

    private final BookRepository bookRepository;
    private final ImageBookRepository imageBookRepository;
    private final ReviewFindService reviewFindService;
    private final BookService bookService;

    @Transactional(readOnly = true)
    public BookWithImagesResponse findBookResponse(Long bookId, Pageable pageable) {
        Book book = bookService.findBookByIdOrElseThrow(bookId);

        Page<ReviewWithImagesResponse> reviews = reviewFindService.getReviews(book.getId(), pageable);
        List<BookImage> bookImage = bookService.getAllByBookId(book);

        return BookWithImagesResponse.of(book, reviews, ofBookImageList(bookImage));
    }

    @Transactional(readOnly = true)
    public Page<BookListResponse> getBooks(Pageable pageable) {
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

}
