package com.example.ililbooks.domain.search.repository;

import com.example.ililbooks.domain.search.entity.BookDocument;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import java.util.Optional;


public interface BookSearchRepository extends ElasticsearchRepository<BookDocument, String> {
    @Query(
            "{\"multi_match\": {\"query\": \"?0\", \"fields\": [\"title^5\", \"author^3\", \"category^2\", \"publisher\"], \"type\": \"best_fields\"}}"
    )
    Page<BookDocument> findByMultiMatch(Pageable pageable, String query);

    //TODO saleStatus는 문자열 아닌가요? "ON_SALE" 이런거로 매칭해야할것같은데 그리고 isDeleted == false조건도 고려 필요
    // raw Json String 사용 말고, elasticsearch Query DSL 추천
    @Query(
            "{" +
                    "  \"bool\": {" +
                    "    \"must\": [" +
                    "      {\"term\": {\"isbn\": \"?0\"}}," +
                    "      {\"term\": {\"saleStatus\": \"true\"}}" +
                    "    ]" +
                    "  }" +
                    "}"
    )
    Optional<BookDocument> findByIsbnOnSale(String isbn);

}
