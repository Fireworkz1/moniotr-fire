package group.fire_monitor.pojo;

import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class MonitorWithListPojo {
    private Integer id;
    private String monitorName;
    private String monitorDescription;
    private String monitorNotpresetPromql;
    private String monitorPresetTarget;
    private Integer monitorIspreset;
    private String monitorDemonstration;//table/graph
    private Integer monitorPermissionLevel;
    private List<Integer> monitorGroupIds;
    private List<Integer> monitorResourceIds;
    private String monitorType;
    private Date monitorAddedTime;
}
