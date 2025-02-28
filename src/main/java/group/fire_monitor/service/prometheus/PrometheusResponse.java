package group.fire_monitor.service.prometheus;

import com.fasterxml.jackson.annotation.JsonProperty;
import group.fire_monitor.pojo.PrometheusResult;
import lombok.Data;
import org.springframework.data.geo.Metric;

import java.sql.Timestamp;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Data
public class PrometheusResponse {
    private String status;
    private Data data;

    @lombok.Data
    public static class Data {
        private String resultType;
        private List<PrometheusResult> result;

    }

    public PrometheusResult getSingleResult()  {
        checkStatus();
        return this.getData().getResult().get(0);
    }
    public List<PrometheusResult> getResults()  {
        checkStatus();
        return this.getData().getResult();
    }
    private void checkStatus()  {
        if (Objects.equals(this.getStatus(), "error")) throw new RuntimeException("prometheus查询错误");
    }

    public String getSingleValue() {
        PrometheusResult result= getSingleResult();
        return (String) result.getValue().get(1);
    }
    public Timestamp getSingleTimestamp()  {
        PrometheusResult result = getSingleResult();
        if (result == null || result.getValue() == null || result.getValue().size() < 1) {
            throw new IllegalArgumentException("Invalid result or value list");
        }

        Object timestampObj = result.getValue().get(0);
        if (!(timestampObj instanceof Double)) {
            throw new IllegalArgumentException("Timestamp value is not a Double");
        }

        // 将 Double 类型的时间戳转换为 long 类型
        Double timestampDouble = (Double) timestampObj;
        long timestampLong = timestampDouble.longValue();

        // 创建 Timestamp 对象
        return new Timestamp(timestampLong);
    }
}
