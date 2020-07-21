package ru.itis.webservices;


import java.io.IOException;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.util.*;
import java.util.logging.Logger;


import com.microsoft.azure.functions.annotation.*;
import com.microsoft.azure.functions.*;
import com.microsoft.azure.storage.CloudStorageAccount;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.blob.CloudBlobClient;
import com.microsoft.azure.storage.blob.CloudBlobContainer;
import com.microsoft.azure.storage.blob.CloudBlockBlob;
import org.apache.tika.mime.MimeType;
import org.apache.tika.mime.MimeTypeException;
import org.apache.tika.mime.MimeTypes;

/**
 * Azure Functions with HTTP Trigger.
 */
public class UploadFunction {
    /**
     * This function listens at endpoint "/api/HttpTrigger-Java". Two ways to invoke it using "curl" command in bash:
     * 1. curl -d "HTTP Body" {your host}/api/HttpTrigger-Java
     * 2. curl {your host}/api/HttpTrigger-Java?name=HTTP%20Query
     */
    @FunctionName("uploadFile")
    public HttpResponseMessage run(@HttpTrigger(name = "req", methods = {
            HttpMethod.POST}, authLevel = AuthorizationLevel.ANONYMOUS) HttpRequestMessage<Optional<byte[]>> request,
                                   final ExecutionContext context) {
        final Logger logger = context.getLogger();
        logger.info("Upload function triggered.");
        final byte[] body = request.getBody().orElseThrow(() -> new IllegalArgumentException("No content attached"));
        final String contentType = request.getHeaders().get("content-type");
        String connectStr = System.getenv("AZURE_STORAGE_CONNECTION_STRING");
        try {
            MimeTypes mimeTypes = MimeTypes.getDefaultMimeTypes();
            MimeType mimeType = mimeTypes.forName(contentType);
            String extension = mimeType.getExtension();
            String filename = UUID.randomUUID() + extension;
            CloudStorageAccount storageAccount = CloudStorageAccount.parse(connectStr);
            CloudBlobClient blobClient = storageAccount.createCloudBlobClient();
            CloudBlobContainer blobContainer = blobClient.getContainerReference("files");
            CloudBlockBlob blockBlob = blobContainer.getBlockBlobReference(filename);
            blockBlob.uploadFromByteArray(body, 0, body.length);
            return request.createResponseBuilder(HttpStatus.OK)
                    .body("https://azuretask3.azurewebsites.net/api/DownloadFunction?name=" + filename).build();
        } catch (URISyntaxException | InvalidKeyException e) {
            return request.createResponseBuilder(HttpStatus.BAD_REQUEST).body("Connection string error").build();
        } catch (StorageException e) {
            return request.createResponseBuilder(HttpStatus.BAD_REQUEST).body("No container error").build();
        } catch (IOException e) {
            return request.createResponseBuilder(HttpStatus.BAD_REQUEST).body("Illegal State error").build();
        } catch (MimeTypeException e) {
            return request.createResponseBuilder(HttpStatus.BAD_REQUEST).body("Illegal content-type").build();
        }
    }
}

