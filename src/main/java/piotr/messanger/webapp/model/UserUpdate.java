package piotr.messanger.webapp.model;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class UserUpdate {

    private UpdateType type;
    private List<String> users;

    public enum UpdateType {
        ADD,
        REMOVE,
        LIST
    }

    public UserUpdate() {}

    public UserUpdate(UpdateType type, List<String> users) {
        this.type = type;
        this.users = users;
    }
}
