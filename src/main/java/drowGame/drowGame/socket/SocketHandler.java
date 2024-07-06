package drowGame.drowGame.socket;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import drowGame.drowGame.service.MemberSessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

@Component
@RequiredArgsConstructor
public class SocketHandler extends TextWebSocketHandler {
    private final SocketService socketService;
    private final MemberSessionService memberSessionService;
    //Request
    private final Request request = new Request();
    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) throws JsonProcessingException {
        String msg = message.getPayload();
        String myId = memberSessionService.getMemberId((String) session.getAttributes().get("httpSessionId"));
//        try {
//            ObjectMapper objectMapper = new ObjectMapper();
//            JsonNode jsonNode = objectMapper.readTree(msg);
//            // JsonNode 출력
//            //System.out.println(jsonNode.toString());
//
//            // JsonNode 구조 탐색
//            String type = jsonNode.get("type").asText();
//            System.out.println("Type: " + type);
//            JsonNode data = jsonNode.get("data");
//            System.out.println(data);
////            System.out.println("Field2: " + dataNode.get("field2").asText());
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
        SocketRequest socketRequest = socketService.socketRequestMapping(msg);

        String requestName = socketRequest.getRequest();
        if(request.getRequest1().contains(requestName)){
            socketRequest.setSender(myId);
            socketService.sendMessageSameRoom(0, session, socketRequest);
        }
        if(request.getRequest2().contains(requestName)){
            socketService.sendMessageSameRoom(1,session, socketRequest);
        }
        if(requestName.equals("nextTurn")) {
            socketService.nextTurn(session, socketRequest);
        }
        if(requestName.equals("gameStart")){
            socketService.gameStart(session, socketRequest);
        }
        if(requestName.equals("addFriendRequest")){
            socketService.addFriendRequest(session, socketRequest);
        }
        if (requestName.equals("addFriendResponse")){
            socketService.addFriend(socketRequest, myId);
            if (Boolean.parseBoolean(socketRequest.getData())){
                socketService.sendFriendList(session, socketRequest);
            }
        }
        if (requestName.equals("matchingStartDrowGame")) {
            socketService.startMatching(session, socketRequest);
        }
        if(requestName.equals("matchingCancleDrowGame")){
            socketService.removeMatchingQueue(session);
        }
        if (requestName.equals("sendMessage")) {
            socketService.sendChatting(session, socketRequest);
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

        //삭제 예정
        socketService.sendChattingData(session);
    }


    //소켓 종료
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        socketService.sendLogoutMember(session);//로그아웃 시 싹다 빨간불 오류
        socketService.removeMatchingQueue(session);
        socketService.removeGameRoom(session);
        socketService.removeSessionInfo(session);
        super.afterConnectionClosed(session, status);
    }
}