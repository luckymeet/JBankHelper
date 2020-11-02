package com.mzj.financial.service;

import com.mzj.financial.vo.MismatchTransactionFlowVO;
import org.springframework.web.multipart.MultipartFile;

/**
 * 对账
 * @author yuminjun
 * @version 1.00
 * @date 2020/10/29 11:51
 * @record <pre>
 * version  author      date      desc
 * -------------------------------------------------
 * 1.00     yuminjun    2020/10/29     新建
 * -------------------------------------------------
 * </pre>
 */
public interface AccountCheckService {

    /**
     * 导入交易流水excel
     * @param file 交易流水excel文件
     */
    void importTransactionFlow(MultipartFile file);

    /**
     * 查询不匹配的流水
     * @return
     */
    MismatchTransactionFlowVO queryMismatchTransactionFlow(String type);
}
