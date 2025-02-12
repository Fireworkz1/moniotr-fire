package group.fire_monitor.pojo.res;

import lombok.Data;

import java.util.Date;

@Data
public class ContainerDetailRes {
    private String status;
    private String startedAt;
    private String name;
    private String containerId;
    private String platform;
}
