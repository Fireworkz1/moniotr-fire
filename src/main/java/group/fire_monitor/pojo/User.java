package group.fire_monitor.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("basic_user")
public class User {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private String  name;
    private Integer permissionLevel;
    private String  account;
    private String  password;
    private String  tel;
    private String  email;
}
