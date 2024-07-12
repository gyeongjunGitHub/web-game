package drowGame.drowGame.socket.data;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AddFriend {
    private String receiver;
    private String sender;
}
