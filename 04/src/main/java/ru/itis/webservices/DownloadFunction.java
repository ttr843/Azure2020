package ru.itis.webservices;

import java.io.File;
import java.util.*;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.microsoft.azure.functions.annotation.*;
import com.microsoft.azure.functions.*;

/**
 * Azure Functions with HTTP Trigger.
 */
public class DownloadFunction {
    /**
     * This function listens at endpoint "/api/DownloadFunction". Two ways to invoke it using "curl" command in bash:
     * 1. curl -d "HTTP Body" {your host}/api/DownloadFunction
     * 2. curl {your host}/api/DownloadFunction?name=HTTP%20Query
     */
    @FunctionName("DownloadFunction")
    public HttpResponseMessage run(
            @HttpTrigger(name = "req", methods = {HttpMethod.GET, HttpMethod.POST}, authLevel = AuthorizationLevel.ANONYMOUS) HttpRequestMessage<Optional<String>> request,
            final ExecutionContext context) {
        context.getLogger().info("Java HTTP trigger processed a request.");
        String query = request.getQueryParameters().get("name");
        String name = request.getBody().orElse(query);
        String connectStr = System.getenv("AZURE_STORAGE_CONNECTION_STRING");
        BlobServiceClient blobServiceClient = new BlobServiceClientBuilder()
                .connectionString(connectStr).buildClient();
        BlobContainerClient containerClient = blobServiceClient.getBlobContainerClient("files");
        BlobClient blobClient = containerClient.getBlobClient("name");
        File downloadedFile = new File("E:\\files", name);
        blobClient.downloadToFile(downloadedFile.getAbsolutePath());
        blobClient.delete();
        return request.createResponseBuilder(HttpStatus.OK).body("download and deleted file").build();
    }
}
