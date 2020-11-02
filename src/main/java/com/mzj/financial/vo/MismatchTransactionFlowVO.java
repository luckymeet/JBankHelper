package com.mzj.financial.vo;

import com.mzj.financial.po.TransactionFlow;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.List;

/**
 * 不匹配的流水
 * @author yuminjun
 * @version 1.00
 * @date 2020/11/2 15:00
 * @record <pre>
 * version  author      date      desc
 * -------------------------------------------------
 * 1.00     yuminjun    2020/11/2     新建
 * -------------------------------------------------
 * </pre>
 */
@Getter
@Setter
@ToString
public class MismatchTransactionFlowVO {

    List<TransactionFlow> sheetList0;

    List<TransactionFlow> sheetList1;

}
