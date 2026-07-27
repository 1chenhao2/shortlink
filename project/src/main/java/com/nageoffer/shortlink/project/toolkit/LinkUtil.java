package com.nageoffer.shortlink.project.toolkit;

import cn.hutool.core.date.DateUnit;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import jakarta.servlet.http.HttpServletRequest;

import java.util.Date;
import java.util.Optional;

import static com.nageoffer.shortlink.project.common.constant.ShortLinkConstant.DEFAULT_CACHE_VALID_TIME;

/**
 * 短链工具类
 *
 * */
public class LinkUtil {
    /**
     * 获取短链缓存有效期
     * @param validDate
     * @return
     */
    public static long getLinkCacheValidDate(Date validDate){
        return Optional.ofNullable(validDate).map(each-> DateUtil.between(new Date(), each, DateUnit.MS)).orElse(DEFAULT_CACHE_VALID_TIME);
    }

    private static final String[] IP_HEADER_NAMES = {
            "X-Forwarded-For",
            "Proxy-Client-IP",
            "WL-Proxy-Client-IP",
            "HTTP_CLIENT_IP",
            "HTTP_X_FORWARDED_FOR",
            "X-Real-IP",
            "CF-Connecting-IP"
    };

    /**
     * 获取用户真实IP
     */
    public static String getActualIp(HttpServletRequest request) {
        for (String headerName : IP_HEADER_NAMES) {
            String ip = request.getHeader(headerName);
            if (StrUtil.isNotBlank(ip) && !"unknown".equalsIgnoreCase(ip)) {
                if (ip.contains(",")) {
                    ip = ip.split(",")[0].trim();
                }
                return ip;
            }
        }
        return request.getRemoteAddr();
    }
    

    /**
     * Get user access operating system from User-Agent.
     */
    public static String getOs(HttpServletRequest request) {
        String userAgent = request.getHeader("User-Agent");
        if (StrUtil.isBlank(userAgent)) {
            return "Unknown";
        }
        String lowerUserAgent = userAgent.toLowerCase();
        if (lowerUserAgent.contains("windows")) {
            return "Windows";
        }
        if (lowerUserAgent.contains("android")) {
            return "Android";
        }
        if (lowerUserAgent.contains("iphone") || lowerUserAgent.contains("ipad") || lowerUserAgent.contains("ipod")) {
            return "iOS";
        }
        if (lowerUserAgent.contains("mac os") || lowerUserAgent.contains("macintosh")) {
            return "macOS";
        }
        if (lowerUserAgent.contains("linux")) {
            return "Linux";
        }
        return "Unknown";
    }
    /**
     * 获取用户访问浏览器
     *
     * @param request 请求
     * @return 访问浏览器
     */
    public static String getBrowser(HttpServletRequest request) {
        String userAgent = request.getHeader("User-Agent");
        if (userAgent.toLowerCase().contains("edg")) {
            return "Microsoft Edge";
        } else if (userAgent.toLowerCase().contains("chrome")) {
            return "Google Chrome";
        } else if (userAgent.toLowerCase().contains("firefox")) {
            return "Mozilla Firefox";
        } else if (userAgent.toLowerCase().contains("safari")) {
            return "Apple Safari";
        } else if (userAgent.toLowerCase().contains("opera")) {
            return "Opera";
        } else if (userAgent.toLowerCase().contains("msie") || userAgent.toLowerCase().contains("trident")) {
            return "Internet Explorer";
        } else {
            return "Unknown";
        }
    }
    /**
     * 获取用户访问设备
     *
     * @param request 请求
     * @return 访问设备
     */
    public static String getDevice(HttpServletRequest request) {
        String userAgent = request.getHeader("User-Agent");
        if (userAgent.toLowerCase().contains("mobile")) {
            return "Mobile";
        }
        return "PC";
    }
    /**
     * 获取用户访问网络
     *
     * @param request 请求
     * @return 访问设备
     */
    public static String getNetwork(HttpServletRequest request) {
        String actualIp = getActualIp(request);
        // 这里简单判断IP地址范围，您可能需要更复杂的逻辑
        // 例如，通过调用IP地址库或调用第三方服务来判断网络类型
        return actualIp.startsWith("192.168.") || actualIp.startsWith("10.") ? "WIFI" : "Mobile";
    }
}
