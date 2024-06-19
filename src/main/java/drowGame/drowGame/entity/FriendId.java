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
    @Column(name = "request_member_id")
    private String request_member_id;

    @Column(name = "requested_member_id")
    private String requested_member_id;
}
