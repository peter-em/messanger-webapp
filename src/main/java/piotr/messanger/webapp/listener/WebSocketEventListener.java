package piotr.messanger.webapp.listener;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import piotr.messanger.webapp.database.entity.User;
import piotr.messanger.webapp.database.service.UserService;
import piotr.messanger.webapp.service.ConnectionService;

import javax.annotation.Resource;
import java.util.List;

@Component
@Slf4j
public class WebSocketEventListener {

    @Resource
    private SimpMessageSendingOperations messagingTemplate;
//
    @Resource
    private ConnectionService connectionService;

    @Resource
    private UserService userService;


    @EventListener
    public void handleWebSocketConnectedEvent(SessionConnectedEvent event) {

        SimpMessageHeaderAccessor headers = SimpMessageHeaderAccessor.wrap(event.getMessage());
        User userEntity = userService.findUserByEmail(event.getUser().getName());

        if (userEntity != null) {

            List<String> sessionIds = connectionService.getSessionIds(userEntity.getLogin());
            if (sessionIds.isEmpty()) {
                messagingTemplate.convertAndSend("/server/user.login", userEntity.getLogin());
            }
            connectionService.addSession(headers.getSessionId(), userEntity.getLogin());
        }
    }

    @EventListener
    public void handleWebSocketDisconnect(SessionDisconnectEvent event) {


        User userEntity = userService.findUserByEmail(event.getUser().getName());

        if (userEntity == null) {
            return;
        }

        connectionService.removeSession(event.getSessionId());
        List<String> sessionIds = connectionService.getSessionIds(userEntity.getLogin());

        if (sessionIds.isEmpty()) {
            messagingTemplate.convertAndSend("/server/user.logout", userEntity.getLogin());
        }
    }
}
