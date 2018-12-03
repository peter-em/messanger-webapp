package piotr.messanger.webapp.database.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import piotr.messanger.webapp.database.document.Conversation;

import java.util.List;
import java.util.Optional;

@Repository
public interface ConversationRepository extends MongoRepository<Conversation, String> {

    Optional<Conversation> findByFirstAndSecond(String login1, String login2);

    List<Conversation> findByFirstOrSecond(String login1, String login2);

}
