package group.fire_monitor.pojo;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("basic_user")
public class User {
    private Integer id;
    private String  name;
    private Integer permissionLevel;
    private String  account;
    private String  password;
    private Integer  tel;
    private String  email;
}
