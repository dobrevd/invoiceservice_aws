package com.dobrev.invoicesservice.models;

import lombok.Getter;
import lombok.Setter;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;

@DynamoDbBean
@Getter
@Setter
public class InvoiceProduct {
    private String id;
    private int quantity;
}