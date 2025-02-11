package group.fire_monitor.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import group.fire_monitor.mapper.ResourceMapper;
import group.fire_monitor.mapper.UserMapper;
import group.fire_monitor.pojo.Resource;
import group.fire_monitor.pojo.User;
import group.fire_monitor.service.ResourceService;
import group.fire_monitor.service.UserService;
import group.fire_monitor.util.response.UniversalResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Service
public class ResourceServiceImpl implements ResourceService {
    @Autowired
    ResourceMapper resourceMapper;

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
    public UniversalResponse<?> addServer(Resource resource) {
        QueryWrapper<Resource> wrapper=new QueryWrapper<>();
        wrapper.eq("resource_ip",resource.getResourceIp());
        List<Resource> resources=resourceMapper.selectList(wrapper);
        if(!resources.isEmpty())return new UniversalResponse<>(500,"当前ip服务器已经被监控");
        try{
            resource.setResourceType("server");
            resource.setAddedTime(new Timestamp(System.currentTimeMillis()));
            resourceMapper.insert(resource);
            //TODO:JAVA API实现注册和自动重启(需要监测有没有安装exporter)
            return new UniversalResponse<>().success();
        }catch (Exception e){
            return new UniversalResponse<>(500,e.getMessage());
        }
    }


    @Override
    public UniversalResponse<?> deleteServer(Integer resourceId) {
        try{
            resourceMapper.deleteById(resourceId);
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
            return new UniversalResponse<>().success(resourceList);
        }catch (Exception e){
            return new UniversalResponse<>(500,e.getMessage());
        }

    }

    @Override
    public UniversalResponse<?> addSoftware(Resource resource) {
        QueryWrapper<Resource> wrapper=new QueryWrapper<>();
        wrapper.eq("resource_ip",resource.getResourceIp());
        List<Resource> resources=resourceMapper.selectList(wrapper);
        if(!resources.isEmpty())return new UniversalResponse<>(500,"当前ip服务器已经被监控");
        try{
            resource.setResourceType("software");
            resource.setAddedTime(new Timestamp(System.currentTimeMillis()));
            resourceMapper.insert(resource);
            //TODO:JAVA API实现注册(需要监测有没有安装exporter)
            return new UniversalResponse<>().success();
        }catch (Exception e){
            return new UniversalResponse<>(500,e.getMessage());
        }
    }

    @Override
    public UniversalResponse<?> deleteSoftware(Integer resourceId) {
        try{
            resourceMapper.deleteById(resourceId);
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
            return new UniversalResponse<>().success(resourceList);
        }catch (Exception e){
            return new UniversalResponse<>(500,e.getMessage());
        }
    }

    @Override
    public UniversalResponse<?> testExporter(Integer type) {
        return null;
    }
}
