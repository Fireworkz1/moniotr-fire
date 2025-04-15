package group.fire_monitor.kimi;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import group.fire_monitor.pojo.res.KimiAnalyzeRes;
import org.springframework.stereotype.Component;

import java.io.IOException;
@Component
public class KimiApiResponseParser {
    public static KimiAnalyzeRes parse(String jsonResponse) {
        if (jsonResponse == null) {
            throw new RuntimeException("未能正确分析数据，请再次请求");
        }
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            JsonNode rootNode = objectMapper.readTree(jsonResponse);

            // 获取 choices 数组的第一个元素
            JsonNode choiceNode = rootNode.path("choices").get(0);
            // 获取 message.content 字段
            JsonNode messageNode = choiceNode.path("message").path("content");
            KimiAnalyzeRes result = new KimiAnalyzeRes();
            // 解析内容
            String content = messageNode.asText();
            String[] sections = content.split("\n### ");
            for (String section : sections) {
                if (section.startsWith("### 性能瓶颈分析")||section.startsWith("性能瓶颈分析")) {
                    String performanceBottleneck = section;
                    result.setPerformanceBottleneck(performanceBottleneck);
                } else if (section.startsWith("趋势预测")) {
                    String trendPrediction = section;
                    result.setTrendPrediction(trendPrediction);

                } else if (section.startsWith("优化建议")) {
                    String optimizationSuggestions = section;
                    result.setOptimizationSuggestions(optimizationSuggestions);

                }
            }




            return result;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("解析响应数据时出错");
        }
    }
}