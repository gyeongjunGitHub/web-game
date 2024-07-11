package drowGame.drowGame.socket;

import lombok.Getter;
import lombok.Setter;
import org.springframework.web.socket.WebSocketSession;

@Getter
@Setter
public class GameRoom {
    private int roomId;
    private int turn;
}
