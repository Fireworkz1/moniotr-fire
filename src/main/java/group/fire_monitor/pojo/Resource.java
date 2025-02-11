package group.fire_monitor.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import net.sf.jsqlparser.expression.DateTimeLiteralExpression;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Date;

@Data
@TableName("resource_info")
public class Resource {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private String resourceType;
    private String resourceName;
    private String resourceIp;
    private String resourcePort;
    private String resourceTypeSecond;
    private Date addedTime;
    private Integer resourceMonitorOn;
    private String resourceDescription;
    private Integer resourceUp;
    private String hardResourceUsername;
    private String hardResourcePassword;
    private Integer resourceCreaterId;

}
