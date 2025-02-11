package group.fire_monitor.pojo.form;

import group.fire_monitor.pojo.Resource;
import lombok.Data;

import java.util.List;

@Data
public class ResourceCreateForm {
    Resource resource;
    List<Integer> groupIdList;
}
