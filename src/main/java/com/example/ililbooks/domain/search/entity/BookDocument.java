package com.example.ililbooks.domain.search.entity;

import jakarta.persistence.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.elasticsearch.annotations.*;

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

    @Field(type = FieldType.Long)
    private Long price;

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



}
