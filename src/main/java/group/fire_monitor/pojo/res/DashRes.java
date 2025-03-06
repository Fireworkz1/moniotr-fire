package group.fire_monitor.pojo.res;

import lombok.Data;

@Data
public class DashRes {
    private Integer userNum;
    private Integer serverNum;
    private Integer softwareNum;
    private Integer warnNum;
    private Integer dbNum;
    private Integer cacheNum;
    private Integer hisWarningNum;
    private Integer totalWarningNum;
}
