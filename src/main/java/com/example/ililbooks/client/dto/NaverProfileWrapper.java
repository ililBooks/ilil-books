package com.example.ililbooks.client.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record NaverProfileWrapper(
        //응답된 response List Mapping
        @JsonProperty("response")
        NaverProfileResponse [] response
) {
}
