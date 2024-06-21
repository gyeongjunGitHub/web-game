package drowGame.drowGame.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@Embeddable
public class FriendId implements Serializable {
    @Column(name = "member_id")
    private String member_id;

    @Column(name = "friend_id")
    private String friend_id;
}
