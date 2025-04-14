package group.fire_monitor.pojo.res;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class AutoRes {
    private Integer id;
    private String autoName;
    private String autoPolicy;
    private String compareType;
    private Double warnThreshold;
    private Integer resourceId;
    private Integer monitorOn;
    private String monitorPresetTarget;
    private Date modifiedTime;
    private Integer triggerTimes;
    private List<Integer> targetIds;
    private String type;
}
