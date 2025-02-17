package group.fire_monitor.service;

import group.fire_monitor.mapper.WarnPolicyMapper;
import group.fire_monitor.pojo.WarnPolicy;
import group.fire_monitor.pojo.form.AddWarnForm;

import java.util.List;

public interface WarnService {
    void create(AddWarnForm form);

    List<WarnPolicy> selectLike(String str);

    void delete(Integer id);


}
