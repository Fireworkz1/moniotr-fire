package group.fire_monitor.pojo;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@Data
@TableName("warn_policy")
public class WarnPolicy {
    private Integer id;
    private String warnName;
    private Integer warnLevel;
    private String warnSource;
    private String warnSourceType;
    private String compareType;
    private Double warnThreshold;
    private Integer warnRepeatTimes;
    private String warnDescription;
    private Integer monitorId;
    private String noticeUserIds;
    private Integer currentStatus;
    private Date lastWarningTime;
    private Integer isActive;
    private Integer hasSentNotice;
    private String noticeWay;

}
