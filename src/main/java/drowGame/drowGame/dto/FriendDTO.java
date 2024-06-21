package drowGame.drowGame.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FriendDTO {
    private String request = "friendList";
    private String member_id;
    private String friend_id;
    private String status = "offline";
}
