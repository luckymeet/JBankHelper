package com.mzj.financial.po;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.format.DateTimeFormat;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.mzj.common.Constans;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.util.Date;


/**
 * 交易流水
 * @author yuminjun
 * @version 1.00
 * @date 2020/10/29 16:18
 * @record <pre>
 * version  author      date      desc
 * -------------------------------------------------
 * 1.00     yuminjun    2020/10/29     新建
 * -------------------------------------------------
 * </pre>
 */
@Getter
@Setter
@Document("transaction_flow")
@CompoundIndexes({
        @CompoundIndex(name = "idx_tf", def = "{'accName': 1, 'tradeDate': 1}")
})
@ColumnWidth(25)
public class TransactionFlow {

    /**
     * 交易流水号
     */
    @ExcelProperty("交易流水号")
    private String tradeNo;
    /**
     * 账户
     */
    @ExcelProperty("账户")
    @Indexed
    private String accName;
    /**
     * 对方账户
     */
    @ExcelProperty("对方账户")
    private String opsAccName;
    /**
     * 交易日期
     */
    @ExcelProperty("交易日期")
//    @DateTimeFormat(Constans.dataFormat)
    private String tradeDate;
    /**
     * 贷出金额
     */
    @ExcelProperty("贷出金额")
    private BigDecimal lendAmt;
    /**
     * 借入金额
     */
    @ExcelProperty("借入金额")
    private BigDecimal borrowAmt;
    /**
     * 备注
     */
    @ExcelProperty("备注")
    private String remark;
    /**
     * 是否核对成功
     */
    @ExcelProperty("核对结果")
    private String check;

}
