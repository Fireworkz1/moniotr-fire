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

    public PrometheusResult getSingleResult() throws Exception {
        checkStatus();
        return this.getData().getResult().get(0);
    }
    public List<PrometheusResult> getResults() throws Exception {
        checkStatus();
        return this.getData().getResult();
    }
    private void checkStatus() throws Exception {
        if (Objects.equals(this.getStatus(), "error")) throw new Exception("prometheus查询错误");
    }

    public String getSingleValue() throws Exception {
        PrometheusResult result= getSingleResult();
        return (String) result.getValue().get(1);
    }
    public Timestamp getSingleTimestamp() throws Exception {
        PrometheusResult result= getSingleResult();
        return (Timestamp) result.getValue().get(0);
    }
}
