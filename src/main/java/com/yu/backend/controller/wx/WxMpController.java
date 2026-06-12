package com.yu.backend.controller.wx;

import com.yu.backend.annotation.AuthCheck;
import com.yu.backend.common.BaseResponse;
import com.yu.backend.common.ResultUtils;
import com.yu.backend.service.WxMpService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 微信公众号服务器配置与消息回调
 */
@Api(tags = "微信公众号")
@RestController
@RequestMapping("/wx/mp")
public class WxMpController {

    @Resource
    private WxMpService wxMpService;

  /**
     * 公众平台「服务器配置」URL 验证
     * 完整地址示例：https://你的域名/api/wx/mp/portal
     */
    @ApiOperation("公众号服务器 URL 验证")
    @GetMapping("/portal")
    public String verifyPortal(@RequestParam("signature") String signature,
                                 @RequestParam("timestamp") String timestamp,
                                 @RequestParam("nonce") String nonce,
                                 @RequestParam("echostr") String echostr) {
        return wxMpService.verifyPortal(signature, timestamp, nonce, echostr);
    }

    /**
     * 接收用户消息与事件（自动回复）
     */
    @ApiOperation("公众号消息/事件回调")
    @PostMapping(value = "/portal", produces = "application/xml;charset=UTF-8")
    public String messagePortal(@RequestBody String body,
                                  @RequestParam("signature") String signature,
                                  @RequestParam("timestamp") String timestamp,
                                  @RequestParam("nonce") String nonce) {
        return wxMpService.handleMessage(body, signature, timestamp, nonce);
    }

    /**
     * 初始化自定义菜单（管理员）
     */
    @AuthCheck(mustRole = "admin")
    @ApiOperation("创建默认自定义菜单")
    @PostMapping("/menu/create")
    public BaseResponse<Boolean> createMenu() {
        wxMpService.createDefaultMenu();
        return ResultUtils.success(true);
    }
}
