package com.tracy.mymall.order.configuration;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.AlipayConfig;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.tracy.mymall.order.vo.PayVo;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@ConfigurationProperties(prefix = "alipay")
@Configuration
@Data
public class AliPayTemplate {
    // 应用ID,您的APPID，收款账号既是您的APPID对应支付宝账号
    private  String app_id = "2021000117690421";

    // 商户私钥，您的PKCS8格式RSA2私钥
    private  String merchant_private_key = "MIIEvAIBADANBgkqhkiG9w0BAQEFAASCBKYwggSiAgEAAoIBAQCnpWuFyTk3qk2mTuEjeXUL0MIhZAnwp/Oralm4XaRi8X1JhwquQ59VbBeNNjfMUFUMHOFVqmehYalUCYNYvI/6YdhzjNII39xik3/BsuMOcvVgESzkeQ1wxNFqgfB8Ki0SlF3CcbAY+umX84x6M3GRo19+ug4oeGbIJg3+nPppBUPSTNWiBb8H/x0mRLpvI8JdyQyoeXp4EquNk8F0QHRnPTR+GqjAXQ/+ah/FKRfDt/RhcDlhOv1vsd1cKjcjXCJzBhNb6tMwNEKa2WJyxMrL880XhQo7hcpMtHyK6kNkQ21NmEhtQpvzxFSV4WRFZ4Ab69Qc1aHnfJYnaIQyqs0TAgMBAAECggEAAfZGpt8vAz7D/+FGc0LVotjDnN5t21or2OK6wBSgeuP2omipMFbsQ3SrZeSvjH2YMTcZUAZZhiL9d1VRKzWxva1tehS41I/CJYkyYpL0Y1PnGUO6McZU6O/0v3lb1igsNN51pnPsKq6tEzEvgLKfbqPp1zLYPZMDKO6BFWzC5X28YkNNRqhy72CAzcKxmqjJhYU95PxoVIqnr3IqgnYk9DP9EsrX24kzjY1xPF4ooWqsaFbx3iRKpwgoqW/eDV9P+UN15GhZyChO3kO7lKVgFAEvhwzQwCM9BigiCYPaYwOjm+gJPFtvQjK1jJ6mUX5uuXfAE6Z4npT6CbRJCMy9iQKBgQDvKlGdUTR/yA+b7AKqQQ3/09vLTvdFH+4NHFt0HyKtzPzhq910BRVYuSyuH6waCAd/OwRJ6vKnSGBhtRKIlfoBEoZA1UV6bCqIEC7WJFVNJYWuXC/nuEFnDEm+WlJri+JrzR08sRlYkfew9H78Danhk7VqYxW+vbH6WzYkllfGhwKBgQCzcll/Fn/xijg0tOspbbQ9Qz85Q2EPT83DdPDj/HMU7AspgdQvmTdU5B15DX3+NWnB6Y7pbOJFsOTG4qkKl5vBuiNREWmJ/mrYuTFxJRAgEXtLC1KH/m5jdBk9I5zh5w5bb+kkTXXTBluTf/DyWYjPoRYwhH9hT07oVkTRZalcFQKBgAOewF2BPSox/Qp/KAsrsOqOamVJbLS/JvtL0paPYhb/Y1SHJXL8ILaXFvYLxK8gL/zdB9OWmtWtYsgX1Y/7tS0O+rlAgSy5NmuY0xyqsB/a0YsyGgeH4nWM2hDnjRfTpjm7gRXKGHgevYEQMlveWRPxCG3z0gBxHqCLgOZM4EdBAoGAbczpcDcvQHen3rBZOHdVEytjs0FEUZ8uM4wJnicpr8KjLdmwIRPqZHpUgm68CmFaffnQpYonhicwRXYJo16TQt4HoCI8ZuCTNltTIn1gOY8Y1xltfaM0now6qUtyQlQkV9TJXpo7H03DmSRvJYudiBZ0QmnAm6Vwjf/DijFfiOUCgYAvfBSXVXQcXP64grW+vGqRpNSg0dSOwQbkplMzdcHAoPAr0FLOC91K5qgy4JuhR6r1VMepcJrnta3im0bvuC9EuRTeg9VLlL/1kdpJ+Bu05hwoAebFkGMSaIHaNx82kFImcjYlhpQdIN6ni3HEcjyxIy8qAyDN8NjOotvuhNs5mw==";

    // 支付宝公钥,查看地址：https://openhome.alipay.com/platform/keyManage.htm 对应APPID下的支付宝公钥。
    private  String alipay_public_key = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAhm2JS7sW2pZEIOQAXZS8kPUOhKUK+CyowXH18LFogcfNo2rjLQ5WCfQQjja5MzP6rVj3nhv32HjOwgr0+JARY+GETU6BA3f+MlaOiDoQFok2hhe1lVo2SE8EbNClVdEqeuIX0dHceWp48psqglJTd3miIg/OsGkdW29GGyS2cuiH0ubz7Ry8NtuapHuDLehqIxluQYjsoT0hWYuDgDkeBAdCkPR/yFTbJMMnJI47F1uicSSe5tknsGuf+FsHhFq9gufiTrWgwjdDmat1dOPzMlw7eY6j8a3dtJKHkkaYCNJUxcN+YMjkefIcKJklxRbQWIM0zMS/H+d/uu75cGq5LwIDAQAB+CyowXH18LFogcfNo2rjLQ5WCfQQjja5MzP6rVj3nhv32HjOwgr0+JARY+GETU6BA3f+MlaOiDoQFok2hhe1lVo2SE8EbNClVdEqeuIX0dHceWp48psqglJTd3miIg/OsGkdW29GGyS2cuiH0ubz7Ry8NtuapHuDLehqIxluQYjsoT0hWYuDgDkeBAdCkPR/yFTbJMMnJI47F1uicSSe5tknsGuf+FsHhFq9gufiTrWgwjdDmat1dOPzMlw7eY6j8a3dtJKHkkaYCNJUxcN+YMjkefIcKJklxRbQWIM0zMS/H+d/uu75cGq5LwIDAQAB";

    // 服务器异步通知页面路径  需http://格式的完整路径，不能加?id=123这类自定义参数，必须外网可以正常访问
    private  String notify_url = "http://e4tp9n.natappfree.cc/pay/notify";

    // 页面跳转同步通知页面路径 需http://格式的完整路径，不能加?id=123这类自定义参数，必须外网可以正常访问
    private  String return_url = "http://e4tp9n.natappfree.cc/pay/return";

    // 签名方式
    private  String sign_type = "RSA2";

    // 字符编码格式
    private  String charset = "utf-8";

    // 支付宝网关
    private  String gatewayUrl = "https://openapi.alipaydev.com/gateway.do";
    private String timeout = "1m";

    public String pay(PayVo payVo)  {

        //获得初始化的AlipayClient
        AlipayClient alipayClient = new DefaultAlipayClient(gatewayUrl, app_id, merchant_private_key, "json", charset, alipay_public_key, sign_type);

        //设置请求参数
        AlipayTradePagePayRequest alipayRequest = new AlipayTradePagePayRequest();
        alipayRequest.setReturnUrl(return_url);
        alipayRequest.setNotifyUrl(notify_url);

        //商户订单号，商户网站订单系统中唯一订单号，必填
        String out_trade_no = payVo.getOut_trade_no();
        //付款金额，必填
        String total_amount = payVo.getTotal_amount();
        //订单名称，必填
        String subject = payVo.getSubject();
        //商品描述，可空
        String body = payVo.getBody();

        alipayRequest.setBizContent("{\"out_trade_no\":\""+ out_trade_no +"\","
                + "\"total_amount\":\""+ total_amount +"\","
                + "\"subject\":\""+ subject +"\","
                + "\"body\":\""+ body +"\","
                + "\"timeout_express\":\""+ timeout +"\"," // 1分钟过期

                + "\"product_code\":\"FAST_INSTANT_TRADE_PAY\"}");

        //若想给BizContent增加其他可选请求参数，以增加自定义超时时间参数timeout_express来举例说明
        //alipayRequest.setBizContent("{\"out_trade_no\":\""+ out_trade_no +"\","
        //		+ "\"total_amount\":\""+ total_amount +"\","
        //		+ "\"subject\":\""+ subject +"\","
        //		+ "\"body\":\""+ body +"\","
        //		+ "\"timeout_express\":\"10m\","
        //		+ "\"product_code\":\"FAST_INSTANT_TRADE_PAY\"}");
        //请求参数可查阅【电脑网站支付的API文档-alipay.trade.page.pay-请求参数】章节

        //请求
        //返回的结果是支付页面
        String result = null;
        try {
            result = alipayClient.pageExecute(alipayRequest).getBody();
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }

        //输出
        System.out.println(result);
        return result;
    }


}
