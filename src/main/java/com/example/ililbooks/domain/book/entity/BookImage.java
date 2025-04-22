package com.example.ililbooks.domain.book.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "book_images")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BookImage{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id")
    private Book book;

    private String imageUrl;

    private String fileName;

    private String extension;

    private int positionIndex;

    @Builder
    private BookImage(Book book, String imageUrl, String fileName, String extension, int positionIndex) {
        this.book = book;
        this.imageUrl = imageUrl;
        this.fileName = fileName;
        this.extension = extension;
        this.positionIndex = positionIndex;
    }

    public static BookImage of(Book book,String imageUrl, String fileName, String extension, int positionIndex) {

        return BookImage.builder()
                .book(book)
                .imageUrl(imageUrl)
                .fileName(fileName)
                .extension(extension)
                .positionIndex(positionIndex)
                .build();
    }
}
