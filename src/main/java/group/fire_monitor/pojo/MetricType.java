package group.fire_monitor.pojo;

import lombok.Data;

@Data
public class MetricType {
    private String type;
    private String target;

    public MetricType(String type, String target) {
        this.type = type;
        this.target = target;
    }

}