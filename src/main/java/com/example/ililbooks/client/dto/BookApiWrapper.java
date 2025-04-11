package com.example.ililbooks.client.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record BookApiWrapper (
        BookApiResponse [] result
){

}