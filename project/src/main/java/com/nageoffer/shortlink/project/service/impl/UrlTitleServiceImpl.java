package com.nageoffer.shortlink.project.service.impl;

import cn.hutool.http.HttpUtil;
import com.nageoffer.shortlink.project.service.UrlTitleService;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Service;

@Service
public class UrlTitleServiceImpl implements UrlTitleService {

    @Override
    public String getTitleByUrl(String url) {
        try {
            // 使用 hutool 获取 HTML 内容（自动处理重定向、超时等）
            String html = HttpUtil.createGet(url)
                    .setConnectionTimeout(5000)
                    .setReadTimeout(5000)
                    .execute()
                    .body();
            // 用 Jsoup 解析 <title>
            Document doc = Jsoup.parse(html);
            return doc.title();
        } catch (Exception e) {
            // 网络异常或解析失败时返回原始 URL 作为兜底
            return url;
        }
    }
}
