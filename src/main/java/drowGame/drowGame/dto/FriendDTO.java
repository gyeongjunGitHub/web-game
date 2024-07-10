package drowGame.drowGame.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FriendDTO {
    private String member_id;
    private String friend_id;
    private String friend_nick_name;
    private String status = "offline";
}
