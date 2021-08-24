package com.tracy.mymall.order.listener;

import com.alipay.api.AlipayApiException;
import com.alipay.api.internal.util.AlipaySignature;
import com.tracy.mymall.order.configuration.AliPayTemplate;
import com.tracy.mymall.order.service.OrderService;
import com.tracy.mymall.order.vo.PayAsyncVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * 支付宝异步回调接口，成功后用来标识订单状态，插入付款记录
 */
@Controller
@Slf4j
public class AliPayListener {

    @Autowired
    private AliPayTemplate aliPayTemplate;

    @Autowired
    private OrderService orderService;

    /**
     * 如果成功，则需要向支付宝返回success， 否则返回任意字符串
     * @param payAsyncVo
     * @param request
     * @return
     * @throws AlipayApiException
     */
    @PostMapping("/pay/notify")
    public String dealPayResult(PayAsyncVo payAsyncVo, HttpServletRequest request) throws AlipayApiException {
        log.info("【订单-付款】收到支付宝通知的付款数据[{}]", payAsyncVo.toString());
        // 需要验证数据是不是支付宝返回的
        // 验签
        Map<String,String> params = new HashMap<>();
        Map<String,String[]> requestParams = request.getParameterMap();
        for (Iterator<String> iter = requestParams.keySet().iterator(); iter.hasNext();) {
            String name = iter.next();
            String[] values = requestParams.get(name);
            String valueStr = "";
            for (int i = 0; i < values.length; i++) {
                valueStr = (i == values.length - 1) ? valueStr + values[i]
                        : valueStr + values[i] + ",";
            }
            //乱码解决，这段代码在出现乱码时使用
//			valueStr = new String(valueStr.getBytes("ISO-8859-1"), "utf-8");
            params.put(name, valueStr);
        }

        // 如果是支付宝返回的 ，则需要插入付款信息和更新订单状态
        // 只要我们收到了支付宝给我们的异步通知 验签成功 我们就要给支付宝返回success
        if(AlipaySignature.rsaCheckV1(params, aliPayTemplate.getAlipay_public_key(), aliPayTemplate.getCharset(), aliPayTemplate.getSign_type())){
            return orderService.handlePayResult(payAsyncVo);
        }
        log.warn("\n受到恶意验签攻击");
        return "fail";


    }

    @GetMapping("/pay/return")
    public String dealPayResult() {
        return "redirect:http://mymall.com:1111";
    }
}
