package group.fire_monitor.pojo.form;

import lombok.Data;

@Data
public class RegisterForm {
    private String  name;
    private String tel;
    private String  account;
    private String  password;
    private String  email;
    private Integer permissionLevel;

}