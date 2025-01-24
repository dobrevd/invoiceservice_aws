package com.dobrev.invoicesservice.models;

import lombok.Getter;
import lombok.Setter;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSortKey;

import java.util.List;

@DynamoDbBean
@Getter
@Setter
public class Invoice {
    private String pk;              //#invoice_customerEmail
    private String sk;              //invoiceNumber
    private Float totalValue;
    private List<InvoiceProduct> products;
    private String invoiceTransactionId;
    private String fileTransactionId;
    private Long ttl;
    private Long createdAt;

    @DynamoDbPartitionKey
    public String getPk() {
        return pk;
    }
    @DynamoDbSortKey
    public String getSk() {
        return sk;
    }
}