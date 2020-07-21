package ru.itis.telegrambot;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;

import com.microsoft.azure.functions.annotation.*;
import com.microsoft.azure.functions.*;
import org.json.JSONObject;

/**
 * Azure Functions with HTTP Trigger.
 */
public class DaDataFunction {

    HttpURLConnection connection = null;

    /**
     * This function listens at endpoint "/api/HttpTrigger-Java". Two ways to invoke it using "curl" command in bash:
     * 1. curl -d "HTTP Body" {your host}/api/HttpTrigger-Java
     * 2. curl {your host}/api/HttpTrigger-Java?name=HTTP%20Query
     */
    @FunctionName("daDataFunction")
    public HttpResponseMessage run(
            @HttpTrigger(name = "req", methods = {HttpMethod.GET, HttpMethod.POST}, authLevel = AuthorizationLevel.ANONYMOUS) HttpRequestMessage<Optional<String>> request,
            final ExecutionContext context) {
        context.getLogger().info("Java HTTP trigger processed a request.");
        String token = "30b33d2dc1557485a4de5e581c60b39d68aaf3a0";
        String input = request.getQueryParameters().get("input");
        try {
            URL url = new URL(
                    "https://suggestions.dadata.ru/suggestions/api/4_1/rs/findById/party");
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Accept", "application/json");
            connection.setRequestProperty("Authorization", "Token " + token);
            connection.setUseCaches(false);
            connection.setDoOutput(true);
            String json = "{\"query\": \"" + input +"\"}";
            byte[] out = json.getBytes(StandardCharsets.UTF_8);
            int length = out.length;
            connection.setFixedLengthStreamingMode(length);
            connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            connection.connect();
            try(OutputStream os = connection.getOutputStream()) {
                os.write(out);
            }
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(),
                    StandardCharsets.UTF_8))) {
                String output = "";
                String line;
                while ((line = reader.readLine()) != null) {
                    output = output + line + "\n";
                }
                reader.close();
                return request.createResponseBuilder(HttpStatus.OK).body(output).build();
            }
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }
}
