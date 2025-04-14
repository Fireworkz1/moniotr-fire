package group.fire_monitor.service;

import group.fire_monitor.pojo.Auto;
import group.fire_monitor.pojo.AutoGroup;
import group.fire_monitor.pojo.Resource;
import group.fire_monitor.pojo.form.AutoGroupForm;
import group.fire_monitor.pojo.form.ResourceCreateForm;
import group.fire_monitor.pojo.res.AutoRes;
import group.fire_monitor.util.response.UniversalResponse;

import java.util.List;

public interface ResourceService {
    UniversalResponse<?> testPing(Resource resource);

    UniversalResponse<?> addServer(ResourceCreateForm resourceCreateForm);

    UniversalResponse<?> deleteServer(Integer resourceId);
    UniversalResponse<?> selectServer(String str);

    UniversalResponse<?> addSoftware(ResourceCreateForm resourceCreateForm);

    UniversalResponse<?> deleteSoftware(Integer resourceId);

    UniversalResponse<?> testSoftware(Resource resource);
    UniversalResponse<?> selectSoftware(String str,String type,Boolean isdocker);

    UniversalResponse<?> testExporter(Integer type);

    List<Integer> checkPermission(List<Integer> resourceIds);

    void checkPermission(Integer resourceId) throws Exception;

    UniversalResponse<?> selectSoftwareDetail(Integer id);

    UniversalResponse<?> selectServerDetail(Integer id);

    UniversalResponse<?> stopDocker(Integer id);

    UniversalResponse<?> restartDocker(Integer id);

    UniversalResponse<?> startDocker(Integer id);

    UniversalResponse<?> dockerDetails(Integer id);


    void checkResourceActivity();

    UniversalResponse<?> dockerLog(Integer id,Integer lines);

    Integer automizationCreate(Auto auto);
    void automizationCreateGroup(AutoGroupForm groupForm);

    void automizationDeleteGroup(Integer autoGroupId);

    void automizationDelete(Integer autoId);

    void automizationModify(Auto auto);

    List<Auto> automizationSelect(String str);
    List<AutoRes> automizationSelectGroupPolicy(Integer groupId);
    List<AutoGroup> automizationSelectGroup(String str);

    void automizationChangeStatus(Integer autoId);

    void automizationChangeGroupStatus(Integer autoGroupId);
}
