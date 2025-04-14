package com.example.ililbooks.domain.search.entity;

import com.example.ililbooks.domain.book.entity.Book;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.*;

import java.math.BigDecimal;

@Getter
@Document(indexName = "books")
@NoArgsConstructor
@Setting(settingPath = "static/elastic-token.json")
@Mapping(mappingPath = "static/elastic-mapping.json")
public class BookDocument {

    @Id
    @Field(type = FieldType.Keyword)
    private String id;

    @Field(type = FieldType.Keyword)
    private String userId;

    @Field(type = FieldType.Text, analyzer = "korean_analyzer")
    private String title;

    @Field(type = FieldType.Text, analyzer = "korean_analyzer")
    private String author;

    @Field(type = FieldType.Text, analyzer = "korean_analyzer")
    private String publisher;

    @Field(type = FieldType.Long)
    private BigDecimal price;

    @Field(type = FieldType.Text, analyzer = "korean_analyzer")
    private String category;

    @Field(type = FieldType.Integer)
    private int stock;

    @Field(type = FieldType.Keyword)
    private String isbn;

    @Field(type = FieldType.Keyword)
    private String saleStatus;

    @Field(type = FieldType.Keyword)
    private String limitedType;

    @Field(type = FieldType.Boolean)
    private boolean isDeleted;


    @Builder
    public BookDocument(String id, String userId, String title, String author, String publisher, BigDecimal price, String category, int stock, String isbn, String saleStatus, String limitedType, boolean isDeleted) {
        this.id = id;
        this.userId = userId;
        this.title = title;
        this.author = author;
        this.publisher = publisher;
        this.price = price;
        this.category = category;
        this.stock = stock;
        this.isbn = isbn;
        this.saleStatus = saleStatus;
        this.limitedType = limitedType;
        this.isDeleted = isDeleted;
    }

    public static BookDocument toDocument(Book book) {
        return BookDocument.builder()
                .id(String.valueOf(book.getId()))
                .userId(String.valueOf(book.getUsers().getId()))
                .title(book.getTitle())
                .author(book.getAuthor())
                .publisher(book.getPublisher())
                .price(book.getPrice())
                .category(book.getCategory())
                .stock(book.getStock())
                .isbn(book.getIsbn())
                .saleStatus(book.getSaleStatus().name())
                .limitedType(book.getLimitedType().name())
                .isDeleted(book.isDeleted())
                .build();
    }

    public void updateBookDocument(Book book) {
        this.title = book.getTitle();
        this.author = book.getAuthor();
        this.price = book.getPrice();
        this.category = book.getCategory();
        this.stock = book.getStock();
        this.saleStatus = book.getSaleStatus().name();
        this.limitedType = book.getLimitedType().name();
        this.isDeleted = book.isDeleted();
    }


    public void deleteBookDocument(boolean isDeleted) { this.isDeleted = isDeleted; }
}
