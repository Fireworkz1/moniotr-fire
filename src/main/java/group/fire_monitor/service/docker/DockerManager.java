package group.fire_monitor.service.docker;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.InspectContainerResponse;
import com.github.dockerjava.api.command.LogContainerCmd;
import com.github.dockerjava.api.model.Container;
import com.github.dockerjava.api.model.Frame;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.core.command.LogContainerResultCallback;
import group.fire_monitor.pojo.res.ContainerDetailRes;
import org.springframework.web.bind.annotation.RequestParam;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;


public class DockerManager {
    private DockerClient dockerClient;

    public DockerManager(String ip) {
        // 创建 Docker 客户端实例
        String url="tcp://"+ip+":2357";
        this.dockerClient = DockerClientBuilder.getInstance(url).build();
    }
    public String createAndStartContainer(String imageName, String containerName) {
        return dockerClient.createContainerCmd(imageName)
                .withName(containerName)
                .exec()
                .getId();
    }
    public void printAllContainersInfo(){
        // 获取所有正在运行的容器
        List<Container> containers = dockerClient.listContainersCmd().withShowAll(true).exec();

        // 打印容器信息
        for (Container container : containers) {
            System.out.println("Container ID: " + container.getId());
            System.out.println("Container Name: " + container.getNames()[0]);
            System.out.println("Container Status: " + container.getStatus());
            System.out.println("Container Publicport: " +container.getPorts()[0].getPublicPort());
            System.out.println("--------------------------------------");
        }
    }
    public ContainerDetailRes showContainerInfo(String containerId){
        ContainerDetailRes containerDetailRes=new ContainerDetailRes();
        try {
            // 替换为实际的容器 ID
            InspectContainerResponse containerDetails = dockerClient.inspectContainerCmd(containerId).exec();
            containerDetailRes.setDriver(containerDetails.getDriver());
            containerDetailRes.setCreatedAt(containerDetails.getCreated());
            containerDetailRes.setContainerId(containerDetails.getId());
            containerDetailRes.setName(containerDetails.getName());
            containerDetailRes.setPlatform(containerDetails.getPlatform());
            containerDetailRes.setStatus(containerDetails.getState().getStatus());
            containerDetailRes.setStartedAt(containerDetails.getState().getStartedAt());
            containerDetailRes.setImage(containerDetails.getConfig().getImage());
            containerDetailRes.setPorts(String.valueOf(containerDetails.getNetworkSettings().getPorts()));

            containerDetailRes.setIPAdress(String.valueOf(containerDetails.getNetworkSettings().getNetworks().get("bridge")));
        } finally {

        }
        return containerDetailRes;
    }
    public String getContainerIdByPort(String port){
        List<Container> containers = dockerClient.listContainersCmd().withShowAll(true).exec();
        for (Container container : containers) {
            // 获取容器的详细信息
            if (container.getPorts().length!=0&& Objects.requireNonNull(container.getPorts()[0].getPublicPort()).toString().equals(port)){
                return container.getId();

        }
    }
        return null;
    }
    public void startContainer(String containerId) {
        InspectContainerResponse containerDetails = dockerClient.inspectContainerCmd(containerId).exec();
        if(Objects.requireNonNull(containerDetails.getState().getRunning())){
            throw new RuntimeException("容器已在运行");
        }
        dockerClient.startContainerCmd(containerId).exec();
    }
    public void stopContainer(String containerId) {
        InspectContainerResponse containerDetails = dockerClient.inspectContainerCmd(containerId).exec();
        if(!Objects.requireNonNull(containerDetails.getState().getRunning())){
            throw new RuntimeException("容器已经停止");
        }
        dockerClient.stopContainerCmd(containerId).exec();
    }
    public void restartContainer(String containerId) {
        try {
            dockerClient.restartContainerCmd(containerId).exec();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public List<String> getDockerLog(String containerId,Integer lines) throws InterruptedException {
        if(lines==null)
            lines=50;
        LogContainerCmd logContainerCmd = dockerClient.logContainerCmd(containerId)
                .withStdOut(true) // 获取标准输出
                .withStdErr(true) // 获取标准错误
                .withTail(lines); // 实时跟随日志流
        List<String> logLines= Collections.synchronizedList(new ArrayList<>());
        StringBuilder logBuilder = new StringBuilder();
        // 注册回调处理日志
        try {
            logContainerCmd.exec(new LogContainerResultCallback() {
                @Override
                public void onNext(Frame frame) {
                    String logContent = new String(frame.getPayload(), StandardCharsets.UTF_8);
                    logBuilder.append(logContent);
                }

                @Override
                public void onComplete() {
                    String[] lines = logBuilder.toString().split("\\R");
                    for (String line : lines) {
                        logLines.add(line.trim());
                    }
                    System.out.println("日志获取完成");
                }

                @Override
                public void onError(Throwable throwable) {
                    System.err.println("日志获取失败: " + throwable.getMessage());
                }
            }).awaitCompletion(3, TimeUnit.SECONDS); // 设置超时时间
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); // 重新设置中断状态
            System.err.println("日志处理被中断: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("日志处理失败: " + e.getMessage());
        }

        System.out.println("111111111"); // 确保这行代码始终执行
        return logLines;
    }
}