package com.yu.backend.api.aliyunai.model;

import cn.hutool.core.annotation.Alias;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 阿里云图像扩图 - 查询任务响应
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GetOutPaintingTaskResponse {

    /**
     * 请求唯一标识
     */
    @Alias("request_id")
    private String requestId;

    /**
     * 输出信息
     */
    private Output output;

    /**
     * 表示任务的输出信息
     */
    @Data
    public static class Output {

        /**
         * 任务 ID
         */
        @Alias("task_id")
        private String taskId;

        /**
         * 任务状态：PENDING / RUNNING / SUSPENDED / SUCCEEDED / FAILED / UNKNOWN
         */
        @Alias("task_status")
        private String taskStatus;

        /**
         * 提交时间，格式：YYYY-MM-DD HH:mm:ss.SSS
         */
        @Alias("submit_time")
        private String submitTime;

        /**
         * 调度时间，格式：YYYY-MM-DD HH:mm:ss.SSS
         */
        @Alias("scheduled_time")
        private String scheduledTime;

        /**
         * 结束时间，格式：YYYY-MM-DD HH:mm:ss.SSS
         */
        @Alias("end_time")
        private String endTime;

        /**
         * 输出图像的 URL
         */
        @Alias("output_image_url")
        private String outputImageUrl;

        /**
         * 接口错误码（成功时不返回）
         */
        private String code;

        /**
         * 接口错误信息（成功时不返回）
         */
        private String message;

        /**
         * 任务指标信息
         */
        @Alias("task_metrics")
        private TaskMetrics taskMetrics;
    }

    /**
     * 任务的统计信息
     */
    @Data
    public static class TaskMetrics {

        /**
         * 总任务数
         */
        private Integer total;

        /**
         * 成功任务数
         */
        private Integer succeeded;

        /**
         * 失败任务数
         */
        private Integer failed;
    }
}
