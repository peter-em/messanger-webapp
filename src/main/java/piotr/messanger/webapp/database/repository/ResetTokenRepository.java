package piotr.messanger.webapp.database.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import piotr.messanger.webapp.database.entity.PsswrdToken;

public interface ResetTokenRepository extends JpaRepository<PsswrdToken, Integer> {

    PsswrdToken findByToken(String token);

}
