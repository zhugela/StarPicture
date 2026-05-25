package com.yu.backend.api.aliyunai;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.yu.backend.api.aliyunai.model.CreateOutPaintingTaskRequest;
import com.yu.backend.api.aliyunai.model.CreateOutPaintingTaskResponse;
import com.yu.backend.api.aliyunai.model.GetOutPaintingTaskResponse;
import com.yu.backend.exception.BusinessException;
import com.yu.backend.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * 阿里云百炼 / DashScope 图像扩图 API
 */
@Slf4j
@Component
public class AliYunAiApi {

    public static final String CREATE_OUT_PAINTING_TASK_URL =
            "https://dashscope.aliyuncs.com/api/v1/services/aigc/image2image/out-painting";

    public static final String GET_OUT_PAINTING_TASK_URL =
            "https://dashscope.aliyuncs.com/api/v1/tasks/%s";

    @Value("${aliYunAi.apiKey}")
    private String apiKey;

    /**
     * 创建扩图任务（异步）
     */
    public CreateOutPaintingTaskResponse createOutPaintingTask(CreateOutPaintingTaskRequest createOutPaintingTaskRequest) {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpPost httpPost = new HttpPost(CREATE_OUT_PAINTING_TASK_URL);
            httpPost.addHeader("X-DashScope-Async", "enable");
            httpPost.addHeader("Authorization", "Bearer " + apiKey);
            httpPost.addHeader("Content-Type", "application/json");
            StringEntity entity = new StringEntity(JSONUtil.toJsonStr(createOutPaintingTaskRequest), StandardCharsets.UTF_8);
            httpPost.setEntity(entity);
            try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
                String responseBody = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
                if (response.getStatusLine().getStatusCode() != 200) {
                    log.error("创建扩图任务 HTTP 异常：{}", responseBody);
                    throw new BusinessException(ErrorCode.OPERATION_ERROR, "AI 扩图失败");
                }
                CreateOutPaintingTaskResponse paintingResponse =
                        JSONUtil.toBean(responseBody, CreateOutPaintingTaskResponse.class);
                if (StrUtil.isNotBlank(paintingResponse.getCode())) {
                    log.error("AI 扩图失败，errorCode:{}, errorMessage:{}",
                            paintingResponse.getCode(), paintingResponse.getMessage());
                    throw new BusinessException(ErrorCode.OPERATION_ERROR, "AI 扩图接口响应异常");
                }
                if (paintingResponse.getOutput() == null
                        || StrUtil.isBlank(paintingResponse.getOutput().getTaskId())) {
                    log.error("AI 扩图创建成功但缺少 task_id，响应：{}", responseBody);
                    throw new BusinessException(ErrorCode.OPERATION_ERROR, "AI 扩图任务 id 获取失败");
                }
                return paintingResponse;
            }
        } catch (IOException e) {
            log.error("创建扩图任务时发生错误", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "系统错误，请稍后再试");
        }
    }

    /**
     * 查询扩图任务状态与结果
     */
    public GetOutPaintingTaskResponse getOutPaintingTask(String taskId) {
        if (StrUtil.isBlank(taskId)) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "任务 id 不能为空");
        }
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpGet httpGet = new HttpGet(String.format(GET_OUT_PAINTING_TASK_URL, taskId));
            httpGet.addHeader("Authorization", "Bearer " + apiKey);
            try (CloseableHttpResponse response = httpClient.execute(httpGet)) {
                String responseBody = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
                if (response.getStatusLine().getStatusCode() != 200) {
                    log.error("查询扩图任务 HTTP 异常：{}", responseBody);
                    throw new BusinessException(ErrorCode.OPERATION_ERROR, "获取任务失败");
                }
                return JSONUtil.toBean(responseBody, GetOutPaintingTaskResponse.class);
            }
        } catch (IOException e) {
            log.error("获取扩图任务信息发生错误", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "系统错误，请稍后再试");
        }
    }
}
