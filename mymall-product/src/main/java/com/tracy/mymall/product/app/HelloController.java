package com.tracy.mymall.product.app;

import org.redisson.Redisson;
import org.redisson.api.RLock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.CountDownLatch;

@RestController
public class HelloController {
    @Autowired
    private Redisson redisson;

    @GetMapping("/hello")
    public String simpleService() {
        RLock llock = redisson.getLock("llock");
        llock.lock();
        try{
            System.out.println(Thread.currentThread().getId()+"获取到锁");
            Thread.sleep(30 * 1000L);
        }catch(InterruptedException e) {
            Thread.currentThread().interrupt();
        }finally {
            llock.unlock();
        }
//        CountDownLatch
//        redisson.getCountDownLatch()
        return "hello";
    }

}
