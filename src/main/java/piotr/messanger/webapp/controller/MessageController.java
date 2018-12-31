package piotr.messanger.webapp.controller;

import org.springframework.messaging.handler.annotation.*;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.messaging.simp.annotation.SubscribeMapping;
import org.springframework.stereotype.Controller;
import piotr.messanger.webapp.chatbot.BotWorker;
import piotr.messanger.webapp.database.MessagesDatabase;
import piotr.messanger.webapp.database.entity.User;
import piotr.messanger.webapp.database.service.ConvService;
import piotr.messanger.webapp.database.service.UserService;
import piotr.messanger.webapp.model.*;
import piotr.messanger.webapp.service.ConnectionService;

import javax.annotation.Resource;
import java.security.Principal;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;


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
    private BotWorker botWorker;

    public MessageController(BotWorker botWorker) {
        this.botWorker = botWorker;
        Thread thread = new Thread(botWorker);
        thread.start();
    }

    /* this annotation handles user request for archived messages */
    @MessageMapping("/priv/archive/{username}")
    @SendTo("/conv/archive/{username}")
    public ArchivedMessages handleArchiveRequest(@DestinationVariable String username,
                                     @Payload ClientRequest request) throws Exception {

        return new ArchivedMessages(
                request.getUser(),
                messagesDatabase.getArchivedMessages(username, request.getUser(),
                        Long.parseLong(request.getContent()))
                );
    }

    @MessageMapping("/priv/NightBot")
    public void handleBotConversation(@Payload ClientRequest request) {
        botWorker.addRequest(request);
    }

    @SubscribeMapping("/activeclients")
    public UserDataPreviewContainer getInitialData(Principal principal) {

        User user = userService.findUserByEmail(principal.getName());
        List<ConversationUpdate> previewList = new LinkedList<>();

        convService.getUserConversations(user.getLogin()).forEach(conv -> {
            ConversationUpdate update = new ConversationUpdate(
                    conv.getFirst().equals(user.getLogin())?conv.getSecond():conv.getFirst(),
                    conv.getUnread(),
                    messagesDatabase.getLastMessageOfConv(conv.getId())
            );
            previewList.add(update);
        });

        List<String> online = connectionService.getUsernames();
        return new UserDataPreviewContainer(online,
                userService.getLogins().stream()
                    .filter(login -> !online.contains(login))
                    .collect(Collectors.toList()),
                previewList);
    }


    @MessageMapping("/priv/{receiver}")
    public void handlePrivateMessage(@DestinationVariable String receiver,
                            @Payload ClientRequest request) {
        boolean isOnline = connectionService.hasLogin(receiver);
        MessageDto msg = messagesDatabase.archiveMessage(
                request.getUser(), receiver, request.getContent(), !isOnline);

        if (isOnline && !receiver.equals(request.getUser())) {
            messagingTemplate.convertAndSend("/conv/priv/" + receiver, msg);
        }

    }
}
