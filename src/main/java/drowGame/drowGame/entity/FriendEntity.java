package drowGame.drowGame.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "friend", uniqueConstraints = {
        @UniqueConstraint(columnNames = {
                "member_id", "friend_id"
        })
})
public class FriendEntity {
    @EmbeddedId
    private FriendId id;
}
