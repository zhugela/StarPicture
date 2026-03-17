package com.yu.backend.manager;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IORuntimeException;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.RandomUtil;
import com.yu.backend.config.CosClientConfig;
import com.yu.backend.exception.BusinessException;
import com.yu.backend.exception.ErrorCode;
import com.yu.backend.exception.ThrowUtils;
import com.yu.backend.model.dto.file.UploadPictureResult;
import com.qcloud.cos.model.PutObjectResult;
import com.qcloud.cos.model.ciModel.persistence.ImageInfo;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;


import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.File;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * @author <a href="https://github.com/lieeew">leikooo</a>
 */
@Service
@Slf4j
public class FileManager {
    /**
     * 1 兆
     */
    private static final long ONE_M = 1024 * 1024L;

    private static final List<String> ALLOW_FORMAT_LIST = Arrays.asList("jpeg", "jpg", "png", "webp");

    @Resource
    private CosClientConfig cosClientConfig;

    @Resource
    private CosManager cosManager;


    public UploadPictureResult uploadPicture2(MultipartFile multipartFile, String uploadPathPrefix) {
        validPicture(multipartFile);
        // 图片上传地址
        String imagePath = generateImageUploadPath(multipartFile, uploadPathPrefix);
        try {
            File uploadFile = File.createTempFile(imagePath, null);
            multipartFile.transferTo(uploadFile);
            return analyzeCosReturn(new AnalyzeCosParams(cosManager.putPictureObject(imagePath, uploadFile), FileUtil.mainName(multipartFile.getOriginalFilename()), imagePath));
        } catch (Exception e) {
            log.error("FileManager#uploadPicture2 error {}", ExceptionUtils.getRootCauseMessage(e));
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "上传图片失败");
        } finally {
            try {
                FileUtil.del(imagePath);
            } catch (IORuntimeException e) {
                log.error("FileManager#uploadPicture2 del filePath {}, error {}", imagePath, ExceptionUtils.getRootCauseMessage(e));
            }
        }
    }

    private UploadPictureResult analyzeCosReturn(AnalyzeCosParams analyzeCosParams) {
        ImageInfo imageInfo = analyzeCosParams.getPutObjectResult().getCiUploadResult().getOriginalInfo().getImageInfo();
        return UploadPictureResult.builder()
                .picFormat(imageInfo.getFormat())
                .picHeight(imageInfo.getHeight())
                .picWidth(imageInfo.getHeight())
                .picSize((long) imageInfo.getQuality())
                .picScale(NumberUtil.round(imageInfo.getHeight() * 1.0 / imageInfo.getWidth(), 2).doubleValue())
                .picName(analyzeCosParams.getImageName())
                .url(String.format("%s/%s", cosManager.getBaseUrl(), analyzeCosParams.getImagePath()))
                .build();

    }

    private String generateImageUploadPath(MultipartFile multipartFile, String uploadPathPrefix) {
        String originalFilename = multipartFile.getOriginalFilename();
        String uploadPath = String.format("%s_%s.%s", LocalDate.now(), RandomUtil.randomString(16), originalFilename);
        return String.format("%s/%s", uploadPathPrefix, uploadPath);
    }

    /**
     * 校验文件
     *
     * @param multipartFile multipart 文件
     */
    public void validPicture(MultipartFile multipartFile) {
        ThrowUtils.throwIf(multipartFile == null, ErrorCode.PARAMS_ERROR, "文件不能为空");
        // 1. 校验文件大小
        long fileSize = multipartFile.getSize();
        ThrowUtils.throwIf(fileSize > 2 * ONE_M, ErrorCode.PARAMS_ERROR, "文件大小不能超过 2M");
        // 2. 校验文件后缀
        String fileSuffix = FileUtil.getSuffix(multipartFile.getOriginalFilename());
        // 允许上传的文件后缀
        ThrowUtils.throwIf(!ALLOW_FORMAT_LIST.contains(fileSuffix), ErrorCode.PARAMS_ERROR, "文件类型错误");
    }

    /**
     * 删除临时文件
     */
    public void deleteTempFile(File file) {
        if (file == null) {
            return;
        }
        // 删除临时文件
        boolean deleteResult = file.delete();
        if (!deleteResult) {
            log.error("file delete error, filepath = {}", file.getAbsolutePath());
        }
    }
}

/**
 * 不用成员变量因为多线程时会出问题
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
class AnalyzeCosParams {
    private PutObjectResult putObjectResult;
    private String imageName;
    private String imagePath;
}

