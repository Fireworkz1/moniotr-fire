package group.fire_monitor.service;

import group.fire_monitor.pojo.Monitor;
import group.fire_monitor.pojo.PrometheusResult;
import group.fire_monitor.pojo.form.AddMonitorForm;
import group.fire_monitor.pojo.form.ChangeMonitorForm;
import group.fire_monitor.util.response.UniversalResponse;

import java.util.List;


public interface MonitorService {
    UniversalResponse<?> create(AddMonitorForm form);

    List<PrometheusResult> getSingleMonitorData(Monitor monitor);

    List<Monitor> selectLike(String str, String type);

    void update(ChangeMonitorForm form);
}
