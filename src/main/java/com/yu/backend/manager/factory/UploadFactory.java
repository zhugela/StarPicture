package com.yu.backend.manager.factory;

import com.yu.backend.manager.upload.PictureUploadTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

@Component
public class UploadFactory {
    @Resource
    private List<PictureUploadTemplate>  pictureUploadTemplates;


}
