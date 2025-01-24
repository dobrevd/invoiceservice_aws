package com.dobrev.invoicesservice.services;

import com.amazonaws.xray.spring.aop.XRayEnabled;
import com.dobrev.invoicesservice.invoices.dto.InvoiceApiDto;
import com.dobrev.invoicesservice.invoices.dto.InvoiceProductApiDto;
import com.dobrev.invoicesservice.invoices.dto.UrlResponseDto;
import com.dobrev.invoicesservice.repositories.InvoicesFileTransactionsRepository;
import com.dobrev.invoicesservice.repositories.InvoicesRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.ThreadContext;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@XRayEnabled
public class InvoicesServices {
    private final S3InvoicesService s3InvoicesService;
    private final InvoicesFileTransactionsRepository invoicesFileTransactionsRepository;
    private final InvoicesRepository invoicesRepository;

    public UrlResponseDto generatePreSignUrl(String requestId) {
        String transactionId = UUID.randomUUID().toString();
        int expiresIn = 300;
        ThreadContext.put("invoiceFileTransactionId", transactionId);

        String preSignedUrl = s3InvoicesService.generatePreSignedUrl(transactionId, expiresIn);
        invoicesFileTransactionsRepository.createInvoiceFileTransaction(transactionId, requestId, expiresIn).join();

        log.info("Invoice file transaction generated...");
        return new UrlResponseDto(preSignedUrl, expiresIn, requestId);
    }

    public List<InvoiceApiDto> findByCustomerEmail(String email){
        List<InvoiceApiDto> invoicesApiDto = new ArrayList<>();
        invoicesRepository.findByCustomerEmail(email).subscribe(invoicePage -> {
            invoicesApiDto.addAll(invoicePage.items().parallelStream()
                            .map(invoice -> new InvoiceApiDto(
                                    invoice.getPk().split("_")[1],
                                    invoice.getSk(),
                                    invoice.getTotalValue(),
                                    invoice.getProducts().parallelStream()
                                            .map(invoiceProduct -> new InvoiceProductApiDto(
                                                    invoiceProduct.getId(),
                                                    invoiceProduct.getQuantity()
                                            )).toList(),
                                    invoice.getInvoiceTransactionId(),
                                    invoice.getFileTransactionId(),
                                    invoice.getCreatedAt()
                            )).toList());
        }).join();
        return invoicesApiDto;
    }
}