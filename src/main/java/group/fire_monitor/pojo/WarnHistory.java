package group.fire_monitor.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@Data
@TableName("warn_policy_history")
public class WarnHistory {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private Integer warnPolicyId;
    private Integer monitorOn;
    private String warnName;
    private Integer warnLevel;
    private String warnSource;
    private String warnSourceType;
    private String compareType;
    private Double warnThreshold;
    private Integer warnRepeatTimes;
    private String warnDescription;
    private Integer monitorId;
    private String monitorName;
    private String noticeUserIds;
    private String currentStatus;
    private Date StartWarningTime;
    private Date lastWarningTime;
    private Integer isActive;
    private Integer hasSentNotice;
    private String noticeWay;

}
