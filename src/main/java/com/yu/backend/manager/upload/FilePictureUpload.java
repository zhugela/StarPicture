package com.yu.backend.manager.upload;

import com.yu.backend.exception.BusinessException;
import com.yu.backend.exception.ErrorCode;
import com.yu.backend.exception.ThrowUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * @author <a href="https://github.com/lieeew">leikooo</a>
 * @date 2024/12/21
 * @description
 */
@Component("file")
public class FilePictureUpload extends PictureUploadTemplate {
    private static final List<String> ALLOW_FILE_TYPE = Arrays.asList("jpg", "jpeg", "png", "webp");
    /**
     *把用户传来的内容写入本地临时文件
     * @param object 内容来源
     * @param file
     * @return
     */
    @Override
    protected File processFile(Object object,File file) {
        MultipartFile multipartFile = (MultipartFile) object;
        try{
            multipartFile.transferTo(file);
            return  file;
        }catch (IOException e){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"上传文件失败");
        }
    }


    @Override
    protected  void checkParamSource(Object object){
        ThrowUtils.throwIf(object == null,ErrorCode.PARAMS_ERROR,"上传文件不能为空");
        MultipartFile multipartFile = (MultipartFile) object;
        String fileName = multipartFile.getOriginalFilename();
        String fileSuffix = extractFileSuffix(fileName);
        if(!ALLOW_FILE_TYPE.contains(fileSuffix)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"不支持的文件类型");
        }
        long fileSize = multipartFile.getSize();
        if(fileSize >= TWO_MB){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"文件大小不能超过2MB");
        }
    }
    @Override
    protected String getOriginFilename(Object object){
        MultipartFile multipartFile = (MultipartFile) object;
        return multipartFile.getOriginalFilename();
    }
}

