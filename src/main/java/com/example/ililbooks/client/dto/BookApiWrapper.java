package com.example.ililbooks.client.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record BookApiWrapper (
        //응답된 result List Mapping
        @JsonProperty("result")
        BookApiResponse [] result
){

}