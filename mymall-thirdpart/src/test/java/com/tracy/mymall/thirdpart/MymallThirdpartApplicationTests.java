package com.tracy.mymall.thirdpart;

import com.aliyun.dysmsapi20170525.models.SendSmsRequest;
import com.aliyun.teaopenapi.models.Config;
import com.tracy.mymall.thirdpart.component.SmsSenderComponent;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class MymallThirdpartApplicationTests {

    @Test
    void contextLoads() {
    }
    @Autowired
    SmsSenderComponent smsSenderComponent;

    /**
     * 使用AK&SK初始化账号Client
     * @param accessKeyId
     * @param accessKeySecret
     * @return Client
     * @throws Exception
     */
    public static com.aliyun.dysmsapi20170525.Client createClient(String accessKeyId, String accessKeySecret) throws Exception {
        Config config = new Config()
                // 您的AccessKey ID
                .setAccessKeyId(accessKeyId)
                // 您的AccessKey Secret
                .setAccessKeySecret(accessKeySecret);
        // 访问的域名
        config.endpoint = "dysmsapi.aliyuncs.com";
        return new com.aliyun.dysmsapi20170525.Client(config);
    }

    @Test
    public void sms() throws Exception {
        smsSenderComponent.sendSms("18251980272");
    }

    @Test
    public void sendSms(String[] args_) throws Exception {
        java.util.List<String> args = java.util.Arrays.asList(args_);
        com.aliyun.dysmsapi20170525.Client client = createClient("xxxxx", "xxxxxx");
        SendSmsRequest sendSmsRequest = new SendSmsRequest()
                .setPhoneNumbers("18251980272")
                .setSignName("[测试专用]阿里云通信")
                .setTemplateCode("SMS_218170092")
                                        .setTemplateParam("{\"code\":\"1234\"}");
        // 复制代码运行请自行打印 API 的返回值
        System.out.println(client.sendSms(sendSmsRequest));
    }

}
