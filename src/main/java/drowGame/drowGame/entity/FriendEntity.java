package drowGame.drowGame.entity;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
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
}
