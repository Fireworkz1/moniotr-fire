package group.fire_monitor.pojo.res;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.util.List;
@Data
public class WarnPolicyRes {
    private Integer monitorOn;
    private String warnName;
    private Integer warnLevel;
    private String warnSource;//monitor 监控指标
    private String warnSourceType;//monitor type
    private String compareType;
    private Double warnThreshold;
    private String warnDescription;
    private Integer monitorId;
    private String monitorName;
    private List<Integer> noticeGroupIds;
    private String noticeWay;

    //Warn非公用字段
    @TableId(type = IdType.AUTO)
    private Integer id;
    private Integer isActive;
}
