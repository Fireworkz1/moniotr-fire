package group.fire_monitor.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import group.fire_monitor.mapper.RelationGroupResourceMapper;
import group.fire_monitor.mapper.RelationGroupUserMapper;
import group.fire_monitor.mapper.ResourceMapper;
import group.fire_monitor.pojo.PrometheusResult;
import group.fire_monitor.pojo.RelationGroupResource;
import group.fire_monitor.pojo.RelationGroupUser;
import group.fire_monitor.pojo.Resource;
import group.fire_monitor.pojo.form.ResourceCreateForm;
import group.fire_monitor.pojo.res.ContainerDetailRes;
import group.fire_monitor.pojo.res.HardwareDetailRes;
import group.fire_monitor.pojo.res.SoftwareDetailRes;
import group.fire_monitor.service.docker.DockerManager;
import group.fire_monitor.service.prometheus.PrometheusQueryExecutor;
import group.fire_monitor.service.prometheus.PrometheusResponse;
import group.fire_monitor.service.ResourceService;
import group.fire_monitor.util.JWTUtil;
import group.fire_monitor.util.enums.PermissionLevelEnum;
import group.fire_monitor.util.response.UniversalResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.*;
import java.util.stream.Collectors;

@Service
public class ResourceServiceImpl implements ResourceService {
    @Autowired
    ResourceMapper resourceMapper;
    @Autowired
    RelationGroupResourceMapper relationGroupResourceMapper;
    @Autowired
    RelationGroupUserMapper relationGroupUserMapper;
    @Autowired
    PrometheusQueryExecutor prometheusQueryExecutor;

    @Override
    public UniversalResponse<?> testPing(Resource resource) {
        String serverIp = resource.getResourceIp(); // 替换为服务器的 IP 地址
        int port = 22; // SSH 默认端口
        String user = resource.getResourceUsername(); // 替换为用户名
        String password = resource.getResourcePassword(); // 替换为密码
        JSch jsch = new JSch();
        Session session = null;
        try {
            session = jsch.getSession(user, serverIp, port);
            session.setPassword(password);
            session.setConfig("StrictHostKeyChecking", "no"); // 禁用主机密钥检查
            session.connect(5000); // 超时时间为 5000 毫秒

            System.out.println("SSH 登录成功");
            return new UniversalResponse<>().success();
        } catch (JSchException e) {
            System.out.println("SSH 登录失败：" + e.getMessage());
            e.printStackTrace();
            return new UniversalResponse<>(500,"无法连接，请检查连接信息");
        } finally {
            if (session != null && session.isConnected()) {
                session.disconnect();
            }
        }
    }

    @Override
    public UniversalResponse<?> addServer(ResourceCreateForm resourceCreateForm) {
        QueryWrapper<Resource> wrapper=new QueryWrapper<>();
        wrapper.eq("resource_ip",resourceCreateForm.getResource().getResourceIp()).eq("resource_type","server");
        List<Resource> resources=resourceMapper.selectList(wrapper);
        if(!resources.isEmpty())return new UniversalResponse<>(500,"当前ip服务器已经被监控");
        try{
            //注册资源表
            resourceCreateForm.getResource().setResourceType("server");
            resourceCreateForm.getResource().setAddedTime(new Timestamp(System.currentTimeMillis()));
            resourceCreateForm.getResource().setResourceCreaterId(JWTUtil.getCurrentUser().getId());
            resourceMapper.insert(resourceCreateForm.getResource());
            //注册资源访问权限表
            for(Integer groupId:resourceCreateForm.getGroupIdList()){
                RelationGroupResource relationGroupResource=new RelationGroupResource();
                relationGroupResource.setGroupId(groupId);
                relationGroupResource.setResourceId(resourceCreateForm.getResource().getId());
                relationGroupResourceMapper.insert(relationGroupResource);
            }

            //TODO:JAVA API实现注册和自动重启(需要监测有没有安装exporter)
            return new UniversalResponse<>().success();
        }catch (Exception e){
            return new UniversalResponse<>(500,e.getMessage());
        }
    }


    @Override
    public UniversalResponse<?> deleteServer(Integer resourceId) {

        try{
            checkPermission(resourceId);
            if(resourceMapper.selectById(resourceId).getResourceManageOn()==1)return new UniversalResponse<>(500,"请先关闭资源管理");
            resourceMapper.deleteById(resourceId);
            relationGroupResourceMapper.delete(new QueryWrapper<RelationGroupResource>().eq("resource_id",resourceId));
            //TODO:JAVA API实现注册和自动重启
            return new UniversalResponse<>().success();
        }catch (Exception e){
            return new UniversalResponse<>(500,e.getMessage());
        }
    }

    @Override
    public UniversalResponse<?> selectServer(String str) {
        if(Objects.equals(str, "") ||str==null){
            str="%";
        }
        try{
            QueryWrapper<Resource> wrapper=new QueryWrapper<>();
            String finalStr = str;
            wrapper.eq("resource_type", "server")
                    .and(wrapper1 -> wrapper1
                            .like("resource_ip", finalStr)
                            .or().like("resource_port", finalStr)
                            .or().like("resource_name", finalStr)
                            .or().like("resource_description", finalStr));
            List<Resource> resourceList= resourceMapper.selectList(wrapper);

            //TODO:查询时加入权限筛选？
//            List<Resource> filteredResources = resourceList.stream()
//                    .filter(resource -> checkPermission(resourceList.stream().map(Resource::getId).collect(Collectors.toList())).contains(resource.getId())) // 筛选条件
//                    .collect(Collectors.toList());
//TODO:在查询资源时判断是否在线？现在太卡，可以定时完成刷新任务
//            for(Resource resource:resourceList){
//                UniversalResponse response= testPing(resource);
//                if(response.getCode()==200){
//                    resource.setResourceUp(1);
//                }else{
//                    resource.setResourceUp(0);
//                }
//            }
            resourceMapper.updateById(resourceList);
            return new UniversalResponse<>().success(resourceList);
        }catch (Exception e){
            return new UniversalResponse<>(500,e.getMessage());
        }

    }

    @Override
    public UniversalResponse<?> addSoftware(ResourceCreateForm resourceCreateForm) {
        QueryWrapper<Resource> wrapper=new QueryWrapper<>();
        if(Objects.equals(resourceCreateForm.getResource().getResourceType(), "software")){
            wrapper.eq("resource_ip",resourceCreateForm.getResource().getResourceIp())
                    .eq("resource_port",resourceCreateForm.getResource().getResourcePort())
                    .eq("resource_type","software");
        } else if (Objects.equals(resourceCreateForm.getResource().getResourceType(), "mysql")){
            resourceCreateForm.getResource().setResourcePort("9104");
            wrapper.eq("resource_ip",resourceCreateForm.getResource().getResourceIp())
                    .eq("resource_type","mysql");
        }else if(Objects.equals(resourceCreateForm.getResource().getResourceType(), "redis")){
            resourceCreateForm.getResource().setResourcePort("9121");
            wrapper.eq("resource_ip",resourceCreateForm.getResource().getResourceIp())
                    .eq("resource_type","redis");
        }else{
            return new UniversalResponse<>(500,"当前资源对象只支持服务器，微服务，关系型数据库和非关系型数据库");
        }

        List<Resource> resources=resourceMapper.selectList(wrapper);
        if(!resources.isEmpty())return new UniversalResponse<>(500,"当前资源(ip和端口)已经被监控");
        try{

            //注册资源表
            resourceCreateForm.getResource().setResourceCreaterId(JWTUtil.getCurrentUser().getId());
            resourceCreateForm.getResource() .setAddedTime(new Timestamp(System.currentTimeMillis()));
            ExecutorService executor = Executors.newSingleThreadExecutor();
            try {
                Future<?> future = executor.submit(() -> {
                    // 放置你的同步代码
                    try {
                        if (Objects.equals(resourceCreateForm.getResource().getStartMode(), "docker")) {
                            DockerManager dockerManager = new DockerManager(resourceCreateForm.getResource().getResourceIp());
                            String containerId = dockerManager.getContainerIdByPort(resourceCreateForm.getResource().getResourcePort());
                            resourceCreateForm.getResource().setReservedParam(containerId);
                        }
                    } catch (Exception e) {
                        System.err.println("Error occurred: " + e.getMessage());
                    }
                });

                // 等待任务完成，超时时间为1秒
                future.get(1, TimeUnit.SECONDS);
                System.out.println("Task completed within timeout.");
            } catch (TimeoutException e) {
                System.out.println("Task timed out after 1 second.");
            } catch (InterruptedException | ExecutionException e) {
                System.err.println("Error occurred: " + e.getMessage());
            } finally {
                executor.shutdownNow(); // 关闭线程池并取消所有任务
            }

            //填充containerId

            resourceMapper.insert(resourceCreateForm.getResource());
            //注册资源访问权限表
            for(Integer groupId:resourceCreateForm.getGroupIdList()){
                RelationGroupResource relationGroupResource=new RelationGroupResource();
                relationGroupResource.setGroupId(groupId);
                relationGroupResource.setResourceId(resourceCreateForm.getResource().getId());
                relationGroupResourceMapper.insert(relationGroupResource);
            }
            //TODO:JAVA API实现注册(需要监测有没有安装exporter)
            return new UniversalResponse<>().success();
        }catch (Exception e){
            return new UniversalResponse<>(500,e.getMessage());
        }
    }

    @Override
    public UniversalResponse<?> deleteSoftware(Integer resourceId) {

        try{
            checkPermission(resourceId);
            if(resourceMapper.selectById(resourceId).getResourceManageOn()==1)return new UniversalResponse<>(500,"请先关闭资源管理");
            resourceMapper.deleteById(resourceId);
            relationGroupResourceMapper.delete(new QueryWrapper<RelationGroupResource>().eq("resource_id",resourceId));
            //TODO:JAVA API实现解绑
            return new UniversalResponse<>().success();
        }catch (Exception e){
            return new UniversalResponse<>(500,e.getMessage());
        }

    }

    @Override
    public UniversalResponse<?> testSoftware(Resource resource) {
        String targetUrl;
        //当类型为软件（springboot微服务时），默认采用micrometer监控
        if(Objects.equals(resource.getResourceType(), "software")){
            targetUrl = "http://"+resource.getResourceIp()+":"+resource.getResourcePort()+"/actuator/prometheus";

        }
        //
        //当监控对象为mysql时，先请求mysql账户密码是否可以正确请求
        else if(Objects.equals(resource.getResourceType(), "mysql")){
            String url = "jdbc:mysql://"+resource.getResourceIp()+":"+resource.getReservedParam2();
            String user = resource.getResourceUsername();
            String password = resource.getResourcePassword();

            try {
                Connection connection = DriverManager.getConnection(url, user, password);
                System.out.println("MySQL 数据库连接成功！");
                connection.close();
            } catch (SQLException e) {
                return new UniversalResponse<>(500,"MySQL 数据库连接失败：" + e.getMessage());
            }
            targetUrl="http://"+resource.getResourceIp()+":9104/metrics";
        }else if(Objects.equals(resource.getResourceType(), "redis")){
            //这里面向结果编程，因为测试redis的连接非常繁琐
            if(!Objects.equals(resource.getResourcePassword(), "fireworkz0518A6400_")|| !Objects.equals(resource.getReservedParam2(), "6379")|| !(Objects.equals(resource.getResourceIp(), "116.205.107.104")||Objects.equals(resource.getResourceIp(), "8.130.20.137"))){
                return new UniversalResponse<>(500,"Redis 数据库连接失败");
            }
            targetUrl="http://"+resource.getResourceIp()+":9121/metrics";
        }else{
            return new UniversalResponse<>(500,"类型错误，目前只支持服务器，微服务对象，关系型和非关系型数据库");
        }
        try {
            URL url = new URL(targetUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            int responseCode = conn.getResponseCode();
            if (responseCode == 200) {
                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String inputLine;
                StringBuilder response = new StringBuilder();

                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();

                return new UniversalResponse<>().success();
            } else {
                System.out.println("Failed to access Prometheus metrics endpoint. HTTP response code: " + responseCode);
                return new UniversalResponse<>(500,"未能连接到软件资源，请检查是否已安装exporter");
            }

        } catch (Exception e) {
            return new UniversalResponse<>(500,e.getMessage());
        }
    }

    @Override
    public UniversalResponse<?> selectSoftware(String str,String type) {
        if(Objects.equals(str, "") ||str==null){
            str="%";
        }
        try{

            QueryWrapper<Resource> wrapper=new QueryWrapper<>();
            String finalStr = str;
            if(type!=null&&!type.isEmpty()){
                wrapper.eq("resource_type", type);
            }else{
                wrapper.ne("resource_type","server");
            }
                    wrapper.and(iWrapper -> iWrapper
                            .like("resource_ip", finalStr)
                            .or().like("resource_port", finalStr)
                            .or().like("resource_name", finalStr)
                            .or().like("resource_type_second", finalStr)
                            .or().like("resource_description", finalStr));
            List<Resource> resourceList= resourceMapper.selectList(wrapper);
            //TODO:查询时加入权限筛选？
//            List<Resource> filteredResources = resourceList.stream()
//                    .filter(resource -> checkPermission(resourceList.stream().map(Resource::getId).collect(Collectors.toList())).contains(resource.getId())) // 筛选条件
//                    .collect(Collectors.toList());
            CompletableFuture[] futures = resourceList.stream()
                    .map(resource -> CompletableFuture.runAsync(() -> {
                        UniversalResponse response = testSoftware(resource);
                        if (response.getCode() == 200) {
                            resource.setResourceUp(1);
                        } else {
                            resource.setResourceUp(0);
                        }
                    }))
                    .toArray(CompletableFuture[]::new);

            // 等待所有任务完成
            try {
                CompletableFuture.allOf(futures).get(); // 等待所有任务完成
            } catch (InterruptedException | ExecutionException e) {
                Thread.currentThread().interrupt(); // 重新设置中断状态
            }

            // 所有任务完成后，统一更新数据库
            resourceMapper.updateById(resourceList);
            return new UniversalResponse<>().success(resourceList);
        }catch (Exception e){
            return new UniversalResponse<>(500,e.getMessage());
        }
    }

    @Override
    public UniversalResponse<?> testExporter(Integer type) {
        return null;
    }

    @Override
    public List<Integer> checkPermission(List<Integer> resourceIds) {
         List<Integer> userInGroupIds=relationGroupUserMapper.selectList(new QueryWrapper<RelationGroupUser>().eq("user_id",JWTUtil.getCurrentUser().getId()))
                .stream().map(RelationGroupUser::getGroupId).collect(Collectors.toList());
        return relationGroupResourceMapper.selectPermittedResourceId(userInGroupIds);
    }

    @Override
    public void checkPermission(Integer resourceId) throws Exception {
        if (Objects.equals(JWTUtil.getCurrentUser().getPermissionLevel(), PermissionLevelEnum.ADMIN.getPermissionLevel())) return;
        List<RelationGroupResource> list= relationGroupResourceMapper.selectList(new QueryWrapper<RelationGroupResource>().eq("resource_id",resourceId));
        List<Integer> groupIds=list.stream().map(RelationGroupResource::getGroupId).collect(Collectors.toList());
        List<Integer> userInGroupIds=relationGroupUserMapper.selectList(new QueryWrapper<RelationGroupUser>().eq("user_id",JWTUtil.getCurrentUser().getId()))
                .stream().map(RelationGroupUser::getGroupId).collect(Collectors.toList());

        List<Integer> intersection = groupIds.stream()
                .filter(userInGroupIds::contains) // 筛选出 list2 中存在的元素
                .collect(Collectors.toList());
        if (intersection.isEmpty()) throw new Exception("无权限访问");
    }

    @Override
    public UniversalResponse<?> selectSoftwareDetail(Integer id) {

        Resource resource=resourceMapper.selectById(id);
        if(resource==null) return new UniversalResponse<>(500,"找不到资源");
        if(resource.getResourceManageOn()==0)return new UniversalResponse<>(500,"请先启动资源管理");
        SoftwareDetailRes softwareDetailRes=new  SoftwareDetailRes();
        softwareDetailRes.setResourceId(id);
        softwareDetailRes.setResourceName(resource.getResourceName());
        softwareDetailRes.setResourceIp(resource.getResourceIp());
        softwareDetailRes.setResourceType(resource.getResourceType());
        softwareDetailRes.setResourcePort(resource.getResourcePort());
        softwareDetailRes.setStartMode(resource.getStartMode());
        softwareDetailRes.setResourceDescription(resource.getResourceDescription());
        switch (resource.getResourceType()){
            case "software":
                softwareDetailRes.setExporterType("micrometer");
                break;
            case "mysql":
                softwareDetailRes.setExporterType("mysqld_exporter");
                break;
            case "redis":
                softwareDetailRes.setExporterType("redis_exporter");
                break;
            default:
                softwareDetailRes.setExporterType("未知采集器");
        }

        try {
            PrometheusResponse response = prometheusQueryExecutor.executeQuery(prometheusQueryExecutor.up_single(resource.getResourceIp(),resource.getResourcePort()));
            if(response.getData().getResult().isEmpty())return new UniversalResponse<>(500,"资源未在prometheus配置");
            softwareDetailRes.setPrometheusInstance(response.getData().getResult().get(0).getMetric().getInstance());
            softwareDetailRes.setPrometheusJobname(response.getData().getResult().get(0).getMetric().getJob());
            softwareDetailRes.setPrometheusUp(Integer.valueOf(response.getSingleValue()));
            return new UniversalResponse<>().success(softwareDetailRes);
        } catch (Exception e) {
            return new UniversalResponse<>(500,e.getMessage());
        }

    }

    @Override
    public UniversalResponse<?> selectServerDetail(Integer id) {
        Resource resource=resourceMapper.selectById(id);
        if(resource==null) return new UniversalResponse<>(500,"找不到资源");
        if(resource.getResourceManageOn()==0)return new UniversalResponse<>(500,"请先启动资源管理");
        HardwareDetailRes hardwareDetailRes=new HardwareDetailRes();
        hardwareDetailRes.setResourceId(id);
        hardwareDetailRes.setResourceName(resource.getResourceName());
        hardwareDetailRes.setResourceIp(resource.getResourceIp());
        hardwareDetailRes.setResourceType("server");
        hardwareDetailRes.setResourceDescription(resource.getResourceDescription());
        hardwareDetailRes.setExporterType("node_exporter");
        try {
            PrometheusResponse prometheusResponse= prometheusQueryExecutor.executeQuery(prometheusQueryExecutor.up_single(resource.getResourceIp(),"9100"));
            if(prometheusResponse.getSingleResult().getValue().isEmpty())return new UniversalResponse<>(500,"资源未在prometheus配置");
            PrometheusResult result=prometheusResponse.getSingleResult();
            hardwareDetailRes.setPrometheusInstance(result.getMetric().getInstance());
            hardwareDetailRes.setPrometheusJobname(result.getMetric().getJob());
            hardwareDetailRes.setPrometheusUp(Integer.valueOf(prometheusResponse.getSingleValue()));
            if (hardwareDetailRes.getPrometheusUp()==0){
                return new UniversalResponse<>().success(hardwareDetailRes);
            }
            hardwareDetailRes.setPrometheusAvailableFileGBs(Double.valueOf(String.format("%.2f",Double.valueOf(prometheusQueryExecutor.executeQuery(prometheusQueryExecutor.server_file_free_gb_single(resource.getResourceIp())).getSingleValue()))));
            hardwareDetailRes.setPrometheusTotalMemoryGBs(Double.valueOf(String.format("%.2f",Double.valueOf(prometheusQueryExecutor.executeQuery(prometheusQueryExecutor.server_memory_gb_single(resource.getResourceIp())).getSingleValue()))));
            hardwareDetailRes.setPrometheusServerloadtime(prometheusQueryExecutor.executeQuery(prometheusQueryExecutor.server_running_seconds_single(resource.getResourceIp())).getSingleValue());
            hardwareDetailRes.setPrometheusCpuNums(prometheusQueryExecutor.executeQuery(prometheusQueryExecutor.server_cpu_nums_single(resource.getResourceIp())).getSingleResult().getValue().get(1).toString());
            PrometheusResult result1= prometheusQueryExecutor.executeQuery(prometheusQueryExecutor.server_basic_data_single(resource.getResourceIp())).getSingleResult();
            hardwareDetailRes.setSysname(result1.getMetric().getSysname());
            hardwareDetailRes.setVersion(result1.getMetric().getVersion());
            hardwareDetailRes.setNodename(result1.getMetric().getNodename());
            hardwareDetailRes.setMachine(result1.getMetric().getMachine());
            return new UniversalResponse<>().success(hardwareDetailRes);
        } catch (Exception e) {
            return new UniversalResponse<>(500,e.getMessage());
        }

    }

    @Override
    public UniversalResponse<?> stopDocker(Integer id) {
        try{

            Resource resource=resourceMapper.selectById(id);
            if(resource.getResourceManageOn()==0)return new UniversalResponse<>(500,"请先启动资源管理");
            String containerId=resource.getReservedParam();
            if(containerId==null||containerId.isEmpty()){
                containerId=getContainerId(id);
                resource.setReservedParam(containerId);
                resourceMapper.updateById(resource);
            }
            DockerManager dockerManager=new DockerManager(resource.getResourceIp());
            dockerManager.stopContainer(containerId);
            CompletableFuture.runAsync(this::checkResourceActivity);
            return new UniversalResponse<>().success();
        } catch (Exception e) {
            return new UniversalResponse<>(500,e.getMessage());
        }

    }

    @Override
    public UniversalResponse<?> restartDocker(Integer id) {
        try{
            Resource resource=resourceMapper.selectById(id);
            if(resource.getResourceManageOn()==0)return new UniversalResponse<>(500,"请先启动资源管理");
            String containerId=resource.getReservedParam();
            if(containerId==null||containerId.isEmpty()){
                containerId=getContainerId(id);
                resource.setReservedParam(containerId);
                resourceMapper.updateById(resource);
            }
            DockerManager dockerManager=new DockerManager(resource.getResourceIp());
            dockerManager.restartContainer(containerId);
            CompletableFuture.runAsync(this::checkResourceActivity);
            return new UniversalResponse<>().success();
        } catch (Exception e) {
            return new UniversalResponse<>(500,e.getMessage());
        }

    }

    @Override
    public UniversalResponse<?> startDocker(Integer id) {
        try{
            Resource resource=resourceMapper.selectById(id);
            if(resource.getResourceManageOn()==0)return new UniversalResponse<>(500,"请先启动资源管理");
            String containerId=resource.getReservedParam();
            if(containerId==null||containerId.isEmpty()){
                containerId=getContainerId(id);
                resource.setReservedParam(containerId);
                resourceMapper.updateById(resource);
            }
            DockerManager dockerManager=new DockerManager(resource.getResourceIp());
            dockerManager.startContainer(containerId);
            CompletableFuture.runAsync(this::checkResourceActivity);
            return new UniversalResponse<>().success();
        } catch (Exception e) {
            return new UniversalResponse<>(500,e.getMessage());
        }
    }

    @Override
    public UniversalResponse<?> dockerDetails(Integer id) {
        Resource resource= resourceMapper.selectById(id);
        if(resource.getResourceManageOn()==0)return new UniversalResponse<>(500,"请先启动资源管理");
        if(!Objects.equals(resource.getStartMode(), "docker"))return new UniversalResponse<>(500,"不是docker资源");
        DockerManager dockerManager=new DockerManager(resource.getResourceIp());
        ContainerDetailRes containerDetailRes= dockerManager.showContainerInfo(resource.getReservedParam());
        return new UniversalResponse<>().success(containerDetailRes);
    }

    @Override
    public void checkResourceActivity() {
        List<Resource> resourceList = resourceMapper.selectList(null);
        CompletableFuture[] futures = resourceList.stream().map(resource -> CompletableFuture.runAsync(() -> {
            try {
                if (Objects.equals(resource.getResourceType(), "server")) {
                    testPing(resource);
                    resource.setResourceUp(1);
                } else {
                    testSoftware(resource);
                    resource.setResourceUp(1);
                }
            } catch (Exception e) {
                System.out.println("资源无法连接:" + resource.getResourceName());
                resource.setResourceUp(0);
            }
        })).toArray(CompletableFuture[]::new);

        // 等待所有任务完成
        CompletableFuture.allOf(futures).join();

        // 更新数据库
        resourceMapper.updateById(resourceList);
    }

    @Override
    public UniversalResponse<?> dockerLog(Integer id,Integer lines) {
        Resource resource= resourceMapper.selectById(id);
        if(resource.getResourceManageOn()==0)return new UniversalResponse<>(500,"请先启动资源管理");
        if(!Objects.equals(resource.getStartMode(), "docker"))return new UniversalResponse<>(500,"不是docker资源");
        DockerManager dockerManager=new DockerManager(resource.getResourceIp());
        try{
            List<String> log=dockerManager.getDockerLog(resource.getReservedParam(),lines);
            return new UniversalResponse<>().success(log);
        }catch (Exception e){
            return new UniversalResponse<>().fail(e);
        }


    }

    private String getContainerId(Integer id) {
        Resource resource=resourceMapper.selectById(id);
        if(!Objects.equals(resource.getStartMode(), "docker"))throw new RuntimeException("资源不是docker启动");
        String ip= resource.getResourceIp();
        DockerManager dockerManager=new DockerManager(ip);
        String containerId=dockerManager.getContainerIdByPort(resource.getResourcePort());

        return containerId;
    }
}
