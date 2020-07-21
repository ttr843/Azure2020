package ru.itis.azurefunction;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

import com.microsoft.azure.functions.annotation.*;
import com.microsoft.azure.functions.*;

/**
 * Azure Functions with HTTP Trigger.
 */
public class HttpTriggerFunction {

    HttpURLConnection connection = null;

    /**
     * This function listens at endpoint "/api/HttpTrigger-Java". Two ways to invoke it using "curl" command in bash:
     * 1. curl -d "HTTP Body" {your host}/api/HttpTrigger-Java
     * 2. curl {your host}/api/HttpTrigger-Java?name=HTTP%20Query
     */
    @FunctionName("check")
    public HttpResponseMessage run(
            @HttpTrigger(name = "req", methods =
                    {HttpMethod.GET, HttpMethod.POST}, authLevel = AuthorizationLevel.ANONYMOUS)
                    HttpRequestMessage<Optional<String>> request,
            final ExecutionContext context) {
        context.getLogger().info("Java HTTP trigger processed a request.");
        String latitude = request.getQueryParameters().get("lat");
        String longitude = request.getQueryParameters().get("lon");
        String radius = request.getQueryParameters().get("rad");
        String token = "30b33d2dc1557485a4de5e581c60b39d68aaf3a0";
        try {
            //Create connection
            URL url = new URL(
                    "https://suggestions.dadata.ru/suggestions/api/4_1/rs/geolocate/address?lat=" + latitude +
                            "&lon=" + longitude +
                            "&radius_meter=" + radius);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Content-Type",
                    "application/json");
            connection.setRequestProperty("Accept", "application/json");
            connection.setRequestProperty("Authorization", "Token " + token);
            connection.setUseCaches(false);
            connection.setDoOutput(true);
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(),
                    StandardCharsets.UTF_8))) {
                String out = "";
                String line;
                while ((line = reader.readLine()) != null) {
                    out = out + line + "\n";
                }
                reader.close();
                return request.createResponseBuilder(HttpStatus.OK).body(out).build();
            }
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }
}
