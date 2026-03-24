package com.yu.backend.manager.upload;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpStatus;
import cn.hutool.http.HttpUtil;
import cn.hutool.http.Method;
import com.yu.backend.exception.BusinessException;
import com.yu.backend.exception.ErrorCode;
import com.yu.backend.exception.ThrowUtils;

import org.springframework.stereotype.Component;
import java.io.File;
import java.net.URL;

@Component
public class UrlPictureUpload extends PictureUploadTemplate{

    /**
     * 处理文件
     * @param object 内容来源
     * @param file
     * @return
     */
    @Override
    protected File processFile(Object object,File file) {
        String url = (String) object;
        HttpUtil.downloadFile(url, file);
        return file;
    }

    @Override
    protected void checkParamSource(Object object) {
        String fileurl = (String) object;
        ThrowUtils.throwIf(StrUtil.isBlank(fileurl), ErrorCode.PARAMS_ERROR, "URL不能为空");
         // 这里可以添加更多的URL校验逻辑，例如检查URL格式、是否为图片链接等
        try{
            //1.验证格式
            new URL(fileurl);
        }catch (Exception e){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件格式不对");
        }
        //2.检查URL协议
        ThrowUtils.throwIf(!fileurl.startsWith("http://") && !fileurl.startsWith("https://"), ErrorCode.PARAMS_ERROR, "URL必须以http://或https://开头");
        //3.发送head请求验证文件是否存在
        try(HttpResponse response = HttpUtil.createRequest(Method.HEAD,fileurl).execute()){
            if(response.getStatus()!= HttpStatus.HTTP_OK){
                return;
            }
            //4.检验文件类型
            String cotentType = response.header("Content-Type");
            if(StrUtil.isNotBlank(cotentType)){
                ThrowUtils.throwIf(!cotentType.startsWith("image/"), ErrorCode.PARAMS_ERROR, "URL必须指向一个图片文件");
            }
            //5.检验文件大小
            String contentLengthStr = response.header("Content-Length");
            if(StrUtil.isNotBlank(contentLengthStr)){
                try{
                    long contentLength = Long.parseLong(contentLengthStr);
                    ThrowUtils.throwIf(contentLength>TWO_MB,ErrorCode.PARAMS_ERROR,"文件大小不能超过2M");
                }catch (NumberFormatException e){
                    throw new BusinessException(ErrorCode.PARAMS_ERROR, "无法获取文件大小");
                }
            }
        }
        }
    @Override
    protected String getOriginFilename(Object object){
        String url = (String) object;
        return FileUtil.mainName(url) + "." + FileUtil.getSuffix(url);
    }
}
