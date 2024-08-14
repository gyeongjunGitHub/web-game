package drowGame.drowGame.socket.TypeProc.Matching;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

@Component
@RequiredArgsConstructor
public class MatchingController {
    private final MatchingProc matchingProc;
    public void controller(WebSocketSession session, String request, String data){
        switch (request)
        {
            case "start":
                matchingProc.startMatching(session, Integer.parseInt(data));
                break;

            case "cancel":
                matchingProc.removeMatchingQueue(session, Integer.parseInt(data));
                break;
        }

    }
}
