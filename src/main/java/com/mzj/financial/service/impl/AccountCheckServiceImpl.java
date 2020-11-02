package com.mzj.financial.service.impl;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.util.DateUtils;
import com.alibaba.fastjson.JSON;
import com.mzj.common.Constans;
import com.mzj.common.response.ResponseCode;
import com.mzj.common.response.ResponseVO;
import com.mzj.financial.po.TransactionFlow;
import com.mzj.financial.service.AccountCheckService;
import com.mzj.financial.vo.TransactionFlowListVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.text.ParseException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 对账
 *
 * @author yuminjun
 * @version 1.00
 * @date 2020/10/29 11:52
 * @record <pre>
 * version  author      date      desc
 * -------------------------------------------------
 * 1.00     yuminjun    2020/10/29     新建
 * -------------------------------------------------
 * </pre>
 */
@Slf4j
@Service
public class AccountCheckServiceImpl implements AccountCheckService {

    @Autowired
    private MongoTemplate mongoTemplate;

    private final String TRANSACTION_FLOW_0 = "transaction_flow_0";

    private final String TRANSACTION_FLOW_1 = "transaction_flow_1";

    /**
     * 导入交易流水excel
     *
     * @param file 交易流水excel文件
     */
    @Override
    public void importTransactionFlow(MultipartFile file) {
        List<TransactionFlow> list0 = null;
        List<TransactionFlow> list1 = null;
        try {
            list0 = EasyExcel.read(file.getInputStream()).head(TransactionFlow.class).sheet(0).doReadSync();
            list1 = EasyExcel.read(file.getInputStream()).head(TransactionFlow.class).sheet(1).doReadSync();
        } catch (IOException e) {
            e.printStackTrace();
        }
        mongoTemplate.dropCollection(TRANSACTION_FLOW_0);
        mongoTemplate.dropCollection(TRANSACTION_FLOW_1);
        mongoTemplate.insert(list0, TRANSACTION_FLOW_0);
        mongoTemplate.insert(list1, TRANSACTION_FLOW_1);
    }

    @Override
    public TransactionFlowListVO queryMismatchTransactionFlow(String type) {
        return getTransactionFlowListVO(type, false);
    }

    private TransactionFlowListVO getTransactionFlowListVO(String type, boolean isAll) {
        List<TransactionFlow> list0 = mongoTemplate.findAll(TransactionFlow.class, TRANSACTION_FLOW_0);
        List<TransactionFlow> list1 = mongoTemplate.findAll(TransactionFlow.class, TRANSACTION_FLOW_1);
        Map<String, TransactionFlow> map0 = listToTreeMap(list0, type);
        Map<String, TransactionFlow> map1 = listToTreeMap(list1, type);
        List<TransactionFlow> sheetList0 = new ArrayList<>();
        List<TransactionFlow> sheetList1 = new ArrayList<>();

        for (Map.Entry<String, TransactionFlow> entry : map0.entrySet()) {
            String key = entry.getKey();
            TransactionFlow tf0 = entry.getValue();
            TransactionFlow tf1 = map1.get(key);
            if (tf1 == null) {
                tf0.setCheck("✘");
                sheetList0.add(tf0);
                continue;
            }
            // 企业总借入金额，一般为负数
            BigDecimal totalBorrowAmt = this.subtract(tf0.getLendAmt(), tf0.getBorrowAmt());
            // 银行总贷出金额，一般为正数
            BigDecimal totalLendAmt = this.subtract(tf0.getLendAmt(), tf1.getBorrowAmt());
            if (this.add(totalBorrowAmt, totalLendAmt).compareTo(BigDecimal.ZERO) != 0) {
                tf0.setCheck("✘");
                sheetList0.add(tf0);
            }
            if (isAll) {
                tf0.setCheck("✔");
                sheetList0.add(tf0);
            }
        }

        for (Map.Entry<String, TransactionFlow> entry : map1.entrySet()) {
            String key = entry.getKey();
            TransactionFlow tf1 = entry.getValue();
            TransactionFlow tf0 = map0.get(key);
            if (tf0 == null) {
                tf1.setCheck("✘");
                sheetList1.add(tf1);
                continue;
            }
            // 企业总借入金额，一般为负数
            BigDecimal totalBorrowAmt = this.subtract(tf0.getLendAmt(), tf0.getBorrowAmt());
            // 银行总贷出金额，一般为正数
            BigDecimal totalLendAmt = this.subtract(tf0.getLendAmt(), tf1.getBorrowAmt());
            if (this.add(totalBorrowAmt, totalLendAmt).compareTo(BigDecimal.ZERO) != 0) {
                tf1.setCheck("✘");
                sheetList1.add(tf1);
            }
            if (isAll) {
                tf1.setCheck("✔");
                sheetList1.add(tf1);
            }
        }

        TransactionFlowListVO vo = new TransactionFlowListVO();
        vo.setSheetList0(sheetList0);
        vo.setSheetList1(sheetList1);
        return vo;
    }

    @Override
    public void exportResult(HttpServletResponse response, String type) throws IOException {
        ExcelWriter excelWriter = null;
        try {
            response.setContentType("application/vnd.ms-excel");
            response.setCharacterEncoding("utf-8");
            String fileName = URLEncoder.encode("对账结果", "UTF-8").replaceAll("\\+", "%20");
            response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");

            TransactionFlowListVO transactionFlowListVO = getTransactionFlowListVO(type, true);

            excelWriter = EasyExcel.write(response.getOutputStream(), TransactionFlow.class).build();
            excelWriter.write(transactionFlowListVO.getSheetList0(), EasyExcel.writerSheet(0, "逆时账").build());
            excelWriter.write(transactionFlowListVO.getSheetList1(), EasyExcel.writerSheet(1, "银行流水").build());
        } catch (Exception e) {
            log.error("下载文件失败", e);
            // 重置response
            response.reset();
            response.setContentType("application/json");
            response.setCharacterEncoding("utf-8");
            ResponseVO<Object> responseVO = ResponseVO.fail(ResponseCode.ERR_500.getCode(), "下载文件失败");
            response.getWriter().println(JSON.toJSONString(responseVO));
        } finally {
            // 千万别忘记finish 会帮忙关闭流
            if (excelWriter != null) {
                excelWriter.finish();
            }
        }
    }

    private Map<String, TransactionFlow> listToTreeMap(List<TransactionFlow> list, String type) {
        return list.stream().collect(Collectors.toMap(a -> getKey(a, type), a -> a, (k1, k2) -> {
            k1.setBorrowAmt(this.add(k1.getBorrowAmt(), k2.getBorrowAmt()));
            k1.setLendAmt(this.add(k1.getLendAmt(), k2.getLendAmt()));
            return k1;
        }, TreeMap::new));
    }

    private String getKey(TransactionFlow tf, String type) {
        type = Optional.ofNullable(type).orElse("10");
        StringBuilder key = new StringBuilder(tf.getAccName());
        if (type.charAt(0) == '1') {
            key.append('-').append(Optional.ofNullable(tf.getOpsAccName()).orElse(""));
        }
        if (type.charAt(1) == '1') {
            try {
                String tradeDate = DateUtils.format(DateUtils.parseDate(tf.getTradeDate(), Constans.dataFormat), "yyyy年M月d日");
                tf.setTradeDate(tradeDate);
                key.append('-').append(tradeDate);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        } else if (type.charAt(1) == '2') {
            try {
                String tradeDate = DateUtils.format(DateUtils.parseDate(tf.getTradeDate(), Constans.dataFormat), "yyyy年M月");
                tf.setTradeDate(tradeDate);
                key.append('-').append(tradeDate);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        return key.toString();
    }

    private BigDecimal add(BigDecimal bd1, BigDecimal bd2) {
        return Optional.ofNullable(bd1).orElse(BigDecimal.ZERO).add(Optional.ofNullable(bd2).orElse(BigDecimal.ZERO));
    }

    private BigDecimal subtract(BigDecimal bd1, BigDecimal bd2) {
        return Optional.ofNullable(bd1).orElse(BigDecimal.ZERO).subtract(Optional.ofNullable(bd2).orElse(BigDecimal.ZERO));
    }

}
