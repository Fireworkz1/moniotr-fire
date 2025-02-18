package group.fire_monitor.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import group.fire_monitor.mapper.GroupMapper;
import group.fire_monitor.mapper.MonitorMapper;
import group.fire_monitor.mapper.RelationGroupUserMapper;
import group.fire_monitor.mapper.ResourceMapper;
import group.fire_monitor.pojo.*;
import group.fire_monitor.pojo.form.AddMonitorForm;
import group.fire_monitor.pojo.form.ChangeMonitorForm;
import group.fire_monitor.service.MonitorService;
import group.fire_monitor.service.prometheus.PrometheusQueryExecutor;
import group.fire_monitor.service.prometheus.PrometheusResponse;
import group.fire_monitor.util.CommonUtil;
import group.fire_monitor.util.JWTUtil;
import group.fire_monitor.pojo.res.MonitorDataRes;
import group.fire_monitor.util.response.UniversalResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class MonitorServiceImpl implements MonitorService {
    @Autowired
    MonitorMapper monitorMapper;
    @Autowired
    GroupMapper groupMapper;
    @Autowired
    ResourceMapper resourceMapper;
    @Autowired
    RelationGroupUserMapper relationGroupUserMapper;
    @Autowired
    PrometheusQueryExecutor prometheusQueryExecutor;
    @Override
    public UniversalResponse<?> create(AddMonitorForm form) {
        try{
            Monitor monitor=new Monitor();
            monitor.setMonitorDescription(form.getMonitorDescription());
            monitor.setMonitorName(form.getMonitorName());
            monitor.setMonitorNotpresetPromql(form.getMonitorNotpresetPromql());
            if(!Objects.equals(form.getMonitorDemonstration(), "table") && !Objects.equals(form.getMonitorDemonstration(), "graph")){
                throw new RuntimeException("demonstration应为table或gtaph");
            }
            monitor.setMonitorDemonstration(form.getMonitorDemonstration());
            monitor.setMonitorPresetTarget(form.getMonitorPresetTarget());
            if(CommonUtil.hasValue(form.getMonitorPresetTarget())&&CommonUtil.hasValue(form.getMonitorNotpresetPromql())){
             throw new RuntimeException("只能同时选择一种监测模式");
            }

            if(CommonUtil.hasValue(form.getMonitorNotpresetPromql())){
                monitor.setMonitorIspreset(0);
            }
            if(CommonUtil.hasValue(form.getMonitorPresetTarget())){
                monitor.setMonitorIspreset(1);
            }
            checkAccessibility(monitor);

            monitor.setMonitorAddedTime(new Date());
            monitor.setMonitorType(form.getMonitorType());

            monitor.setMonitorGroupIds(CommonUtil.listToString(form.getMonitorGroupIds()));
            if(!CommonUtil.hasValue(form.getMonitorGroupIds())){
                User user= JWTUtil.getCurrentUser();
                QueryWrapper<RelationGroupUser> userwrapper=new QueryWrapper<>();
                userwrapper.eq("user_id",user.getId());
                List<Integer> groupIds=relationGroupUserMapper.selectList(userwrapper).stream().map(RelationGroupUser::getGroup_id).collect(Collectors.toList());
                monitor.setMonitorGroupIds(CommonUtil.listToString(groupIds));
            }
            monitor.setMonitorResourceIds(CommonUtil.listToString(form.getMonitorResourceIds()));



            monitorMapper.insert(monitor);
            return new UniversalResponse<>().success();
        } catch (RuntimeException e) {
            return new UniversalResponse<>(500,e.getMessage());
        }
    }

    @Override
    public List<PrometheusResult> getMonitorData(Monitor monitor) {
        List<PrometheusResult> rawResults=new ArrayList<>();
        if (monitor.getMonitorIspreset()==1)
            rawResults= getData(monitor.getMonitorPresetTarget());
        else
            rawResults= prometheusQueryExecutor.custom(monitor.getMonitorNotpresetPromql()).getResults();


        //TODO：使用redis优化函数运行速度
        List<Integer> resourceIdList= (List<Integer>) CommonUtil.stringToList(monitor.getMonitorResourceIds());
        List<Resource> resourceList= resourceMapper.selectBatchIds(resourceIdList);
        List<String> instanceList=new ArrayList<>();
        for(Resource resource:resourceList){
            String instance=resource.getResourceIp()+":"+resource.getResourcePort();
            if(Objects.equals(resource.getResourceType(), "server")){
                instance=instance+"9100";
            }
            instanceList.add(instance);
        }
        List<PrometheusResult> filteredResults=new ArrayList<>();
        for(PrometheusResult result:rawResults) {
            if(instanceList.contains(result.getMetric().getInstance())){
                filteredResults.add(result);
            }
        }

        return filteredResults;
    }

    @Override
    public List<Monitor> selectLike(String str, String type) {
        if(!Objects.equals(type, "server") && !Objects.equals(type, "software"))
            throw new RuntimeException("类型应为server或software");
        QueryWrapper<Monitor> wrapper=new QueryWrapper<>();
        wrapper.like("monitor_name",str)
                .like("monitor_description",str)
                .eq("monitor_type",type);
        return monitorMapper.selectList(wrapper);
    }

    @Override
    public void update(ChangeMonitorForm form) {
        Monitor monitor= monitorMapper.selectById(form.getMonitorId());
        monitor.setMonitorResourceIds(CommonUtil.listToString(form.getNewResourceIdList()));
        monitorMapper.updateById(monitor);

    }

    private void checkAccessibility(Monitor monitor) {
        if(monitor.getMonitorIspreset()==1)
            checkPreset(monitor.getMonitorPresetTarget());
        else
            checkNotPreset(monitor.getMonitorNotpresetPromql());
    }


    private void checkNotPreset(String promql)  {
        prometheusQueryExecutor.custom(promql);
    }
    private void checkPreset(String target){
        getData(target);
    }

    //获取数据
    private List<PrometheusResult> getData(String target) {
        try{
            PrometheusResponse response = null;
            switch (target){
                case "server_file_free_gb":
                    response= prometheusQueryExecutor.server_file_free_gb();
                    break;
                case "server_load_1min":
                    response= prometheusQueryExecutor.server_load_1min();
                    break;
                case "server_cpu_usage_1min":
                    response= prometheusQueryExecutor.server_cpu_usage_1min();
                    break;
                case "server_memory_usage_1min":
                    response= prometheusQueryExecutor.server_memory_usage_1min();
                    break;
                case "server_cpu_context_switches_5m":
                    response= prometheusQueryExecutor.server_cpu_context_switches_5m();
                    break;
                case "server_disk_input_rate_5m":
                    response= prometheusQueryExecutor.server_disk_input_rate_5m();
                    break;
                case "server_disk_output_rate_5m":
                    response= prometheusQueryExecutor.server_disk_output_rate_5m();
                    break;
                case "server_processes_running":
                    response= prometheusQueryExecutor.server_processes_running();
                    break;
                case "server_threads_running":
                    response= prometheusQueryExecutor.server_threads_running();
                    break;

                    /*
                    * 软件指标
                    * */
                case "software_jvm_nonheap_memory_usage":
                    response= prometheusQueryExecutor.software_jvm_nonheap_memory_usage();
                    break;
                case "software_jvm_heap_memory_usage":
                    response= prometheusQueryExecutor.software_jvm_heap_memory_usage();
                    break;
                case "software_jvm_gc_times_5min":
                    response= prometheusQueryExecutor.software_jvm_gc_times_5min();
                    break;
                case "software_jvm_threads_number":
                    response= prometheusQueryExecutor.software_jvm_threads_number();
                    break;

                default:
                    throw new RuntimeException("请输入正确的监测指标");
            }

            return response.getResults();
        } catch (RuntimeException e) {
            throw new RuntimeException(e);
        }

    }

}
