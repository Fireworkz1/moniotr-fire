package group.fire_monitor.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.StringReader;
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
    private Integer resourceManageOn;
    private String resourceDescription;
    private Integer resourceUp;
    private String resourceUsername;
    private String resourcePassword;
    private Integer resourceCreaterId;
    private String startMode;//docker or linux
    private String reservedParam;//docker:containerId
    private String reservedParam2;//保存mysql和docker的启动端口，resourceport处写采集器端口
    private String reservedParam3;
}
