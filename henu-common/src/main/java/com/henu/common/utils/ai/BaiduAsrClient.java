package com.henu.common.utils.ai;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Base64;
import java.util.Comparator;
import java.util.concurrent.TimeUnit;

@Component
public class BaiduAsrClient {
    private static final Logger log = LoggerFactory.getLogger(BaiduAsrClient.class);

    @Value("${ai.baidu.api-key:}")
    private String apiKey;

    @Value("${ai.baidu.secret-key:}")
    private String secretKey;

    private final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .build();

    private String getAccessToken() throws Exception {
        String url = "https://aip.baidubce.com/oauth/2.0/token?grant_type=client_credentials"
                + "&client_id=" + apiKey
                + "&client_secret=" + secretKey;

        Request request = new Request.Builder()
                .url(url)
                .post(RequestBody.create(MediaType.parse("application/json"), ""))
                .build();

        try (Response response = client.newCall(request).execute()) {
            String respBody = response.body().string();
            if (!response.isSuccessful()) {
                log.error("获取百度 Access Token 失败: HttpCode={}, Body={}, apiKey={}, secretKey={}",
                        response.code(), respBody, apiKey, secretKey);
                throw new RuntimeException("获取百度 Access Token 失败: " + response.code() + " 详情: " + respBody);
            }
            JSONObject jsonObject = JSON.parseObject(respBody);
            return jsonObject.getString("access_token");
        }
    }

    /**
     * 批量提取分割后的音频块文本结果并合并
     */
    public String transcribeAudioChunks(String audioDir) {
        log.info("准备调用百度短语音分片转写, Key状态: apiKey={}, secretKey={}",
                (apiKey != null ? apiKey.substring(0, Math.min(6, apiKey.length())) + "***" : "null"),
                (secretKey != null ? "***" : "null"));

        if (apiKey == null || apiKey.trim().isEmpty() || secretKey == null || secretKey.trim().isEmpty()) {
            throw new RuntimeException("百度智能云 ASR 密钥未配置 (注入失败或为空配置)");
        }

        try {
            String token = getAccessToken();
            log.info("成功获取百度 ASR Access Token");

            File dir = new File(audioDir);
            if (!dir.exists() || !dir.isDirectory()) {
                throw new RuntimeException("音频片段目录不存在: " + audioDir);
            }

            File[] audioFiles = dir.listFiles((d, name) -> name.endsWith(".wav"));
            if (audioFiles == null || audioFiles.length == 0) {
                throw new RuntimeException("没有找到可供识别的纯音频切片");
            }

            // 按 _000, _001 顺序排序
            Arrays.sort(audioFiles, Comparator.comparing(File::getName));

            StringBuilder fullTranscript = new StringBuilder();

            for (File file : audioFiles) {
                log.info("正在转写百度 ASR 音频切片: {}", file.getName());
                byte[] audioData = Files.readAllBytes(file.toPath());
                String base64Audio = Base64.getEncoder().encodeToString(audioData);

                JSONObject payload = new JSONObject();
                payload.put("format", "wav");
                payload.put("rate", 16000);
                payload.put("dev_pid", 1537); // MANDATORY: 普通话输入法模型
                payload.put("channel", 1);
                payload.put("cuid", "henu_experiment_platform");
                payload.put("token", token);
                payload.put("speech", base64Audio);
                payload.put("len", audioData.length);

                RequestBody requestBody = RequestBody.create(
                        MediaType.parse("application/json; charset=utf-8"),
                        payload.toJSONString());

                Request request = new Request.Builder()
                        .url("http://vop.baidu.com/server_api")
                        .post(requestBody)
                        .build();

                try (Response response = client.newCall(request).execute()) {
                    if (!response.isSuccessful()) {
                        log.error("百度 ASR 请求失败，状态码: {}", response.code());
                        continue;
                    }

                    String respBody = response.body().string();
                    JSONObject resultJson = JSON.parseObject(respBody);

                    int errNo = resultJson.getIntValue("err_no");
                    if (errNo == 0) {
                        String text = resultJson.getJSONArray("result").getString(0);
                        fullTranscript.append(text).append(" ");
                        log.info("切片 {} 转写成功: {}", file.getName(), text);
                    } else if (errNo == 3301 || errNo == 3302) {
                        log.warn("切片 {} 无有效语音或质量差 (err_no={}), 已按静音处理", file.getName(), errNo);
                    } else {
                        log.error("切片 {} 转写异常: {}", file.getName(), respBody);
                        throw new RuntimeException("百度 ASR 异常: " + respBody);
                    }
                }
            }

            return fullTranscript.toString();

        } catch (Exception e) {
            log.error("百度 ASR 识别全流程异常", e);
            throw new RuntimeException("百度 ASR 识别失败: " + e.getMessage(), e);
        }
    }
}
