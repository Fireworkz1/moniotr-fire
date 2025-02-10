package group.fire_monitor.pojo.form;

import lombok.Data;

@Data
public class ChangePermissionForm {
    String targetId;
    String targetPermissionLevel;
}
