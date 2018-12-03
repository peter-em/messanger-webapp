package piotr.messanger.webapp.database.document;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.persistence.Id;
import java.time.LocalDateTime;

@Document
@Data
@Builder
public class Message {

    @Id
    private String id;

    private String author;
//    private long time;
    private LocalDateTime time;
    private String content;
}
