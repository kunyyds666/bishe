package com.easypan.entity.config;

import com.easypan.utils.StringTools;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;


@Component("appConfig")
public class AppConfig {

    @Getter
    private static final Logger logger = LoggerFactory.getLogger(AppConfig.class);

    /**
     * 文件目录
     */
    @Value("${project.folder:}")
    private String projectFolder;

    /**
     * 发送人
     */
    @Getter
    @Value("${spring.mail.username:}")
    private String sendUserName;


    @Getter
    @Value("${admin.emails:}")
    private String adminEmails;

    @Getter
    @Value("${dev:false}")
    private Boolean dev;


    @Getter
    @Value("${qq.app.id:}")
    private String qqAppId;

    @Getter
    @Value("${qq.app.key:}")
    private String qqAppKey;


    @Getter
    @Value("${qq.url.authorization:}")
    private String qqUrlAuthorization;


    @Getter
    @Value("${qq.url.access.token:}")
    private String qqUrlAccessToken;


    @Getter
    @Value("${qq.url.openid:}")
    private String qqUrlOpenId;

    @Getter
    @Value("${qq.url.user.info:}")
    private String qqUrlUserInfo;

    @Getter
    @Value("${qq.url.redirect:}")
    private String qqUrlRedirect;


    public String getProjectFolder() {
        if (!StringTools.isEmpty(projectFolder) && !projectFolder.endsWith("/")) {
            projectFolder = projectFolder + "/";
        }
        return projectFolder;
    }

}
