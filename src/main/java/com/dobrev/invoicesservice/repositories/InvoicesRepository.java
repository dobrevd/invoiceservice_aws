package com.dobrev.invoicesservice.repositories;

import com.amazonaws.xray.spring.aop.XRayEnabled;
import com.dobrev.invoicesservice.invoices.dto.InvoiceFileDto;
import com.dobrev.invoicesservice.models.Invoice;
import com.dobrev.invoicesservice.models.InvoiceProduct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.core.async.SdkPublisher;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbAsyncTable;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedAsyncClient;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.Page;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryEnhancedRequest;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Repository
@XRayEnabled
public class InvoicesRepository {
    private static final String PARTITION_KEY = "#invoice_";
    private final DynamoDbEnhancedAsyncClient dynamoDbEnhancedAsyncClient;
    private final DynamoDbAsyncTable<Invoice> invoiceTable;

    public InvoicesRepository(@Value("${invoices.ddb.name}") String invoicesDdbName,
                              DynamoDbEnhancedAsyncClient dynamoDbEnhancedAsyncClient) {
        this.dynamoDbEnhancedAsyncClient = dynamoDbEnhancedAsyncClient;
        invoiceTable = dynamoDbEnhancedAsyncClient.table(invoicesDdbName, TableSchema.fromBean(Invoice.class));
    }

    public CompletableFuture<Void> createInvoice(InvoiceFileDto invoiceFileDto, String invoiceTransactionId,
                                                 String invoiceFileTransactionId) {
        long timestamp = Instant.now().toEpochMilli();

        Invoice invoice = new Invoice();
        invoice.setPk(generateInvoicePartitionKey(invoiceFileDto.customerEmail()));    //#invoice_customerEmail
        invoice.setSk(invoiceFileDto.invoiceNumber());                          //invoiceNumber
        invoice.setTotalValue(invoiceFileDto.totalValue());
        invoice.setProducts(getInvoiceProductList(invoiceFileDto));
        invoice.setInvoiceTransactionId(invoiceTransactionId);
        invoice.setFileTransactionId(invoiceFileTransactionId);
        invoice.setCreatedAt(timestamp);
        invoice.setTtl(0L);

        return invoiceTable.putItem(invoice);
    }

    public SdkPublisher<Page<Invoice>> findByCustomerEmail(String email){
        String key = generateInvoicePartitionKey(email);
        return invoiceTable.query(QueryEnhancedRequest.builder()
                        .queryConditional(QueryConditional.keyEqualTo(Key.builder().partitionValue(key).build()))
                .build());
    }

    private static String generateInvoicePartitionKey(String email) {
        return PARTITION_KEY.concat(email);
    }

    private static List<InvoiceProduct> getInvoiceProductList(InvoiceFileDto invoiceFileDto) {
        return invoiceFileDto.products().stream().map(invoiceProductFileDto -> {
            InvoiceProduct invoiceProduct = new InvoiceProduct();
            invoiceProduct.setId(invoiceProductFileDto.id());
            invoiceProduct.setQuantity(invoiceProductFileDto.quantity());
            return invoiceProduct;
        }).toList();
    }
}