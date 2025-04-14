package group.fire_monitor.pojo.form;

import lombok.Data;

import java.util.List;
@Data
public class AutoGroupForm {
    private Integer id;
    private String autoGroupName;
    private List<Integer> autoPolicyIds;
    private List<Integer> resourceIds;
    private String description;
    private Integer masterNodeResourceId;
}
