package group.fire_monitor.prometheus;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.util.Objects;


public class PrometheusResponseParser {

    public static PrometheusResponse parse(String json) throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();

            PrometheusResponse response = objectMapper.readValue(json, PrometheusResponse.class);
            if (Objects.equals(response.getStatus(), "error")){
                throw new Exception("prometheus解析失败");
            }
        if (response.getData().getResult().isEmpty()){
            throw new Exception("未找到数据");
        }
            return response;
    }
}
