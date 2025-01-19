package com.dobrev.invoicesservice.repositories;

import com.amazonaws.xray.spring.aop.XRayEnabled;
import com.dobrev.invoicesservice.models.InvoiceTransaction;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbAsyncTable;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedAsyncClient;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;

@XRayEnabled
@Repository
public class InvoiceTransactionRepository {
    private static final String PARTITION_KEY = "#invoiceTransaction_";
    private final DynamoDbEnhancedAsyncClient dynamoDbEnhancedAsyncClient;
    private final DynamoDbAsyncTable<InvoiceTransaction> invoiceTransactionTable;

    public InvoiceTransactionRepository(@Value("${invoices.ddb.name}") String invoicesDdbName,
                                              DynamoDbEnhancedAsyncClient dynamoDbEnhancedAsyncClient) {
        this.dynamoDbEnhancedAsyncClient = dynamoDbEnhancedAsyncClient;
        invoiceTransactionTable = dynamoDbEnhancedAsyncClient.table(invoicesDdbName,
                TableSchema.fromBean(InvoiceTransaction.class));
    }


}
