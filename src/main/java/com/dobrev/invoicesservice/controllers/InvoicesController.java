package com.dobrev.invoicesservice.controllers;

import com.amazonaws.xray.spring.aop.XRayEnabled;
import com.dobrev.invoicesservice.invoices.dto.UrlResponseDto;
import com.dobrev.invoicesservice.services.InvoicesServices;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@XRayEnabled
@RequestMapping("/api/invoices")
@RequiredArgsConstructor
public class InvoicesController {
    private final InvoicesServices invoicesServices;

    @PostMapping
    public UrlResponseDto generatePreSignUrl(@RequestHeader("requestId") String requestId){
        return invoicesServices.generatePreSignUrl(requestId);
    }
}