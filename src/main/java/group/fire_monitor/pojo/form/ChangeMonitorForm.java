package group.fire_monitor.pojo.form;

import lombok.Data;

import java.util.List;

@Data
public class ChangeMonitorForm {
    private Integer monitorId;
    private List<Integer> newResourceIdList;
}
