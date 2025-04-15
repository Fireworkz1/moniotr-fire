package group.fire_monitor.pojo;

import lombok.Data;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

@Data
public class PrometheusResult {
    private Metric metric;
    private List<Object> value;
    private List<List<Object>> values; // 用于时间序列
    @lombok.Data
    public static class Metric{
        private String __name__;
        private String instance;
        private String job;
        private String area;
        private String id;

        private String device;
        private String domainname;
        private String machine;
        private String nodename;
        private String sysname;
        private String release;
        private String version;
        private String cmd;
    }
}
