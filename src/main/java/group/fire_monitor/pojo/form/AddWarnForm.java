package group.fire_monitor.pojo.form;

import lombok.Data;

import java.util.List;

@Data
public class AddWarnForm {
    private String warnName;
    private Integer warnLevel;
    private String warnSource;
    private String warnSourceType;
    private String compareType;
    private Double warnThreshold;
//    private Integer warnRepeatTimes;
    private String warnDescription;
    private Integer monitorId;
    private String monitorName;
    List<Integer> noticeUserIds;
}
