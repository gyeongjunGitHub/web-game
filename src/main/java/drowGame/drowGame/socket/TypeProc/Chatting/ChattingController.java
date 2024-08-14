package drowGame.drowGame.socket.TypeProc.Chatting;

import drowGame.drowGame.socket.data.SocketRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

@Component
@RequiredArgsConstructor
public class ChattingController {
    private final ChattingProc chattingProc;
    public void controller(WebSocketSession session, String request, SocketRequest socketRequest) {
        switch (request){
            case "sendChatting":
                chattingProc.sendChatting(session, socketRequest);
                break;
        }
    }
}
