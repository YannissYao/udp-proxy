package com.demo.gateway.jms;

import com.demo.gateway.client.ClientAsync;
import com.demo.gateway.common.Constant;
import com.demo.gateway.common.CreateRequest;
import com.vip.vjtools.vjkit.mapper.JsonMapper;
import io.netty.handler.codec.http.FullHttpRequest;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import java.util.Map;

import static com.demo.gateway.common.Constant.*;

/**
 * mq request topic consumer
 *
 * @author Yannis
 */
@Component
public class RequestTopicConsumerListener {

    private final ClientAsync clientAsync;

    public RequestTopicConsumerListener(ClientAsync clientAsync) {
        this.clientAsync = clientAsync;
    }

    /**
     * topic模式的消费者
     */
    @JmsListener(destination = "${spring.activemq.request-topic-name}", containerFactory = "RequestTopicListener")
    public void readActiveQueue(String json) {
        Map<String, Object> map = JsonMapper.INSTANCE.fromJson(json, Map.class);
        String method = map.get(Constant.REQUEST_METHOD).toString();//get
        String uri = map.get(Constant.REQUEST_URI).toString();//
        String version = map.get(Constant.REQUEST_VERSION).toString();//http1.1
        String content = map.get(Constant.CONTENT).toString();//content body
        Map<String, String> headers = (Map<String, String>) map.get(Constant.HEADERS);//headers

        FullHttpRequest fullHttpRequest = CreateRequest.create(method, uri, version, content, headers);
        clientAsync.sendRequest(fullHttpRequest, Integer.parseInt(String.valueOf(map.get(CHANNEL_HASH_CODE))));
    }
}