package group.fire_monitor.prometheus;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.util.Objects;


public class PrometheusResponseParser {

    public static PrometheusResponse parse(String json) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();

            PrometheusResponse response = objectMapper.readValue(json, PrometheusResponse.class);
            if (Objects.equals(response.getStatus(), "error")){
                throw new RuntimeException("prometheus解析失败");
            }
            return response;
    }
}
