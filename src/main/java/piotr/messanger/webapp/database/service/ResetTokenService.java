package piotr.messanger.webapp.database.service;

import org.hibernate.Hibernate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import piotr.messanger.webapp.database.entity.PsswrdToken;
import piotr.messanger.webapp.database.entity.User;
import piotr.messanger.webapp.database.repository.ResetTokenRepository;
import piotr.messanger.webapp.database.repository.UserRepository;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Collections;

@Service
public class ResetTokenService {

    @Resource
    private ResetTokenRepository tokenRepository;

    @Resource
    private UserRepository userRepository;

    public void addPasswordResetTokenForUser(String token, long userId) {
        PsswrdToken userToken = new PsswrdToken(token, userId);
        tokenRepository.save(userToken);
    }

//    @Transactional(propagation = Propagation.REQUIRED, readOnly = true, noRollbackFor = Exception.class)
    public String validatePasswordResetToken(long id, String token) {

        PsswrdToken psswrdToken = tokenRepository.findByToken(token);
        if (psswrdToken == null || psswrdToken.getUserId() != id) {
            return "Change password error: invalid token";
        }

        if (LocalDateTime.now(ZoneOffset.UTC).isAfter(psswrdToken.getExpiry())) {
            return "Change password error: token has expired";
        }

//        Hibernate.initialize(psswrdToken.getUser());
//        User user = psswrdToken.getUser();
//        User user = userRepository.getOne(id);
        User user = userRepository.findById(id).orElse(null);
        if (user == null) {
            return "Changer password error: user not found";
        }
        Hibernate.initialize(user.getRoleEntities());
        Authentication auth = new UsernamePasswordAuthenticationToken(
                user, null, Collections.singleton(new SimpleGrantedAuthority("CHANGE_PASSWORD")));

        SecurityContextHolder.getContext().setAuthentication(auth);
        return null;
    }
}
