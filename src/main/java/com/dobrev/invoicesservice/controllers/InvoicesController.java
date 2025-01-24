package com.dobrev.invoicesservice.controllers;

import com.amazonaws.xray.spring.aop.XRayEnabled;
import com.dobrev.invoicesservice.invoices.dto.InvoiceApiDto;
import com.dobrev.invoicesservice.invoices.dto.InvoiceFileTransactionApiDto;
import com.dobrev.invoicesservice.invoices.dto.InvoiceProductApiDto;
import com.dobrev.invoicesservice.invoices.dto.UrlResponseDto;
import com.dobrev.invoicesservice.services.InvoicesFileTransactionsService;
import com.dobrev.invoicesservice.services.InvoicesServices;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@XRayEnabled
@RequestMapping("/api/invoices")
@RequiredArgsConstructor
public class InvoicesController {
    private final InvoicesServices invoicesServices;
    private final InvoicesFileTransactionsService invoicesFileTransactionsService;

    @PostMapping
    public UrlResponseDto generatePreSignUrl(@RequestHeader("requestId") String requestId){
        return invoicesServices.generatePreSignUrl(requestId);
    }

    @GetMapping("/transactions/{fileTransactionId}")
    public InvoiceFileTransactionApiDto getInvoiceFileTransaction(@PathVariable("fileTransactionId")
                                                       String fileTransactionId) {
        return invoicesFileTransactionsService.getInvoiceFileTransaction(fileTransactionId).join();
    }

    @GetMapping
    public List<InvoiceApiDto> findByCustomerEmail(@PathVariable("email") String email){
        return invoicesServices.findByCustomerEmail(email);
    }
}