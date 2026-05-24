package com.yu.backend.manager;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IORuntimeException;
import cn.hutool.core.util.RandomUtil;
import com.yu.backend.exception.BusinessException;
import com.yu.backend.exception.ErrorCode;
import com.yu.backend.exception.ThrowUtils;
import com.yu.backend.model.dto.file.UploadPictureResult;
import lombok.extern.slf4j.Slf4j;


import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.File;
import java.time.LocalDate;
import java.util.Arrays;
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

    public static final List<String> ALLOW_FORMAT_LIST = Arrays.asList("jpeg", "jpg", "png", "webp");

    @Resource
    private CosManager cosManager;


    public UploadPictureResult uploadPicture2(MultipartFile multipartFile, String uploadPathPrefix) {
        validPicture(multipartFile);
        // 图片上传地址（COS 对象键，不能用作本地临时文件前缀）
        String imagePath = generateImageUploadPath(multipartFile, uploadPathPrefix);
        File uploadFile = null;
        try {
            String ext = FileUtil.getSuffix(multipartFile.getOriginalFilename());
            String suffix = (ext != null && !ext.isEmpty()) ? "." + ext : ".tmp";
            uploadFile = File.createTempFile("star_picture_", suffix);
            multipartFile.transferTo(uploadFile);
            return cosManager.analyzeUploadResult(
                    cosManager.putPictureObject(imagePath, uploadFile),
                    FileUtil.mainName(multipartFile.getOriginalFilename()),
                    imagePath,
                    uploadFile);
        } catch (Exception e) {
            log.error("FileManager#uploadPicture2 error {}", ExceptionUtils.getRootCauseMessage(e));
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "上传图片失败");
        } finally {
            if (uploadFile != null) {
                try {
                    FileUtil.del(uploadFile);
                } catch (IORuntimeException e) {
                    log.error("FileManager#uploadPicture2 del temp {}, error {}",
                            uploadFile.getAbsolutePath(), ExceptionUtils.getRootCauseMessage(e));
                }
            }
        }
    }
    public UploadPictureResult uploadPicture2(File file, String uploadPathPrefix) {
        // 图片上传地址
        String imagePath = String.format("%s/%s_%s.%s", uploadPathPrefix, LocalDate.now(),
                RandomUtil.randomString(16), FileUtil.getSuffix(file.getName()));
        try {
            return cosManager.analyzeUploadResult(
                    cosManager.putPictureObject(imagePath, file),
                    FileUtil.mainName(file.getName()),
                    imagePath,
                    file);
        } catch (Exception e) {
            log.error("FileManager#uploadPicture2 error {}", ExceptionUtils.getRootCauseMessage(e));
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "上传图片失败");
        }
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

