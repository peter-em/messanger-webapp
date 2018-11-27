package piotr.messanger.webapp.controller;

import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.messaging.simp.annotation.SubscribeMapping;
import org.springframework.stereotype.Controller;
import org.springframework.web.util.HtmlUtils;
import piotr.messanger.webapp.model.ClientRequest;
import piotr.messanger.webapp.model.MessageDto;
import piotr.messanger.webapp.model.ServerMessage;
import piotr.messanger.webapp.service.ConnectionService;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Collection;
import java.util.Collections;


@Controller
public class ClientMessageController {


    @Resource
    private ConnectionService connectionService;

    @Resource
    private SimpMessageSendingOperations messagingTemplate;

    /* this annotation handles user request for archived messages */
//    @MessageMapping("/archive/{username}")
//    @SendTo("/conv/{username}")
//    public ServerMessage greeting(@DestinationVariable String username,
//                                  Message<ClientRequest> msg) throws Exception {
//    }

    @SubscribeMapping("/activeclients")
    public Collection<String> retrieveActiveClients() {

        return connectionService.getUsernames();
    }

    @MessageMapping("/priv/{receiver}")
    @SendTo("/conv/{receiver}")
    public ServerMessage privateConversation(@DestinationVariable String receiver,
                                             @Payload ClientRequest request) {
        LocalDateTime time = LocalDateTime.now();

        return new ServerMessage(
                "NEWMSG",
                HtmlUtils.htmlEscape(request.getUser()),
                Collections.singletonList(new MessageDto(
                        HtmlUtils.htmlEscape(request.getUser()),
                        time.toInstant(ZoneOffset.UTC).toEpochMilli(),
                        HtmlUtils.htmlEscape(request.getContent()))
                )
        );
    }
}
