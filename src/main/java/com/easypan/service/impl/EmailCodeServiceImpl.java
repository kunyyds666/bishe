package com.easypan.service.impl;

import com.easypan.component.RedisComponent;
import com.easypan.entity.config.AppConfig;
import com.easypan.entity.constants.Constants;
import com.easypan.entity.dto.SysSettingsDto;
import com.easypan.entity.enums.PageSize;
import com.easypan.entity.po.EmailCode;
import com.easypan.entity.po.UserInfo;
import com.easypan.entity.query.EmailCodeQuery;
import com.easypan.entity.query.SimplePage;
import com.easypan.entity.query.UserInfoQuery;
import com.easypan.entity.vo.PaginationResultVO;
import com.easypan.exception.BusinessException;
import com.easypan.mappers.EmailCodeMapper;
import com.easypan.mappers.UserInfoMapper;
import com.easypan.service.EmailCodeService;
import com.easypan.utils.StringTools;
import freemarker.cache.FileTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;

import java.io.File;
import java.util.*;


/**
 * 邮箱验证码 业务接口实现
 */
@Service("emailCodeService")
public class EmailCodeServiceImpl implements EmailCodeService {

    @Resource
    private JavaMailSender mailSender;

    private static final Logger logger = LoggerFactory.getLogger(EmailCodeServiceImpl.class);

    @Resource
    private EmailCodeMapper<EmailCode, EmailCodeQuery> emailCodeMapper;


    @Resource
    private AppConfig appConfig;

    @Resource
    private UserInfoMapper<UserInfo, UserInfoQuery> userInfoMapper;

    @Resource
    private RedisComponent redisComponent;

    /**
     * 根据条件查询列表
     */
    @Override
    public List<EmailCode> findListByParam(EmailCodeQuery param) {
        return this.emailCodeMapper.selectList(param);
    }

    /**
     * 根据条件查询列表
     */
    @Override
    public Integer findCountByParam(EmailCodeQuery param) {
        return this.emailCodeMapper.selectCount(param);
    }

    /**
     * 分页查询方法
     */
    @Override
    public PaginationResultVO<EmailCode> findListByPage(EmailCodeQuery param) {
        int count = this.findCountByParam(param);
        int pageSize = param.getPageSize() == null ? PageSize.SIZE15.getSize() : param.getPageSize();

        SimplePage page = new SimplePage(param.getPageNo(), count, pageSize);
        param.setSimplePage(page);
        List<EmailCode> list = this.findListByParam(param);
        return new PaginationResultVO<>(count, page.getPageSize(), page.getPageNo(), page.getPageTotal(), list);
    }

    /**
     * 新增
     */
    @Override
    public Integer add(EmailCode bean) {
        return this.emailCodeMapper.insert(bean);
    }

    /**
     * 批量新增
     */
    @Override
    public Integer addBatch(List<EmailCode> listBean) {
        if (listBean == null || listBean.isEmpty()) {
            return 0;
        }
        return this.emailCodeMapper.insertBatch(listBean);
    }

    /**
     * 批量新增或者修改
     */
    @Override
    public Integer addOrUpdateBatch(List<EmailCode> listBean) {
        if (listBean == null || listBean.isEmpty()) {
            return 0;
        }
        return this.emailCodeMapper.insertOrUpdateBatch(listBean);
    }

    /**
     * 根据EmailAndCode获取对象
     */
    @Override
    public EmailCode getEmailCodeByEmailAndCode(String email, String code) {
        return this.emailCodeMapper.selectByEmailAndCode(email, code);
    }

    /**
     * 根据EmailAndCode修改
     */
    @Override
    public Integer updateEmailCodeByEmailAndCode(EmailCode bean, String email, String code) {
        return this.emailCodeMapper.updateByEmailAndCode(bean, email, code);
    }

    /**
     * 根据EmailAndCode删除
     */
    @Override
    public Integer deleteEmailCodeByEmailAndCode(String email, String code) {
        return this.emailCodeMapper.deleteByEmailAndCode(email, code);
    }

    private void sendEmailCode(String toEmail, String code) {
        try {


            // 加载FreeMarker 模板
            Configuration configuration = new Configuration(Configuration.VERSION_2_3_23);
            FileTemplateLoader templateLoader = new FileTemplateLoader(
                    new File(Objects.requireNonNull(Thread.currentThread().getContextClassLoader().getResource("templates/")).getPath())
            );
            configuration.setTemplateLoader(templateLoader);
            Template template = configuration.getTemplate("email_template.html", "UTF-8");

            // 构建模板数据模型
            Map<String, Object> model = new HashMap<>();
            model.put("code", code);


            // 生成HTML 内容
            String htmlContent = FreeMarkerTemplateUtils.processTemplateIntoString(template, model);

            // 创建邮件消息
            MimeMessageHelper messageHelper = new MimeMessageHelper(mailSender.createMimeMessage(), true);
            // 发件人
            messageHelper.setFrom(appConfig.getSendUserName());
            // 收件人
            messageHelper.setTo(toEmail);
            // 设置发送时间
            messageHelper.setSentDate(new Date());
            // 内容（HTML 格式）
            messageHelper.setText(htmlContent, true);
            //设置主题
            messageHelper.setSubject("【EasyPan】邮箱验证码");

            // 发送邮件
            mailSender.send(messageHelper.getMimeMessage());
        } catch (Exception e) {
            logger.error("邮件发送失败", e);
            throw new BusinessException("邮件发送失败");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void sendEmailCode(String toEmail, Integer type) {
        //如果是注册，校验邮箱是否已存在
        if (Objects.equals(type, Constants.ZERO)) {
            UserInfo userInfo = userInfoMapper.selectByEmail(toEmail);
            if (null != userInfo) {
                throw new BusinessException("邮箱已经存在");
            }
        }

        String code = StringTools.getRandomNumber(Constants.LENGTH_5);
        sendEmailCode(toEmail, code);

        emailCodeMapper.disableEmailCode(toEmail);
        EmailCode emailCode = new EmailCode();
        emailCode.setCode(code);
        emailCode.setEmail(toEmail);
        emailCode.setStatus(Constants.ZERO);
        emailCode.setCreateTime(new Date());
        emailCodeMapper.insert(emailCode);
    }

    @Override
    public void checkCode(String email, String code) {
        EmailCode emailCode = emailCodeMapper.selectByEmailAndCode(email, code);
        if (null == emailCode) {
            throw new BusinessException("邮箱验证码不正确");
        }
        if (emailCode.getStatus() == 1 || System.currentTimeMillis() - emailCode.getCreateTime().getTime() > Constants.LENGTH_15 * 1000 * 60) {
            throw new BusinessException("邮箱验证码已失效");
        }
        emailCodeMapper.disableEmailCode(email);
    }
}