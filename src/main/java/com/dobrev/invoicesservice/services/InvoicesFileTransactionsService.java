package com.dobrev.invoicesservice.services;

import com.amazonaws.xray.spring.aop.XRayEnabled;
import com.dobrev.invoicesservice.invoices.dto.InvoiceFileTransactionApiDto;
import com.dobrev.invoicesservice.repositories.InvoicesFileTransactionsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@XRayEnabled
@Slf4j
public class InvoicesFileTransactionsService {
    private final InvoicesFileTransactionsRepository invoicesFileTransactionsRepository;

    public CompletableFuture<InvoiceFileTransactionApiDto> getInvoiceFileTransaction(String fileTransactionId){
        log.info("Get invoice file transaction by its id: {}", fileTransactionId);
        return invoicesFileTransactionsRepository.getInvoiceFileTransaction(fileTransactionId)
                .thenApply(invoiceFileTransaction -> {
                    if (invoiceFileTransaction == null) {
                        throw new IllegalArgumentException("Invoice file transaction not found for id: " + fileTransactionId);
                    }
                    return new InvoiceFileTransactionApiDto(
                            fileTransactionId,
                            invoiceFileTransaction.getFileTransactionStatus().name()
                    );
                });
    }
}