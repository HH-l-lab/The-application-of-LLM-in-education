package com.henu.common.utils.ai;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

@Component
public class DeepSeekClient {
    private static final Logger log = LoggerFactory.getLogger(DeepSeekClient.class);

    @Value("${ai.deepseek.api-key:}")
    private String apiKey;

    @Value("${ai.deepseek.base-url:https://api.deepseek.com}")
    private String baseUrl;

    private final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .readTimeout(180, TimeUnit.SECONDS)
            .build();

    public String generateText(String systemPrompt, String userPrompt) {
        if (apiKey == null || apiKey.isEmpty()) {
            throw new RuntimeException("DeepSeek API Key is not configured in application.yml");
        }

        JSONObject requestBody = new JSONObject();
        // Use deepseek-chat or deepseek-reasoner based on needs
        requestBody.put("model", "deepseek-chat");

        JSONArray messages = new JSONArray();
        if (systemPrompt != null && !systemPrompt.isEmpty()) {
            JSONObject sysMessage = new JSONObject();
            sysMessage.put("role", "system");
            sysMessage.put("content", systemPrompt);
            messages.add(sysMessage);
        }

        JSONObject userMessage = new JSONObject();
        userMessage.put("role", "user");
        userMessage.put("content", userPrompt);
        messages.add(userMessage);

        requestBody.put("messages", messages);
        requestBody.put("temperature", 0.7);

        MediaType mediaType = MediaType.parse("application/json; charset=utf-8");
        RequestBody body = RequestBody.create(mediaType, requestBody.toJSONString());

        Request request = new Request.Builder()
                .url(baseUrl + "/v1/chat/completions")
                .post(body)
                .addHeader("Authorization", "Bearer " + apiKey)
                .addHeader("Content-Type", "application/json")
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                String errorBody = response.body() != null ? response.body().string() : "";
                log.error("DeepSeek API request failed: {}, body: {}", response, errorBody);
                throw new RuntimeException("DeepSeek API request failed: " + response.code());
            }
            if (response.body() != null) {
                String responseString = response.body().string();
                JSONObject jsonResponse = JSON.parseObject(responseString);
                return jsonResponse.getJSONArray("choices")
                        .getJSONObject(0)
                        .getJSONObject("message")
                        .getString("content");
            }
        } catch (IOException e) {
            log.error("Error communicating with DeepSeek API", e);
            throw new RuntimeException("Error communicating with DeepSeek API", e);
        }
        return null;
    }

    /**
     * Sends a streaming request to DeepSeek and consumes the tokens as they arrive.
     * 
     * @param systemPrompt The expert persona instructions.
     * @param userMessage  The user's specific query or JSON data form.
     * @param onToken      Consumer callback that fires every time a new token word
     *                     arrives.
     * @param onComplete   Runnable callback that fires when the stream is fully
     *                     finished.
     * @param onError      Consumer callback for errors.
     */
    public void streamChat(String systemPrompt, String userMessage,
            java.util.function.Consumer<String> onToken,
            Runnable onComplete,
            java.util.function.Consumer<Exception> onError) {
        if (apiKey == null || apiKey.isEmpty()) {
            if (onError != null)
                onError.accept(new RuntimeException("API Key missing"));
            return;
        }

        JSONObject requestBody = new JSONObject();
        requestBody.put("model", "deepseek-chat");
        requestBody.put("stream", true);

        JSONArray messages = new JSONArray();
        if (systemPrompt != null && !systemPrompt.isEmpty()) {
            JSONObject sysMessage = new JSONObject();
            sysMessage.put("role", "system");
            sysMessage.put("content", systemPrompt);
            messages.add(sysMessage);
        }

        JSONObject usrMessage = new JSONObject();
        usrMessage.put("role", "user");
        usrMessage.put("content", userMessage);
        messages.add(usrMessage);

        requestBody.put("messages", messages);
        requestBody.put("temperature", 0.7);

        MediaType mediaType = MediaType.parse("application/json; charset=utf-8");
        RequestBody body = RequestBody.create(mediaType, requestBody.toJSONString());

        Request request = new Request.Builder()
                .url(baseUrl + "/v1/chat/completions")
                .post(body)
                .addHeader("Authorization", "Bearer " + apiKey)
                .addHeader("Content-Type", "application/json")
                .build();

        client.newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(okhttp3.Call call, IOException e) {
                if (onError != null)
                    onError.accept(e);
            }

            @Override
            public void onResponse(okhttp3.Call call, okhttp3.Response response) throws IOException {
                if (!response.isSuccessful()) {
                    if (onError != null)
                        onError.accept(new IOException("Unexpected code " + response));
                    return;
                }

                try (okhttp3.ResponseBody responseBody = response.body()) {
                    if (responseBody == null)
                        return;
                    okio.BufferedSource source = responseBody.source();
                    while (!source.exhausted()) {
                        String line = source.readUtf8LineStrict();
                        if (line.isEmpty())
                            continue;
                        if (line.equals("data: [DONE]")) {
                            if (onComplete != null)
                                onComplete.run();
                            break;
                        }
                        if (line.startsWith("data: ")) {
                            String data = line.substring(6);
                            try {
                                JSONObject json = JSON.parseObject(data);
                                JSONArray choices = json.getJSONArray("choices");
                                if (choices != null && !choices.isEmpty()) {
                                    JSONObject delta = choices.getJSONObject(0).getJSONObject("delta");
                                    if (delta != null && delta.containsKey("content")) {
                                        String content = delta.getString("content");
                                        if (onToken != null && content != null) {
                                            onToken.accept(content);
                                        }
                                    }
                                }
                            } catch (Exception e) {
                                // Ignore partial JSON parsing errors during stream
                            }
                        }
                    }
                }
            }
        });
    }
}
