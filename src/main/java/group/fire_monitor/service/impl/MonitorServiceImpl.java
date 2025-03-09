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
import java.util.Calendar;
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
            if(form.getMonitorResourceIds().isEmpty())throw new RuntimeException("请至少选择一个资源");
            Monitor monitor=new Monitor();
            monitor.setMonitorDescription(form.getMonitorDescription());
            monitor.setMonitorName(form.getMonitorName());
            monitor.setMonitorNotpresetPromql(form.getMonitorNotpresetPromql());
            if(!Objects.equals(form.getMonitorDemonstration(), "table") && !Objects.equals(form.getMonitorDemonstration(), "graph")){
                throw new RuntimeException("demonstration应为table或graph");
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
                List<Integer> groupIds=relationGroupUserMapper.selectList(userwrapper).stream().map(RelationGroupUser::getGroupId).collect(Collectors.toList());
                monitor.setMonitorGroupIds(CommonUtil.listToString(groupIds));
            }
            monitor.setMonitorResourceIds(CommonUtil.listToString(form.getMonitorResourceIds()));


        try {
            getSingleMonitorData(monitor);
            }catch (Exception e){
            return new UniversalResponse<>(500,"无法获取prometheus数据，请检查表格信息");
        }
            monitorMapper.insert(monitor);
            return new UniversalResponse<>().success();
        } catch (RuntimeException e) {
            return new UniversalResponse<>(500,e.getMessage());
        }
    }

    @Override
    public List<PrometheusResult> getSingleMonitorData(Monitor monitor) {
        List<PrometheusResult> rawResults;
        if (monitor.getMonitorIspreset()==1)
            rawResults= getData(monitor.getMonitorPresetTarget(),"table",null,null);
        else
            rawResults= prometheusQueryExecutor.executeQuery(prometheusQueryExecutor.custom(monitor.getMonitorNotpresetPromql())) .getResults();
        return resultFilter(monitor,rawResults);


    }

    @Override
    public List<PrometheusResult> getSequenceMonitorData(Monitor monitor, Date startTime, Date endTime) {
        List<PrometheusResult> rawResults;
        if (monitor.getMonitorIspreset()==1)
            rawResults= getData(monitor.getMonitorPresetTarget(),"graph",startTime,endTime);
        else
            rawResults= prometheusQueryExecutor.executeQuery(prometheusQueryExecutor.custom(monitor.getMonitorNotpresetPromql()),startTime,endTime) .getResults();
        return resultFilter(monitor,rawResults);
    }

    @Override
    public List<Monitor> selectLike(String str, String type) {
//        if(!Objects.equals(type, "server") && !Objects.equals(type, "software"))
//            throw new RuntimeException("类型应为server或software");
        QueryWrapper<Monitor> wrapper=new QueryWrapper<>();
        if(str!=null&&! str.isEmpty()){
            wrapper.like("monitor_name",str).or()
                    .like("monitor_preset_target",str).or()
                    .like("monitor_notpreset_promql",str).or()
                    .like("monitor_description",str);
        }
        if(type!=null&&!type.isEmpty()){
            wrapper.eq("monitor_type",type);
        }

        return monitorMapper.selectList(wrapper);
    }

    @Override
    public void update(ChangeMonitorForm form) {
        if(form.getNewResourceIdList().isEmpty())throw new RuntimeException("请至少选择一个资源");
        Monitor monitor= monitorMapper.selectById(form.getMonitorId());
        monitor.setMonitorResourceIds(CommonUtil.listToString(form.getNewResourceIdList()));
        monitorMapper.updateById(monitor);

    }

    private void checkAccessibility(Monitor monitor) {
        if(monitor.getMonitorIspreset()==1)
            checkPreset(monitor.getMonitorPresetTarget(),"table");
        else
            checkNotPreset(monitor.getMonitorNotpresetPromql());
    }


    private void checkNotPreset(String promql)  {
        prometheusQueryExecutor.custom(promql);
    }
    private void checkPreset(String target,String type){
        getData(target,type,null,null);
    }

    //获取数据
    private List<PrometheusResult> getData(String target,String type,Date start,Date end) {
        try {
            String query;
            switch (target){
                case "up":
                    query=prometheusQueryExecutor.up();
                    break;
                /*
                 * 服务器指标
                 * */
                case "server_file_free_gb":
                    query= prometheusQueryExecutor.server_file_free_gb();
                    break;
                case "server_load_1min":
                    query= prometheusQueryExecutor.server_load_1min();
                    break;
                case "server_cpu_usage_1min":
                    query= prometheusQueryExecutor.server_cpu_usage_1min();
                    break;
                case "server_memory_usage_1min":
                    query= prometheusQueryExecutor.server_memory_usage_1min();
                    break;
                case "server_cpu_context_switches_5m":
                    query= prometheusQueryExecutor.server_cpu_context_switches_5m();
                    break;
                case "server_disk_input_rate_5m":
                    query= prometheusQueryExecutor.server_disk_input_rate_5m();
                    break;
                case "server_disk_output_rate_5m":
                    query= prometheusQueryExecutor.server_disk_output_rate_5m();
                    break;
                case "node_forks_total":
                    query= prometheusQueryExecutor.node_forks_total();
                    break;


                /*
                 * 软件指标
                 * */
                case "software_jvm_nonheap_memory_usage":
                    query= prometheusQueryExecutor.software_jvm_nonheap_memory_usage();
                    break;
                case "software_jvm_heap_memory_usage":
                    query= prometheusQueryExecutor.software_jvm_heap_memory_usage();
                    break;
                case "software_jvm_gc_times_5min":
                    query= prometheusQueryExecutor.software_jvm_gc_times_5min();
                    break;

                case "software_jvm_threads_number":
                    query= prometheusQueryExecutor.software_jvm_threads_number();
                    break;

                case "software_jvm_threads_states_threads":
                    query= prometheusQueryExecutor.software_jvm_threads_states_threads();
                    break;

                /*
                 * mysql指标
                 * */
                case "mysql_qps":
                    query = prometheusQueryExecutor.mysqlQps();
                    break;
                case "mysql_slow_queries":
                    query = prometheusQueryExecutor.mysqlSlowQueries();
                    break;
                case "mysql_connections":
                    query = prometheusQueryExecutor.mysqlConnections();
                    break;
                case "mysql_available_connections":
                    query = prometheusQueryExecutor.mysqlAvailableConnections();
                    break;

                    /*redis*/
                case "redis_connected_clients":
                    query = prometheusQueryExecutor.redisConnectedClients();
                    break;
                case "redis_operations_per_second":
                    query = prometheusQueryExecutor.redisOperationsPerSecond();
                    break;
                case "redis_memory_usage":
                    query = prometheusQueryExecutor.redisMemoryUsage();
                    break;
                case "redis_keyspace_hits_and_misses":
                    query = prometheusQueryExecutor.redisKeyspaceHitsAndMisses();
                    break;
                case "redis_memory_fragmentation_ratio":
                    query = prometheusQueryExecutor.redisMemoryFragmentationRatio();
                    break;
                case "redis_network_traffic":
                    query = prometheusQueryExecutor.redisNetworkTraffic();
                    break;
                case "redis_command_latency":
                    query = prometheusQueryExecutor.redisCommandLatency();
                    break;
                case "redis_uptime_in_seconds":
                    query = prometheusQueryExecutor.redisUptimeInSeconds();
                    break;
                default:
                    throw new RuntimeException("请输入正确的监测指标");}

            PrometheusResponse response=new PrometheusResponse();
                    if(Objects.equals(type, "table")){
                        response= prometheusQueryExecutor.executeQuery(query);
                    }else{
                        if(end==null){
                            end=new Date();
                        }
                        if(start==null){
                            Calendar calendar = Calendar.getInstance();
                            calendar.setTime(end);
                            calendar.add(Calendar.MINUTE, -30); // 减去 30 分钟
                            start = calendar.getTime();

                        }
                        response=prometheusQueryExecutor.executeQuery(query,start,end);
                    }

            return   response.getResults();
        } catch (RuntimeException e) {
            throw new RuntimeException(e);
        }

    }
    private List<PrometheusResult> resultFilter(Monitor monitor,List<PrometheusResult> rawResults){
        //TODO：使用redis优化函数运行速度
        List<Integer> resourceIdList= CommonUtil.stringToList(monitor.getMonitorResourceIds());
        List<Resource> resourceList= resourceMapper.selectBatchIds(resourceIdList);
        List<String> instanceList=new ArrayList<>();
        for(Resource resource:resourceList){
            String instance=resource.getResourceIp()+":";
            if(resource.getResourcePort()!=null&&!resource.getResourcePort().isEmpty())instance+=resource.getResourcePort();
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
}
