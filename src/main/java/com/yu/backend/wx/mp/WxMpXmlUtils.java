package com.yu.backend.wx.mp;

import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.XmlUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.HashMap;
import java.util.Map;

/**
 * 公众号被动回复 XML（明文模式）
 */
public final class WxMpXmlUtils {

    private WxMpXmlUtils() {
    }

    public static Map<String, String> parseIncomingXml(String xml) {
        Map<String, String> map = new HashMap<>(16);
        if (StrUtil.isBlank(xml)) {
            return map;
        }
        Document doc = XmlUtil.parseXml(xml);
        Element root = doc.getDocumentElement();
        if (root == null) {
            return map;
        }
        NodeList children = root.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node node = children.item(i);
            if (node instanceof Element) {
                Element el = (Element) node;
                map.put(el.getNodeName(), el.getTextContent());
            }
        }
        return map;
    }

    public static String buildTextReply(String toUser, String fromUser, String content) {
        long now = System.currentTimeMillis() / 1000;
        return "<xml>"
                + "<ToUserName><![CDATA[" + toUser + "]]></ToUserName>"
                + "<FromUserName><![CDATA[" + fromUser + "]]></FromUserName>"
                + "<CreateTime>" + now + "</CreateTime>"
                + "<MsgType><![CDATA[text]]></MsgType>"
                + "<Content><![CDATA[" + content + "]]></Content>"
                + "</xml>";
    }
}
