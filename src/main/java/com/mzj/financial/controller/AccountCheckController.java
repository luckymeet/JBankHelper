package com.mzj.financial.controller;

import com.mzj.common.response.ResponseVO;
import com.mzj.financial.service.AccountCheckService;
import com.mzj.financial.vo.TransactionFlowListVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 对账
 * @author yuminjun
 * @version 1.00
 * @date 2020/10/28 11:54
 * @record <pre>
 * version  author      date      desc
 * -------------------------------------------------
 * 1.00     yuminjun    2020/10/28     新建
 * -------------------------------------------------
 * </pre>
 */
@RestController
@RequestMapping("/financial/account-check")
public class AccountCheckController {

    @Autowired
    private AccountCheckService accountCheckService;

    @PostMapping("/upload")
    public ResponseVO<String> importTransactionFlow(@RequestParam("file") MultipartFile file) {
        accountCheckService.importTransactionFlow(file);
        return ResponseVO.success(null, "核对成功");
    }

    @GetMapping("/list")
    public ResponseVO<TransactionFlowListVO> queryMismatchTransactionFlow(String type) {
        return ResponseVO.success(accountCheckService.queryMismatchTransactionFlow(type), "查询成功");
    }

    @GetMapping("export")
    public void download(HttpServletResponse response, String type) throws IOException {
        accountCheckService.exportResult(response, type);
    }
}
