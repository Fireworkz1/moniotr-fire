package group.fire_monitor.service.prometheus;

import com.fasterxml.jackson.databind.ObjectMapper;

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
