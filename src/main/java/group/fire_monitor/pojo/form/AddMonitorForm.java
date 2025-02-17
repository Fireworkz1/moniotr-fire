package group.fire_monitor.pojo.form;

import lombok.Data;

import java.util.List;

@Data
public class AddMonitorForm {

    private String monitorName;
    private String monitorNotpresetPromql;
    private String monitorPresetTarget;
    private String monitorDescription;
    private String monitorDemonstration;//table/graph
    private List<Integer> monitorGroupIds;
    private List<Integer> monitorResourceIds;
    private String monitorType;
}
