package com.tracy.mymall.thirdpart.component;

import com.aliyun.dysmsapi20170525.Client;
import com.aliyun.dysmsapi20170525.models.SendSmsRequest;
import com.aliyun.teaopenapi.models.Config;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 阿里云短信接口api
 */
@ConfigurationProperties(prefix = "springcloud.cloud.alicloud.sms")
@Configuration
public class SmsSenderComponent {
    private String accessKey;
    private String secretKey;
    private String host;
    private String modelId;
    private String signName;


    @Bean
    public Client client() throws Exception {
        Config config = new Config()
                // 您的AccessKey ID
                .setAccessKeyId(accessKey)
                // 您的AccessKey Secret
                .setAccessKeySecret(secretKey);
        // 访问的域名
        config.endpoint = host;
        return new com.aliyun.dysmsapi20170525.Client(config);
    }


    public void sendSms(String phoneNumber) throws Exception {
//        SendSmsRequest sendSmsRequest = new SendSmsRequest()
//                .setPhoneNumbers(phoneNumber)
//                .setSignName(signName)
//                .setTemplateCode(modelId)
//                .setTemplateParam("{\"code\":\"1234\"}");
//        client().sendSms(sendSmsRequest)
        // 复制代码运行请自行打印 API 的返回值

        System.out.println(client().toString());
    }
}
