package group.fire_monitor.pojo.form;

import lombok.Data;

import java.util.Date;

@Data
public class KimiAnalyzeForm {
    Integer monitorId;
    Date startTime;
    Date endTime;
}
