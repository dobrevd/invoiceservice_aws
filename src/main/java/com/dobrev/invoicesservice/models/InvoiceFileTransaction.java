package com.dobrev.invoicesservice.models;

import com.dobrev.invoicesservice.enums.InvoiceFileTransactionStatus;
import lombok.Getter;
import lombok.Setter;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSortKey;

@DynamoDbBean
@Getter
@Setter
public class InvoiceFileTransaction {
    private String pk;                      //#fileTransaction
    private String sk;                      //file transaction id
    private String requestId;
    private Long createdAt;
    private Long ttl;
    private Integer expiresIn;
    private InvoiceFileTransactionStatus fileTransactionStatus;

    @DynamoDbPartitionKey
    public String getPk() {
        return pk;
    }
    @DynamoDbSortKey
    public String getSk() {
        return sk;
    }
}