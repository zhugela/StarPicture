package com.yu.backend.manager;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.StrUtil;
import com.qcloud.cos.COSClient;
import com.qcloud.cos.model.DeleteObjectsRequest;
import com.qcloud.cos.model.PutObjectRequest;
import com.qcloud.cos.model.PutObjectResult;
import com.qcloud.cos.model.ciModel.persistence.CIObject;
import com.qcloud.cos.model.ciModel.persistence.CIUploadResult;
import com.qcloud.cos.model.ciModel.persistence.ImageInfo;
import com.qcloud.cos.model.ciModel.persistence.PicOperations;
import com.qcloud.cos.model.ciModel.persistence.ProcessResults;
import com.yu.backend.config.CosClientProperties;
import com.yu.backend.exception.BusinessException;
import com.yu.backend.exception.ErrorCode;
import com.yu.backend.exception.ThrowUtils;
import com.yu.backend.model.dto.file.UploadPictureResult;
import com.yu.backend.model.entity.Urls;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
@Slf4j
public class CosManager {

    private static final List<String> ALLOW_FILE_TYPE = Arrays.asList("png", "jpg", "jpeg");

    @Resource
    private CosClientProperties cosClientProperties;

    @Resource
    private COSClient cosClient;

    /**
     * 上传对象
     *
     * @param key  唯一键
     * @param file 文件
     */
    public PutObjectResult putObject(String key, File file) {
        PutObjectRequest putObjectRequest =
                new PutObjectRequest(cosClientProperties.getBucket(), key, file);
        return cosClient.putObject(putObjectRequest);
    }

    /**
     * 上传对象（附带原图信息，并异步转存 webp、可选缩略图与格式转换）
     *
     * @param key  COS 对象键（含路径）
     * @param file 本地文件
     */
    public PutObjectResult putPictureObject(String key, File file) {
        if (Boolean.TRUE.equals(cosClientProperties.getSimpleUploadOnly())) {
            return putObject(key, file);
        }
        PutObjectRequest putObjectRequest = new PutObjectRequest(cosClientProperties.getBucket(), key, file);
        PicOperations picOperations = new PicOperations();
        picOperations.setIsPicInfo(1);
        List<PicOperations.Rule> rules = new ArrayList<>();
        int lastSlash = key.lastIndexOf('/');
        String dirPrefix = lastSlash >= 0 ? key.substring(0, lastSlash + 1) : "";
        String webpKey = dirPrefix + FileUtil.mainName(key) + ".webp";
        PicOperations.Rule compressRule = new PicOperations.Rule();
        compressRule.setRule("imageMogr2/format/webp");
        compressRule.setBucket(cosClientProperties.getBucket());
        compressRule.setFileId(webpKey);
        rules.add(compressRule);
        // 缩略图：仅对大于 2KB 的图片生成（与参考实现一致）
        if (file.length() > 2 * 1024) {
            PicOperations.Rule thumbnailRule = new PicOperations.Rule();
            thumbnailRule.setBucket(cosClientProperties.getBucket());
            String thumbnailKey = dirPrefix + FileUtil.mainName(key) + "_thumbnail." + FileUtil.getSuffix(key);
            thumbnailRule.setFileId(thumbnailKey);
            thumbnailRule.setRule(String.format("imageMogr2/thumbnail/%sx%s>", 128, 128));
            rules.add(thumbnailRule);
        }
        // 非 png/jpg/jpeg 时转 JPG，便于后续图搜等接口
        String suffix = StrUtil.emptyToDefault(FileUtil.getSuffix(key), "").toLowerCase();
        if (!ALLOW_FILE_TYPE.contains(suffix)) {
            PicOperations.Rule transferRule = new PicOperations.Rule();
            transferRule.setBucket(cosClientProperties.getBucket());
            transferRule.setRule("imageMogr2/format/jpg");
            String transferKey = dirPrefix + FileUtil.mainName(key) + "_transfer.jpg";
            transferRule.setFileId(transferKey);
            rules.add(transferRule);
        }
        picOperations.setRules(rules);
        putObjectRequest.setPicOperations(picOperations);
        return cosClient.putObject(putObjectRequest);
    }

    /**
     * 解析 COS 数据万象上传返回，填充 {@link Urls}（含 original / 主图 / 缩略图 / transfer）
     */
    public UploadPictureResult analyzeUploadResult(PutObjectResult putObjectResult, String imageName, String imagePath) {
        return analyzeUploadResult(putObjectResult, imageName, imagePath, null);
    }

    /**
     * @param localFile 本地临时文件；普通 PUT 无万象回包时用于读取宽高与大小（可为 null）
     */
    public UploadPictureResult analyzeUploadResult(PutObjectResult putObjectResult, String imageName, String imagePath,
                                                   File localFile) {
        ThrowUtils.throwIf(putObjectResult == null, ErrorCode.SYSTEM_ERROR, "上传结果为空");
        CIUploadResult ciUploadResult = putObjectResult.getCiUploadResult();
        if (ciUploadResult == null) {
            if (localFile != null && localFile.isFile()) {
                return buildUploadResultFromLocalFile(localFile, imageName, imagePath);
            }
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "解析 COS 上传结果失败");
        }
        ImageInfo imageInfo = ciUploadResult.getOriginalInfo() != null
                ? ciUploadResult.getOriginalInfo().getImageInfo() : null;
        ProcessResults processResults = ciUploadResult.getProcessResults();
        List<CIObject> objectList = processResults != null ? processResults.getObjectList() : null;
        if (CollUtil.isNotEmpty(objectList)) {
            return buildCiUploadPictureResult(imageName, imagePath, objectList, imageInfo);
        }
        ThrowUtils.throwIf(imageInfo == null, ErrorCode.SYSTEM_ERROR, "无法获取原图信息");
        return buildOriginalOnlyUploadResult(imageName, imagePath, imageInfo);
    }

    /**
     * @deprecated 请使用 {@link #analyzeUploadResult}
     */
    @Deprecated
    public UploadPictureResult parseUploadPictureResult(PutObjectResult putObjectResult, String imageName, String imagePath) {
        return analyzeUploadResult(putObjectResult, imageName, imagePath);
    }

    private UploadPictureResult buildCiUploadPictureResult(String imageName, String imagePath, List<CIObject> objectList,
                                                           ImageInfo imageInfo) {
        CIObject compressedCiObject = objectList.get(0);
        String baseUrl = getBaseUrl();
        Urls urls = new Urls();
        urls.setOriginalUrl(String.format("%s/%s", baseUrl, imagePath));
        urls.setUrl(String.format("%s/%s", baseUrl, compressedCiObject.getKey()));
        if (objectList.size() > 1) {
            urls.setThumbnailUrl(String.format("%s/%s", baseUrl, objectList.get(1).getKey()));
        } else {
            urls.setThumbnailUrl(String.format("%s/%s", baseUrl, compressedCiObject.getKey()));
        }
        if (objectList.size() > 2) {
            urls.setTransferUrl(String.format("%s/%s", baseUrl, objectList.get(2).getKey()));
        }
        UploadPictureResult meta = buildMetaFromCiObject(imageName, compressedCiObject);
        meta.setUrls(urls);
        if (imageInfo != null && StrUtil.isNotBlank(imageInfo.getAve())) {
            meta.setPicColor(imageInfo.getAve());
        }
        return meta;
    }

    private UploadPictureResult buildMetaFromCiObject(String fileName, CIObject ciObject) {
        int w = ciObject.getWidth() != null ? ciObject.getWidth() : 0;
        int h = ciObject.getHeight() != null ? ciObject.getHeight() : 0;
        long size = ciObject.getSize() != null ? ciObject.getSize().longValue() : 0L;
        double scale = w > 0 ? NumberUtil.round(h * 1.0 / w, 2).doubleValue() : 0D;
        return UploadPictureResult.builder()
                .picFormat(ciObject.getFormat())
                .picHeight(h)
                .picWidth(w)
                .picSize(size)
                .picScale(scale)
                .picName(fileName)
                .build();
    }

    private UploadPictureResult buildUploadResultFromLocalFile(File file, String imageName, String imagePath) {
        BufferedImage img;
        try {
            img = ImageIO.read(file);
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "读取本地上传图片失败");
        }
        int w = img != null ? img.getWidth() : 0;
        int h = img != null ? img.getHeight() : 0;
        double scale = w > 0 ? NumberUtil.round(h * 1.0 / w, 2).doubleValue() : 0D;
        String fmt = StrUtil.emptyToDefault(FileUtil.getSuffix(imagePath), FileUtil.getSuffix(file.getName())).toLowerCase();
        String publicUrl = String.format("%s/%s", getBaseUrl(), imagePath);
        Urls urls = Urls.builder()
                .originalUrl(publicUrl)
                .url(publicUrl)
                .thumbnailUrl(publicUrl)
                .build();
        return UploadPictureResult.builder()
                .picFormat(fmt)
                .picHeight(h)
                .picWidth(w)
                .picSize(file.length())
                .picScale(scale)
                .picName(imageName)
                .urls(urls)
                .build();
    }

    private UploadPictureResult buildOriginalOnlyUploadResult(String imageName, String imagePath, ImageInfo imageInfo) {
        int w = imageInfo.getWidth();
        int h = imageInfo.getHeight();
        double scale = w > 0 ? NumberUtil.round(h * 1.0 / w, 2).doubleValue() : 0D;
        Urls urls = Urls.builder()
                .url(String.format("%s/%s", getBaseUrl(), imagePath))
                .build();
        return UploadPictureResult.builder()
                .picFormat(imageInfo.getFormat())
                .picHeight(h)
                .picWidth(w)
                .picSize((long) imageInfo.getQuality())
                .picScale(scale)
                .picName(imageName)
                .urls(urls)
                .picColor(StrUtil.isNotBlank(imageInfo.getAve()) ? imageInfo.getAve() : null)
                .build();
    }

    public String getBaseUrl() {
        if (StrUtil.isNotBlank(cosClientProperties.getHost())) {
            String host = cosClientProperties.getHost().trim();
            return host.endsWith("/") ? host.substring(0, host.length() - 1) : host;
        }
        return String.format("https://%s.cos.%s.myqcloud.com",
                cosClientProperties.getBucket(), cosClientProperties.getRegion());
    }

    public String keyFromPublicUrl(String fullUrl) {
        if (StrUtil.isBlank(fullUrl)) {
            return null;
        }
        String base = getBaseUrl();
        if (!fullUrl.startsWith(base)) {
            return null;
        }
        String rest = fullUrl.substring(base.length());
        if (rest.startsWith("/")) {
            rest = rest.substring(1);
        }
        int q = rest.indexOf('?');
        if (q >= 0) {
            rest = rest.substring(0, q);
        }
        return rest;
    }

    public void deleteObject(String key) {
        if (StrUtil.isBlank(key)) {
            return;
        }
        try {
            cosClient.deleteObject(cosClientProperties.getBucket(), key);
        } catch (Exception e) {
            log.warn("COS delete failed, key={}", key, e);
        }
    }

    /**
     * 批量删除 COS 对象（单键失败不影响其它键；整体失败打日志）
     */
    public void deleteObjects(List<String> keys) {
        if (CollUtil.isEmpty(keys)) {
            return;
        }
        List<DeleteObjectsRequest.KeyVersion> keyVersions = new ArrayList<>();
        for (String key : keys) {
            if (StrUtil.isNotBlank(key)) {
                keyVersions.add(new DeleteObjectsRequest.KeyVersion(key));
            }
        }
        if (keyVersions.isEmpty()) {
            return;
        }
        DeleteObjectsRequest request = new DeleteObjectsRequest(cosClientProperties.getBucket());
        request.setKeys(keyVersions);
        try {
            cosClient.deleteObjects(request);
        } catch (Exception e) {
            log.warn("COS batch delete failed, count={}", keyVersions.size(), e);
        }
    }
}
