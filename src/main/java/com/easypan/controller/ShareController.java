package com.easypan.controller;

import com.easypan.annotation.GlobalInterceptor;
import com.easypan.annotation.VerifyParam;
import com.easypan.entity.dto.SessionWebUserDto;
import com.easypan.entity.po.FileShare;
import com.easypan.entity.query.FileShareQuery;
import com.easypan.entity.vo.PaginationResultVO;
import com.easypan.entity.vo.ResponseVO;
import com.easypan.service.FileShareService;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.annotation.Resource;

//分享
@RestController("shareController")
@RequestMapping("/share")
public class ShareController extends ABaseController {
    @Resource
    private FileShareService fileShareService;


    /**
     *加载分享列表
     */
    @RequestMapping("/loadShareList")
    @GlobalInterceptor(checkParams = true)
    public ResponseVO loadShareList(HttpSession session, FileShareQuery query) {
        query.setOrderBy("share_time desc");
        SessionWebUserDto userDto = getUserInfoFromSession(session);
        query.setUserId(userDto.getUserId());
        query.setQueryFileName(true);
        PaginationResultVO resultVO = this.fileShareService.findListByPage(query);
        return getSuccessResponseVO(resultVO);
    }

    /**
     *分享文件
     */
    @RequestMapping("/shareFile")
    @GlobalInterceptor(checkParams = true)
    public ResponseVO shareFile(HttpSession session,
                                @VerifyParam(required = true) String fileId,
                                @VerifyParam(required = true) Integer validType,
                                String code) {
        SessionWebUserDto userDto = getUserInfoFromSession(session);
        FileShare share = new FileShare();
        share.setFileId(fileId);
        share.setValidType(validType);
        share.setCode(code);
        share.setUserId(userDto.getUserId());
        fileShareService.saveShare(share);
        return getSuccessResponseVO(share);
    }

    /**
     *取消分享
     */
    @RequestMapping("/cancelShare")
    @GlobalInterceptor(checkParams = true)
    public ResponseVO cancelShare(HttpSession session, @VerifyParam(required = true) String shareIds) {
        SessionWebUserDto userDto = getUserInfoFromSession(session);
        fileShareService.deleteFileShareBatch(shareIds.split(","), userDto.getUserId());
        return getSuccessResponseVO(null);
    }
}
