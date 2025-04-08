package com.example.ililbooks.domain.book.entity;

import com.example.ililbooks.global.image.entity.Image;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "book_images")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BookImage extends Image {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id")
    private Book book;

    private String imageUrl;

    private String fileName;

    private String extension;

    @Builder
    private BookImage(Book book, String imageUrl, String fileName, String extension) {
        this.book = book;
        this.imageUrl = imageUrl;
        this.fileName = fileName;
        this.extension = extension;
    }

    public static BookImage of(Book book, String imageUrl) {

        // URL에서 파일 이름 추출
        String fileName = extractFileName(imageUrl);

        // 확장자 추출
        String extension = extractExtension(fileName);

        return BookImage.builder()
                .book(book)
                .imageUrl(imageUrl)
                .fileName(fileName)
                .extension(extension)
                .build();
    }
}
