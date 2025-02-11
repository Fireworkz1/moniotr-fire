package group.fire_monitor.prometheus;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.springframework.data.geo.Metric;

import java.util.List;
import java.util.Map;
@Data
public class PrometheusResponse {
    private String status;
    private Data data;

    @lombok.Data
    public static class Data {
        private String resultType;
        private List<Result> result;

    }
    @lombok.Data
    public static class Result {
        private Metric metric;
        private List<Object> value;
        private List<List<Object>> values; // 用于时间序列
        @lombok.Data
        public static class Metric{
            private String __name__;
            private String instance;
            private String job;
        }

    }

}
