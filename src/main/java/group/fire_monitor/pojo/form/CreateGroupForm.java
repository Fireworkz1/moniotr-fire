package group.fire_monitor.pojo.form;

import lombok.Data;

@Data
public class CreateGroupForm {
    String groupName;
    Integer permissionLevel;
    Integer groupLeaderId;
}
