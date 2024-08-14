package drowGame.drowGame.socket.TypeProc.Game;

import drowGame.drowGame.socket.data.SocketRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

@Component
@RequiredArgsConstructor
public class GameController {
    private final GameProc gameProc;

    public void controller(WebSocketSession session, String request, SocketRequest socketRequest) {
        switch (request)
        {
            case "push":
            case "clear":
            case "rollBack":
            case "sendCoordinate":
                gameProc.sendMessageSameRoom(1, session, socketRequest);
                break;
            case "answer":
                gameProc.answer(session, socketRequest);
                break;
            case "ttabong":
                gameProc.ttabong(session, socketRequest.getData().toString());
            case "start":
                gameProc.gameStart(session);
            case "startRound":
                gameProc.startRound(session);
            default:
                break;
        }
    }
}
