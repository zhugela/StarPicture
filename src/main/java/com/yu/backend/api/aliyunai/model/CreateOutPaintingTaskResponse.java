package com.yu.backend.api.aliyunai.model;

import cn.hutool.core.annotation.Alias;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 阿里云图像扩图 - 创建任务响应
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateOutPaintingTaskResponse {

    private Output output;

    /**
     * 接口错误码（成功时不返回）
     */
    private String code;

    /**
     * 接口错误信息（成功时不返回）
     */
    private String message;

    /**
     * 请求唯一标识
     */
    @Alias("request_id")
    private String requestId;

    /**
     * 任务的输出信息
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
    }
}
