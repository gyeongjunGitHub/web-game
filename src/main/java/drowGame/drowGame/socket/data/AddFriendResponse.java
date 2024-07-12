package drowGame.drowGame.socket.data;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AddFriendResponse {
    private boolean response;
    private String receiver;
}
