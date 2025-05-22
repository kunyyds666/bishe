package com.easypan.controller;

import com.easypan.entity.constants.Constants;
import com.easypan.entity.dto.SessionShareDto;
import com.easypan.entity.dto.SessionWebUserDto;
import com.easypan.entity.enums.ResponseCodeEnum;
import com.easypan.entity.vo.PaginationResultVO;
import com.easypan.entity.vo.ResponseVO;
import com.easypan.utils.CopyTools;
import com.easypan.utils.StringTools;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.OutputStream;


public class ABaseController {

    private static final Logger logger = LoggerFactory.getLogger(ABaseController.class);

    protected static final String STATUS_SUCCESS = "success";

    protected static final String STATUS_ERROR = "error";

    protected <T> ResponseVO getSuccessResponseVO(T t) {
        ResponseVO<T> responseVO = new ResponseVO<>();
        responseVO.setStatus(STATUS_SUCCESS);
        responseVO.setCode(ResponseCodeEnum.CODE_200.getCode());
        responseVO.setInfo(ResponseCodeEnum.CODE_200.getMsg());
        responseVO.setData(t);
        return responseVO;
    }

    protected <S, T> PaginationResultVO<T> convert2PaginationVO(PaginationResultVO<S> result, Class<T> classz) {
        PaginationResultVO<T> resultVO = new PaginationResultVO<>();
        resultVO.setList(CopyTools.copyList(result.getList(), classz));
        resultVO.setPageNo(result.getPageNo());
        resultVO.setPageSize(result.getPageSize());
        resultVO.setPageTotal(result.getPageTotal());
        resultVO.setTotalCount(result.getTotalCount());
        return resultVO;
    }

    protected SessionWebUserDto getUserInfoFromSession(HttpSession session) {
        return (SessionWebUserDto) session.getAttribute(Constants.SESSION_KEY);
    }


    protected SessionShareDto getSessionShareFromSession(HttpSession session, String shareId) {
        return (SessionShareDto) session.getAttribute(Constants.SESSION_SHARE_KEY + shareId);
    }


    protected void readFile(HttpServletResponse response, String filePath, String contentType) {
        if (!StringTools.pathIsOk(filePath)) {
            return;
        }

        try (FileInputStream in = new FileInputStream(filePath);
             OutputStream out = response.getOutputStream()) {

            File file = new File(filePath);
            if (!file.exists()) {
                return;
            }

            // 设置响应头
            response.setContentType(contentType != null ? contentType : "application/octet-stream");
            response.setContentLengthLong(file.length());
            response.setHeader("Accept-Ranges", "bytes");

            byte[] buffer = new byte[1024];
            int len;
            while ((len = in.read(buffer)) != -1) {
                out.write(buffer, 0, len);
            }
            out.flush();
        } catch (Exception e) {
            logger.error("读取文件异常", e);
        }
    }

}
