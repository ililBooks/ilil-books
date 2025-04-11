package com.example.ililbooks.client.book.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Open API 응답의 필드명(titleInfo, authorInfo, kdcName1s, isbn, pubInfo)은
 * 본 프로젝트의 Book 엔티티 필드명(title, author, category, isbn, publisher)과 다르기 때문에,
 * 이를 매핑하기 위해 각 필드에 @JsonProperty 어노테이션을 사용한다.
 *
 * @param title     책 제목
 * @param author    저자
 * @param category  분류(카테고리)
 * @param isbn      ISBN
 * @param publisher 출판사
 */
public record BookApiResponse (
        @JsonProperty("titleInfo")
        String title,

       @JsonProperty("authorInfo")
       String author,

       @JsonProperty("kdcName1s")
       String category,

       @JsonProperty("isbn")
       String isbn,

       @JsonProperty("pubInfo")
       String publisher
) {

}