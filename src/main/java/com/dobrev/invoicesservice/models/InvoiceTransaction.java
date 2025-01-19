package com.dobrev.invoicesservice.models;

import com.dobrev.invoicesservice.enums.InvoiceTransactionStatus;
import lombok.Getter;
import lombok.Setter;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSortKey;

@DynamoDbBean
@Getter
@Setter
public class InvoiceTransaction {
    private String pk;              //#invoiceTransaction_fileTransactionId
    private String sk;              //invoiceTransactionId
    private Long ttl;
    private Long createdAt;
    private String customerEmail;
    private String invoiceNumber;
    private InvoiceTransactionStatus transactionStatus;

    @DynamoDbPartitionKey
    public String getPk() {
        return pk;
    }
    @DynamoDbSortKey
    public String getSk() {
        return sk;
    }
}