package com.veezean.idea.plugin.codereviewer.common;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.veezean.idea.plugin.codereviewer.model.GlobalConfigInfo;
import com.veezean.idea.plugin.codereviewer.model.Response;
import com.veezean.idea.plugin.codereviewer.util.CommonUtil;
import com.veezean.idea.plugin.codereviewer.util.LanguageUtil;
import com.veezean.idea.plugin.codereviewer.util.Logger;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * 网络操作辅助类
 *
 * @author Veezean, 公众号 @架构悟道
 * @since 2021/6/12
 */
public class NetworkOperationHelper {

    private static final int TIMEOUT_MILLIS = 3000;

    public static void openBrowser(String url) {
        try {
            Desktop.getDesktop().browse(URI.create(url));
        } catch (IOException ex) {
            Logger.error("打开浏览器失败，URL：" + url, ex);
        }
    }

    private static Map<String, String> buildAuthHeader() {
        GlobalConfigInfo globalConfig = GlobalConfigManager.getInstance().getGlobalConfig();
        Map<String, String> headers = new HashMap<>();
        headers.put("account", globalConfig.getAccount());
        headers.put("pwd", CommonUtil.md5(globalConfig.getPwd()));
        return headers;
    }

    public static <T> void doGet(String reqUrl, TypeReference<Response<T>> respType,
                                 Consumer<Response<T>> consumer) {
        String url;
        if (HttpUtil.isHttp(reqUrl) || HttpUtil.isHttps(reqUrl)) {
            url = reqUrl;
        } else {
            GlobalConfigInfo globalConfig = GlobalConfigManager.getInstance().getGlobalConfig();
            url = globalConfig.getServerAddress() + reqUrl;
        }
        Logger.info("发起GET请求，目标地址：" + url);
        String respBodyString = HttpRequest.get(url)
                .addHeaders(buildAuthHeader())
                .timeout(TIMEOUT_MILLIS)
                .execute()
                .body();
        Logger.info("服务端响应数据：" + respBodyString);
        Response<T> responseBean = JSON.parseObject(respBodyString, respType);
        if (responseBean.getCode() != 0) {
            throw new CodeReviewException(LanguageUtil.getString("CONFIG_UI_SERV_ERROR_HINT"));
        }

        // 执行业务处理
        consumer.accept(responseBean);
    }

    public static <T, R> void doPost(String reqUrl, T body, TypeReference<Response<R>> respType,
                                   Consumer<Response<R>> consumer) {
        String url;
        if (HttpUtil.isHttp(reqUrl) || HttpUtil.isHttps(reqUrl)) {
            url = reqUrl;
        } else {
            GlobalConfigInfo globalConfig = GlobalConfigManager.getInstance().getGlobalConfig();
            url = globalConfig.getServerAddress() + reqUrl;
        }

        Logger.info("发起POST请求，目标地址：" + url);

        String respBodyString = HttpRequest.post(url)
                .addHeaders(buildAuthHeader())
                .timeout(TIMEOUT_MILLIS)
                .body(JSON.toJSONString(body))
                .execute()
                .body();
        Logger.info("服务端响应数据：" + respBodyString);
        Response<R> responseBean = JSON.parseObject(respBodyString, respType);
        if (responseBean.getCode() != 0) {
            throw new CodeReviewException("服务端响应不成功");
        }

        // 执行业务处理
        consumer.accept(responseBean);
    }
}
