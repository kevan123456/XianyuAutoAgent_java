package org.automation.goofish;

import org.automation.goofish.data.ChatContext;
import org.automation.goofish.data.ChatRepository;
import org.automation.goofish.data.ItemRepository;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import reactor.core.publisher.Mono;

import static net.bytebuddy.implementation.bytecode.member.MethodInvocation.lookup;

@ActiveProfiles("test")
@SpringBootTest(classes = Starter.class)
public class TestDatabase {

    static final Logger logger = LoggerFactory.getLogger(lookup().getClass());
    @Autowired
    ChatRepository chatRepository;

    @Autowired
    ItemRepository itemRepository;

    @Test
    void testQuery() {

        ChatContext chatContext = new ChatContext("4531751557270722", chatHistory, "1751550171041");
        chatRepository.insert(chatContext).block();

        Mono<ChatContext> byId = chatRepository.findById("4531751557270722");
        ChatContext block = byId.block();
        logger.info(block.toString());
    }

    String chatHistory = """
            "userMessageModels" : [ {
                  "readStatus" : 2,
                  "userExtension" : {
                    "needPush" : "true"
                  },
                  "recallFeature" : {
                    "showRecallStatusSetting" : 1,
                    "operatorType" : 0
                  },
                  "message" : {
                    "extension" : {
                      "reminderContent" : "[举杯]",
                      "senderUserId" : "2218732689285",
                      "umid" : "WV610495b63ec2b2f202127dd7485386a",
                      "utdid" : "ZFS6eIvxeiEDAG6jQ0omVLGV",
                      "extJson" : "{\\"quickReply\\":\\"1\\",\\"utdid\\":\\"ZFS6eIvxeiEDAG6jQ0omVLGV\\",\\"messageId\\":\\"294216caea384bf8b35281f5253f683d\\",\\"umidToken\\":\\"lkYBhuFLPERxIRKXy3fuIqskzmXiXTYh\\",\\"tag\\":\\"u\\"}",
                      "umidToken" : "lkYBhuFLPERxIRKXy3fuIqskzmXiXTYh",
                      "_platform" : "ios",
                      "reminderNotice" : "发来一条新消息",
                      "senderUserType" : "0",
                      "detailNotice" : "[举杯]",
                      "port" : "62571",
                      "clientIp" : "183.76.102.166",
                      "sessionType" : "1",
                      "reminderUrl" : "fleamarket://message_chat?itemId=767254271372&peerUserId=2218732689285&sid=51388124157&messageId=294216caea384bf8b35281f5253f683d&adv=no",
                      "bizTag" : "{\\"sourceId\\":\\"S:1\\",\\"messageId\\":\\"294216caea384bf8b35281f5253f683d\\"}",
                      "reminderTitle" : "七星数码是DS直接拍"
                    },
                    "messageId" : "3592104759482.PNM",
                    "unreadCount" : 0,
                    "msgReadStatusSetting" : 1,
                    "createAt" : 1751557270331,
                    "content" : {
                      "custom" : {
                        "summary" : "[举杯]",
                        "data" : "eyJhdFVzZXJzIjpbXSwiY29udGVudFR5cGUiOjEsInRleHQiOnsidGV4dCI6IlvkuL7mna9dIn19",
                        "type" : 1
                      },
                      "contentType" : 101
                    },
                    "displayStyle" : 0,
                    "redPointPolicy" : 0,
                    "sender" : {
                      "uid" : "2218732689285@goofish",
                      "tag" : 0
                    },
                    "receiverCount" : 2,
                    "cid" : "51388124157@goofish"
                  },
                  "msgStatus" : 1
                }, {
                  "readStatus" : 2,
                  "userExtension" : {
                    "needPush" : "false"
                  },
                  "recallFeature" : {
                    "showRecallStatusSetting" : 1,
                    "operatorType" : 0
                  },
                  "message" : {
                    "extension" : {
                      "reminderContent" : "再来再来",
                      "senderUserId" : "2150369819",
                      "umid" : "WV6101d34659a9f8920213fc9146cbc61",
                      "utdid" : "ZZqfhZIbsZADAIGYfeRRPOok",
                      "extJson" : "{\\"quickReply\\":\\"1\\",\\"utdid\\":\\"ZZqfhZIbsZADAIGYfeRRPOok\\",\\"messageId\\":\\"1974d9a7858b4432a70bb24523f3335d\\",\\"umidToken\\":\\"gx4BnFxLPJrdwhKX0IhP1VQUEcZ0clQI\\",\\"tag\\":\\"u\\"}",
                      "umidToken" : "gx4BnFxLPJrdwhKX0IhP1VQUEcZ0clQI",
                      "_platform" : "ios",
                      "reminderNotice" : "发来一条新消息",
                      "senderUserType" : "0",
                      "detailNotice" : "再来再来",
                      "port" : "64603",
                      "clientIp" : "218.82.103.117",
                      "sessionType" : "1",
                      "reminderUrl" : "fleamarket://message_chat?itemId=767254271372&peerUserId=2150369819&sid=51388124157&messageId=1974d9a7858b4432a70bb24523f3335d&adv=no",
                      "bizTag" : "{\\"sourceId\\":\\"S:1\\",\\"messageId\\":\\"1974d9a7858b4432a70bb24523f3335d\\"}",
                      "reminderTitle" : "Joki2020"
                    },
                    "messageId" : "3592118743651.PNM",
                    "unreadCount" : 0,
                    "msgReadStatusSetting" : 1,
                    "createAt" : 1751557243688,
                    "content" : {
                      "custom" : {
                        "summary" : "再来再来",
                        "data" : "eyJhdFVzZXJzIjpbXSwiY29udGVudFR5cGUiOjEsInRleHQiOnsidGV4dCI6IuWGjeadpeWGjeadpSJ9fQ==",
                        "type" : 1
                      },
                      "contentType" : 101
                    },
                    "displayStyle" : 0,
                    "redPointPolicy" : 0,
                    "sender" : {
                      "uid" : "2150369819@goofish",
                      "tag" : 0
                    },
                    "receiverCount" : 2,
                    "cid" : "51388124157@goofish"
                  },
                  "msgStatus" : 1
                }, {
                  "readStatus" : 2,
                  "userExtension" : {
                    "needPush" : "true"
                  },
                  "recallFeature" : {
                    "showRecallStatusSetting" : 1,
                    "operatorType" : 0
                  },
                  "message" : {
                    "extension" : {
                      "reminderContent" : "[激推]",
                      "senderUserId" : "2218732689285",
                      "umid" : "WV610495b63ec2b2f202127dd7485386a",
                      "utdid" : "ZFS6eIvxeiEDAG6jQ0omVLGV",
                      "extJson" : "{\\"quickReply\\":\\"1\\",\\"utdid\\":\\"ZFS6eIvxeiEDAG6jQ0omVLGV\\",\\"messageId\\":\\"ed1b09aaf9a5488a9a1ef502d33f1b99\\",\\"umidToken\\":\\"lkYBhuFLPERxIRKXy3fuIqskzmXiXTYh\\",\\"tag\\":\\"u\\"}",
                      "umidToken" : "lkYBhuFLPERxIRKXy3fuIqskzmXiXTYh",
                      "_platform" : "ios",
                      "reminderNotice" : "发来一条新消息",
                      "senderUserType" : "0",
                      "detailNotice" : "[激推]",
                      "port" : "63306",
                      "clientIp" : "183.76.102.166",
                      "sessionType" : "1",
                      "reminderUrl" : "fleamarket://message_chat?itemId=767254271372&peerUserId=2218732689285&sid=51388124157&messageId=ed1b09aaf9a5488a9a1ef502d33f1b99&adv=no",
                      "bizTag" : "{\\"sourceId\\":\\"S:1\\",\\"messageId\\":\\"ed1b09aaf9a5488a9a1ef502d33f1b99\\"}",
                      "reminderTitle" : "七星数码是DS直接拍"
                    },
                    "messageId" : "3596479199738.PNM",
                    "unreadCount" : 0,
                    "msgReadStatusSetting" : 1,
                    "createAt" : 1751556365710,
                    "content" : {
                      "custom" : {
                        "summary" : "[激推]",
                        "data" : "eyJhdFVzZXJzIjpbXSwiY29udGVudFR5cGUiOjEsInRleHQiOnsidGV4dCI6Ilvmv4DmjqhdIn19",
                        "type" : 1
                      },
                      "contentType" : 101
                    },
                    "displayStyle" : 0,
                    "redPointPolicy" : 0,
                    "sender" : {
                      "uid" : "2218732689285@goofish",
                      "tag" : 0
                    },
                    "receiverCount" : 2,
                    "cid" : "51388124157@goofish"
                  },
                  "msgStatus" : 1
                }, {
                  "readStatus" : 2,
                  "userExtension" : {
                    "needPush" : "false"
                  },
                  "recallFeature" : {
                    "showRecallStatusSetting" : 1,
                    "operatorType" : 0
                  },
                  "message" : {
                    "extension" : {
                      "_platform" : "web",
                      "reminderContent" : "auto reply",
                      "reminderNotice" : "发来一条新消息",
                      "senderUserType" : "0",
                      "detailNotice" : "auto reply",
                      "senderUserId" : "2150369819",
                      "extJson" : "{\\"quickReply\\":\\"1\\",\\"messageId\\":\\"bb6b3e2423404fc79a5f67e43d4eaa95\\",\\"tag\\":\\"u\\"}",
                      "sessionType" : "1",
                      "reminderUrl" : "fleamarket://message_chat?itemId=767254271372&peerUserId=2150369819&sid=51388124157&messageId=bb6b3e2423404fc79a5f67e43d4eaa95&adv=no",
                      "bizTag" : "{\\"sourceId\\":\\"S:1\\",\\"messageId\\":\\"bb6b3e2423404fc79a5f67e43d4eaa95\\"}",
                      "reminderTitle" : "Joki2020"
                    },
                    "messageId" : "3592102700053.PNM",
                    "unreadCount" : 0,
                    "msgReadStatusSetting" : 1,
                    "createAt" : 1751556358946,
                    "content" : {
                      "custom" : {
                        "summary" : "auto reply",
                        "data" : "eyJjb250ZW50VHlwZSI6MSwidGV4dCI6eyJ0ZXh0IjoiYXV0byByZXBseSJ9fQ==",
                        "type" : 1
                      },
                      "contentType" : 101
                    },
                    "displayStyle" : 0,
                    "redPointPolicy" : 0,
                    "sender" : {
                      "uid" : "2150369819@goofish",
                      "tag" : 0
                    },
                    "receiverCount" : 2,
                    "cid" : "51388124157@goofish"
                  },
                  "msgStatus" : 1
                }, {
                  "readStatus" : 2,
                  "userExtension" : {
                    "needPush" : "true"
                  },
                  "recallFeature" : {
                    "showRecallStatusSetting" : 1,
                    "operatorType" : 0
                  },
                  "message" : {
                    "extension" : {
                      "reminderContent" : "[激推]",
                      "senderUserId" : "2218732689285",
                      "umid" : "WV610495b63ec2b2f202127dd7485386a",
                      "utdid" : "ZFS6eIvxeiEDAG6jQ0omVLGV",
                      "extJson" : "{\\"quickReply\\":\\"1\\",\\"utdid\\":\\"ZFS6eIvxeiEDAG6jQ0omVLGV\\",\\"messageId\\":\\"a6dee611f899498ba750d7e7248be726\\",\\"umidToken\\":\\"lkYBhuFLPERxIRKXy3fuIqskzmXiXTYh\\",\\"tag\\":\\"u\\"}",
                      "umidToken" : "lkYBhuFLPERxIRKXy3fuIqskzmXiXTYh",
                      "_platform" : "ios",
                      "reminderNotice" : "发来一条新消息",
                      "senderUserType" : "0",
                      "detailNotice" : "[激推]",
                      "port" : "63306",
                      "clientIp" : "183.76.102.166",
                      "sessionType" : "1",
                      "reminderUrl" : "fleamarket://message_chat?itemId=767254271372&peerUserId=2218732689285&sid=51388124157&messageId=a6dee611f899498ba750d7e7248be726&adv=no",
                      "bizTag" : "{\\"sourceId\\":\\"S:1\\",\\"messageId\\":\\"a6dee611f899498ba750d7e7248be726\\"}",
                      "reminderTitle" : "七星数码是DS直接拍"
                    },
                    "messageId" : "3592090987929.PNM",
                    "unreadCount" : 0,
                    "msgReadStatusSetting" : 1,
                    "createAt" : 1751556349105,
                    "content" : {
                      "custom" : {
                        "summary" : "[激推]",
                        "data" : "eyJhdFVzZXJzIjpbXSwiY29udGVudFR5cGUiOjEsInRleHQiOnsidGV4dCI6Ilvmv4DmjqhdIn19",
                        "type" : 1
                      },
                      "contentType" : 101
                    },
                    "displayStyle" : 0,
                    "redPointPolicy" : 0,
                    "sender" : {
                      "uid" : "2218732689285@goofish",
                      "tag" : 0
                    },
                    "receiverCount" : 2,
                    "cid" : "51388124157@goofish"
                  },
                  "msgStatus" : 1
                }, {
                  "readStatus" : 2,
                  "userExtension" : {
                    "needPush" : "false"
                  },
                  "recallFeature" : {
                    "showRecallStatusSetting" : 1,
                    "operatorType" : 0
                  },
                  "message" : {
                    "extension" : {
                      "_platform" : "web",
                      "reminderContent" : "发个消息老哥",
                      "reminderNotice" : "发来一条新消息",
                      "senderUserType" : "0",
                      "detailNotice" : "发个消息老哥",
                      "senderUserId" : "2150369819",
                      "extJson" : "{\\"quickReply\\":\\"1\\",\\"messageId\\":\\"0a654feca1ac4b6eb7ba0a696d30db29\\",\\"tag\\":\\"u\\"}",
                      "sessionType" : "1",
                      "reminderUrl" : "fleamarket://message_chat?itemId=767254271372&peerUserId=2150369819&sid=51388124157&messageId=0a654feca1ac4b6eb7ba0a696d30db29&adv=no",
                      "bizTag" : "{\\"sourceId\\":\\"S:1\\",\\"messageId\\":\\"0a654feca1ac4b6eb7ba0a696d30db29\\"}",
                      "reminderTitle" : "Joki2020"
                    },
                    "messageId" : "3596479185529.PNM",
                    "unreadCount" : 0,
                    "msgReadStatusSetting" : 1,
                    "createAt" : 1751556340898,
                    "content" : {
                      "custom" : {
                        "summary" : "发个消息老哥",
                        "data" : "eyJjb250ZW50VHlwZSI6MSwidGV4dCI6eyJ0ZXh0Ijoi5Y+R5Liq5raI5oGv6ICB5ZOlIn19",
                        "type" : 1
                      },
                      "contentType" : 101
                    },
                    "displayStyle" : 0,
                    "redPointPolicy" : 0,
                    "sender" : {
                      "uid" : "2150369819@goofish",
                      "tag" : 0
                    },
                    "receiverCount" : 2,
                    "cid" : "51388124157@goofish"
                  },
                  "msgStatus" : 1
                }, {
                  "readStatus" : 2,
                  "userExtension" : {
                    "needPush" : "true"
                  },
                  "recallFeature" : {
                    "showRecallStatusSetting" : 1,
                    "operatorType" : 0
                  },
                  "message" : {
                    "extension" : {
                      "reminderContent" : "1",
                      "senderUserId" : "2218732689285",
                      "umid" : "WV610495b63ec2b2f202127dd7485386a",
                      "utdid" : "ZFS6eIvxeiEDAG6jQ0omVLGV",
                      "extJson" : "{\\"quickReply\\":\\"1\\",\\"utdid\\":\\"ZFS6eIvxeiEDAG6jQ0omVLGV\\",\\"messageId\\":\\"eb2332760e144825a6c9881a886955cb\\",\\"umidToken\\":\\"lkYBhuFLPERxIRKXy3fuIqskzmXiXTYh\\",\\"tag\\":\\"u\\"}",
                      "umidToken" : "lkYBhuFLPERxIRKXy3fuIqskzmXiXTYh",
                      "_platform" : "ios",
                      "reminderNotice" : "发来一条新消息",
                      "senderUserType" : "0",
                      "detailNotice" : "1",
                      "port" : "63570",
                      "clientIp" : "183.76.102.166",
                      "sessionType" : "1",
                      "reminderUrl" : "fleamarket://message_chat?itemId=767254271372&peerUserId=2218732689285&sid=51388124157&messageId=eb2332760e144825a6c9881a886955cb&adv=no",
                      "bizTag" : "{\\"sourceId\\":\\"S:1\\",\\"messageId\\":\\"eb2332760e144825a6c9881a886955cb\\"}",
                      "reminderTitle" : "七星数码是DS直接拍"
                    },
                    "messageId" : "3588636051770.PNM",
                    "unreadCount" : 0,
                    "msgReadStatusSetting" : 1,
                    "createAt" : 1751552707175,
                    "content" : {
                      "custom" : {
                        "summary" : "1",
                        "data" : "eyJhdFVzZXJzIjpbXSwiY29udGVudFR5cGUiOjEsInRleHQiOnsidGV4dCI6IjEifX0=",
                        "type" : 1
                      },
                      "contentType" : 101
                    },
                    "displayStyle" : 0,
                    "redPointPolicy" : 0,
                    "sender" : {
                      "uid" : "2218732689285@goofish",
                      "tag" : 0
                    },
                    "receiverCount" : 2,
                    "cid" : "51388124157@goofish"
                  },
                  "msgStatus" : 1
                }, {
                  "readStatus" : 2,
                  "userExtension" : {
                    "needPush" : "false"
                  },
                  "recallFeature" : {
                    "showRecallStatusSetting" : 1,
                    "operatorType" : 0
                  },
                  "message" : {
                    "extension" : {
                      "_platform" : "web",
                      "reminderContent" : "1",
                      "reminderNotice" : "发来一条新消息",
                      "senderUserType" : "0",
                      "detailNotice" : "1",
                      "senderUserId" : "2150369819",
                      "extJson" : "{\\"quickReply\\":\\"1\\",\\"messageId\\":\\"ce308635164f4ac38c41285096e9a3bb\\",\\"tag\\":\\"u\\"}",
                      "sessionType" : "1",
                      "reminderUrl" : "fleamarket://message_chat?itemId=767254271372&peerUserId=2150369819&sid=51388124157&messageId=ce308635164f4ac38c41285096e9a3bb&adv=no",
                      "bizTag" : "{\\"sourceId\\":\\"S:1\\",\\"messageId\\":\\"ce308635164f4ac38c41285096e9a3bb\\"}",
                      "reminderTitle" : "Joki2020"
                    },
                    "messageId" : "3588634132529.PNM",
                    "unreadCount" : 0,
                    "msgReadStatusSetting" : 1,
                    "createAt" : 1751552695745,
                    "content" : {
                      "custom" : {
                        "summary" : "1",
                        "data" : "eyJjb250ZW50VHlwZSI6MSwidGV4dCI6eyJ0ZXh0IjoiMSJ9fQ==",
                        "type" : 1
                      },
                      "contentType" : 101
                    },
                    "displayStyle" : 0,
                    "redPointPolicy" : 0,
                    "sender" : {
                      "uid" : "2150369819@goofish",
                      "tag" : 0
                    },
                    "receiverCount" : 2,
                    "cid" : "51388124157@goofish"
                  },
                  "msgStatus" : 1
                }, {
                  "readStatus" : 2,
                  "userExtension" : {
                    "needPush" : "false"
                  },
                  "recallFeature" : {
                    "showRecallStatusSetting" : 1,
                    "operatorType" : 0
                  },
                  "message" : {
                    "extension" : {
                      "_platform" : "web",
                      "reminderContent" : "auto reply",
                      "reminderNotice" : "发来一条新消息",
                      "senderUserType" : "0",
                      "detailNotice" : "auto reply",
                      "senderUserId" : "2150369819",
                      "extJson" : "{\\"quickReply\\":\\"1\\",\\"messageId\\":\\"5ff483287c4f45f28b972dfbdab6e5d4\\",\\"tag\\":\\"u\\"}",
                      "sessionType" : "1",
                      "reminderUrl" : "fleamarket://message_chat?itemId=767254271372&peerUserId=2150369819&sid=51388124157&messageId=5ff483287c4f45f28b972dfbdab6e5d4&adv=no",
                      "bizTag" : "{\\"sourceId\\":\\"S:1\\",\\"messageId\\":\\"5ff483287c4f45f28b972dfbdab6e5d4\\"}",
                      "reminderTitle" : "Joki2020"
                    },
                    "messageId" : "3596499675997.PNM",
                    "unreadCount" : 0,
                    "msgReadStatusSetting" : 1,
                    "createAt" : 1751552308768,
                    "content" : {
                      "custom" : {
                        "summary" : "auto reply",
                        "data" : "eyJjb250ZW50VHlwZSI6MSwidGV4dCI6eyJ0ZXh0IjoiYXV0byByZXBseSJ9fQ==",
                        "type" : 1
                      },
                      "contentType" : 101
                    },
                    "displayStyle" : 0,
                    "redPointPolicy" : 0,
                    "sender" : {
                      "uid" : "2150369819@goofish",
                      "tag" : 0
                    },
                    "receiverCount" : 2,
                    "cid" : "51388124157@goofish"
                  },
                  "msgStatus" : 1
                }, {
                  "readStatus" : 2,
                  "userExtension" : {
                    "needPush" : "true"
                  },
                  "recallFeature" : {
                    "showRecallStatusSetting" : 1,
                    "operatorType" : 0
                  },
                  "message" : {
                    "extension" : {
                      "reminderContent" : "这个呢",
                      "senderUserId" : "2218732689285",
                      "umid" : "WV610495b63ec2b2f202127dd7485386a",
                      "utdid" : "ZFS6eIvxeiEDAG6jQ0omVLGV",
                      "extJson" : "{\\"quickReply\\":\\"1\\",\\"utdid\\":\\"ZFS6eIvxeiEDAG6jQ0omVLGV\\",\\"messageId\\":\\"fe0a09484c884e6f83221e71406202ec\\",\\"umidToken\\":\\"lkYBhuFLPERxIRKXy3fuIqskzmXiXTYh\\",\\"tag\\":\\"u\\"}",
                      "umidToken" : "lkYBhuFLPERxIRKXy3fuIqskzmXiXTYh",
                      "_platform" : "ios",
                      "reminderNotice" : "发来一条新消息",
                      "senderUserType" : "0",
                      "detailNotice" : "这个呢",
                      "port" : "60416",
                      "clientIp" : "183.76.102.166",
                      "sessionType" : "1",
                      "reminderUrl" : "fleamarket://message_chat?itemId=767254271372&peerUserId=2218732689285&sid=51388124157&messageId=fe0a09484c884e6f83221e71406202ec&adv=no",
                      "bizTag" : "{\\"sourceId\\":\\"S:1\\",\\"messageId\\":\\"fe0a09484c884e6f83221e71406202ec\\"}",
                      "reminderTitle" : "七星数码是DS直接拍"
                    },
                    "messageId" : "3596377459145.PNM",
                    "unreadCount" : 0,
                    "msgReadStatusSetting" : 1,
                    "createAt" : 1751552308431,
                    "content" : {
                      "custom" : {
                        "summary" : "这个呢",
                        "data" : "eyJhdFVzZXJzIjpbXSwiY29udGVudFR5cGUiOjEsInRleHQiOnsidGV4dCI6Iui/meS4quWRoiJ9fQ==",
                        "type" : 1
                      },
                      "contentType" : 101
                    },
                    "displayStyle" : 0,
                    "redPointPolicy" : 0,
                    "sender" : {
                      "uid" : "2218732689285@goofish",
                      "tag" : 0
                    },
                    "receiverCount" : 2,
                    "cid" : "51388124157@goofish"
                  },
                  "msgStatus" : 1
                }, {
                  "readStatus" : 2,
                  "userExtension" : {
                    "needPush" : "true"
                  },
                  "recallFeature" : {
                    "showRecallStatusSetting" : 1,
                    "operatorType" : 0
                  },
                  "message" : {
                    "extension" : {
                      "reminderContent" : "45卖我怎么样",
                      "senderUserId" : "2218732689285",
                      "umid" : "WV610495b63ec2b2f202127dd7485386a",
                      "utdid" : "ZFS6eIvxeiEDAG6jQ0omVLGV",
                      "extJson" : "{\\"quickReply\\":\\"1\\",\\"utdid\\":\\"ZFS6eIvxeiEDAG6jQ0omVLGV\\",\\"messageId\\":\\"c529042ee0e7457faca78f3e4627fb9b\\",\\"umidToken\\":\\"lkYBhuFLPERxIRKXy3fuIqskzmXiXTYh\\",\\"tag\\":\\"u\\"}",
                      "umidToken" : "lkYBhuFLPERxIRKXy3fuIqskzmXiXTYh",
                      "_platform" : "ios",
                      "reminderNotice" : "发来一条新消息",
                      "senderUserType" : "0",
                      "detailNotice" : "45卖我怎么样",
                      "port" : "61997",
                      "clientIp" : "183.76.102.166",
                      "sessionType" : "1",
                      "reminderUrl" : "fleamarket://message_chat?itemId=767254271372&peerUserId=2218732689285&sid=51388124157&messageId=c529042ee0e7457faca78f3e4627fb9b&adv=no",
                      "bizTag" : "{\\"sourceId\\":\\"S:1\\",\\"messageId\\":\\"c529042ee0e7457faca78f3e4627fb9b\\"}",
                      "reminderTitle" : "七星数码是DS直接拍"
                    },
                    "messageId" : "3588610153614.PNM",
                    "unreadCount" : 0,
                    "msgReadStatusSetting" : 1,
                    "createAt" : 1751551328697,
                    "content" : {
                      "custom" : {
                        "summary" : "45卖我怎么样",
                        "data" : "eyJhdFVzZXJzIjpbXSwiY29udGVudFR5cGUiOjEsInRleHQiOnsidGV4dCI6IjQ15Y2W5oiR5oCO5LmI5qC3In19",
                        "type" : 1
                      },
                      "contentType" : 101
                    },
                    "displayStyle" : 0,
                    "redPointPolicy" : 0,
                    "sender" : {
                      "uid" : "2218732689285@goofish",
                      "tag" : 0
                    },
                    "receiverCount" : 2,
                    "cid" : "51388124157@goofish"
                  },
                  "msgStatus" : 1
                }, {
                  "readStatus" : 2,
                  "userExtension" : {
                    "needPush" : "true"
                  },
                  "recallFeature" : {
                    "showRecallStatusSetting" : 1,
                    "operatorType" : 0
                  },
                  "message" : {
                    "extension" : {
                      "reminderContent" : "我看别家都40",
                      "senderUserId" : "2218732689285",
                      "umid" : "WV610495b63ec2b2f202127dd7485386a",
                      "utdid" : "ZFS6eIvxeiEDAG6jQ0omVLGV",
                      "extJson" : "{\\"quickReply\\":\\"1\\",\\"utdid\\":\\"ZFS6eIvxeiEDAG6jQ0omVLGV\\",\\"messageId\\":\\"37a90d1210a14f4fa02deac583fb2a8d\\",\\"umidToken\\":\\"lkYBhuFLPERxIRKXy3fuIqskzmXiXTYh\\",\\"tag\\":\\"u\\"}",
                      "umidToken" : "lkYBhuFLPERxIRKXy3fuIqskzmXiXTYh",
                      "_platform" : "ios",
                      "reminderNotice" : "发来一条新消息",
                      "senderUserType" : "0",
                      "detailNotice" : "我看别家都40",
                      "port" : "61997",
                      "clientIp" : "183.76.102.166",
                      "sessionType" : "1",
                      "reminderUrl" : "fleamarket://message_chat?itemId=767254271372&peerUserId=2218732689285&sid=51388124157&messageId=37a90d1210a14f4fa02deac583fb2a8d&adv=no",
                      "bizTag" : "{\\"sourceId\\":\\"S:1\\",\\"messageId\\":\\"37a90d1210a14f4fa02deac583fb2a8d\\"}",
                      "reminderTitle" : "七星数码是DS直接拍"
                    },
                    "messageId" : "3596371045042.PNM",
                    "unreadCount" : 0,
                    "msgReadStatusSetting" : 1,
                    "createAt" : 1751551320100,
                    "content" : {
                      "custom" : {
                        "summary" : "我看别家都40",
                        "data" : "eyJhdFVzZXJzIjpbXSwiY29udGVudFR5cGUiOjEsInRleHQiOnsidGV4dCI6IuaIkeeci+WIq+WutumDvTQwIn19",
                        "type" : 1
                      },
                      "contentType" : 101
                    },
                    "displayStyle" : 0,
                    "redPointPolicy" : 0,
                    "sender" : {
                      "uid" : "2218732689285@goofish",
                      "tag" : 0
                    },
                    "receiverCount" : 2,
                    "cid" : "51388124157@goofish"
                  },
                  "msgStatus" : 1
                }, {
                  "readStatus" : 2,
                  "userExtension" : {
                    "needPush" : "true"
                  },
                  "recallFeature" : {
                    "showRecallStatusSetting" : 1,
                    "operatorType" : 0
                  },
                  "message" : {
                    "extension" : {
                      "reminderContent" : "能便宜多少",
                      "senderUserId" : "2218732689285",
                      "umid" : "WV610495b63ec2b2f202127dd7485386a",
                      "utdid" : "ZFS6eIvxeiEDAG6jQ0omVLGV",
                      "extJson" : "{\\"quickReply\\":\\"1\\",\\"utdid\\":\\"ZFS6eIvxeiEDAG6jQ0omVLGV\\",\\"messageId\\":\\"9d014bd8127c41ab86ac951932774543\\",\\"umidToken\\":\\"lkYBhuFLPERxIRKXy3fuIqskzmXiXTYh\\",\\"tag\\":\\"u\\"}",
                      "umidToken" : "lkYBhuFLPERxIRKXy3fuIqskzmXiXTYh",
                      "_platform" : "ios",
                      "reminderNotice" : "发来一条新消息",
                      "senderUserType" : "0",
                      "detailNotice" : "能便宜多少",
                      "port" : "61997",
                      "clientIp" : "183.76.102.166",
                      "sessionType" : "1",
                      "reminderUrl" : "fleamarket://message_chat?itemId=767254271372&peerUserId=2218732689285&sid=51388124157&messageId=9d014bd8127c41ab86ac951932774543&adv=no",
                      "bizTag" : "{\\"sourceId\\":\\"S:1\\",\\"messageId\\":\\"9d014bd8127c41ab86ac951932774543\\"}",
                      "reminderTitle" : "七星数码是DS直接拍"
                    },
                    "messageId" : "3591998702682.PNM",
                    "unreadCount" : 0,
                    "msgReadStatusSetting" : 1,
                    "createAt" : 1751551314266,
                    "content" : {
                      "custom" : {
                        "summary" : "能便宜多少",
                        "data" : "eyJhdFVzZXJzIjpbXSwiY29udGVudFR5cGUiOjEsInRleHQiOnsidGV4dCI6IuiDveS+v+WunOWkmuWwkSJ9fQ==",
                        "type" : 1
                      },
                      "contentType" : 101
                    },
                    "displayStyle" : 0,
                    "redPointPolicy" : 0,
                    "sender" : {
                      "uid" : "2218732689285@goofish",
                      "tag" : 0
                    },
                    "receiverCount" : 2,
                    "cid" : "51388124157@goofish"
                  },
                  "msgStatus" : 1
                }, {
                  "readStatus" : 2,
                  "userExtension" : {
                    "needPush" : "true"
                  },
                  "recallFeature" : {
                    "showRecallStatusSetting" : 1,
                    "operatorType" : 0
                  },
                  "message" : {
                    "extension" : {
                      "reminderContent" : "[瑞思拜]",
                      "senderUserId" : "2218732689285",
                      "umid" : "WV610495b63ec2b2f202127dd7485386a",
                      "utdid" : "ZFS6eIvxeiEDAG6jQ0omVLGV",
                      "extJson" : "{\\"quickReply\\":\\"1\\",\\"utdid\\":\\"ZFS6eIvxeiEDAG6jQ0omVLGV\\",\\"messageId\\":\\"02878eb175634861bcefc59259fb9951\\",\\"umidToken\\":\\"lkYBhuFLPERxIRKXy3fuIqskzmXiXTYh\\",\\"tag\\":\\"u\\"}",
                      "umidToken" : "lkYBhuFLPERxIRKXy3fuIqskzmXiXTYh",
                      "_platform" : "ios",
                      "reminderNotice" : "发来一条新消息",
                      "senderUserType" : "0",
                      "detailNotice" : "[瑞思拜]",
                      "port" : "61997",
                      "clientIp" : "183.76.102.166",
                      "sessionType" : "1",
                      "reminderUrl" : "fleamarket://message_chat?itemId=767254271372&peerUserId=2218732689285&sid=51388124157&messageId=02878eb175634861bcefc59259fb9951&adv=no",
                      "bizTag" : "{\\"sourceId\\":\\"S:1\\",\\"messageId\\":\\"02878eb175634861bcefc59259fb9951\\"}",
                      "reminderTitle" : "七星数码是DS直接拍"
                    },
                    "messageId" : "3588594427625.PNM",
                    "unreadCount" : 0,
                    "msgReadStatusSetting" : 1,
                    "createAt" : 1751551307251,
                    "content" : {
                      "custom" : {
                        "summary" : "[瑞思拜]",
                        "data" : "eyJhdFVzZXJzIjpbXSwiY29udGVudFR5cGUiOjEsInRleHQiOnsidGV4dCI6IlvnkZ7mgJ3mi5xdIn19",
                        "type" : 1
                      },
                      "contentType" : 101
                    },
                    "displayStyle" : 0,
                    "redPointPolicy" : 0,
                    "sender" : {
                      "uid" : "2218732689285@goofish",
                      "tag" : 0
                    },
                    "receiverCount" : 2,
                    "cid" : "51388124157@goofish"
                  },
                  "msgStatus" : 1
                }, {
                  "readStatus" : 2,
                  "userExtension" : {
                    "needPush" : "true"
                  },
                  "recallFeature" : {
                    "showRecallStatusSetting" : 1,
                    "operatorType" : 0
                  },
                  "message" : {
                    "extension" : {
                      "reminderContent" : "是不是",
                      "senderUserId" : "2218732689285",
                      "umid" : "WV610495b63ec2b2f202127dd7485386a",
                      "utdid" : "ZFS6eIvxeiEDAG6jQ0omVLGV",
                      "extJson" : "{\\"quickReply\\":\\"1\\",\\"utdid\\":\\"ZFS6eIvxeiEDAG6jQ0omVLGV\\",\\"messageId\\":\\"c635b5c715584f60ac0d6e888f497b55\\",\\"umidToken\\":\\"lkYBhuFLPERxIRKXy3fuIqskzmXiXTYh\\",\\"tag\\":\\"u\\"}",
                      "umidToken" : "lkYBhuFLPERxIRKXy3fuIqskzmXiXTYh",
                      "_platform" : "ios",
                      "reminderNotice" : "发来一条新消息",
                      "senderUserType" : "0",
                      "detailNotice" : "是不是",
                      "port" : "61997",
                      "clientIp" : "183.76.102.166",
                      "sessionType" : "1",
                      "reminderUrl" : "fleamarket://message_chat?itemId=767254271372&peerUserId=2218732689285&sid=51388124157&messageId=c635b5c715584f60ac0d6e888f497b55&adv=no",
                      "bizTag" : "{\\"sourceId\\":\\"S:1\\",\\"messageId\\":\\"c635b5c715584f60ac0d6e888f497b55\\"}",
                      "reminderTitle" : "七星数码是DS直接拍"
                    },
                    "messageId" : "3588610135424.PNM",
                    "unreadCount" : 0,
                    "msgReadStatusSetting" : 1,
                    "createAt" : 1751551299043,
                    "content" : {
                      "custom" : {
                        "summary" : "是不是",
                        "data" : "eyJhdFVzZXJzIjpbXSwiY29udGVudFR5cGUiOjEsInRleHQiOnsidGV4dCI6IuaYr+S4jeaYryJ9fQ==",
                        "type" : 1
                      },
                      "contentType" : 101
                    },
                    "displayStyle" : 0,
                    "redPointPolicy" : 0,
                    "sender" : {
                      "uid" : "2218732689285@goofish",
                      "tag" : 0
                    },
                    "receiverCount" : 2,
                    "cid" : "51388124157@goofish"
                  },
                  "msgStatus" : 1
                }, {
                  "readStatus" : 2,
                  "userExtension" : {
                    "needPush" : "true"
                  },
                  "recallFeature" : {
                    "showRecallStatusSetting" : 1,
                    "operatorType" : 0
                  },
                  "message" : {
                    "extension" : {
                      "reminderContent" : "是钱不钱的问题",
                      "senderUserId" : "2218732689285",
                      "umid" : "WV610495b63ec2b2f202127dd7485386a",
                      "utdid" : "ZFS6eIvxeiEDAG6jQ0omVLGV",
                      "extJson" : "{\\"quickReply\\":\\"1\\",\\"utdid\\":\\"ZFS6eIvxeiEDAG6jQ0omVLGV\\",\\"messageId\\":\\"5386248e8f914aaf95c59cbd53e88327\\",\\"umidToken\\":\\"lkYBhuFLPERxIRKXy3fuIqskzmXiXTYh\\",\\"tag\\":\\"u\\"}",
                      "umidToken" : "lkYBhuFLPERxIRKXy3fuIqskzmXiXTYh",
                      "_platform" : "ios",
                      "reminderNotice" : "发来一条新消息",
                      "senderUserType" : "0",
                      "detailNotice" : "是钱不钱的问题",
                      "port" : "60695",
                      "clientIp" : "183.76.102.166",
                      "sessionType" : "1",
                      "reminderUrl" : "fleamarket://message_chat?itemId=767254271372&peerUserId=2218732689285&sid=51388124157&messageId=5386248e8f914aaf95c59cbd53e88327&adv=no",
                      "bizTag" : "{\\"sourceId\\":\\"S:1\\",\\"messageId\\":\\"5386248e8f914aaf95c59cbd53e88327\\"}",
                      "reminderTitle" : "七星数码是DS直接拍"
                    },
                    "messageId" : "3591954941003.PNM",
                    "unreadCount" : 0,
                    "msgReadStatusSetting" : 1,
                    "createAt" : 1751550514743,
                    "content" : {
                      "custom" : {
                        "summary" : "是钱不钱的问题",
                        "data" : "eyJhdFVzZXJzIjpbXSwiY29udGVudFR5cGUiOjEsInRleHQiOnsidGV4dCI6IuaYr+mSseS4jemSseeahOmXrumimCJ9fQ==",
                        "type" : 1
                      },
                      "contentType" : 101
                    },
                    "displayStyle" : 0,
                    "redPointPolicy" : 0,
                    "sender" : {
                      "uid" : "2218732689285@goofish",
                      "tag" : 0
                    },
                    "receiverCount" : 2,
                    "cid" : "51388124157@goofish"
                  },
                  "msgStatus" : 1
                }, {
                  "readStatus" : 2,
                  "userExtension" : {
                    "needPush" : "false"
                  },
                  "recallFeature" : {
                    "showRecallStatusSetting" : 1,
                    "operatorType" : 0
                  },
                  "message" : {
                    "extension" : {
                      "_platform" : "web",
                      "reminderContent" : "auto reply",
                      "reminderNotice" : "发来一条新消息",
                      "senderUserType" : "0",
                      "detailNotice" : "auto reply",
                      "senderUserId" : "2150369819",
                      "extJson" : "{\\"quickReply\\":\\"1\\",\\"messageId\\":\\"8d7154f37d134d408c88f043fbb47900\\",\\"tag\\":\\"u\\"}",
                      "sessionType" : "1",
                      "reminderUrl" : "fleamarket://message_chat?itemId=767254271372&peerUserId=2150369819&sid=51388124157&messageId=8d7154f37d134d408c88f043fbb47900&adv=no",
                      "bizTag" : "{\\"sourceId\\":\\"S:1\\",\\"messageId\\":\\"8d7154f37d134d408c88f043fbb47900\\"}",
                      "reminderTitle" : "Joki2020"
                    },
                    "messageId" : "3596465681732.PNM",
                    "unreadCount" : 0,
                    "msgReadStatusSetting" : 1,
                    "createAt" : 1751550496594,
                    "content" : {
                      "custom" : {
                        "summary" : "auto reply",
                        "data" : "eyJjb250ZW50VHlwZSI6MSwidGV4dCI6eyJ0ZXh0IjoiYXV0byByZXBseSJ9fQ==",
                        "type" : 1
                      },
                      "contentType" : 101
                    },
                    "displayStyle" : 0,
                    "redPointPolicy" : 0,
                    "sender" : {
                      "uid" : "2150369819@goofish",
                      "tag" : 0
                    },
                    "receiverCount" : 2,
                    "cid" : "51388124157@goofish"
                  },
                  "msgStatus" : 1
                }, {
                  "readStatus" : 2,
                  "userExtension" : {
                    "needPush" : "false"
                  },
                  "recallFeature" : {
                    "showRecallStatusSetting" : 1,
                    "operatorType" : 0
                  },
                  "message" : {
                    "extension" : {
                      "_platform" : "web",
                      "reminderContent" : "不是钱不钱的问题",
                      "reminderNotice" : "发来一条新消息",
                      "senderUserType" : "0",
                      "detailNotice" : "不是钱不钱的问题",
                      "senderUserId" : "2150369819",
                      "extJson" : "{\\"quickReply\\":\\"1\\",\\"messageId\\":\\"1eaef7df5cbf4f58b64613e40249fdda\\",\\"tag\\":\\"u\\"}",
                      "sessionType" : "1",
                      "reminderUrl" : "fleamarket://message_chat?itemId=767254271372&peerUserId=2150369819&sid=51388124157&messageId=1eaef7df5cbf4f58b64613e40249fdda&adv=no",
                      "bizTag" : "{\\"sourceId\\":\\"S:1\\",\\"messageId\\":\\"1eaef7df5cbf4f58b64613e40249fdda\\"}",
                      "reminderTitle" : "Joki2020"
                    },
                    "messageId" : "3596343177088.PNM",
                    "unreadCount" : 0,
                    "msgReadStatusSetting" : 1,
                    "createAt" : 1751550496144,
                    "content" : {
                      "custom" : {
                        "summary" : "不是钱不钱的问题",
                        "data" : "eyJjb250ZW50VHlwZSI6MSwidGV4dCI6eyJ0ZXh0Ijoi5LiN5piv6ZKx5LiN6ZKx55qE6Zeu6aKYIn19",
                        "type" : 1
                      },
                      "contentType" : 101
                    },
                    "displayStyle" : 0,
                    "redPointPolicy" : 0,
                    "sender" : {
                      "uid" : "2150369819@goofish",
                      "tag" : 0
                    },
                    "receiverCount" : 2,
                    "cid" : "51388124157@goofish"
                  },
                  "msgStatus" : 1
                }, {
                  "readStatus" : 2,
                  "userExtension" : {
                    "needPush" : "false"
                  },
                  "recallFeature" : {
                    "showRecallStatusSetting" : 1,
                    "operatorType" : 0
                  },
                  "message" : {
                    "extension" : {
                      "reminderContent" : "[未付款，买家关闭了订单]",
                      "receiver" : "2150369819",
                      "senderUserId" : "2218732689285",
                      "extJson" : "{\\"msgArgs\\":{\\"task_id\\":\\"q0vL5sbzzhSd\\",\\"source\\":\\"im\\",\\"msg_id\\":\\"5164db72c62e49e896ccd6791c77b38d\\"},\\"msgArg1\\":\\"MsgTips\\",\\"messageId\\":\\"5164db72c62e49e896ccd6791c77b38d\\",\\"contentType\\":\\"14\\"}",
                      "closePushReceiver" : "true",
                      "reminderNotice" : "买家已关闭交易",
                      "senderUserType" : "0",
                      "closeUnreadNumber" : "true",
                      "detailNotice" : "[未付款，买家关闭了订单]",
                      "redReminderStyle" : "11",
                      "sessionType" : "1",
                      "reminderUrl" : "fleamarket://message_chat?itemId=767254271372&peerUserId=2218732689285&sid=51388124157&messageId=5164db72c62e49e896ccd6791c77b38d&adv=no",
                      "bizTag" : "{\\"sourceId\\":\\"C2C:q0vL5sbzzhSd\\",\\"taskName\\":\\"买家关闭订单_卖家\\",\\"materialId\\":\\"q0vL5sbzzhSd\\",\\"taskId\\":\\"q0vL5sbzzhSd\\"}",
                      "reminderTitle" : "交易消息",
                      "redReminder" : "交易关闭",
                      "updateHead" : "true"
                    },
                    "messageId" : "3596455852749.PNM",
                    "unreadCount" : 0,
                    "msgReadStatusSetting" : 1,
                    "createAt" : 1751550182398,
                    "content" : {
                      "custom" : {
                        "summary" : "[未付款，买家关闭了订单]",
                        "data" : "eyJjb250ZW50VHlwZSI6MTQsInRpcCI6eyJ0aXAiOiLmnKrku5jmrL7vvIzkubDlrrblhbPpl63kuoborqLljZUifX0=",
                        "type" : 14
                      },
                      "contentType" : 101
                    },
                    "displayStyle" : 0,
                    "redPointPolicy" : 0,
                    "sender" : {
                      "uid" : "2218732689285@goofish",
                      "tag" : 0
                    },
                    "receiverCount" : 1,
                    "cid" : "51388124157@goofish"
                  },
                  "msgStatus" : 1
                }, {
                  "readStatus" : 2,
                  "userExtension" : {
                    "needPush" : "true"
                  },
                  "recallFeature" : {
                    "showRecallStatusSetting" : 1,
                    "operatorType" : 0
                  },
                  "message" : {
                    "extension" : {
                      "reminderContent" : "多少钱？",
                      "senderUserId" : "2218732689285",
                      "umid" : "WV610495b63ec2b2f202127dd7485386a",
                      "utdid" : "ZFS6eIvxeiEDAG6jQ0omVLGV",
                      "extJson" : "{\\"quickReply\\":\\"1\\",\\"utdid\\":\\"ZFS6eIvxeiEDAG6jQ0omVLGV\\",\\"messageId\\":\\"e4b584a62da643e4b75e762137d9c42d\\",\\"umidToken\\":\\"lkYBhuFLPERxIRKXy3fuIqskzmXiXTYh\\",\\"tag\\":\\"u\\"}",
                      "umidToken" : "lkYBhuFLPERxIRKXy3fuIqskzmXiXTYh",
                      "_platform" : "ios",
                      "reminderNotice" : "发来一条新消息",
                      "senderUserType" : "0",
                      "detailNotice" : "多少钱？",
                      "port" : "60983",
                      "clientIp" : "183.76.102.166",
                      "sessionType" : "1",
                      "reminderUrl" : "fleamarket://message_chat?itemId=767254271372&peerUserId=2218732689285&sid=51388124157&messageId=e4b584a62da643e4b75e762137d9c42d&adv=no",
                      "bizTag" : "{\\"sourceId\\":\\"S:1\\",\\"messageId\\":\\"e4b584a62da643e4b75e762137d9c42d\\"}",
                      "reminderTitle" : "七星数码是DS直接拍"
                    },
                    "messageId" : "3596327378588.PNM",
                    "unreadCount" : 0,
                    "msgReadStatusSetting" : 1,
                    "createAt" : 1751550174546,
                    "content" : {
                      "custom" : {
                        "summary" : "多少钱？",
                        "data" : "eyJhdFVzZXJzIjpbXSwiY29udGVudFR5cGUiOjEsInRleHQiOnsidGV4dCI6IuWkmuWwkemSse+8nyJ9fQ==",
                        "type" : 1
                      },
                      "contentType" : 101
                    },
                    "displayStyle" : 0,
                    "redPointPolicy" : 0,
                    "sender" : {
                      "uid" : "2218732689285@goofish",
                      "tag" : 0
                    },
                    "receiverCount" : 2,
                    "cid" : "51388124157@goofish"
                  },
                  "msgStatus" : 1
                } ]
            """.trim();
}
