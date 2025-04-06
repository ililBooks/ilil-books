package com.example.ililbooks.client.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
public class BookApiResponse {

    @JsonProperty("titleInfo")
    private final String title;

    @JsonProperty("authorInfo")
    private final String author;

    @JsonProperty("kdcName1s")
    private final String category;

    @JsonProperty("isbn")
    private final String isbn;

    public BookApiResponse(
            String title,
            String author,
            String category,
            String isbn
    ) {
        this.title = title;
        this.author = author;
        this.category = category;
        this.isbn = isbn;
    }
}