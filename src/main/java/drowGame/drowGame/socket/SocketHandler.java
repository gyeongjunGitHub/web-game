package drowGame.drowGame.socket;

import drowGame.drowGame.service.MemberSessionService;
import drowGame.drowGame.socket.data.Request1;
import drowGame.drowGame.socket.data.RequestType;
import drowGame.drowGame.socket.data.SocketRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.Timer;
import java.util.TimerTask;

@Component
@RequiredArgsConstructor
public class SocketHandler extends TextWebSocketHandler {
    private final SocketService socketService;
    private final MemberSessionService memberSessionService;
    //Request
    private final RequestType requestType = new RequestType();
    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message){
        String msg = message.getPayload();
        String myId = memberSessionService.getMemberId((String) session.getAttributes().get("httpSessionId"));
        SocketRequest socketRequest = socketService.socketRequestMapping(msg);
        String requestName = socketRequest.getType();

        if(requestType.getRequest1().contains(requestName)){
            socketService.setRequest1(socketRequest, myId, session);
        }
        if(requestType.getRequest2().contains(requestName)){
            socketService.sendMessageSameRoom(1,session, socketRequest);
        }
        if(requestName.equals("answer")){
            Request1 result = socketRequest.typeRequest1(socketRequest);
            result.setSender(myId);
            socketRequest.setData(result);
            socketService.sendMessageSameRoom(0, session, socketRequest);
            socketService.answerCheck(session, result);
        }
        if(requestName.equals("startRound")) {
            socketService.startRound(session);
        }
        if (requestName.equals("start")) {
            socketService.gameStart(session);
        }
        if(requestName.equals("addFriendRequest")){
            socketService.addFriendRequest(session, socketRequest);
        }
        if (requestName.equals("addFriendResponse")){
            socketService.addFriend(socketRequest, session);
        }

        if (requestName.equals("matchingStartDrowGame")) {
            socketService.startMatching(session, socketRequest, 2);
        }
        if(requestName.equals("matchingCancleDrowGame")){
            socketService.removeMatchingQueue(session, 2);
        }

        if(requestName.equals("matchingStartDrowGame3")){
            socketService.startMatching(session, socketRequest, 3);
        }
        if(requestName.equals("matchingCancleDrowGame3")){
            socketService.removeMatchingQueue(session, 3);
        }


        if (requestName.equals("sendMessage")) {
            socketService.sendChatting(session, socketRequest);
        }
        if(requestName.equals("score")){
            System.out.println(msg);
        }
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        super.afterConnectionEstablished(session);
        //중복 로그인 체크
        WebSocketSession isDuplicateLogin = socketService.duplicateLoginCheck(session);
        if (isDuplicateLogin != null){
            CloseStatus status = new CloseStatus(1001);
            afterConnectionClosed(isDuplicateLogin, status);
        }
        socketService.addSessionInfo(session);
        socketService.sendLoginMemberList(session);
        socketService.sendFriendInfo(session);
    }


    //소켓 종료
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        socketService.sendLogoutMember(session);//로그아웃 시 싹다 빨간불 오류
        socketService.removeMatchingQueue(session, 2);
        socketService.removeMatchingQueue(session, 3);
        //socketService.removeGameRoom(session);
        socketService.removeSessionInfo(session);
        super.afterConnectionClosed(session, status);
    }
}