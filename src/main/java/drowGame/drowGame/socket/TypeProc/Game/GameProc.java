package drowGame.drowGame.socket.TypeProc.Game;

import drowGame.drowGame.socket.TypeProc.Message.MessageProc;
import drowGame.drowGame.socket.data.Answer;
import drowGame.drowGame.socket.data.SocketRequest;
import drowGame.drowGame.socket.manager.GameManager;
import drowGame.drowGame.socket.manager.SocketSessionManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

@Component
@RequiredArgsConstructor
public class GameProc {
    private final GameManager gm;
    private final MessageProc messageProc;
    private final SocketSessionManager sm;
    public void sendMessageSameRoom(int num, WebSocketSession session, SocketRequest socketRequest) {
        int roomId = gm.getGameRoomId(session);

        //자기 자신 포함
        if (num == 0) {
            for (WebSocketSession wss : gm.getPlayerSession(roomId)) {
                messageProc.sendMessage(wss, messageProc.dtoToJson(socketRequest));
            }
        }
        //자기 자신 제외
        if (num == 1) {
            for (WebSocketSession wss : gm.getPlayerSession(roomId)){
                if(!session.equals(wss)){
                    messageProc.sendMessage(wss, messageProc.dtoToJson(socketRequest));
                }
            }
        }
    }
    public void answer(WebSocketSession session, SocketRequest socketRequest){
        String myId = sm.getMyId(session);
        Answer answer = socketRequest.typeRequest1(socketRequest);
        answer.setSender(myId);
        socketRequest.setData(answer);
        sendMessageSameRoom(0, session, socketRequest);
        gm.answerCheck(session, answer.getAnswer(), answer.getTimeCount());
    }
    public void ttabong(WebSocketSession session, String nick_name) {
        gm.ttabong(session, nick_name);
    }
    public void gameStart(WebSocketSession session){
        gm.startGameRoundTimer(session);
    }
    public void startRound(WebSocketSession session) {
        gm.startGameRoundTimer(session);
    }

}
