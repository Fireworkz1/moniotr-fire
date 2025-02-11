package group.fire_monitor.service.impl;

import com.auth0.jwt.JWT;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import group.fire_monitor.mapper.RelationGroupResourceMapper;
import group.fire_monitor.mapper.RelationGroupUserMapper;
import group.fire_monitor.mapper.ResourceMapper;
import group.fire_monitor.pojo.RelationGroupResource;
import group.fire_monitor.pojo.RelationGroupUser;
import group.fire_monitor.pojo.Resource;
import group.fire_monitor.pojo.form.ResourceCreateForm;
import group.fire_monitor.pojo.res.SoftwareDetailRes;
import group.fire_monitor.prometheus.PrometheusQueryExecutor;
import group.fire_monitor.prometheus.PrometheusResponse;
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
import java.sql.Timestamp;
import java.util.List;
import java.util.Objects;
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
        String user = resource.getHardResourceUsername(); // 替换为用户名
        String password = resource.getHardResourcePassword(); // 替换为密码
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
        wrapper.eq("resource_ip",resourceCreateForm.getResource().getResourceIp());
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
            wrapper.eq("resource_type","server")
                    .like("resource_ip",str)
                    .like("resource_port",str)
                    .like("resource_name",str)
                    .like("resource_description",str);
            List<Resource> resourceList= resourceMapper.selectList(wrapper);

            //TODO:查询时加入权限筛选？
//            List<Resource> filteredResources = resourceList.stream()
//                    .filter(resource -> checkPermission(resourceList.stream().map(Resource::getId).collect(Collectors.toList())).contains(resource.getId())) // 筛选条件
//                    .collect(Collectors.toList());
            return new UniversalResponse<>().success(resourceList);
        }catch (Exception e){
            return new UniversalResponse<>(500,e.getMessage());
        }

    }

    @Override
    public UniversalResponse<?> addSoftware(ResourceCreateForm resourceCreateForm) {
        QueryWrapper<Resource> wrapper=new QueryWrapper<>();
        wrapper.eq("resource_ip",resourceCreateForm.getResource().getResourceIp());
        List<Resource> resources=resourceMapper.selectList(wrapper);
        if(!resources.isEmpty())return new UniversalResponse<>(500,"当前ip服务器已经被监控");
        try{

            //注册资源表
            resourceCreateForm.getResource().setResourceCreaterId(JWTUtil.getCurrentUser().getId());
            resourceCreateForm.getResource() .setResourceType("software");
            resourceCreateForm.getResource() .setAddedTime(new Timestamp(System.currentTimeMillis()));
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

        String targetUrl = "http://"+resource.getResourceIp()+":"+resource.getResourcePort()+"/actuator/prometheus"; // 替换为目标微服务的 IP 和端口
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
    public UniversalResponse<?> selectSoftware(String str) {
        if(Objects.equals(str, "") ||str==null){
            str="%";
        }
        try{
            QueryWrapper<Resource> wrapper=new QueryWrapper<>();
            wrapper.eq("resource_type","software")
                    .like("resource_ip",str)
                    .like("resource_port",str)
                    .like("resource_name",str)
                    .like("resource_type_second",str)
                    .like("resource_description",str);
            List<Resource> resourceList= resourceMapper.selectList(wrapper);
            //TODO:查询时加入权限筛选？
//            List<Resource> filteredResources = resourceList.stream()
//                    .filter(resource -> checkPermission(resourceList.stream().map(Resource::getId).collect(Collectors.toList())).contains(resource.getId())) // 筛选条件
//                    .collect(Collectors.toList());
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
                .stream().map(RelationGroupUser::getGroup_id).collect(Collectors.toList());
        return relationGroupResourceMapper.selectPermittedResourceId(userInGroupIds);
    }

    @Override
    public void checkPermission(Integer resourceId) throws Exception {
        if (Objects.equals(JWTUtil.getCurrentUser().getPermissionLevel(), PermissionLevelEnum.ADMIN.getPermissionLevel())) return;
        List<RelationGroupResource> list= relationGroupResourceMapper.selectList(new QueryWrapper<RelationGroupResource>().eq("resource_id",resourceId));
        List<Integer> groupIds=list.stream().map(RelationGroupResource::getGroupId).collect(Collectors.toList());
        List<Integer> userInGroupIds=relationGroupUserMapper.selectList(new QueryWrapper<RelationGroupUser>().eq("user_id",JWTUtil.getCurrentUser().getId()))
                .stream().map(RelationGroupUser::getGroup_id).collect(Collectors.toList());

        List<Integer> intersection = groupIds.stream()
                .filter(userInGroupIds::contains) // 筛选出 list2 中存在的元素
                .collect(Collectors.toList());
        if (intersection.isEmpty()) throw new Exception("无权限访问");
    }

    @Override
    public UniversalResponse<?> selectSoftwareDetail(Integer id) {

        Resource resource=resourceMapper.selectById(id);
        if(resource==null) return new UniversalResponse<>(500,"找不到资源");
        SoftwareDetailRes softwareDetailRes=new  SoftwareDetailRes();
        softwareDetailRes.setResouceId(id);
        softwareDetailRes.setResouceName(resource.getResourceName());
        softwareDetailRes.setResouceIp(resource.getResourceIp());
        softwareDetailRes.setResourceType(resource.getResourceType());
        softwareDetailRes.setResourcePort(resource.getResourcePort());
        softwareDetailRes.setStartMode(resource.getStartMode());

        PrometheusResponse response=prometheusQueryExecutor.isMicroserviceOnline(resource.getResourceIp(),resource.getResourcePort());
        if(response.getData().getResult().isEmpty())return new UniversalResponse<>(200,"资源未上线");
        softwareDetailRes.setPrometheusInstance(response.getData().getResult().get(0).getMetric().getInstance());
        softwareDetailRes.setPrometheusJobname(response.getData().getResult().get(0).getMetric().getJob());
        softwareDetailRes.setPrometheusUp(1);
        return new UniversalResponse<>().success(softwareDetailRes);
    }

    @Override
    public UniversalResponse<?> selectServerDetail(Integer id) {
        return null;
    }
}
