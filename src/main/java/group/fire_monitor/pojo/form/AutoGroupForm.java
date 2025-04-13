package group.fire_monitor.pojo.form;

import lombok.Data;

import java.util.List;
@Data
public class AutoGroupForm {

    private String autoGroupName;
    private List<Integer> autoPolicyIds;
    private List<Integer> resourceIds;
    private String description;
}
