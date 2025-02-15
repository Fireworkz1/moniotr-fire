package group.fire_monitor.service.prometheus;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Objects;


public class PrometheusResponseParser {

    public static PrometheusResponse parse(String json)  {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            PrometheusResponse response = objectMapper.readValue(json, PrometheusResponse.class);
            if (Objects.equals(response.getStatus(), "error")){
                throw new RuntimeException("prometheus解析失败");
            }
            if (response.getData().getResult().isEmpty()){
                throw new RuntimeException("未找到数据,请检查prometheus配置");
            }
            return response;
        } catch (Exception e){
            throw new RuntimeException("json解析失败");
        }

    }
}
