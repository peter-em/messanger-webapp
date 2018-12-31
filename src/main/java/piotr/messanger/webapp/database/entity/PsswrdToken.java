package piotr.messanger.webapp.database.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "pswrd_token")
public class PsswrdToken {

    // token expiration in minutes
    private static final int EXPIRATION = 60 * 2;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "token_id")
    private Integer id;

    private String token;

//    @OneToOne(targetEntity = User.class, fetch = FetchType.LAZY)
//    @JoinColumn(nullable = false, name = "user_id")
//    private User user;
    private Long userId;

    private LocalDateTime expiry;

    public PsswrdToken(String token, /*User user*/ Long userId) {
        this.token = token;
//        this.user = user;
        this.userId = userId;
        this.expiry = LocalDateTime.now(ZoneOffset.UTC).plusMinutes(EXPIRATION);
    }
}
