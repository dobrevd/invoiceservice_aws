package com.dobrev.invoicesservice.invoices.dto;

public record InvoiceFileTransactionApiDto(
        String transactionId,
        String status
) {}