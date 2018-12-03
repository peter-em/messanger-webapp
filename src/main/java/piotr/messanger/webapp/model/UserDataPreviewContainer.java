package piotr.messanger.webapp.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
//@NoArgsConstructor
public class UserDataPreviewContainer {

    private List<String> activeUsers;
    private List<ConversationUpdate> conversations;

}
