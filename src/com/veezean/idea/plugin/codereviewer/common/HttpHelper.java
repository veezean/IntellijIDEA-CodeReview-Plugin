package com.veezean.idea.plugin.codereviewer.common;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson.JSON;

/**
 * <类功能简要描述>
 *
 * @author Wang Weiren
 * @since 2021/6/12
 */
public class HttpHelper {

    public static HttpResponse get(String urlString, int timeout) {
        return HttpRequest.get(urlString).timeout(timeout).execute();
    }

    public static <T> HttpResponse post(String urlString, T body, int timeout) {
        return HttpRequest.post(urlString).timeout(timeout).body(JSON.toJSONString(body)).execute();
    }
}
