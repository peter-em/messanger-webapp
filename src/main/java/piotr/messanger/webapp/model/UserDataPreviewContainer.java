package piotr.messanger.webapp.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class UserDataPreviewContainer {

    private List<String> onlineUsers;
    private List<String> offlineUsers;
    private List<ConversationUpdate> conversations;

}
