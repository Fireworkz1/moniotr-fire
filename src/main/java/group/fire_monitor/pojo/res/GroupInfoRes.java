package group.fire_monitor.pojo.res;

import group.fire_monitor.pojo.Group;
import group.fire_monitor.pojo.User;
import lombok.Data;

import java.util.List;

@Data
public class GroupInfoRes {
    private List<GroupUserRelation> groupUserRelationList;

}
