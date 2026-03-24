package com.yu.backend.manager.upload;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IORuntimeException;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.RandomUtil;
import com.qcloud.cos.model.PutObjectResult;
import com.qcloud.cos.model.ciModel.persistence.ImageInfo;
import com.yu.backend.exception.BusinessException;
import com.yu.backend.exception.ErrorCode;
import com.yu.backend.manager.CosManager;
import com.yu.backend.model.dto.file.UploadPictureResult;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.File;
import java.util.Date;
import java.util.Optional;

/**
 * @author leikooo
 * @description 图片上传抽象类 模板方法
 * @date 2024-12-16 11:56:07
 */
@Slf4j
@Component
public abstract class PictureUploadTemplate {
    @Resource
    private CosManager cosManager;

    /**
     * 限制文件大小为 2MB
     */
    protected final long TWO_MB = 2 * 1024 * 1024L;

    /**
     * 上传图片接口
     *
     * @param obj url 或者  multipartFile
     * @return 封装的 VO
     */
    public UploadPictureResult uploadPicture(Object obj, String uploadPathPrefix) {
        // 1）校验文件
        checkParamSource(obj);
        // 2）获取上传地址
        String templatePath = System.getProperty("user.dir") +
                File.separator + getOriginFilename(obj);
        File tempFile = new File(templatePath);
        // 3）获取本地临时文件
        File file = processFile(obj, tempFile);
        // 4）上传文件到对象存储
        return uploadPicture(file, uploadPathPrefix);
    }

    /**
     * 处理文件
     *
     * @param object 内容来源
     */
    protected abstract File processFile(Object object, File file);

    /**
     * 校验参数
     */
    protected abstract void checkParamSource(Object object);

    /**
     * 获取 FileName
     */
    protected abstract String getOriginFilename(Object object);

    public UploadPictureResult uploadPicture(File file, String uploadPathPrefix) {
        String imagePath = generateImageUploadPath(file, uploadPathPrefix);
        try {
            // 5）封装解析到图片信息
            return analyzeCosReturn(new AnalyzeCosParams(
                    cosManager.putPictureObject(imagePath, file),
                    FileUtil.mainName(file),
                    imagePath
            ));
        } catch (Exception e) {
            log.error("Error uploading picture: {}", ExceptionUtils.getRootCauseMessage(e), e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "上传图片失败");
        } finally {
            try {
                // 6）清理临时文件
                FileUtil.del(file);
            } catch (IORuntimeException e) {
                log.error("Error deleting temp file: {}", file.getAbsolutePath(), e);
            }
        }
    }

    private String generateImageUploadPath(File file, String uploadPathPrefix) {
        String originalFilename = FileUtil.getName(file);
        // 自己拼接文件上传路径，而不是使用原始文件名称，可以增强安全性
        String uploadFilename = String.format("%s_%s.%s", DateUtil.formatDate(new Date()), RandomUtil.randomString(16),
                originalFilename);
        // 最后结果类似这种  public/1867564572229492994/2024-12-21_REArPZjceu7DkRp3.Konachan.jpg
        return String.format("%s/%s", uploadPathPrefix, uploadFilename);
    }

    /**
     * 获取文件后缀，默认转成小写进行判断
     *
     * @param fileName 文件名
     * @return 文件后缀
     */
    protected String extractFileSuffix(String fileName) {
        return Optional.of(fileName)
                .filter(name -> name.contains("."))
                .map(name -> name.substring(name.lastIndexOf('.') + 1))
                .map(String::toLowerCase)
                .orElse("");
    }

    private UploadPictureResult analyzeCosReturn(AnalyzeCosParams params) {
        ImageInfo imageInfo = params.getPutObjectResult().getCiUploadResult().getOriginalInfo().getImageInfo();
        return UploadPictureResult.builder()
                .picFormat(imageInfo.getFormat())
                .picHeight(imageInfo.getHeight())
                .picWidth(imageInfo.getWidth())
                .picSize((long) imageInfo.getQuality())
                .picScale(NumberUtil.round(imageInfo.getHeight() * 1.0 / imageInfo.getWidth(), 2).doubleValue())
                .picName(params.getImageName())
                .url(String.format("%s/%s", cosManager.getBaseUrl(), params.getImagePath()))
                .build();
    }

    /**
     * 不用成员变量因为多线程时会出问题
     */
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    private static class AnalyzeCosParams {
        private PutObjectResult putObjectResult;
        private String imageName;
        private String imagePath;
    }
}
