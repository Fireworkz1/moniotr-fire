package group.fire_monitor.service;

import group.fire_monitor.pojo.Resource;
import group.fire_monitor.pojo.form.ResourceCreateForm;
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
    UniversalResponse<?> selectSoftware(String str,String type);

    UniversalResponse<?> testExporter(Integer type);

    List<Integer> checkPermission(List<Integer> resourceIds);

    void checkPermission(Integer resourceId) throws Exception;

    UniversalResponse<?> selectSoftwareDetail(Integer id);

    UniversalResponse<?> selectServerDetail(Integer id);

    UniversalResponse<?> stopContainer(Integer id);

    UniversalResponse<?> restartDocker(Integer id);

    UniversalResponse<?> startDocker(Integer id);

    UniversalResponse<?> dockerDetails(Integer id);


    void checkResourceActivity();
}
