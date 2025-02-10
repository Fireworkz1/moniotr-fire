package group.fire_monitor.pojo.form;

import lombok.Data;

import java.util.List;
@Data
public class ChangeGroupMemberForm {
    Integer groupId;
    List<Integer> userIdList;
}
