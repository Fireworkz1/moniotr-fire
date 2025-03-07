package group.fire_monitor.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@Data
@TableName("warn_entity")
public class WarnEntity {

    private Integer monitorOn;
    private String warnName;
    private Integer warnLevel;
    private String warnSource;
    private String warnSourceType;
    private String compareType;
    private Double warnThreshold;
    private String warnDescription;
    private Integer monitorId;
    private String monitorName;
    private String noticeGroupIds;
    private String noticeWay;

    //Warn非公用字段
    @TableId(type = IdType.AUTO)
    private Integer id;
    private Integer warnPolicyId;
    private Integer warnRepeatTimes;
    private String currentStatus;
    private Date startWarningTime;
    private Date lastWarningTime;
    private Integer hasSentNotice;
    private Integer hasIgnored;
    private String currentWarningTarget;
}
