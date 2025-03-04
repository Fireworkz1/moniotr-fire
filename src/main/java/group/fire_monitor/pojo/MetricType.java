package group.fire_monitor.pojo;

import lombok.Data;

@Data
public class MetricType {
    private String type;
    private String target;
    private String description; // 新增字段

    // 修改构造函数以包含 description
    public MetricType(String type, String target, String description) {
        this.type = type;
        this.target = target;
        this.description = description;
    }



}