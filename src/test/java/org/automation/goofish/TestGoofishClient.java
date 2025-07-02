package org.automation.goofish;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.automation.goofish.core.socket.msg.receive.ReceiveMsg;
import org.automation.goofish.core.socket.msg.send.AckMsg;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.msgpack.jackson.dataformat.MessagePackFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.io.IOException;
import java.util.Base64;
import java.util.stream.Stream;

import static net.bytebuddy.implementation.bytecode.member.MethodInvocation.lookup;


@ActiveProfiles("test")
@SpringBootTest(classes = Starter.class)
public class TestGoofishClient {

    static final Logger logger = LoggerFactory.getLogger(lookup().getClass());

    @Test
    void testDecoder() throws IOException {
        String base64Data = """
                hgGxMzU5Mzg2NzExNjg0Ni5QTk0CAgMABLM1MTIwNDYxMzE4NEBnb29maXNoBQEGzwAAAZfJtRMa
                """.trim();

        byte[] msgpackBytes = Base64.getDecoder().decode(base64Data);

        ObjectMapper msgpackMapper = new ObjectMapper(new MessagePackFactory());
        JsonNode msgpackNode = msgpackMapper.readTree(msgpackBytes);
        logger.info("convert to JsonNode: {}", msgpackNode);
    }

    static Stream<String> messageProvider() {
        return Stream.of("""
                {
                  "headers": {
                    "app-key": "444e9908a51d1cb236a27862abc769c9",
                    "mid": "84a4001c 0",
                    "ua": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/133.0.0.0 Safari/537.36 DingTalk(2.1.5) OS(Windows/10) Browser(Chrome/133.0.0.0) DingWeb/2.1.5 IMPaaS DingWeb/2.1.5",
                    "sid": "213ffe1d6864f8fe0753a9cd09b592ae3ae5a1e2e4a5"
                  },
                  "lwp": "/s/sync",
                  "body": {
                    "syncPushPackage": {
                      "maxHighPts": 0,
                      "startSeq": 1,
                      "endSeq": 2,
                      "minCreateTime": 1751448105809,
                      "data": [
                        {
                          "bizType": 40,
                          "data": "hgGxMzU5NDMwNTc5NDE1OS5QTk0CAgMABLM1MTIwNDYxMzE4NEBnb29maXNoBQEGzwAAAZfKcTNG",
                          "streamId": "40",
                          "objectType": 40103
                        },
                        {
                          "bizType": 40,
                          "data": "ggGLAYEBtTIyMTg3MzI2ODkyODVAZ29vZmlzaAKzNTEyMDQ2MTMxODRAZ29vZmlzaAOxMzU4NjUxODAxNzUxMi5QTk0EAAXPAAABl8pxiBsGggFlA4UBoALaACrmraPluLjnmoTkvLDorqHlsLHooajmg4XljIXlkoznroDkvZPlrZfkuoYDoAQBBdoAW3siYXRVc2VycyI6W10sImNvbnRlbnRUeXBlIjoxLCJ0ZXh0Ijp7InRleHQiOiLmraPluLjnmoTkvLDorqHlsLHooajmg4XljIXlkoznroDkvZPlrZfkuoYifX0HAggBCQAK3gAQqV9wbGF0Zm9ybaNpb3OmYml6VGFn2gBBeyJzb3VyY2VJZCI6IlM6MSIsIm1lc3NhZ2VJZCI6Ijc0MjZlM2I1ODg4OTQ2MDNhYzY0YzQyMTJhYzFjOTUxIn2oY2xpZW50SXCuMTI2LjI1My43MS4yMDOsZGV0YWlsTm90aWNl2gAq5q2j5bi455qE5Lyw6K6h5bCx6KGo5oOF5YyF5ZKM566A5L2T5a2X5LqGp2V4dEpzb27aAJ17InF1aWNrUmVwbHkiOiIxIiwidXRkaWQiOiJaRlM2ZUl2eGVpRURBRzZqUTBvbVZMR1YiLCJtZXNzYWdlSWQiOiI3NDI2ZTNiNTg4ODk0NjAzYWM2NGM0MjEyYWMxYzk1MSIsInVtaWRUb2tlbiI6Inh4Z0JrZDlMUEsyaU5oS1h4dDFlMUxlb2NaWVF3QVNiIiwidGFnIjoidSJ9pHBvcnSlNTk5MjevcmVtaW5kZXJDb250ZW502gAq5q2j5bi455qE5Lyw6K6h5bCx6KGo5oOF5YyF5ZKM566A5L2T5a2X5LqGrnJlbWluZGVyTm90aWNlteWPkeadpeS4gOadoeaWsOa2iOaBr61yZW1pbmRlclRpdGxluuS4g+aYn+aVsOeggeaYr0RT55u05o6l5ouNq3JlbWluZGVyVXJs2gCIZmxlYW1hcmtldDovL21lc3NhZ2VfY2hhdD9pdGVtSWQ9Nzg4MTc1NDc5NDcxJnBlZXJVc2VySWQ9MjIxODczMjY4OTI4NSZzaWQ9NTEyMDQ2MTMxODQmbWVzc2FnZUlkPTc0MjZlM2I1ODg4OTQ2MDNhYzY0YzQyMTJhYzFjOTUxJmFkdj1ub6xzZW5kZXJVc2VySWStMjIxODczMjY4OTI4Na5zZW5kZXJVc2VyVHlwZaEwq3Nlc3Npb25UeXBloTGkdW1pZNoAIVdWNjEwNDk1YjYzZWMyYjJmMjAyMTI3ZGQ3NDg1Mzg2Yal1bWlkVG9rZW7aACB4eGdCa2Q5TFBLMmlOaEtYeHQxZTFMZW9jWllRd0FTYqV1dGRpZLhaRlM2ZUl2eGVpRURBRzZqUTBvbVZMR1YMAQOBqG5lZWRQdXNopHRydWU=",
                          "streamId": "40",
                          "objectType": 40000
                        }
                      ],
                      "maxPts": 1751448127805000,
                      "hasMore": 0,
                      "timestamp": 1751448127809
                    },
                    "syncExtensionModel": {
                      "reconnectType": 0,
                      "failover": 0,
                      "fingerprint": -898528959
                    }
                  }
                }
                """.trim(), """
                {
                  "headers": {
                    "ip-digest": "804eb93db847f204d3180290b8ab9a90",
                    "dt": "j",
                    "reg-sid": "21076ab26864f3a06b5237627668384835693b57ac82",
                    "ip-region-digest": "42a7775c5ad8abed2791196747313f9e",
                    "reg-uid": "2150369819@goofish",
                    "mid": "8701751446438481 0",
                    "real-ip": "218.82.103.117",
                    "sid": "21076ab26864f3a06b5237627668384835693b57ac82"
                  },
                  "code": 200,
                  "body": {
                    "unitName": "PNM",
                    "cookie": "",
                    "timestamp": 1751446433049,
                    "isFromChina": true
                  }
                }
                """.trim(), """
                {
                  "headers": {
                    "app-key": "444e9908a51d1cb236a27862abc769c9",
                    "mid": "a3e20004 0",
                    "ua": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/133.0.0.0 Safari/537.36 DingTalk(2.1.5) OS(Windows/10) Browser(Chrome/133.0.0.0) DingWeb/2.1.5 IMPaaS DingWeb/2.1.5",
                    "sid": "2127f5e86864eb274923fbd26cad77317038ab5e34be"
                  },
                  "lwp": "/s/para",
                  "body": {
                    "syncPushPackage": {
                      "maxHighPts": 0,
                      "startSeq": 0,
                      "endSeq": 0,
                      "data": [
                        {
                          "bizType": 40,
                          "data": "gQGRhAGzNTEyMDQ2MTMxODRAZ29vZmlzaAIBAwEEsjIxNTAzNjk4MTlAZ29vZmlzaA==",
                          "streamId": "40",
                          "objectType": 40006,
                          "syncId": ""
                        }
                      ],
                      "maxPts": 0,
                      "hasMore": 0,
                      "timestamp": 0
                    },
                    "syncExtensionModel": {
                
                    }
                  }
                }
                """.trim(), """
                {
                  "headers": {
                    "app-key": "444e9908a51d1cb236a27862abc769c9",
                    "mid": "2d540003 0",
                    "ua": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/133.0.0.0 Safari/537.36 DingTalk(2.1.5) OS(Windows/10) Browser(Chrome/133.0.0.0) DingWeb/2.1.5 IMPaaS DingWeb/2.1.5",
                    "sid": "0b516ab16864b7b36c24a4e1a66449f0483d53b944f4"
                  },
                  "lwp": "/s/sync",
                  "body": {
                    "syncPushPackage": {
                      "maxHighPts": 0,
                      "startSeq": 1,
                      "endSeq": 1,
                      "minCreateTime": 1751431117553,
                      "data": [
                        {
                          "bizType": 40,
                          "data": "ggGLAYEBsjIxNTAzNjk4MTlAZ29vZmlzaAKzNTEyMDQ2MTMxODRAZ29vZmlzaAOxMzU5Mzg2NzExNjg0Ni5QTk0EAAXPAAABl8lt+vEGggFlA4UBoAKiY2MDoAQBBdoAJnsiY29udGVudFR5cGUiOjEsInRleHQiOnsidGV4dCI6ImNjIn19BwIIAQkACoupX3BsYXRmb3Jto3dlYqZiaXpUYWfaAEF7InNvdXJjZUlkIjoiUzoxIiwibWVzc2FnZUlkIjoiOTg1NjE5YzM4MDdkNGU5ZTg4MzA1MDBjNmE0NzU5MmMifaxkZXRhaWxOb3RpY2WiY2OnZXh0SnNvbtoAS3sicXVpY2tSZXBseSI6IjEiLCJtZXNzYWdlSWQiOiI5ODU2MTljMzgwN2Q0ZTllODgzMDUwMGM2YTQ3NTkyYyIsInRhZyI6InUifa9yZW1pbmRlckNvbnRlbnSiY2OucmVtaW5kZXJOb3RpY2W15Y+R5p2l5LiA5p2h5paw5raI5oGvrXJlbWluZGVyVGl0bGWoSm9raTIwMjCrcmVtaW5kZXJVcmzaAIVmbGVhbWFya2V0Oi8vbWVzc2FnZV9jaGF0P2l0ZW1JZD03ODgxNzU0Nzk0NzEmcGVlclVzZXJJZD0yMTUwMzY5ODE5JnNpZD01MTIwNDYxMzE4NCZtZXNzYWdlSWQ9OTg1NjE5YzM4MDdkNGU5ZTg4MzA1MDBjNmE0NzU5MmMmYWR2PW5vrHNlbmRlclVzZXJJZKoyMTUwMzY5ODE5rnNlbmRlclVzZXJUeXBloTCrc2Vzc2lvblR5cGWhMQwBA4GobmVlZFB1c2ilZmFsc2U=",
                          "streamId": "40",
                          "objectType": 40000
                        }
                      ],
                      "maxPts": 1751431117962000,
                      "hasMore": 0,
                      "timestamp": 1751431117966
                    },
                    "syncExtensionModel": {
                      "reconnectType": 0,
                      "failover": 0,
                      "fingerprint": -915538802
                    }
                  }
                }
                """.trim(), """
                {
                   "headers": {
                     "app-key": "444e9908a51d1cb236a27862abc769c9",
                     "mid": "6f690002 0",
                     "ua": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/133.0.0.0 Safari/537.36 DingTalk(2.1.5) OS(Windows/10) Browser(Chrome/133.0.0.0) DingWeb/2.1.5 IMPaaS DingWeb/2.1.5",
                     "sid": "213d347e6864b635269ca3508f96f011fa7d18a7bd9c"
                   },
                   "lwp": "/s/vulcan",
                   "body": {
                     "syncPushPackage": {
                       "maxHighPts": 0,
                       "startSeq": 1,
                       "endSeq": 14,
                       "minCreateTime": 1738168886550,
                       "data": [
                         {
                           "bizType": 370,
                           "data": "eyJjaGF0VHlwZSI6MSwiaW5jcmVtZW50VHlwZSI6MSwib3BlcmF0aW9uIjp7ImNvbnRlbnQiOnsiY29udGVudFR5cGUiOjgsInNlc3Npb25Bcm91c2UiOnsibWVtYmVyRmxhZ3MiOjAsIm5lZWRLZXlCb2FyZCI6dHJ1ZSwic2Vzc2lvbkFyb3VzZUluZm8iOnsicmVzaWRlbnRGdW5jdGlvbnMiOltdLCJhcm91c2VUaW1lU3RhbXAiOjE3MzgxNjg4ODYzOTgsImFyb3VzZUNoYXRTY3JpcHRJbmZvIjpbeyJjaGF0U2NyaXAiOiLmsZfokrjljaHov4fmnJ/kuoblkJfvvJ8iLCJjaGF0U2NyaXBTdHJhdGVneSI6InNoYWRpbmdfb3BlbmluZyIsImFyZ0luZm8iOnsiYXJncyI6eyJ0b3BpY190aXRsZSI6InNoYWRpbmdfb3BlbmluZyIsInNlc3Npb25faWQiOjQ2NzMxMjA5NzUxLCJjb250ZW50Ijoi5rGX6JK45Y2h6L+H5pyf5LqG5ZCX77yfIn0sImFyZzEiOiJDaGF0VG9waWMifX0seyJjaGF0U2NyaXAiOiLku7fmoLzov5jog73mm7TkvY7lkJciLCJjaGF0U2NyaXBTdHJhdGVneSI6InNoYWRpbmdfb3BlbmluZyIsImFyZ0luZm8iOnsiYXJncyI6eyJ0b3BpY190aXRsZSI6InNoYWRpbmdfb3BlbmluZyIsInNlc3Npb25faWQiOjQ2NzMxMjA5NzUxLCJjb250ZW50Ijoi5Lu35qC86L+Y6IO95pu05L2O5ZCXIn0sImFyZzEiOiJDaGF0VG9waWMifX1dLCJjaGF0R3VpZGFuY2UiOnsidHh0Ijoi5bCP6Zey6bG854yc5L2g5oOz6ZeuIiwiY2hhdEd1aWRhbmNlSWNvbiI6Imh0dHBzOi8vZy5hbGljZG4uY29tL2V2YS1hc3NldHMvOTAwYjgwODk2YjBhNGEzMjc0Yzg5OTBkYzc2YTM5MDYvMC4wLjEvdG1wL2M4MzljZmQvYzgzOWNmZC5qc29uIiwiaWNvbiI6Imh0dHBzOi8vZy5hbGljZG4uY29tL2V2YS1hc3NldHMvYWYyOTA2YjM0ODYwYzFmM2Y1YjFlNWU1YzY3MWE5NjYvMC4wLjEvdG1wL2EzZjgyMzIvYTNmODIzMi5qc29uP3NwbT1hMjE3MTQuaG9tZXBhZ2UuMC4wLjRlMGEzZmUwamxPZVl5JmZpbGU9YTNmODIzMi5qc29uIn19fX0sInJlY2VpdmVySWRzIjpbIjIxNTAzNjk4MTkiXSwic2Vzc2lvbkluZm8iOnsiY3JlYXRlVGltZSI6MTczODE2ODg4NjAwMCwiZXh0ZW5zaW9ucyI6eyJleHRVc2VyVHlwZSI6IjAiLCJpdGVtVGl0bGUiOiLkuIrmtbfmsLTlloTmsYfpg73luILmsZfokrgo5p2o5rWm5Yy65LqU6KeS5Zy65bqXKeeJueS7t+mXqOelqDc15YWDIiwic3F1YWRJZF8yMTUwMzY5ODE5IjoiMjAxNDUzMTE1IiwiZXh0VXNlcklkIjoiMjE1MDM2OTgxOSIsIml0ZW1NYWluUGljIjoiaHR0cHM6Ly9pbWcuYWxpY2RuLmNvbS9iYW8vdXBsb2FkZWQvaTQvTzFDTjAxTkhCUWdZMktFeHdGbWd6QW9fISE0NjExNjg2MDE4NDI3MzgxNjA2LTAtZmxlYW1hcmtldC5qcGciLCJvd25lclVzZXJUeXBlIjoiMCIsIml0ZW1JZCI6Ijg1ODQwNTA5MTc5OSIsInNxdWFkTmFtZV84NjU5OTUyNiI6IuS4iua1t+awtOWWhOaxh+mDveW4guaxl+iSuCjmnajmtabljLrkupTop5LlnLrlupcp54m55Lu36Zeo56WoNzXlhYMiLCJpdGVtU2VsbGVySWQiOiI4NjU5OTUyNiIsIm93bmVyVXNlcklkIjoiODY1OTk1MjYiLCJzcXVhZElkXzg2NTk5NTI2IjoiODU4NDA1MDkxNzk5Iiwic3F1YWROYW1lXzIxNTAzNjk4MTkiOiLmuKnms4kv5rSX5rW0In0sImdyb3VwT3duZXJJZCI6Ijg2NTk5NTI2Iiwic2Vzc2lvbklkIjoiNDY3MzEyMDk3NTEiLCJzZXNzaW9uVHlwZSI6MSwidHlwZSI6MX19LCJzZXNzaW9uSWQiOiI0NjczMTIwOTc1MSJ9",
                           "streamId": "370",
                           "objectType": 370000
                         },
                         {
                           "bizType": 370,
                           "data": "eyJjaGF0VHlwZSI6MSwiaW5jcmVtZW50VHlwZSI6MSwib3BlcmF0aW9uIjp7ImNvbnRlbnQiOnsiY29udGVudFR5cGUiOjgsInNlc3Npb25Bcm91c2UiOnsibWVtYmVyRmxhZ3MiOjAsIm5lZWRLZXlCb2FyZCI6ZmFsc2UsInNlc3Npb25Bcm91c2VJbmZvIjp7InJlc2lkZW50RnVuY3Rpb25zIjpbXSwiYXJvdXNlVGltZVN0YW1wIjoxNzM4MTY4ODg2Mzk4LCJhcm91c2VDaGF0U2NyaXB0SW5mbyI6W119fX0sInJlY2VpdmVySWRzIjpbIjIxNTAzNjk4MTkiXSwic2Vzc2lvbkluZm8iOnsiY3JlYXRlVGltZSI6MTczODE2ODg4NjAwMCwiZXh0ZW5zaW9ucyI6eyJleHRVc2VyVHlwZSI6IjAiLCJpdGVtVGl0bGUiOiLkuIrmtbfmsLTlloTmsYfpg73luILmsZfokrgo5p2o5rWm5Yy65LqU6KeS5Zy65bqXKeeJueS7t+mXqOelqDc15YWDIiwic3F1YWRJZF8yMTUwMzY5ODE5IjoiMjAxNDUzMTE1IiwiZXh0VXNlcklkIjoiMjE1MDM2OTgxOSIsIml0ZW1NYWluUGljIjoiaHR0cHM6Ly9pbWcuYWxpY2RuLmNvbS9iYW8vdXBsb2FkZWQvaTQvTzFDTjAxTkhCUWdZMktFeHdGbWd6QW9fISE0NjExNjg2MDE4NDI3MzgxNjA2LTAtZmxlYW1hcmtldC5qcGciLCJvd25lclVzZXJUeXBlIjoiMCIsIml0ZW1JZCI6Ijg1ODQwNTA5MTc5OSIsInNxdWFkTmFtZV84NjU5OTUyNiI6IuS4iua1t+awtOWWhOaxh+mDveW4guaxl+iSuCjmnajmtabljLrkupTop5LlnLrlupcp54m55Lu36Zeo56WoNzXlhYMiLCJpdGVtU2VsbGVySWQiOiI4NjU5OTUyNiIsIm93bmVyVXNlcklkIjoiODY1OTk1MjYiLCJzcXVhZElkXzg2NTk5NTI2IjoiODU4NDA1MDkxNzk5Iiwic3F1YWROYW1lXzIxNTAzNjk4MTkiOiLmuKnms4kv5rSX5rW0In0sImdyb3VwT3duZXJJZCI6Ijg2NTk5NTI2Iiwic2Vzc2lvbklkIjoiNDY3MzEyMDk3NTEiLCJzZXNzaW9uVHlwZSI6MSwidHlwZSI6MX19LCJzZXNzaW9uSWQiOiI0NjczMTIwOTc1MSJ9",
                           "streamId": "370",
                           "objectType": 370000
                         },
                         {
                           "bizType": 370,
                           "data": "eyJjaGF0VHlwZSI6MSwiaW5jcmVtZW50VHlwZSI6MSwib3BlcmF0aW9uIjp7ImNvbnRlbnQiOnsiY29udGVudFR5cGUiOjgsInNlc3Npb25Bcm91c2UiOnsibWVtYmVyRmxhZ3MiOjAsIm5lZWRLZXlCb2FyZCI6dHJ1ZSwic2Vzc2lvbkFyb3VzZUluZm8iOnsicmVzaWRlbnRGdW5jdGlvbnMiOltdLCJhcm91c2VUaW1lU3RhbXAiOjE3MzgxNjkwNTcwMDAsImFyb3VzZUNoYXRTY3JpcHRJbmZvIjpbeyJjaGF0U2NyaXAiOiLov5jmnInliKvnmoTmuKnms4kv5rSX5rW06Zey572u55So5ZOB5ZCX77yfIiwiY2hhdFNjcmlwU3RyYXRlZ3kiOiJTVElNVUxBVEVEX1NBTEVfQlVZIiwiYXJnSW5mbyI6eyJhcmdzIjp7InRvcGljX3RpdGxlIjoiU1RJTVVMQVRFRF9TQUxFX0JVWSIsInNlc3Npb25faWQiOjQ2NzMxMjA5NzUxLCJjb250ZW50Ijoi6L+Y5pyJ5Yir55qE5rip5rOJL+a0l+a1tOmXsue9rueUqOWTgeWQl++8nyJ9LCJhcmcxIjoiQ2hhdFRvcGljIn19XSwiY2hhdEd1aWRhbmNlIjp7fX19fSwicmVjZWl2ZXJJZHMiOlsiMjE1MDM2OTgxOSJdLCJzZXNzaW9uSW5mbyI6eyJjcmVhdGVUaW1lIjoxNzM4MTY4ODg2MDAwLCJleHRlbnNpb25zIjp7ImV4dFVzZXJUeXBlIjoiMCIsIml0ZW1UaXRsZSI6IuS4iua1t+awtOWWhOaxh+mDveW4guaxl+iSuCjmnajmtabljLrkupTop5LlnLrlupcp54m55Lu36Zeo56WoNzXlhYMiLCJzcXVhZElkXzIxNTAzNjk4MTkiOiIyMDE0NTMxMTUiLCJleHRVc2VySWQiOiIyMTUwMzY5ODE5IiwiaXRlbU1haW5QaWMiOiJodHRwczovL2ltZy5hbGljZG4uY29tL2Jhby91cGxvYWRlZC9pNC9PMUNOMDFOSEJRZ1kyS0V4d0ZtZ3pBb18hITQ2MTE2ODYwMTg0MjczODE2MDYtMC1mbGVhbWFya2V0LmpwZyIsIm93bmVyVXNlclR5cGUiOiIwIiwiaXRlbUlkIjoiODU4NDA1MDkxNzk5Iiwic3F1YWROYW1lXzg2NTk5NTI2Ijoi5LiK5rW35rC05ZaE5rGH6YO95biC5rGX6JK4KOadqOa1puWMuuS6lOinkuWcuuW6lynnibnku7fpl6jnpag3NeWFgyIsIml0ZW1TZWxsZXJJZCI6Ijg2NTk5NTI2Iiwib3duZXJVc2VySWQiOiI4NjU5OTUyNiIsInNxdWFkSWRfODY1OTk1MjYiOiI4NTg0MDUwOTE3OTkiLCJzcXVhZE5hbWVfMjE1MDM2OTgxOSI6Iua4qeaziS/mtJfmtbQifSwiZ3JvdXBPd25lcklkIjoiODY1OTk1MjYiLCJzZXNzaW9uSWQiOiI0NjczMTIwOTc1MSIsInNlc3Npb25UeXBlIjoxLCJ0eXBlIjoxfX0sInNlc3Npb25JZCI6IjQ2NzMxMjA5NzUxIn0=",
                           "streamId": "370",
                           "objectType": 370000
                         },
                         {
                           "bizType": 370,
                           "data": "eyJjaGF0VHlwZSI6MSwiaW5jcmVtZW50VHlwZSI6MSwib3BlcmF0aW9uIjp7ImNvbnRlbnQiOnsiY29udGVudFR5cGUiOjgsInNlc3Npb25Bcm91c2UiOnsibWVtYmVyRmxhZ3MiOjAsIm5lZWRLZXlCb2FyZCI6ZmFsc2UsInNlc3Npb25Bcm91c2VJbmZvIjp7InJlc2lkZW50RnVuY3Rpb25zIjpbXSwiYXJvdXNlVGltZVN0YW1wIjoxNzM4MTY5MDU3MDAwLCJhcm91c2VDaGF0U2NyaXB0SW5mbyI6W119fX0sInJlY2VpdmVySWRzIjpbIjIxNTAzNjk4MTkiXSwic2Vzc2lvbkluZm8iOnsiY3JlYXRlVGltZSI6MTczODE2ODg4NjAwMCwiZXh0ZW5zaW9ucyI6eyJleHRVc2VyVHlwZSI6IjAiLCJpdGVtVGl0bGUiOiLkuIrmtbfmsLTlloTmsYfpg73luILmsZfokrgo5p2o5rWm5Yy65LqU6KeS5Zy65bqXKeeJueS7t+mXqOelqDc15YWDIiwic3F1YWRJZF8yMTUwMzY5ODE5IjoiMjAxNDUzMTE1IiwiZXh0VXNlcklkIjoiMjE1MDM2OTgxOSIsIml0ZW1NYWluUGljIjoiaHR0cHM6Ly9pbWcuYWxpY2RuLmNvbS9iYW8vdXBsb2FkZWQvaTQvTzFDTjAxTkhCUWdZMktFeHdGbWd6QW9fISE0NjExNjg2MDE4NDI3MzgxNjA2LTAtZmxlYW1hcmtldC5qcGciLCJvd25lclVzZXJUeXBlIjoiMCIsIml0ZW1JZCI6Ijg1ODQwNTA5MTc5OSIsInNxdWFkTmFtZV84NjU5OTUyNiI6IuS4iua1t+awtOWWhOaxh+mDveW4guaxl+iSuCjmnajmtabljLrkupTop5LlnLrlupcp54m55Lu36Zeo56WoNzXlhYMiLCJpdGVtU2VsbGVySWQiOiI4NjU5OTUyNiIsIm93bmVyVXNlcklkIjoiODY1OTk1MjYiLCJzcXVhZElkXzg2NTk5NTI2IjoiODU4NDA1MDkxNzk5Iiwic3F1YWROYW1lXzIxNTAzNjk4MTkiOiLmuKnms4kv5rSX5rW0In0sImdyb3VwT3duZXJJZCI6Ijg2NTk5NTI2Iiwic2Vzc2lvbklkIjoiNDY3MzEyMDk3NTEiLCJzZXNzaW9uVHlwZSI6MSwidHlwZSI6MX19LCJzZXNzaW9uSWQiOiI0NjczMTIwOTc1MSJ9",
                           "streamId": "370",
                           "objectType": 370000
                         },
                         {
                           "bizType": 370,
                           "data": "eyJjaGF0VHlwZSI6MSwiaW5jcmVtZW50VHlwZSI6MSwib3BlcmF0aW9uIjp7ImNvbnRlbnQiOnsiY29udGVudFR5cGUiOjgsInNlc3Npb25Bcm91c2UiOnsibWVtYmVyRmxhZ3MiOjAsIm5lZWRLZXlCb2FyZCI6ZmFsc2UsInNlc3Npb25Bcm91c2VJbmZvIjp7InJlc2lkZW50RnVuY3Rpb25zIjpbXSwiYXJvdXNlVGltZVN0YW1wIjoxNzM3MjE5OTcyMjk0LCJhcm91c2VDaGF0U2NyaXB0SW5mbyI6W119fX0sInJlY2VpdmVySWRzIjpbIjIxNTAzNjk4MTkiXSwic2Vzc2lvbkluZm8iOnsiY3JlYXRlVGltZSI6MTczNTkyNjgwODAwMCwiZXh0ZW5zaW9ucyI6eyJleHRVc2VyVHlwZSI6IjAiLCJzcXVhZE5hbWVfMjgxMjcxOTEyMSI6IuS4iua1t+awtOWWhOaxh+mDveW4guaxl+iSuCDmnajmtablupcgIOeUteWtkOa1tOi1hOWIuCIsIml0ZW1UaXRsZSI6IuS4iua1t+awtOWWhOaxh+mDveW4guaxl+iSuCDmnajmtablupcgIOeUteWtkOa1tOi1hOWIuCIsInNxdWFkSWRfMjE1MDM2OTgxOSI6IjIwMTQ1MzExNSIsImV4dFVzZXJJZCI6IjIxNTAzNjk4MTkiLCJpdGVtTWFpblBpYyI6Imh0dHBzOi8vaW1nLmFsaWNkbi5jb20vYmFvL3VwbG9hZGVkL2kxL08xQ04wMTB4TnFuSDJIRlRXbEFuMWlyXyEhNDYxMTY4NjAxODQyNzM4MzgyNS01My1mbGVhbWFya2V0LmhlaWMiLCJvd25lclVzZXJUeXBlIjoiMCIsIml0ZW1JZCI6Ijg1NjYwMDI1NTAwNSIsIml0ZW1TZWxsZXJJZCI6IjI4MTI3MTkxMjEiLCJvd25lclVzZXJJZCI6IjI4MTI3MTkxMjEiLCJzcXVhZE5hbWVfMjE1MDM2OTgxOSI6Iua4qeaziS/mtJfmtbQiLCJzcXVhZElkXzI4MTI3MTkxMjEiOiI4NTY2MDAyNTUwMDUifSwiZ3JvdXBPd25lcklkIjoiMjgxMjcxOTEyMSIsInNlc3Npb25JZCI6IjQ2MTQzNDI2NjA5Iiwic2Vzc2lvblR5cGUiOjEsInR5cGUiOjF9fSwic2Vzc2lvbklkIjoiNDYxNDM0MjY2MDkifQ==",
                           "streamId": "370",
                           "objectType": 370000
                         },
                         {
                           "bizType": 370,
                           "data": "eyJjaGF0VHlwZSI6MSwiaW5jcmVtZW50VHlwZSI6MSwib3BlcmF0aW9uIjp7ImNvbnRlbnQiOnsiY29udGVudFR5cGUiOjgsInNlc3Npb25Bcm91c2UiOnsibWVtYmVyRmxhZ3MiOjAsIm5lZWRLZXlCb2FyZCI6dHJ1ZSwic2Vzc2lvbkFyb3VzZUluZm8iOnsicmVzaWRlbnRGdW5jdGlvbnMiOltdLCJhcm91c2VUaW1lU3RhbXAiOjE3Mzk3MTAyNTAwMDAsImFyb3VzZUNoYXRTY3JpcHRJbmZvIjpbeyJjaGF0U2NyaXAiOiLov5jmnInliKvnmoTmuKnms4kv5rSX5rW06Zey572u55So5ZOB5ZCX77yfIiwiY2hhdFNjcmlwU3RyYXRlZ3kiOiJTVElNVUxBVEVEX1NBTEVfQlVZIiwiYXJnSW5mbyI6eyJhcmdzIjp7InRvcGljX3RpdGxlIjoiU1RJTVVMQVRFRF9TQUxFX0JVWSIsInNlc3Npb25faWQiOjQ2MTQzNDI2NjA5LCJjb250ZW50Ijoi6L+Y5pyJ5Yir55qE5rip5rOJL+a0l+a1tOmXsue9rueUqOWTgeWQl++8nyJ9LCJhcmcxIjoiQ2hhdFRvcGljIn19XSwiY2hhdEd1aWRhbmNlIjp7fX19fSwicmVjZWl2ZXJJZHMiOlsiMjE1MDM2OTgxOSJdLCJzZXNzaW9uSW5mbyI6eyJjcmVhdGVUaW1lIjoxNzM1OTI2ODA4MDAwLCJleHRlbnNpb25zIjp7ImV4dFVzZXJUeXBlIjoiMCIsInNxdWFkTmFtZV8yODEyNzE5MTIxIjoi5LiK5rW35rC05ZaE5rGH6YO95biC5rGX6JK4IOadqOa1puW6lyAg55S15a2Q5rW06LWE5Yi4IiwiaXRlbVRpdGxlIjoi5LiK5rW35rC05ZaE5rGH6YO95biC5rGX6JK4IOadqOa1puW6lyAg55S15a2Q5rW06LWE5Yi4Iiwic3F1YWRJZF8yMTUwMzY5ODE5IjoiMjAxNDUzMTE1IiwiZXh0VXNlcklkIjoiMjE1MDM2OTgxOSIsIml0ZW1NYWluUGljIjoiaHR0cHM6Ly9pbWcuYWxpY2RuLmNvbS9iYW8vdXBsb2FkZWQvaTEvTzFDTjAxMHhOcW5IMkhGVFdsQW4xaXJfISE0NjExNjg2MDE4NDI3MzgzODI1LTUzLWZsZWFtYXJrZXQuaGVpYyIsIm93bmVyVXNlclR5cGUiOiIwIiwiaXRlbUlkIjoiODU2NjAwMjU1MDA1IiwiaXRlbVNlbGxlcklkIjoiMjgxMjcxOTEyMSIsIm93bmVyVXNlcklkIjoiMjgxMjcxOTEyMSIsInNxdWFkTmFtZV8yMTUwMzY5ODE5Ijoi5rip5rOJL+a0l+a1tCIsInNxdWFkSWRfMjgxMjcxOTEyMSI6Ijg1NjYwMDI1NTAwNSJ9LCJzZXNzaW9uSWQiOiI0NjE0MzQyNjYwOSIsInNlc3Npb25UeXBlIjoxLCJ0eXBlIjoxfX0sInNlc3Npb25JZCI6IjQ2MTQzNDI2NjA5In0=",
                           "streamId": "370",
                           "objectType": 370000
                         },
                         {
                           "bizType": 370,
                           "data": "eyJjaGF0VHlwZSI6MSwiaW5jcmVtZW50VHlwZSI6MSwib3BlcmF0aW9uIjp7ImNvbnRlbnQiOnsiY29udGVudFR5cGUiOjgsInNlc3Npb25Bcm91c2UiOnsibWVtYmVyRmxhZ3MiOjAsIm5lZWRLZXlCb2FyZCI6dHJ1ZSwic2Vzc2lvbkFyb3VzZUluZm8iOnsicmVzaWRlbnRGdW5jdGlvbnMiOltdLCJhcm91c2VUaW1lU3RhbXAiOjE3NDI1MzY4NTQ2NDIsImFyb3VzZUNoYXRTY3JpcHRJbmZvIjpbeyJjaGF0U2NyaXAiOiLpl67kuIvmmK9j5Y+j5ZibIiwiY2hhdFNjcmlwU3RyYXRlZ3kiOiJzaGFkaW5nX29wZW5pbmciLCJhcmdJbmZvIjp7ImFyZ3MiOnsidG9waWNfdGl0bGUiOiJzaGFkaW5nX29wZW5pbmciLCJzZXNzaW9uX2lkIjo0ODI0Mzg2NTY4NiwiY29udGVudCI6IumXruS4i+aYr2Plj6PlmJsifSwiYXJnMSI6IkNoYXRUb3BpYyJ9fSx7ImNoYXRTY3JpcCI6IuWKn+iDvemDveato+W4uOWQl++8nyIsImNoYXRTY3JpcFN0cmF0ZWd5Ijoic2hhZGluZ19vcGVuaW5nIiwiYXJnSW5mbyI6eyJhcmdzIjp7InRvcGljX3RpdGxlIjoic2hhZGluZ19vcGVuaW5nIiwic2Vzc2lvbl9pZCI6NDgyNDM4NjU2ODYsImNvbnRlbnQiOiLlip/og73pg73mraPluLjlkJfvvJ8ifSwiYXJnMSI6IkNoYXRUb3BpYyJ9fSx7ImNoYXRTY3JpcCI6IuS7t+mSsei/mOWPr+S7peiwiOWQlyIsImNoYXRTY3JpcFN0cmF0ZWd5Ijoic2hhZGluZ19vcGVuaW5nIiwiYXJnSW5mbyI6eyJhcmdzIjp7InRvcGljX3RpdGxlIjoic2hhZGluZ19vcGVuaW5nIiwic2Vzc2lvbl9pZCI6NDgyNDM4NjU2ODYsImNvbnRlbnQiOiLku7fpkrHov5jlj6/ku6XosIjlkJcifSwiYXJnMSI6IkNoYXRUb3BpYyJ9fV0sImNoYXRHdWlkYW5jZSI6eyJ0eHQiOiLlsI/pl7LpsbznjJzkvaDmg7Ppl64iLCJjaGF0R3VpZGFuY2VJY29uIjoiaHR0cHM6Ly9nLmFsaWNkbi5jb20vZXZhLWFzc2V0cy85MDBiODA4OTZiMGE0YTMyNzRjODk5MGRjNzZhMzkwNi8wLjAuMS90bXAvYzgzOWNmZC9jODM5Y2ZkLmpzb24iLCJpY29uIjoiaHR0cHM6Ly9nLmFsaWNkbi5jb20vZXZhLWFzc2V0cy9hZjI5MDZiMzQ4NjBjMWYzZjViMWU1ZTVjNjcxYTk2Ni8wLjAuMS90bXAvYTNmODIzMi9hM2Y4MjMyLmpzb24/c3BtPWEyMTcxNC5ob21lcGFnZS4wLjAuNGUwYTNmZTBqbE9lWXkmZmlsZT1hM2Y4MjMyLmpzb24ifX19fSwicmVjZWl2ZXJJZHMiOlsiMjE1MDM2OTgxOSJdLCJzZXNzaW9uSW5mbyI6eyJjcmVhdGVUaW1lIjoxNzQyNTM2ODU0MDAwLCJleHRlbnNpb25zIjp7Iml0ZW1JZCI6Ijg5NDYzNDMxNTQzNyIsIml0ZW1GZWF0dXJlcyI6IntcImlkbGVfY2F0X2xlYWZcIjpcIjEyNjkyMDE0NVwifSIsIml0ZW1TZWxsZXJJZCI6IjIyMTU4NTg5MTU5NzciLCJWVUxDQU5fQ1JFQVRFX1RJTUUiOiIxNzQyNTM2ODU0NTU5IiwiZXh0VXNlclR5cGUiOiIwIiwiaXRlbVRpdGxlIjoiRHM05omL5p+E5pS56KOF5oiQ5ZOB77yMMDU15Li75p2/77yM55So5p2l5omTYXBleOeahO+8jOeOqeS6huWHoOWkqemAgCIsIm93bmVyVXNlcklkIjoiMjIxNTg1ODkxNTk3NyIsImV4dFVzZXJJZCI6IjIxNTAzNjk4MTkiLCJpdGVtTWFpblBpYyI6Imh0dHBzOi8vaW1nLmFsaWNkbi5jb20vYmFvL3VwbG9hZGVkL2kzL08xQ04wMWcwZFU0bjF1MVdLNjdLWWRTXyEhNDYxMTY4NjAxODQyNzM4MjQwOS0wLWZsZWFtYXJrZXQuanBnIiwib3duZXJVc2VyVHlwZSI6IjAifSwiZ3JvdXBPd25lcklkIjoiMjIxNTg1ODkxNTk3NyIsInNlc3Npb25JZCI6IjQ4MjQzODY1Njg2Iiwic2Vzc2lvblR5cGUiOjEsInR5cGUiOjF9fSwic2Vzc2lvbklkIjoiNDgyNDM4NjU2ODYifQ==",
                           "streamId": "370",
                           "objectType": 370000
                         },
                         {
                           "bizType": 370,
                           "data": "eyJjaGF0VHlwZSI6MSwiaW5jcmVtZW50VHlwZSI6MSwib3BlcmF0aW9uIjp7ImNvbnRlbnQiOnsiY29udGVudFR5cGUiOjgsInNlc3Npb25Bcm91c2UiOnsibWVtYmVyRmxhZ3MiOjAsIm5lZWRLZXlCb2FyZCI6dHJ1ZSwic2Vzc2lvbkFyb3VzZUluZm8iOnsicmVzaWRlbnRGdW5jdGlvbnMiOltdLCJhcm91c2VUaW1lU3RhbXAiOjE3NDI1NTAzMzM4MDgsImFyb3VzZUNoYXRTY3JpcHRJbmZvIjpbeyJjaGF0U2NyaXAiOiLpl67kuIvmmK9j5Y+j5ZibIiwiY2hhdFNjcmlwU3RyYXRlZ3kiOiJzaGFkaW5nX29wZW5pbmciLCJhcmdJbmZvIjp7ImFyZ3MiOnsidG9waWNfdGl0bGUiOiJzaGFkaW5nX29wZW5pbmciLCJzZXNzaW9uX2lkIjo0ODI5NDUwNDc3OSwiY29udGVudCI6IumXruS4i+aYr2Plj6PlmJsifSwiYXJnMSI6IkNoYXRUb3BpYyJ9fSx7ImNoYXRTY3JpcCI6IuiDveS+v+WunOeCueiAg+iZkeWHuuWQlyIsImNoYXRTY3JpcFN0cmF0ZWd5Ijoic2hhZGluZ19vcGVuaW5nIiwiYXJnSW5mbyI6eyJhcmdzIjp7InRvcGljX3RpdGxlIjoic2hhZGluZ19vcGVuaW5nIiwic2Vzc2lvbl9pZCI6NDgyOTQ1MDQ3NzksImNvbnRlbnQiOiLog73kvr/lrpzngrnogIPomZHlh7rlkJcifSwiYXJnMSI6IkNoYXRUb3BpYyJ9fV0sImNoYXRHdWlkYW5jZSI6eyJ0eHQiOiLlsI/pl7LpsbznjJzkvaDmg7Ppl64iLCJjaGF0R3VpZGFuY2VJY29uIjoiaHR0cHM6Ly9nLmFsaWNkbi5jb20vZXZhLWFzc2V0cy85MDBiODA4OTZiMGE0YTMyNzRjODk5MGRjNzZhMzkwNi8wLjAuMS90bXAvYzgzOWNmZC9jODM5Y2ZkLmpzb24iLCJpY29uIjoiaHR0cHM6Ly9nLmFsaWNkbi5jb20vZXZhLWFzc2V0cy9hZjI5MDZiMzQ4NjBjMWYzZjViMWU1ZTVjNjcxYTk2Ni8wLjAuMS90bXAvYTNmODIzMi9hM2Y4MjMyLmpzb24/c3BtPWEyMTcxNC5ob21lcGFnZS4wLjAuNGUwYTNmZTBqbE9lWXkmZmlsZT1hM2Y4MjMyLmpzb24ifX19fSwicmVjZWl2ZXJJZHMiOlsiMjE1MDM2OTgxOSJdLCJzZXNzaW9uSW5mbyI6eyJjcmVhdGVUaW1lIjoxNzQyNTUwMzMzMDAwLCJleHRlbnNpb25zIjp7Iml0ZW1JZCI6Ijg5MzAxMDMwNTQ4NyIsIml0ZW1GZWF0dXJlcyI6IntcImlkbGVfY2F0X2xlYWZcIjpcIjEyNjkyMDE0NVwifSIsIml0ZW1TZWxsZXJJZCI6IjIyMDA3MjAzNTYyOTYiLCJWVUxDQU5fQ1JFQVRFX1RJTUUiOiIxNzQyNTUwMzMzNzY3IiwiZXh0VXNlclR5cGUiOiIwIiwiaXRlbVRpdGxlIjoiU0NVRiBpbmZpbml0eSIsIm93bmVyVXNlcklkIjoiMjIwMDcyMDM1NjI5NiIsImV4dFVzZXJJZCI6IjIxNTAzNjk4MTkiLCJpdGVtTWFpblBpYyI6Imh0dHBzOi8vaW1nLmFsaWNkbi5jb20vYmFvL3VwbG9hZGVkL2kxL08xQ04wMTN4bnZMODF3TmNmUGw5NXJjXyEhNDYxMTY4NjAxODQyNzM4NDc3Ni0wLWZsZWFtYXJrZXQuanBnIiwib3duZXJVc2VyVHlwZSI6IjAifSwiZ3JvdXBPd25lcklkIjoiMjIwMDcyMDM1NjI5NiIsInNlc3Npb25JZCI6IjQ4Mjk0NTA0Nzc5Iiwic2Vzc2lvblR5cGUiOjEsInR5cGUiOjF9fSwic2Vzc2lvbklkIjoiNDgyOTQ1MDQ3NzkifQ==",
                           "streamId": "370",
                           "objectType": 370000
                         },
                         {
                           "bizType": 370,
                           "data": "eyJjaGF0VHlwZSI6MSwiaW5jcmVtZW50VHlwZSI6MSwib3BlcmF0aW9uIjp7ImNvbnRlbnQiOnsiY29udGVudFR5cGUiOjgsInNlc3Npb25Bcm91c2UiOnsibWVtYmVyRmxhZ3MiOjAsIm5lZWRLZXlCb2FyZCI6dHJ1ZSwic2Vzc2lvbkFyb3VzZUluZm8iOnsicmVzaWRlbnRGdW5jdGlvbnMiOltdLCJhcm91c2VUaW1lU3RhbXAiOjE3NDQzNzMwMzIxNDIsImFyb3VzZUNoYXRTY3JpcHRJbmZvIjpbeyJjaGF0U2NyaXAiOiLmnInnm5LlrZDlkJfvvJ8iLCJjaGF0U2NyaXBTdHJhdGVneSI6InNoYWRpbmdfb3BlbmluZyIsImFyZ0luZm8iOnsiYXJncyI6eyJ0b3BpY190aXRsZSI6InNoYWRpbmdfb3BlbmluZyIsInNlc3Npb25faWQiOjQ4ODM1NjkyNDczLCJjb250ZW50Ijoi5pyJ55uS5a2Q5ZCX77yfIn0sImFyZzEiOiJDaGF0VG9waWMifX0seyJjaGF0U2NyaXAiOiLpg73mmK/mraPniYjlkJfvvJ8iLCJjaGF0U2NyaXBTdHJhdGVneSI6InNoYWRpbmdfb3BlbmluZyIsImFyZ0luZm8iOnsiYXJncyI6eyJ0b3BpY190aXRsZSI6InNoYWRpbmdfb3BlbmluZyIsInNlc3Npb25faWQiOjQ4ODM1NjkyNDczLCJjb250ZW50Ijoi6YO95piv5q2j54mI5ZCX77yfIn0sImFyZzEiOiJDaGF0VG9waWMifX0seyJjaGF0U2NyaXAiOiLnu5nkuKrmnIvlj4vku7flkKciLCJjaGF0U2NyaXBTdHJhdGVneSI6InNoYWRpbmdfb3BlbmluZyIsImFyZ0luZm8iOnsiYXJncyI6eyJ0b3BpY190aXRsZSI6InNoYWRpbmdfb3BlbmluZyIsInNlc3Npb25faWQiOjQ4ODM1NjkyNDczLCJjb250ZW50Ijoi57uZ5Liq5pyL5Y+L5Lu35ZCnIn0sImFyZzEiOiJDaGF0VG9waWMifX1dLCJjaGF0R3VpZGFuY2UiOnsidHh0Ijoi5bCP6Zey6bG854yc5L2g5oOz6ZeuIiwiY2hhdEd1aWRhbmNlSWNvbiI6Imh0dHBzOi8vZy5hbGljZG4uY29tL2V2YS1hc3NldHMvOTAwYjgwODk2YjBhNGEzMjc0Yzg5OTBkYzc2YTM5MDYvMC4wLjEvdG1wL2M4MzljZmQvYzgzOWNmZC5qc29uIiwiaWNvbiI6Imh0dHBzOi8vZy5hbGljZG4uY29tL2V2YS1hc3NldHMvYWYyOTA2YjM0ODYwYzFmM2Y1YjFlNWU1YzY3MWE5NjYvMC4wLjEvdG1wL2EzZjgyMzIvYTNmODIzMi5qc29uP3NwbT1hMjE3MTQuaG9tZXBhZ2UuMC4wLjRlMGEzZmUwamxPZVl5JmZpbGU9YTNmODIzMi5qc29uIn19fX0sInJlY2VpdmVySWRzIjpbIjIxNTAzNjk4MTkiXSwic2Vzc2lvbkluZm8iOnsiY3JlYXRlVGltZSI6MTc0NDM3MzAzMjAwMCwiZXh0ZW5zaW9ucyI6eyJleHRVc2VyVHlwZSI6IjAiLCJpdGVtVGl0bGUiOiLmgKrniannjI7kurroib7pnLLnjKvnjKvppbzmjILku7bjgILlj6/lgZrlhrDnrrHotLTjgILlt6XmnJ/kuIDlkajjgILljIXpgq7jgIIiLCJzcXVhZElkXzIxNTAzNjk4MTkiOiIxMjY4NjAyOTYiLCJleHRVc2VySWQiOiIyMTUwMzY5ODE5IiwiaXRlbU1haW5QaWMiOiJodHRwczovL2ltZy5hbGljZG4uY29tL2Jhby91cGxvYWRlZC9pMS9PMUNOMDFXQzBWdGwyM1Y4VTRxSjNieF8hITQ2MTE2ODYwMTg0MjczODI3NDgtMC1mbGVhbWFya2V0LmpwZyIsIm93bmVyVXNlclR5cGUiOiIwIiwic3F1YWROYW1lXzcyODg3MjYwIjoi5oCq54mp54yO5Lq66Im+6Zyy54yr54yr6aW85oyC5Lu244CC5Y+v5YGa5Yaw566x6LS044CC5bel5pyf5LiA5ZGo44CC5YyF6YKu44CCIiwic3F1YWRJZF83Mjg4NzI2MCI6IjkwMTM5OTAzNDg1NyIsIml0ZW1JZCI6IjkwMTM5OTAzNDg1NyIsIml0ZW1GZWF0dXJlcyI6IntcImlkbGVfY2F0X2xlYWZcIjpcIjEyNjg2MDI5NlwifSIsIml0ZW1TZWxsZXJJZCI6IjcyODg3MjYwIiwib3duZXJVc2VySWQiOiI3Mjg4NzI2MCIsInNxdWFkTmFtZV8yMTUwMzY5ODE5Ijoi5YW25LuW5ri45oiP5Yqo5ryr5ZGo6L65In0sImdyb3VwT3duZXJJZCI6IjcyODg3MjYwIiwic2Vzc2lvbklkIjoiNDg4MzU2OTI0NzMiLCJzZXNzaW9uVHlwZSI6MSwidHlwZSI6MX19LCJzZXNzaW9uSWQiOiI0ODgzNTY5MjQ3MyJ9",
                           "streamId": "370",
                           "objectType": 370000
                         },
                         {
                           "bizType": 370,
                           "data": "eyJjaGF0VHlwZSI6MSwiaW5jcmVtZW50VHlwZSI6MSwib3BlcmF0aW9uIjp7ImNvbnRlbnQiOnsiY29udGVudFR5cGUiOjgsInNlc3Npb25Bcm91c2UiOnsibWVtYmVyRmxhZ3MiOjAsIm5lZWRLZXlCb2FyZCI6ZmFsc2UsInNlc3Npb25Bcm91c2VJbmZvIjp7InJlc2lkZW50RnVuY3Rpb25zIjpbXSwiYXJvdXNlVGltZVN0YW1wIjoxNzQ0MzczMDMyMTQyLCJhcm91c2VDaGF0U2NyaXB0SW5mbyI6W119fX0sInJlY2VpdmVySWRzIjpbIjIxNTAzNjk4MTkiXSwic2Vzc2lvbkluZm8iOnsiY3JlYXRlVGltZSI6MTc0NDM3MzAzMjAwMCwiZXh0ZW5zaW9ucyI6eyJleHRVc2VyVHlwZSI6IjAiLCJpdGVtVGl0bGUiOiLmgKrniannjI7kurroib7pnLLnjKvnjKvppbzmjILku7bjgILlj6/lgZrlhrDnrrHotLTjgILlt6XmnJ/kuIDlkajjgILljIXpgq7jgIIiLCJzcXVhZElkXzIxNTAzNjk4MTkiOiIxMjY4NjAyOTYiLCJleHRVc2VySWQiOiIyMTUwMzY5ODE5IiwiaXRlbU1haW5QaWMiOiJodHRwczovL2ltZy5hbGljZG4uY29tL2Jhby91cGxvYWRlZC9pMS9PMUNOMDFXQzBWdGwyM1Y4VTRxSjNieF8hITQ2MTE2ODYwMTg0MjczODI3NDgtMC1mbGVhbWFya2V0LmpwZyIsIm93bmVyVXNlclR5cGUiOiIwIiwic3F1YWROYW1lXzcyODg3MjYwIjoi5oCq54mp54yO5Lq66Im+6Zyy54yr54yr6aW85oyC5Lu244CC5Y+v5YGa5Yaw566x6LS044CC5bel5pyf5LiA5ZGo44CC5YyF6YKu44CCIiwic3F1YWRJZF83Mjg4NzI2MCI6IjkwMTM5OTAzNDg1NyIsIml0ZW1JZCI6IjkwMTM5OTAzNDg1NyIsIml0ZW1GZWF0dXJlcyI6IntcImlkbGVfY2F0X2xlYWZcIjpcIjEyNjg2MDI5NlwifSIsIml0ZW1TZWxsZXJJZCI6IjcyODg3MjYwIiwib3duZXJVc2VySWQiOiI3Mjg4NzI2MCIsInNxdWFkTmFtZV8yMTUwMzY5ODE5Ijoi5YW25LuW5ri45oiP5Yqo5ryr5ZGo6L65In0sImdyb3VwT3duZXJJZCI6IjcyODg3MjYwIiwic2Vzc2lvbklkIjoiNDg4MzU2OTI0NzMiLCJzZXNzaW9uVHlwZSI6MSwidHlwZSI6MX19LCJzZXNzaW9uSWQiOiI0ODgzNTY5MjQ3MyJ9",
                           "streamId": "370",
                           "objectType": 370000
                         },
                         {
                           "bizType": 370,
                           "data": "eyJjaGF0VHlwZSI6MSwiaW5jcmVtZW50VHlwZSI6MSwib3BlcmF0aW9uIjp7ImNvbnRlbnQiOnsiY29udGVudFR5cGUiOjgsInNlc3Npb25Bcm91c2UiOnsibWVtYmVyRmxhZ3MiOjAsIm5lZWRLZXlCb2FyZCI6dHJ1ZSwic2Vzc2lvbkFyb3VzZUluZm8iOnsicmVzaWRlbnRGdW5jdGlvbnMiOltdLCJhcm91c2VUaW1lU3RhbXAiOjE3NDQ0MjUyODI0OTcsImFyb3VzZUNoYXRTY3JpcHRJbmZvIjpbeyJjaGF0U2NyaXAiOiLmmoLml6Dms5XlvIDpgJog6K+35Zyo6Zey6bG8QVBQ5LiL5Y2VIiwiY2hhdFNjcmlwU3RyYXRlZ3kiOiJ3eFNlbGxlckNoZWNrIiwiYXJnSW5mbyI6eyJhcmdzIjp7InRvcGljX3RpdGxlIjoid3hTZWxsZXJDaGVjayIsInNlc3Npb25faWQiOjQ4NzY5NTEwODEwLCJjb250ZW50Ijoi5pqC5peg5rOV5byA6YCaIOivt+WcqOmXsumxvEFQUOS4i+WNlSJ9LCJhcmcxIjoiQ2hhdFRvcGljIn0sImV4dEluZm8iOnsidGFvY29kZSI6eyJwaWNVcmwiOiJodHRwOi8vaW1nLmFsaWNkbi5jb20vYmFvL3VwbG9hZGVkL2kyL08xQ04wMWJUMGRpMjJNUDl2M01XUWxBXyEhMC1mbGVhbWFya2V0LmpwZyIsInNvdXJjZVR5cGUiOiJvdGhlciIsImJpeklkIjoieGlhbnl1IiwidGl0bGUiOiIj5a6Y5pa55Y2h5aWXICMxNeWRqOW5tCAj5q2m6Jek5ri45oiPICPmoJflrZDnkIMgI+e7neeJiOWNoeWllyIsIm9wZW5BcHBOYW1lIjoiJ+mXsumxvOaIluaJi+acuua3mOWunSIsInRhcmdldFVybCI6Imh0dHBzOi8vaDUubS5nb29maXNoLmNvbS9pdGVtP2lkPTc4ODE3NTQ3OTQ3MSJ9fX1dfX19LCJyZWNlaXZlcklkcyI6WyIyMTUwMzY5ODE5Il0sInNlc3Npb25JbmZvIjp7ImNyZWF0ZVRpbWUiOjE3NDQ0MjUyNjI2NzcsImV4dGVuc2lvbnMiOnsiaXRlbUlkIjoiNzg4MTc1NDc5NDcxIiwiaXRlbVNlbGxlcklkIjoiMjE1MDM2OTgxOSIsIlZVTENBTl9DUkVBVEVfVElNRSI6IjE3NDQ0MjUyNjI2NzAiLCJleHRVc2VyVHlwZSI6IjAiLCJvd25lclVzZXJJZCI6IjIxNTAzNjk4MTkiLCJleHRVc2VySWQiOiIyMjE2OTc5OTA5MTgzIiwiVlVMQ0FOX1NSQyI6ImhlcmFjbGVzIiwic291cmNlIjoid3giLCJpdGVtTWFpblBpYyI6Imh0dHBzOi8vaW1nLmFsaWNkbi5jb20vYmFvL3VwbG9hZGVkL2kyL08xQ04wMWJUMGRpMjJNUDl2M01XUWxBXyEhMC1mbGVhbWFya2V0LmpwZyIsIm93bmVyVXNlclR5cGUiOiIwIn0sInNlc3Npb25JZCI6IjQ4NzY5NTEwODEwIiwic2Vzc2lvblR5cGUiOjEsInR5cGUiOjF9fSwic2Vzc2lvbklkIjoiNDg3Njk1MTA4MTAifQ==",
                           "streamId": "370",
                           "objectType": 370000
                         },
                         {
                           "bizType": 370,
                           "data": "eyJjaGF0VHlwZSI6MSwiaW5jcmVtZW50VHlwZSI6MSwib3BlcmF0aW9uIjp7ImNvbnRlbnQiOnsiY29udGVudFR5cGUiOjgsInNlc3Npb25Bcm91c2UiOnsibWVtYmVyRmxhZ3MiOjAsIm5lZWRLZXlCb2FyZCI6dHJ1ZSwic2Vzc2lvbkFyb3VzZUluZm8iOnsicmVzaWRlbnRGdW5jdGlvbnMiOltdLCJhcm91c2VUaW1lU3RhbXAiOjE3NTA5ODgxMTc1NDgsImFyb3VzZUNoYXRTY3JpcHRJbmZvIjpbeyJjaGF0U2NyaXAiOiLmnInovazljaHotLnlkJfvvJ8iLCJjaGF0U2NyaXBTdHJhdGVneSI6InNoYWRpbmdfb3BlbmluZyIsImFyZ0luZm8iOnsiYXJncyI6eyJ0b3BpY190aXRsZSI6InNoYWRpbmdfb3BlbmluZyIsInNlc3Npb25faWQiOjUxMTI3NjcwODU2LCJjb250ZW50Ijoi5pyJ6L2s5Y2h6LS55ZCX77yfIn0sImFyZzEiOiJDaGF0VG9waWMifX0seyJjaGF0U2NyaXAiOiLmnInnpajmja7miJbogIXku5jmrL7orrDlvZXlkJfvvJ8iLCJjaGF0U2NyaXBTdHJhdGVneSI6InNoYWRpbmdfb3BlbmluZyIsImFyZ0luZm8iOnsiYXJncyI6eyJ0b3BpY190aXRsZSI6InNoYWRpbmdfb3BlbmluZyIsInNlc3Npb25faWQiOjUxMTI3NjcwODU2LCJjb250ZW50Ijoi5pyJ56Wo5o2u5oiW6ICF5LuY5qy+6K6w5b2V5ZCX77yfIn0sImFyZzEiOiJDaGF0VG9waWMifX0seyJjaGF0U2NyaXAiOiLpgJrnlKjlkJfvvJ8iLCJjaGF0U2NyaXBTdHJhdGVneSI6InNoYWRpbmdfb3BlbmluZyIsImFyZ0luZm8iOnsiYXJncyI6eyJ0b3BpY190aXRsZSI6InNoYWRpbmdfb3BlbmluZyIsInNlc3Npb25faWQiOjUxMTI3NjcwODU2LCJjb250ZW50Ijoi6YCa55So5ZCX77yfIn0sImFyZzEiOiJDaGF0VG9waWMifX0seyJjaGF0U2NyaXAiOiLog73muLjms7PlkJfvvJ8iLCJjaGF0U2NyaXBTdHJhdGVneSI6InNoYWRpbmdfb3BlbmluZyIsImFyZ0luZm8iOnsiYXJncyI6eyJ0b3BpY190aXRsZSI6InNoYWRpbmdfb3BlbmluZyIsInNlc3Npb25faWQiOjUxMTI3NjcwODU2LCJjb250ZW50Ijoi6IO95ri45rOz5ZCX77yfIn0sImFyZzEiOiJDaGF0VG9waWMifX0seyJjaGF0U2NyaXAiOiLlj6/ku6XlhY3otLnlgZzovablkJfvvJ8iLCJjaGF0U2NyaXBTdHJhdGVneSI6InNoYWRpbmdfb3BlbmluZyIsImFyZ0luZm8iOnsiYXJncyI6eyJ0b3BpY190aXRsZSI6InNoYWRpbmdfb3BlbmluZyIsInNlc3Npb25faWQiOjUxMTI3NjcwODU2LCJjb250ZW50Ijoi5Y+v5Lul5YWN6LS55YGc6L2m5ZCX77yfIn0sImFyZzEiOiJDaGF0VG9waWMifX0seyJjaGF0U2NyaXAiOiLlm6Lor77lj6/ku6XmiqXlkJfvvJ8iLCJjaGF0U2NyaXBTdHJhdGVneSI6InNoYWRpbmdfb3BlbmluZyIsImFyZ0luZm8iOnsiYXJncyI6eyJ0b3BpY190aXRsZSI6InNoYWRpbmdfb3BlbmluZyIsInNlc3Npb25faWQiOjUxMTI3NjcwODU2LCJjb250ZW50Ijoi5Zui6K++5Y+v5Lul5oql5ZCX77yfIn0sImFyZzEiOiJDaGF0VG9waWMifX1dLCJjaGF0R3VpZGFuY2UiOnsidHh0Ijoi5bCP6Zey6bG854yc5L2g5oOz6ZeuIiwiY2hhdEd1aWRhbmNlSWNvbiI6Imh0dHBzOi8vZy5hbGljZG4uY29tL2V2YS1hc3NldHMvOTAwYjgwODk2YjBhNGEzMjc0Yzg5OTBkYzc2YTM5MDYvMC4wLjEvdG1wL2M4MzljZmQvYzgzOWNmZC5qc29uIiwiaWNvbiI6Imh0dHBzOi8vZy5hbGljZG4uY29tL2V2YS1hc3NldHMvYWYyOTA2YjM0ODYwYzFmM2Y1YjFlNWU1YzY3MWE5NjYvMC4wLjEvdG1wL2EzZjgyMzIvYTNmODIzMi5qc29uP3NwbT1hMjE3MTQuaG9tZXBhZ2UuMC4wLjRlMGEzZmUwamxPZVl5JmZpbGU9YTNmODIzMi5qc29uIn19fX0sInJlY2VpdmVySWRzIjpbIjIxNTAzNjk4MTkiXSwic2Vzc2lvbkluZm8iOnsiY3JlYXRlVGltZSI6MTc1MDk4ODExNzAwMCwiZXh0ZW5zaW9ucyI6eyJleHRVc2VyVHlwZSI6IjAiLCJpdGVtVGl0bGUiOiLlh7rllK7nj4Dmm7zlgaXouqvmnIjljaHvvIgzMOWkqe+8ie+8jOWKnuWNoemAgeeahOmXsue9ru+8jOi9rOWUrue7meacieefreacn+WBpei6q+mcgCIsInNxdWFkSWRfMjE1MDM2OTgxOSI6IjIwMTcwNjUwMSIsImV4dFVzZXJJZCI6IjIxNTAzNjk4MTkiLCJpdGVtTWFpblBpYyI6Imh0dHBzOi8vaW1nLmFsaWNkbi5jb20vYmFvL3VwbG9hZGVkL2k0L08xQ04wMXBRVzZJWjFycVBVdzlzVkpjXyEhNDYxMTY4NjAxODQyNzM4MzY4Mi0wLWZsZWFtYXJrZXQuanBnIiwib3duZXJVc2VyVHlwZSI6IjAiLCJzcXVhZE5hbWVfMjIwMTI5MTI1NTY4MiI6IuWHuuWUruePgOabvOWBpei6q+aciOWNoe+8iDMw5aSp77yJ77yM5Yqe5Y2h6YCB55qE6Zey572u77yM6L2s5ZSu57uZ5pyJ55+t5pyf5YGl6Lqr6ZyAIiwiaXRlbUlkIjoiOTE0MTYxNzk0NTY2IiwiaXRlbUZlYXR1cmVzIjoie1wiaWRsZV9jYXRfbGVhZlwiOlwiMjAxNzA2NTAxXCJ9IiwiaXRlbVNlbGxlcklkIjoiMjIwMTI5MTI1NTY4MiIsIm93bmVyVXNlcklkIjoiMjIwMTI5MTI1NTY4MiIsInNxdWFkTmFtZV8yMTUwMzY5ODE5Ijoi5YGl6Lqr5Y2hIiwic3F1YWRJZF8yMjAxMjkxMjU1NjgyIjoiOTE0MTYxNzk0NTY2In0sImdyb3VwT3duZXJJZCI6IjIyMDEyOTEyNTU2ODIiLCJzZXNzaW9uSWQiOiI1MTEyNzY3MDg1NiIsInNlc3Npb25UeXBlIjoxLCJ0eXBlIjoxfX0sInNlc3Npb25JZCI6IjUxMTI3NjcwODU2In0=",
                           "streamId": "370",
                           "objectType": 370000
                         },
                         {
                           "bizType": 370,
                           "data": "eyJjaGF0VHlwZSI6MSwiaW5jcmVtZW50VHlwZSI6MSwib3BlcmF0aW9uIjp7ImNvbnRlbnQiOnsiY29udGVudFR5cGUiOjgsInNlc3Npb25Bcm91c2UiOnsibWVtYmVyRmxhZ3MiOjAsIm5lZWRLZXlCb2FyZCI6ZmFsc2UsInNlc3Npb25Bcm91c2VJbmZvIjp7InJlc2lkZW50RnVuY3Rpb25zIjpbXSwiYXJvdXNlVGltZVN0YW1wIjoxNzUwOTg4MTE3NTQ4LCJhcm91c2VDaGF0U2NyaXB0SW5mbyI6W119fX0sInJlY2VpdmVySWRzIjpbIjIxNTAzNjk4MTkiXSwic2Vzc2lvbkluZm8iOnsiY3JlYXRlVGltZSI6MTc1MDk4ODExNzAwMCwiZXh0ZW5zaW9ucyI6eyJleHRVc2VyVHlwZSI6IjAiLCJpdGVtVGl0bGUiOiLlh7rllK7nj4Dmm7zlgaXouqvmnIjljaHvvIgzMOWkqe+8ie+8jOWKnuWNoemAgeeahOmXsue9ru+8jOi9rOWUrue7meacieefreacn+WBpei6q+mcgCIsInNxdWFkSWRfMjE1MDM2OTgxOSI6IjIwMTcwNjUwMSIsImV4dFVzZXJJZCI6IjIxNTAzNjk4MTkiLCJpdGVtTWFpblBpYyI6Imh0dHBzOi8vaW1nLmFsaWNkbi5jb20vYmFvL3VwbG9hZGVkL2k0L08xQ04wMXBRVzZJWjFycVBVdzlzVkpjXyEhNDYxMTY4NjAxODQyNzM4MzY4Mi0wLWZsZWFtYXJrZXQuanBnIiwib3duZXJVc2VyVHlwZSI6IjAiLCJzcXVhZE5hbWVfMjIwMTI5MTI1NTY4MiI6IuWHuuWUruePgOabvOWBpei6q+aciOWNoe+8iDMw5aSp77yJ77yM5Yqe5Y2h6YCB55qE6Zey572u77yM6L2s5ZSu57uZ5pyJ55+t5pyf5YGl6Lqr6ZyAIiwiaXRlbUlkIjoiOTE0MTYxNzk0NTY2IiwiaXRlbUZlYXR1cmVzIjoie1wiaWRsZV9jYXRfbGVhZlwiOlwiMjAxNzA2NTAxXCJ9IiwiaXRlbVNlbGxlcklkIjoiMjIwMTI5MTI1NTY4MiIsIm93bmVyVXNlcklkIjoiMjIwMTI5MTI1NTY4MiIsInNxdWFkTmFtZV8yMTUwMzY5ODE5Ijoi5YGl6Lqr5Y2hIiwic3F1YWRJZF8yMjAxMjkxMjU1NjgyIjoiOTE0MTYxNzk0NTY2In0sImdyb3VwT3duZXJJZCI6IjIyMDEyOTEyNTU2ODIiLCJzZXNzaW9uSWQiOiI1MTEyNzY3MDg1NiIsInNlc3Npb25UeXBlIjoxLCJ0eXBlIjoxfX0sInNlc3Npb25JZCI6IjUxMTI3NjcwODU2In0=",
                           "streamId": "370",
                           "objectType": 370000
                         },
                         {
                           "bizType": 370,
                           "data": "eyJjaGF0VHlwZSI6MSwiaW5jcmVtZW50VHlwZSI6MSwib3BlcmF0aW9uIjp7ImNvbnRlbnQiOnsiY29udGVudFR5cGUiOjgsInNlc3Npb25Bcm91c2UiOnsibWVtYmVyRmxhZ3MiOjAsIm5lZWRLZXlCb2FyZCI6dHJ1ZSwic2Vzc2lvbkFyb3VzZUluZm8iOnsicmVzaWRlbnRGdW5jdGlvbnMiOltdLCJhcm91c2VUaW1lU3RhbXAiOjE3NTExMjI1ODE3NTEsImFyb3VzZUNoYXRTY3JpcHRJbmZvIjpbeyJjaGF0U2NyaXAiOiLov5nkuKrkvJjmg6DluYXluqbmnInngrnlpKcg5L2g5YaN6ICD6JmR5LiL5Lu35qC85ZCnIiwiY2hhdFNjcmlwU3RyYXRlZ3kiOiJzaGFkaW5nX2NoYXQiLCJhcmdJbmZvIjp7ImFyZ3MiOnsidG9waWNfdGl0bGUiOiJzaGFkaW5nX2NoYXQiLCJzZXNzaW9uX2lkIjo1MTIwNDYxMzE4NCwiY29udGVudCI6Iui/meS4quS8mOaDoOW5heW6puacieeCueWkpyDkvaDlho3ogIPomZHkuIvku7fmoLzlkKcifSwiYXJnMSI6IkNoYXRUb3BpYyJ9fV0sImNoYXRHdWlkYW5jZSI6eyJ0eHQiOiLlsI/pl7LpsbznjJzkvaDmg7Por7QiLCJjaGF0R3VpZGFuY2VJY29uIjoiaHR0cHM6Ly9nLmFsaWNkbi5jb20vZXZhLWFzc2V0cy85MDBiODA4OTZiMGE0YTMyNzRjODk5MGRjNzZhMzkwNi8wLjAuMS90bXAvYzgzOWNmZC9jODM5Y2ZkLmpzb24iLCJpY29uIjoiaHR0cHM6Ly9nLmFsaWNkbi5jb20vZXZhLWFzc2V0cy9hZjI5MDZiMzQ4NjBjMWYzZjViMWU1ZTVjNjcxYTk2Ni8wLjAuMS90bXAvYTNmODIzMi9hM2Y4MjMyLmpzb24/c3BtPWEyMTcxNC5ob21lcGFnZS4wLjAuNGUwYTNmZTBqbE9lWXkmZmlsZT1hM2Y4MjMyLmpzb24ifX19fSwicmVjZWl2ZXJJZHMiOlsiMjE1MDM2OTgxOSJdLCJzZXNzaW9uSW5mbyI6eyJjcmVhdGVUaW1lIjoxNzUxMTIyNTI1MDAwLCJleHRlbnNpb25zIjp7InNxdWFkSWRfMjIxODczMjY4OTI4NSI6IjEyNjg2MDM3NSIsImV4dFVzZXJUeXBlIjoiMCIsIml0ZW1UaXRsZSI6IiPlrpjmlrnljaHlpZcgIzE15ZGo5bm0ICPmrabol6TmuLjmiI8gI+agl+WtkOeQgyAj57ud54mI5Y2h5aWXIiwic3F1YWRJZF8yMTUwMzY5ODE5IjoiNzg4MTc1NDc5NDcxIiwiZXh0VXNlcklkIjoiMjIxODczMjY4OTI4NSIsIml0ZW1NYWluUGljIjoiaHR0cHM6Ly9pbWcuYWxpY2RuLmNvbS9iYW8vdXBsb2FkZWQvaTIvTzFDTjAxYlQwZGkyMk1QOXYzTVdRbEFfISEwLWZsZWFtYXJrZXQuanBnIiwib3duZXJVc2VyVHlwZSI6IjAiLCJpdGVtSWQiOiI3ODgxNzU0Nzk0NzEiLCJzcXVhZE5hbWVfMjIxODczMjY4OTI4NSI6IuWNoeeJjCIsIml0ZW1GZWF0dXJlcyI6IntcImlkbGVfY2F0X2xlYWZcIjpcIjEyNjg2MDM3NVwifSIsIml0ZW1TZWxsZXJJZCI6IjIxNTAzNjk4MTkiLCJvd25lclVzZXJJZCI6IjIxNTAzNjk4MTkiLCJzcXVhZE5hbWVfMjE1MDM2OTgxOSI6IiPlrpjmlrnljaHlpZcgIzE15ZGo5bm0ICPmrabol6TmuLjmiI8gI+agl+WtkOeQgyAj57ud54mI5Y2h5aWXIn0sImdyb3VwT3duZXJJZCI6IjIxNTAzNjk4MTkiLCJzZXNzaW9uSWQiOiI1MTIwNDYxMzE4NCIsInNlc3Npb25UeXBlIjoxLCJ0eXBlIjoxfX0sInNlc3Npb25JZCI6IjUxMjA0NjEzMTg0In0=",
                           "streamId": "370",
                           "objectType": 370000
                         }
                       ],
                       "maxPts": 1750988117564003,
                       "hasMore": 0,
                       "timestamp": 1751430709768
                     },
                     "syncExtensionModel": {
                       "reconnectType": 1,
                       "failover": 0,
                       "fingerprint": -915947000
                     },
                     "syncExtraType": {
                       "type": 3
                     }
                   }
                 }
                """.trim());

    }

    @ParameterizedTest
    @MethodSource("messageProvider")
    @SneakyThrows
    void testMsg(String json) {
        ObjectMapper objectMapper = new ObjectMapper();
        ReceiveMsg message = objectMapper.readValue(json, ReceiveMsg.class);
        logger.info(json);
    }

    @Test
    void testIgnoreNullJsonField() throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);

        AckMsg ackMsg = new AckMsg("444e9908a51d1cb236a27862abc769c9", "2127f5e86864eb274923fbd26cad77317038ab5e34be",
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/133.0.0.0 Safari/537.36 DingTalk(2.1.5) OS(Windows/10) Browser(Chrome/133.0.0.0) DingWeb/2.1.5 IMPaaS DingWeb/2.1.5", null, "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/133.0.0.0 Safari/537.36 DingTalk(2.1.5) OS(Windows/10) Browser(Chrome/133.0.0.0) DingWeb/2.1.5 IMPaaS DingWeb/2.1.5 0");
        String json = objectMapper.writeValueAsString(ackMsg);
        logger.info(json);
    }
}

