package drowGame.drowGame.socket;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

import com.fasterxml.jackson.core.JsonProcessingException;
import drowGame.drowGame.dto.*;
import drowGame.drowGame.service.MemberSessionService;
import drowGame.drowGame.service.SocketService;
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

    //웹소켓 세션을 담아둘 맵
    ConcurrentHashMap<String, WebSocketSession> sessionMap = new ConcurrentHashMap<>();
    //웹소켓 세션Id와 MemberId를 담아둘 맵
    ConcurrentHashMap<String, String> socketSessionAndMemberID = new ConcurrentHashMap<>();
    //매칭 대기열 que
    ConcurrentLinkedQueue<String> drowGameMatchingInfo = new ConcurrentLinkedQueue<>();
    //Request
    Request request = new Request();
    //gameRoom 정보를 담을 Map
    private final ConcurrentHashMap<WebSocketSession, GameRoom> gameRooms = new ConcurrentHashMap<WebSocketSession, GameRoom>();
    //room id
    private final AtomicInteger roomIdGenerator = new AtomicInteger();
    public void createGameRoom(List<WebSocketSession> player_session) {
        int roomId = roomIdGenerator.incrementAndGet();
        int turn = 1;
        for (WebSocketSession ws : player_session){
            GameRoom gameRoom = new GameRoom();
            gameRoom.setRoomId(roomId);
            gameRoom.setTurn(turn++);
            gameRooms.put(ws, gameRoom);
        }
    }
    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) throws JsonProcessingException {
        String msg = message.getPayload();
        String myId = memberSessionService.getMemberId((String) session.getAttributes().get("httpSessionId"));
        SocketRequest socketRequest = socketService.socketRequestMapping(msg);

        String requestName = socketRequest.getRequest();
        if(request.getRequest1().contains(requestName)){
            socketRequest.setSender(myId);
            socketService.sendMessageSameRoom(0, session, gameRooms, socketRequest);
        }
        if(request.getRequest2().contains(requestName)){
            socketService.sendMessageSameRoom(1,session, gameRooms, socketRequest);
        }
        if(request.getRequest3().contains(requestName)){
            //필요 없으면 삭제
        }
        if(requestName.equals("nextTurn")) {
            socketService.nextTurn(socketRequest, session, gameRooms);
        }
        if(requestName.equals("gameStart") && gameRooms.get(session).getTurn() == 1){
            socketService.gameStart(socketRequest, session, gameRooms);
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
        if(requestName.equals("matchingStartDrowGame")){
            //매칭 대기열에 추가
            drowGameMatchingInfo.add(myId);

            //매치 인원 충족
            if(drowGameMatchingInfo.size()==2){
                List<WebSocketSession> player_session = new ArrayList<>();
                List<String> memebers = new ArrayList<>();
                for (int i = 0; i < 2; i++) {
                    //memberId
                    String player = drowGameMatchingInfo.poll();
                    memebers.add(player);

                    //memberSession
                    for (String sessionId : socketSessionAndMemberID.keySet()) {
                        if (socketSessionAndMemberID.get(sessionId).equals(player)) {
                            player_session.add(sessionMap.get(sessionId));
                        }
                    }
                }
                socketRequest.setRoomUsers(memebers);

                //roomId와 session 저장
                createGameRoom(player_session);
                socketRequest.setResponse("success");

                for(WebSocketSession wss : player_session){
                    socketRequest.setYourTurn(gameRooms.get(wss).getTurn());
                    socketService.sendMessage(wss, socketService.dtoToJson(socketRequest));
                }
            }
        }
        if(requestName.equals("matchingCancleDrowGame")){
            drowGameMatchingInfo.remove(myId);
        }
        if (requestName.equals("sendMessage")) {
            socketService.sendChatting(session, socketRequest);
        }
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        super.afterConnectionEstablished(session);

        ////지울부분

        sessionMap.put(session.getId(), session);
        socketSessionAndMemberID.put(session.getId(), memberSessionService.getMemberId((String) session.getAttributes().get("httpSessionId")));

        //중복 로그인 체크
        WebSocketSession isDuplicateLogin = socketService.duplicateLoginCheck(session);
        if (isDuplicateLogin != null){
            CloseStatus status = new CloseStatus(1001);
            afterConnectionClosed(isDuplicateLogin, status);
        }
        //세션 정보 추가
        socketService.addSessionInfo(session);
        //로그인 유저 전송
        socketService.sendLoginMemberList(session);
        //친구 목록 전송
        socketService.sendFriendInfo(session);
        //채팅 데이터 전송
        socketService.sendChattingData(session);
    }


    //소켓 종료
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        String myId = socketSessionAndMemberID.get(session.getId());//httpSession이 이미 제거되었기 때문에 memberSessionService 사용 불가


        socketService.sendLogoutMember(session);//로그아웃 시 싹다 빨간불 오류
        drowGameMatchingInfo.removeIf(s -> s.equals(myId)); //매칭 대기열에서 Member 삭제
//        for(String s : drowGameMatchingInfo){
//            if(s.equals(myId)){
//                drowGameMatchingInfo.remove(s);
//            }
//        }

        //gameRooms 에서 삭제
        int roomId = 0;
        int roomMemberCount = 0;
        boolean isDuringGame = false;

        for(WebSocketSession wss : gameRooms.keySet()){
            if(myId.equals(socketSessionAndMemberID.get(wss.getId()))){
                roomId = gameRooms.get(wss).getRoomId();
                isDuringGame = true;
                gameRooms.remove(wss);
            }
        }
        if(isDuringGame){
            for(WebSocketSession wss : gameRooms.keySet()){
                if(gameRooms.get(wss).getRoomId() == roomId){
                    roomMemberCount++;
                }
            }
            //gameRoom의 인원이 1명이면 강제 게임종료
            if(roomMemberCount == 1){
                for(WebSocketSession wss : gameRooms.keySet()){
                    if (gameRooms.get(wss).getRoomId() == roomId){
                        SocketRequest socketRequest = new SocketRequest();
                        socketRequest.setRequest("leaveOtherMember");
                        socketService.sendMessage(wss, socketService.dtoToJson(socketRequest));
                        gameRooms.remove(wss);
                    }
                }
            }
        }
        sessionMap.remove(session.getId());
        socketSessionAndMemberID.remove(session.getId());

        //세션 정보 제거
        socketService.removeSessionInfo(session);
        
        
        
        super.afterConnectionClosed(session, status);
    }
}