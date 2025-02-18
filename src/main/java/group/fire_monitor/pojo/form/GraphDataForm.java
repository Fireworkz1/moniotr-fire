package group.fire_monitor.pojo.form;

import lombok.Data;

import java.util.Date;

@Data
public class GraphDataForm {
    Integer monitorId;
    Date startTime;
    Date endTime;
}
