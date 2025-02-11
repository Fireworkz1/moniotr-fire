package group.fire_monitor.service;

import group.fire_monitor.pojo.Resource;
import group.fire_monitor.util.response.UniversalResponse;

public interface ResourceService {
    UniversalResponse<?> testPing(Resource resource);

    UniversalResponse<?> addServer(Resource resource);

    UniversalResponse<?> deleteServer(Integer resourceId);
    UniversalResponse<?> selectServer(String str);

    UniversalResponse<?> addSoftware(Resource resource);

    UniversalResponse<?> deleteSoftware(Integer resourceId);

    UniversalResponse<?> testSoftware(Resource resource);
    UniversalResponse<?> selectSoftware(String str);

    UniversalResponse<?> testExporter(Integer type);
}
