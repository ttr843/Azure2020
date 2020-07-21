package ru.itis.azure;


import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.azure.storage.blob.models.BlobContainerItem;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Stream;

public class Main {


    public static void main(String[] args) {
        String connectStr = System.getenv("AZURE_STORAGE_CONNECTION_STRING");
        BlobServiceClient blobServiceClient = new BlobServiceClientBuilder()
                .connectionString(connectStr).buildClient();
        Date date = new Date();
        SimpleDateFormat formatForDateNow = new SimpleDateFormat("yyyy-MM-dd-hh-mm-ss");
        if (args.length == 0) {
            Iterator<BlobContainerItem> iterator = blobServiceClient.listBlobContainers().iterator();
            while (iterator.hasNext()) {
                System.out.println("Container: " + iterator.next().getName());
            }
        } else {
            BlobContainerClient containerClient = blobServiceClient.createBlobContainer(formatForDateNow.format(date));
            System.out.println("Create container with name: " + formatForDateNow.format(date));
            try (Stream<Path> filesPaths = Files.walk(Paths.get(args[0]))) {
                filesPaths.filter(filePath -> filePath.toFile().isFile()).forEach(
                        filePath -> {
                            File file = filePath.toFile();
                            BlobClient blobClient = containerClient.getBlobClient(file.getName());
                            blobClient.uploadFromFile(file.getAbsolutePath());
                            System.out.println(file.getName() + " file uploaded");
                        }
                );
            } catch (IOException e) {
                throw new IllegalArgumentException(e);
            }
            System.out.println("all files upload from directory: " + args[0]);
        }
    }
}
