package com.example.ililbooks.domain.bestseller.controller;

import com.example.ililbooks.domain.bestseller.dto.response.BestSellerChartResponse;
import com.example.ililbooks.domain.bestseller.enums.PeriodType;
import com.example.ililbooks.domain.bestseller.service.BestSellerService;
import com.example.ililbooks.global.dto.response.Response;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/bestsellers")
@RequiredArgsConstructor
public class BestSellerController {

    private final BestSellerService bestSellerService;

    @GetMapping
    public Response<List<BestSellerChartResponse>> getBestSellerChart(
            @RequestParam String date,
            @RequestParam String type
    ) {
        return Response.of(bestSellerService.getBestSellerChart(PeriodType.valueOf(type.toUpperCase()), date));
    }
}
