package com.dobrev.invoicesservice.services;

import com.amazonaws.services.lambda.runtime.events.models.s3.S3EventNotification;
import com.amazonaws.services.lambda.runtime.serialization.PojoSerializer;
import com.amazonaws.services.lambda.runtime.serialization.events.LambdaEventSerializers;
import com.dobrev.invoicesservice.enums.InvoiceFileTransactionStatus;
import com.dobrev.invoicesservice.models.InvoiceFileTransaction;
import com.dobrev.invoicesservice.repositories.InvoicesFileTransactionsRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.ThreadContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.async.AsyncResponseTransformer;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectResponse;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;
import software.amazon.awssdk.services.sqs.model.DeleteMessageRequest;
import software.amazon.awssdk.services.sqs.model.Message;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
@Slf4j
public class S3InvoicesConsumer {
    private final S3AsyncClient s3AsyncClient;
    private final InvoicesFileTransactionsRepository invoicesFileTransactionsRepository;
    private final ObjectMapper objectMapper;
    private final SqsAsyncClient sqsAsyncClient;
    @Value("${aws.sqs.queue.invoice.events.url}")
    private final String invoiceEventsQueueUrl;
    private final ReceiveMessageRequest receiveMessageRequest;

    public S3InvoicesConsumer(S3AsyncClient s3AsyncClient, InvoicesFileTransactionsRepository invoicesFileTransactionsRepository,
            ObjectMapper objectMapper, SqsAsyncClient sqsAsyncClient, String invoiceEventsQueueUrl) {
        this.s3AsyncClient = s3AsyncClient;
        this.invoicesFileTransactionsRepository = invoicesFileTransactionsRepository;
        this.objectMapper = objectMapper;
        this.sqsAsyncClient = sqsAsyncClient;
        this.invoiceEventsQueueUrl = invoiceEventsQueueUrl;
        this.receiveMessageRequest = ReceiveMessageRequest.builder()
                .maxNumberOfMessages(5)
                .queueUrl(invoiceEventsQueueUrl)
                .build();
    }

    @Scheduled(fixedDelay = 1000)
    public void receiveInvoiceEventsMessage() {
        List<Message> messages;
        while (!(messages = sqsAsyncClient.receiveMessage(receiveMessageRequest).join().messages()).isEmpty()) {
            AtomicBoolean allInvoicesProcessed = new AtomicBoolean(true);
            log.info("Reading messages: {}", messages.size());

            messages.parallelStream().forEach(message -> {
                S3EventNotification eventNotification;
                log.info("Parsing S3 event message");
                PojoSerializer<S3EventNotification> s3EventNotificationPojoSerializer =
                        LambdaEventSerializers.serializerFor(S3EventNotification.class,
                                ClassLoader.getSystemClassLoader());
                eventNotification = s3EventNotificationPojoSerializer.fromJson(message.body());
                if (eventNotification == null || eventNotification.getRecords().isEmpty()) {
                    log.error("Failed to parse S3 event notification");
                    deleteMessage(message);
                    return;
                }
                log.info("Number of record: {}", eventNotification.getRecords().size());

                List<CompletableFuture<Boolean>> futures = new ArrayList<>();
                eventNotification.getRecords().parallelStream().forEach(s3EventNotificationRecord -> {
                    String key = s3EventNotificationRecord.getS3().getObject().getKey();
                    ThreadContext.put("invoiceFileTransactionId", key);
                    log.info("Invoice file transactionId: {}", key);

                    futures.add(this.processRecord(s3EventNotificationRecord));

                    ThreadContext.clearAll();
                });

                CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new)).join();
                allInvoicesProcessed.set(futures.stream().allMatch(CompletableFuture::join));

                log.info("All records were processed...");

                deleteMessage(message);

                if(allInvoicesProcessed.get()) {
                    log.info("All invoices processed");
                } else {
                    log.error("Some invoice file was not treated.");
                }

                log.info("Finish...");
            });
        }
    }

    private CompletableFuture<Boolean> processRecord(S3EventNotification.S3EventNotificationRecord record) {
        String key = record.getS3().getObject().getKey();
        log.info("Start processing the record - key: {}", key);
        String bucketName = record.getS3().getBucket().getName();

        //Get the invoice file transaction
        InvoiceFileTransaction invoiceFileTransaction = invoicesFileTransactionsRepository
                .getInvoiceFileTransaction(key).join();
        if ((invoiceFileTransaction == null) ||
                (!invoiceFileTransaction.getFileTransactionStatus().name()
                        .equals(InvoiceFileTransactionStatus.GENERATED.name()))) {
            log.error("Invoice file transaction not found or non valid transaction status - key: {}", key);
            return CompletableFuture.supplyAsync(() -> false);
        }

        //Get the S3 object (invoice file)
        log.info("Reading invoice file from S3 bucket...");
        ResponseInputStream<GetObjectResponse> s3Object = this.s3AsyncClient.getObject(GetObjectRequest.builder()
                .key(key)
                .bucket(bucketName)
                .build(), AsyncResponseTransformer.toBlockingInputStream()).join();

        //Update the invoice file transaction status
        invoicesFileTransactionsRepository.updateInvoiceFileTransactionStatus(key,
                InvoiceFileTransactionStatus.FILE_RECEIVED).join();

        int invoiceCount = 0;
        try (s3Object; BufferedReader reader = new BufferedReader(new InputStreamReader(s3Object))){
            //Process each invoice from the file (also update the invoice transaction status)
            String line;
            while ((line = reader.readLine()) != null) {
                processInvoice(line, invoiceFileTransaction).join();
                log.info("Invoice processed...");
                invoiceCount++;
            }
            //Delete the object from the S3 bucket
            log.info("Deleting the invoice file from S3...");
            CompletableFuture<DeleteObjectResponse> deleteObjectResponseCompletableFuture =
                    this.s3AsyncClient.deleteObject(DeleteObjectRequest.builder()
                            .bucket(bucketName)
                            .key(key)
                            .build());

            //Update the invoice file transaction status
            CompletableFuture<InvoiceFileTransaction> invoiceFileTransactionCompletableFuture =
                    this.invoicesFileTransactionsRepository
                            .updateInvoiceFileTransactionStatus(key, InvoiceFileTransactionStatus.FILE_PROCESSED);

            CompletableFuture.allOf(deleteObjectResponseCompletableFuture, invoiceFileTransactionCompletableFuture).join();
        } catch (IOException e) {
            log.error("Failed to read invoice file");
            invoicesFileTransactionsRepository.updateInvoiceFileTransactionStatus(key,
                    InvoiceFileTransactionStatus.ERROR).join();
            return CompletableFuture.supplyAsync(() -> false);
        }

        log.info("Number of invoices processed: {}", invoiceCount);

        return CompletableFuture.supplyAsync(() -> true);
    }


    private void deleteMessage(Message message){
        sqsAsyncClient.deleteMessage(DeleteMessageRequest.builder()
                        .queueUrl(invoiceEventsQueueUrl)
                        .receiptHandle(message.receiptHandle())
                .build()).join();
        log.info("Messages deleted...");
    }

}
