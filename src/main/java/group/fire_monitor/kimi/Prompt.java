package group.fire_monitor.kimi;

import group.fire_monitor.pojo.Monitor;
import group.fire_monitor.pojo.PrometheusResult;
import group.fire_monitor.pojo.Resource;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
@Component
public class Prompt {
    public  String getPrometheusAnalyzePrompt(List<PrometheusResult> prometheusResultList, Monitor monitor, List<Resource> resourceList, Date startTime, Date endTime){
        StringBuilder promptBuilder = new StringBuilder();
        promptBuilder.append("## 监控对象和资源信息\n\n");
        promptBuilder.append("### 监控对象\n");
        promptBuilder.append("- **监控对象名称**：`").append(monitor.getMonitorName()).append("`\n");
        promptBuilder.append("- **监控预设目标**：`").append(monitor.getMonitorPresetTarget()).append("`\n");
        promptBuilder.append("- **监控资源ID**：`").append(monitor.getMonitorResourceIds()).append("`\n");
        promptBuilder.append("- **监控类型**：`").append(monitor.getMonitorType()).append("`\n\n");

        promptBuilder.append("### 监控资源信息\n");
        for (Resource resource : resourceList) {
            promptBuilder.append("- **资源id**：`").append(resource.getId()).append("`\n");
            promptBuilder.append("- **资源类型**：`").append(resource.getResourceType()).append("`\n");
            promptBuilder.append("- **资源名称**：`").append(resource.getResourceName()).append("`\n");
            promptBuilder.append("- **资源IP**：`").append(resource.getResourceIp()).append("`\n");
            promptBuilder.append("- **资源端口**：`").append(resource.getResourcePort()).append("`\n");
            promptBuilder.append("- **资源二级类型**：`").append(resource.getResourceTypeSecond()).append("`\n");
            promptBuilder.append("- **启动模式**：`").append(resource.getStartMode()).append("`\n\n");
        }
        // 添加数据描述
        promptBuilder.append("## Prometheus 数据分析\n\n");
        promptBuilder.append("### 数据描述\n");
        promptBuilder.append("以下是从 Prometheus 收集的系统性能指标数据。每个指标可能会包含以下字段；其中实例通常与监控对象和资源信息中的资源IP和资源端口对应，一个监控资源对象对应一条或多条监控数据。（如一个服务器可能有多个磁盘分区，一个JVM有多个内存分区）\n");
        promptBuilder.append("- **指标名称**：`__name__`\n");
        promptBuilder.append("- **实例**：`instance`\n");
        promptBuilder.append("- **作业**：`job`\n");
        promptBuilder.append("- **区域**：`area`\n");
        promptBuilder.append("- **设备**：`device`\n");
        promptBuilder.append("- **域名**：`domainname`\n");
        promptBuilder.append("- **机器**：`machine`\n");
        promptBuilder.append("- **节点名称**：`nodename`\n");
        promptBuilder.append("- **系统名称**：`sysname`\n");
        promptBuilder.append("- **系统版本**：`release`\n");
        promptBuilder.append("- **系统版本号**：`version`\n");
        promptBuilder.append("- **命令**：`cmd`\n\n");

        promptBuilder.append("数据以时间序列格式提供，每个指标都有多个时间点的采样值。\n\n");

        // 添加数据示例
        promptBuilder.append("### 数据示例\n");
        for (PrometheusResult result : prometheusResultList) {
            PrometheusResult.Metric metric = result.getMetric();
            promptBuilder.append("- **指标名称**：`").append(metric.get__name__()).append("`\n");
            if (metric.getInstance() != null && !metric.getInstance().isEmpty()) {
                promptBuilder.append("- **实例**：`").append(metric.getInstance()).append("`\n");
            }
            if (metric.getJob() != null && !metric.getJob().isEmpty()) {
                promptBuilder.append("- **作业**：`").append(metric.getJob()).append("`\n");
            }
            if (metric.getArea() != null && !metric.getArea().isEmpty()) {
                promptBuilder.append("- **区域**：`").append(metric.getArea()).append("`\n");
            }
            if (metric.getDevice() != null && !metric.getDevice().isEmpty()) {
                promptBuilder.append("- **设备**：`").append(metric.getDevice()).append("`\n");
            }
            if (metric.getDomainname() != null && !metric.getDomainname().isEmpty()) {
                promptBuilder.append("- **域名**：`").append(metric.getDomainname()).append("`\n");
            }
            if (metric.getMachine() != null && !metric.getMachine().isEmpty()) {
                promptBuilder.append("- **机器**：`").append(metric.getMachine()).append("`\n");
            }
            if (metric.getNodename() != null && !metric.getNodename().isEmpty()) {
                promptBuilder.append("- **节点名称**：`").append(metric.getNodename()).append("`\n");
            }
            if (metric.getSysname() != null && !metric.getSysname().isEmpty()) {
                promptBuilder.append("- **系统名称**：`").append(metric.getSysname()).append("`\n");
            }
            if (metric.getRelease() != null && !metric.getRelease().isEmpty()) {
                promptBuilder.append("- **系统版本**：`").append(metric.getRelease()).append("`\n");
            }
            if (metric.getVersion() != null && !metric.getVersion().isEmpty()) {
                promptBuilder.append("- **系统版本号**：`").append(metric.getVersion()).append("`\n");
            }
            if (metric.getCmd() != null && !metric.getCmd().isEmpty()) {
                promptBuilder.append("- **命令**：`").append(metric.getCmd()).append("`\n");
            }

            promptBuilder.append("\n");

            promptBuilder.append("时间序列数据：\n");
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            for (List<Object> value : result.getValues()) {
                long timestamp;
                double metricValue;

                // 处理时间戳
                if (value.get(0) instanceof Long) {
                    timestamp = (long) value.get(0);
                } else if (value.get(0) instanceof Integer) {
                    timestamp = ((Integer) value.get(0)).longValue();
                } else {
                    throw new IllegalArgumentException("Unsupported timestamp type: " + value.get(0).getClass());
                }

                // 处理指标值
                if (value.get(1) instanceof Double) {
                    metricValue = (double) value.get(1);
                } else if (value.get(1) instanceof String) {
                    try {
                        metricValue = Double.parseDouble((String) value.get(1));
                    } catch (NumberFormatException e) {
                        throw new IllegalArgumentException("Invalid metric value format: " + value.get(1), e);
                    }
                } else {
                    throw new IllegalArgumentException("Unsupported metric value type: " + value.get(1).getClass());
                }

                // 格式化时间戳并追加到 StringBuilder
                promptBuilder.append(sdf.format(new Date(timestamp * 1000))).append(": ").append(metricValue).append("\n");
            }
            promptBuilder.append("\n");
        }

        // 添加分析目标
        promptBuilder.append("### 分析目标\n");
        promptBuilder.append("1. **性能瓶颈分析**：请分析当前指标是否存在性能瓶颈，如果有，请指出瓶颈所在（如 CPU、内存、磁盘或网络）。\n");
        promptBuilder.append("2. **趋势预测**：根据现有数据，预测未来 1 小时内各指标的变化趋势。\n");
        promptBuilder.append("3. **优化建议**：基于分析结果，提供优化系统性能的建议。\n\n");

        // 添加输出格式
        promptBuilder.append("### 输出格式\n");
        promptBuilder.append("- **性能瓶颈**：[瓶颈指标名称]\n");
        promptBuilder.append("- **趋势预测**：[各指标未来趋势描述]\n");
        promptBuilder.append("- **优化建议**：[具体优化建议]\n");

        return promptBuilder.toString();

    }
}
