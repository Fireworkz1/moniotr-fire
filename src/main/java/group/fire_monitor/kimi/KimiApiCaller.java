package group.fire_monitor.kimi;


import okhttp3.*;
import org.apache.http.client.HttpClient;
import org.springframework.stereotype.Component;

import java.io.IOException;


@Component
public class KimiApiCaller {
    private static final String API_URL = "https://api.moonshot.cn/v1/chat/completions";
    // 替换为你的实际 API Key
    private static final String API_KEY = "sk-70Sym6lVCUg46lbmHEb2n7Ohsd1RcnwlwzuHLvUZYinWN7iB";
    // 替换为你的实际 Secret Key



    public String getKimiResponse(String prompt){
        // 构建请求数据（JSON 格式）

        String jsonPayload = "{"
                + "\"model\": \"moonshot-v1-32k\","
                + "\"messages\": ["
                + "    { \"role\": \"user\", \"content\": \"" + prompt.replace("\n", "\\n") + "\" }"
                + "]"
                + "}";

        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS) // 设置连接超时时间为 30 秒
                .writeTimeout(30, java.util.concurrent.TimeUnit.SECONDS) // 设置写超时时间为 30 秒
                .readTimeout(30, java.util.concurrent.TimeUnit.SECONDS) // 设置读超时时间为 30 秒
                .build();
        RequestBody body = RequestBody.create(jsonPayload, MediaType.parse("application/json; charset=utf-8"));
        Request request = new Request.Builder()
                .url(API_URL)
                .header("Authorization", "Bearer " + API_KEY)
                .header("Content-Type", "application/json")
                .post(body)
                .build();
        System.out.println(jsonPayload);
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected code " + response);
            }
            String responseBody = response.body().string();
            return responseBody;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}