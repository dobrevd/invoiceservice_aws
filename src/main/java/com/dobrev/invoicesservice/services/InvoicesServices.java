package com.dobrev.invoicesservice.services;

import com.dobrev.invoicesservice.invoices.dto.UrlResponseDto;
import com.dobrev.invoicesservice.repositories.InvoicesFileTransactionsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.ThreadContext;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class InvoicesServices {
    private final S3InvoicesService s3InvoicesService;
    private final InvoicesFileTransactionsRepository invoicesFileTransactionsRepository;

    public UrlResponseDto generatePreSignUrl(String requestId) {
        String transactionId = UUID.randomUUID().toString();
        int expiresIn = 300;
        ThreadContext.put("invoiceFileTransactionId", transactionId);

        String preSignedUrl = s3InvoicesService.generatePreSignedUrl(transactionId, expiresIn);
        invoicesFileTransactionsRepository.createInvoiceFileTransaction(transactionId, requestId, expiresIn).join();

        log.info("Invoice file transaction generated...");
        return new UrlResponseDto(preSignedUrl, expiresIn, requestId);
    }
}