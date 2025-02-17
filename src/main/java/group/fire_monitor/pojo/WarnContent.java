package group.fire_monitor.pojo;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import javax.persistence.Table;
import java.util.Date;

@Data
@TableName("warn_content")
public class WarnContent {
    private String warnName;
    private String warnContent;
    private Integer warnLevel;
    private Date warnTime;
    private String warnDescription;
    private String warnSource;
    private String warnSourcetype;
}
