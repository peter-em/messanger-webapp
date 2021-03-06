package piotr.messanger.webapp.database.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import piotr.messanger.webapp.database.entity.User;

import java.util.List;


@Repository("userRepository")
public interface UserRepository extends JpaRepository<User, Long> {

    User findByEmail(String email);

    User findByLogin(String login);

    @Query("SELECT u.login FROM User u")
    List<String> getLogins();

}