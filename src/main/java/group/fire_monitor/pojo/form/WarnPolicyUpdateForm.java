package group.fire_monitor.pojo.form;

import lombok.Data;

import java.util.List;

@Data
public class WarnPolicyUpdateForm {
    private Integer id;
    private Integer monitorOn;
    private String warnName;
    private Integer warnLevel;
    private String warnSource;
    private String warnSourceType;
    private String compareType;
    private Double warnThreshold;
    private String warnDescription;
    private List<Integer> noticeGroupIds;
    private String noticeWay;
}
