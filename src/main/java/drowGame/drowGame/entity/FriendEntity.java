package drowGame.drowGame.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "friend", uniqueConstraints = {
        @UniqueConstraint(columnNames = {
                "request_member_id", "requested_member_id"
        })
})
public class FriendEntity {
    @EmbeddedId
    private FriendId id;

    @Column(name = "acceptance_status")
    private boolean acceptance_status;
}
