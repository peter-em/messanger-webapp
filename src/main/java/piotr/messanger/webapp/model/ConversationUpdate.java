package piotr.messanger.webapp.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ConversationUpdate {

    private String partner;
//    private int unreadCount;
    private MessageDto message;
}
