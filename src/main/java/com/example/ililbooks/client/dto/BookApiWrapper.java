package com.example.ililbooks.client.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
public class BookApiWrapper {
    @JsonProperty("result")
    private BookApiResponse[] result;

    public BookApiWrapper() {
    }
}