package piotr.messanger.webapp.controller;

import org.springframework.messaging.handler.annotation.*;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.messaging.simp.annotation.SubscribeMapping;
import org.springframework.stereotype.Controller;
import piotr.messanger.webapp.database.MessagesDatabase;
import piotr.messanger.webapp.database.entity.User;
import piotr.messanger.webapp.database.service.ConvService;
import piotr.messanger.webapp.database.service.UserService;
import piotr.messanger.webapp.model.*;
import piotr.messanger.webapp.service.ConnectionService;

import javax.annotation.Resource;
import java.security.Principal;
import java.util.LinkedList;
import java.util.List;


@Controller
public class MessageController {

    @Resource
    private ConnectionService connectionService;
    @Resource
    private SimpMessageSendingOperations messagingTemplate;
    @Resource
    private MessagesDatabase messagesDatabase;
    @Resource
    private UserService userService;
    @Resource
    private ConvService convService;

    /* this annotation handles user request for archived messages */
    @MessageMapping("/priv/archive/{username}")
    @SendTo("/conv/archive/{username}")
    public ArchivedMessages greeting(@DestinationVariable String username,
                                     @Payload ClientRequest request) throws Exception {

        return new ArchivedMessages(
                request.getUser(),
                messagesDatabase.getArchivedMessages(username, request.getUser(),
                        Long.parseLong(request.getContent()))
                );
    }

    @SubscribeMapping("/activeclients")
    public UserDataPreviewContainer retrieveActiveClients(Principal principal) {

        User user = userService.findUserByEmail(principal.getName());
        List<ConversationUpdate> previewList = new LinkedList<>();
        convService.getUserConversations(user.getLogin()).forEach(conv -> {
            ConversationUpdate update = new ConversationUpdate(
                    conv.getFirst().equals(user.getLogin())?conv.getSecond():conv.getFirst(),
                    messagesDatabase.getLastMessageOfConv(conv.getId())
            );
            previewList.add(update);
        });
        return new UserDataPreviewContainer(connectionService.getUsernames(), previewList);
    }

    @MessageMapping("/priv/{receiver}")
    @SendTo("/conv/priv/{receiver}")
    public ConversationUpdate privateConversation(@DestinationVariable String receiver,
                                                  @Payload ClientRequest request) {

        return new ConversationUpdate(
                request.getUser(),
                messagesDatabase.archiveMessage(request.getUser(), receiver, request.getContent())
        );
    }
}
