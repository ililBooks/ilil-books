package com.example.ililbooks.client.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class BookApiWrapper {
    //응답된 result List Mapping
    @JsonProperty("result")
    private BookApiResponse [] result;
}