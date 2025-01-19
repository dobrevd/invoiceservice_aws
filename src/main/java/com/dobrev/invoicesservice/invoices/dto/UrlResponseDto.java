package com.dobrev.invoicesservice.invoices.dto;

import software.amazon.awssdk.services.sqs.endpoints.internal.Value;

public record UrlResponseDto(
        String url,
        int expiresIn,
        String transactionId
) {}