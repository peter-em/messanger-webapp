package piotr.messanger.webapp.database.document;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.persistence.Id;

@Document
@Getter
public class Conversation {

    @Id
    private String id;

    private final String first;
    private final String second;
    @Setter
    private int unread;

    public Conversation(String first, String second) {
        this.first = first;
        this.second = second;
        unread = 0;
    }
}
