package com.dobrev.invoicesservice.repositories;

import com.amazonaws.xray.spring.aop.XRayEnabled;
import com.dobrev.invoicesservice.enums.InvoiceFileTransactionStatus;
import com.dobrev.invoicesservice.models.InvoiceFileTransaction;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbAsyncTable;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedAsyncClient;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.GetItemEnhancedRequest;

import java.time.Instant;
import java.util.concurrent.CompletableFuture;

@XRayEnabled
@Repository
public class InvoicesFileTransactionsRepository {
    private static final String PARTITION_KEY = "#fileTransaction";
    private final DynamoDbEnhancedAsyncClient dynamoDbEnhancedAsyncClient;
    private final DynamoDbAsyncTable<InvoiceFileTransaction> invoiceFileTransactionTable;

    public InvoicesFileTransactionsRepository(@Value("${invoices.ddb.name}") String invoicesDdbName,
                                              DynamoDbEnhancedAsyncClient dynamoDbEnhancedAsyncClient) {
        this.dynamoDbEnhancedAsyncClient = dynamoDbEnhancedAsyncClient;
        invoiceFileTransactionTable = dynamoDbEnhancedAsyncClient.table(invoicesDdbName,
                TableSchema.fromBean(InvoiceFileTransaction.class));
    }

    public CompletableFuture<Void> createInvoiceFileTransaction(String transactionId, String requestId, int expiresIn) {
        long timestamp = Instant.now().toEpochMilli();
        long ttl = Instant.now().plusSeconds(300).getEpochSecond();

        InvoiceFileTransaction invoiceFileTransaction = new InvoiceFileTransaction();
        invoiceFileTransaction.setPk(PARTITION_KEY);
        invoiceFileTransaction.setSk(transactionId);
        invoiceFileTransaction.setTtl(ttl);
        invoiceFileTransaction.setRequestId(requestId);
        invoiceFileTransaction.setCreatedAt(timestamp);
        invoiceFileTransaction.setExpiresIn(expiresIn);
        invoiceFileTransaction.setFileTransactionStatus(InvoiceFileTransactionStatus.GENERATED);
        return invoiceFileTransactionTable.putItem(invoiceFileTransaction);
    }

    public CompletableFuture<InvoiceFileTransaction> updateInvoiceFileTransactionStatus(
            String transactionId, InvoiceFileTransactionStatus status) {

       return getInvoiceFileTransaction(transactionId)
                .thenApply(invoiceFileTransaction -> {
                    if (invoiceFileTransaction == null) {
                        throw new IllegalArgumentException("InvoiceFileTransaction not found");
                    }
                    invoiceFileTransaction.setFileTransactionStatus(status);
                    return invoiceFileTransaction;
                })
               .thenCompose(invoiceFileTransactionTable::updateItem);
    }

    public CompletableFuture<InvoiceFileTransaction> getInvoiceFileTransaction  (String transactionId) {
        return invoiceFileTransactionTable.getItem(GetItemEnhancedRequest.builder()
                .key(Key.builder()
                        .partitionValue(PARTITION_KEY)
                        .sortValue(transactionId)
                        .build())
                .build());
    }
}