package org.automation.goofish.interceptor;

import org.automation.goofish.Starter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@SpringBootTest(classes = Starter.class)
public class TestAI {

    @Autowired
    AutoReplyService service;

    @Test
    void testReply(){

        String context = """
                (买家,在吗)
                (商家,您好，在的！有什么可以帮到您的吗？)
                (买家,你这是什么商品啊)
                (买家,你这卖的是什么)
                (买家,你这卖的啥)
                (买家,你这是啥)
                (买家,你这是啥)
                (买家,在吗)
                (买家,在吗)
                (买家,wei)
                (买家,在吗)
                (买家,在吗)
                (买家,q)
                (买家,你恢复了嘛)
                (买家,好了嘛)
                (商家,您好，在的！我这里出售的是ua协战之境偶像大师紫安提卡 紫偶卡组，带三张红卡，售价1600元。所有商品均为现货，仅出玩家，中介勿扰。如果您感兴趣的话，可以点击“我想要”与我私聊了解更多详情。)
                (买家,能不能便宜点啊)
                (商家,您好，目前这个价格已经是比较优惠的了，暂时没有降价空间哦。如果您对这套卡组感兴趣的话，可以考虑一下，质量绝对有保证！)
                """;
        String reply = service.generateReply(context).block();
        System.out.println(reply);
    }
}
