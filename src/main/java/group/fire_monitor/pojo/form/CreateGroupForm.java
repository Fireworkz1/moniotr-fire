package group.fire_monitor.pojo.form;

import lombok.Data;

import java.util.List;

@Data
public class CreateGroupForm {
    String groupName;
    Integer permissionLevel;
    Integer groupLeaderId;
    List<Integer> userIds;
}
