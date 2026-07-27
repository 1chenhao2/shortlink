package com.nageoffer.shortlink.project.service;


public interface UrlTitleService {
    /**
     * 根据url获取标题
     * @param url
     * @return
     */
    String getTitleByUrl(String url);
}
