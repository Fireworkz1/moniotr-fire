package group.fire_monitor.service.docker;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.InspectContainerResponse;
import com.github.dockerjava.api.command.ListContainersCmd;
import com.github.dockerjava.api.model.Container;
import com.github.dockerjava.api.model.Ports;
import com.github.dockerjava.core.DockerClientBuilder;

import java.util.Collections;
import java.util.List;
import java.util.Objects;


public class DockerManager {
    private DockerClient dockerClient;

    public DockerManager(String ip) {
        // 创建 Docker 客户端实例
        String url="tcp://"+ip+":2375";
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
    public void printContainerInfo(){
        try {
            // 替换为实际的容器 ID
            String containerId = "your-container-id";
            InspectContainerResponse containerDetails = dockerClient.inspectContainerCmd(containerId).exec();

            System.out.println("Container ID: " + containerDetails.getId());
            System.out.println("Container Name: " + containerDetails.getName());
            System.out.println("Container Image: " + containerDetails.getConfig().getImage());
            System.out.println("Container Status: " + containerDetails.getState().getStatus());
            System.out.println("Container Ports: " + containerDetails.getNetworkSettings().getPorts());
            System.out.println("--------------------------------------------------");
        } finally {

        }
    }
    public String getContainerIdByPort(String port){
        List<Container> containers = dockerClient.listContainersCmd().withShowAll(true).exec();
        for (Container container : containers) {
            // 获取容器的详细信息
            if (Objects.requireNonNull(container.getPorts()[0].getPublicPort()).toString().equals(port)){
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
}