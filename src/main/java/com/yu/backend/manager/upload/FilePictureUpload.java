package com.yu.backend.manager.upload;


import com.yu.backend.exception.BusinessException;
import com.yu.backend.exception.ErrorCode;
import com.yu.backend.exception.ThrowUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;

import java.util.Arrays;
import java.util.List;


@Component("file")
public class FilePictureUpload extends PictureUploadTemplate {
    private final static List<String> ALLOW_FILE_TYPE = Arrays.asList("jpeg", "jpg", "png", "webp");


    @Override
    protected File processFile(Object object, File file) {
        MultipartFile multipartFile = (MultipartFile) object;
        try{
            multipartFile.transferTo(file);
            return file;
        }catch (Exception e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"文件处理失败");
        }
    }

    @Override
    protected void checkParamSource(Object object) {
        MultipartFile multipartFile = (MultipartFile) object;
        ThrowUtils.throwIf(multipartFile == null, ErrorCode.PARAMS_ERROR, "文件不能为空");
        ThrowUtils.throwIf(multipartFile.getSize()>TWO_MB, ErrorCode.PARAMS_ERROR, "文件不能大于2MB");
        String suffix  = extractFileSuffix(multipartFile.getOriginalFilename());
        ThrowUtils.throwIf(!ALLOW_FILE_TYPE.contains(suffix), ErrorCode.PARAMS_ERROR,"文件格式不合法");

    }

    @Override
    protected String getOriginFilename(Object object) {
       MultipartFile multipartFile = (MultipartFile) object;
         return multipartFile.getOriginalFilename();
    }
}
