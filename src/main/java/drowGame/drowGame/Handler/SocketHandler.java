package drowGame.drowGame.Handler;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import drowGame.drowGame.dto.*;
import drowGame.drowGame.entity.ChattingEntity;
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

    //gameRoom 정보를 담을 Map
    private final ConcurrentHashMap<WebSocketSession, GameRoom> gameRooms = new ConcurrentHashMap<WebSocketSession, GameRoom>();

    //room id
    private final AtomicInteger roomIdGenerator = new AtomicInteger();

    public void createGameRoom(List<WebSocketSession> player_session) {
        // roomId 생성
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
        //메시지 발송
        String msg = message.getPayload();
        ObjectMapper objectMapper = new ObjectMapper();
        RequestDTO requestDTO = new RequestDTO();
        requestDTO = objectMapper.readValue(msg, RequestDTO.class);
        String myId = memberSessionService.getMemberId((String) session.getAttributes().get("httpSessionId"));

        if(requestDTO.getRequest().equals("answer") || requestDTO.getRequest().equals("gameOver") || requestDTO.getRequest().equals("timeCount")){
            requestDTO.setSender(myId);
            socketService.sendMessageSameRoomId(session, gameRooms, requestDTO);
        }
        if(requestDTO.getRequest().equals("rollBack") || requestDTO.getRequest().equals("clear")
                || requestDTO.getRequest().equals("all_clear") || requestDTO.getRequest().equals("push")
                || requestDTO.getRequest().equals("sendCoordinate")){
            socketService.sendMessageSameRoomIdNotMe(session, gameRooms, requestDTO);
        }
        if (requestDTO.getRequest().equals("nextTurn")) {
            int turn = Integer.parseInt(requestDTO.getData()) + 1;
            if(turn > 2){ turn = 1; } //사이클 종료
            QuizDTO quiz = socketService.getQuiz();
            requestDTO.setAnswer(quiz.getAnswer());
            requestDTO.setQuiz(quiz.getQuiz());
            requestDTO.setYourTurn(turn);

            socketService.sendMessageSameRoomId(session, gameRooms, requestDTO);
        }
        if(requestDTO.getRequest().equals("gameStart") && gameRooms.get(session).getTurn() == 1){
            QuizDTO quiz = socketService.getQuiz();
            requestDTO.setAnswer(quiz.getAnswer());
            requestDTO.setQuiz(quiz.getQuiz());
            requestDTO.setYourTurn(1);

            socketService.sendMessageSameRoomId(session, gameRooms, requestDTO);
        }
        if(requestDTO.getRequest().equals("addFriendRequest")){
            requestDTO.setSender(myId);
            WebSocketSession receiverSession = socketService.findReceiverSession(requestDTO.getReceiver(), sessionMap, socketSessionAndMemberID);
            socketService.sendMessage(receiverSession, socketService.dtoToJson(requestDTO));
        }
        if (requestDTO.getRequest().equals("addFriendResponse")){
            socketService.addFriend(requestDTO, myId);
            if (Boolean.parseBoolean(requestDTO.getData())){
                //친구 목록 전송
                List<FriendDTO> friendDTOList = socketService.getFriendList(myId);
                List<FriendDTO> receiverFriendDTOList = socketService.getFriendList(requestDTO.getReceiver());

                List<String> friendList = new ArrayList<>();
                List<String> receiverFriendList = new ArrayList<>();

                //친구 리스트 순회
                for (FriendDTO f : friendDTOList) {
                    //친구 ID가 socketSessionAndMemberID안에 존재한다면
                    if(socketSessionAndMemberID.containsValue(f.getFriend_id())){
                        f.setStatus("online");
                    }
                    friendList.add(socketService.dtoToJson(f));
                }
                for (FriendDTO f : receiverFriendDTOList) {
                    //친구 ID가 socketSessionAndMemberID안에 존재한다면
                    if(socketSessionAndMemberID.containsValue(f.getFriend_id())){
                        f.setStatus("online");
                    }
                    receiverFriendList.add(socketService.dtoToJson(f));
                }

                if(!friendList.isEmpty()){
                    socketService.sendMessage(socketService.findReceiverSession(myId, sessionMap, socketSessionAndMemberID), friendList.toString());
                }
                if(!friendList.isEmpty()){
                    socketService.sendMessage(socketService.findReceiverSession(requestDTO.getReceiver(), sessionMap, socketSessionAndMemberID), receiverFriendList.toString());
                }
            }
        }
        if(requestDTO.getRequest().equals("matchingStartDrowGame")){
            //매칭 대기열에 추가
            //소캣 연결 종료시 제거 필요
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
                requestDTO.setRoomUsers(memebers);

                //roomId와 session 저장
                createGameRoom(player_session);
                requestDTO.setResponse("success");

                for(WebSocketSession wss : player_session){
                    requestDTO.setYourTurn(gameRooms.get(wss).getTurn());
                    socketService.sendMessage(wss, socketService.dtoToJson(requestDTO));
                }

                System.out.println("========= gameRooms Info =========");
                for(WebSocketSession wss : gameRooms.keySet()){
                    System.out.println(socketSessionAndMemberID.get(wss.getId()) + " : " + gameRooms.get(wss).getRoomId());
                }
            }
        }
        if(requestDTO.getRequest().equals("matchingCancleDrowGame")){
            drowGameMatchingInfo.remove(myId);
        }
        
        if (requestDTO.getRequest().equals("sendMessage")) {
            ChattingDTO chattingDTO = new ChattingDTO();
            chattingDTO.setSender(myId);
            chattingDTO.setReceiver(requestDTO.getReceiver());
            chattingDTO.setContent(requestDTO.getData());
            ChattingDTO result = socketService.chatContentSave(chattingDTO);
            result.setRequest(requestDTO.getRequest());

            WebSocketSession receiverSession = socketService.findReceiverSession(result.getReceiver(), sessionMap, socketSessionAndMemberID);
            WebSocketSession senderSession = socketService.findReceiverSession(myId, sessionMap, socketSessionAndMemberID);
            socketService.sendMessage(receiverSession, socketService.dtoToJson(result));
            socketService.sendMessage(senderSession, socketService.dtoToJson(result));
        }
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        super.afterConnectionEstablished(session);
        String myId = memberSessionService.getMemberId((String) session.getAttributes().get("httpSessionId"));
        //소켓 연결 System.out.println("소캣 연결 member ID -> "+myId);

        for (String s : socketSessionAndMemberID.keySet()){
            //이미 소캣에 세션이 등록되어 있는 경우 등록된 세션 강제 로그아웃
            if(myId.equals(socketSessionAndMemberID.get(s))){
                RequestDTO requestDTO = new RequestDTO();
                requestDTO.setRequest("duplicateLogin");
                socketService.sendMessage(sessionMap.get(s), socketService.dtoToJson(requestDTO));
                CloseStatus status = new CloseStatus(1001);
                afterConnectionClosed(sessionMap.get(s), status);
            }
        }

        //소켓에 연결된 member 의 socket session ID 를 sessionMap 에 추가
        sessionMap.put(session.getId(), session);

        //소켓에 연결된 member 의 아이디를 httpSession 에서 가져와 SocketSessionAndMemberID 에 추가
        socketSessionAndMemberID.put(session.getId(), memberSessionService.getMemberId((String) session.getAttributes().get("httpSessionId")));

//        System.out.println("-----------------------------------------------------------------------");
//        System.out.println("http session ID -> "+session.getAttributes().get("httpSessionId"));
//        System.out.println("-----------------------------------------------------------------------");

        //소캣 연결 시 현재 소캣에 연결되어 있는 member의 목록을 전송
        socketService.sendLoginMemberList(session, sessionMap, socketSessionAndMemberID, myId);

        //소캣 연결 시 친구 목록 전송
        List<FriendDTO> friendDTOList = socketService.getFriendList(myId);
        List<String> friendList = new ArrayList<>();

        //친구 리스트 순회
        for (FriendDTO f : friendDTOList) {
            //친구 ID가 socketSessionAndMemberID안에 존재한다면
            if(socketSessionAndMemberID.containsValue(f.getFriend_id())){
                f.setStatus("online");
            }
            friendList.add(socketService.dtoToJson(f));
        }
        if(!friendList.isEmpty()){
            socketService.sendMessage(socketService.findReceiverSession(myId, sessionMap, socketSessionAndMemberID), friendList.toString());
        }

        //채팅 데이터 전송
        List<ChattingDTO> result = socketService.getChattingData(myId);
        List<String> chattingData = new ArrayList<>();
        for(ChattingDTO c : result){
            chattingData.add(socketService.dtoToJson(c));
        }
        if(!chattingData.isEmpty()){
            socketService.sendMessage(socketService.findReceiverSession(myId, sessionMap, socketSessionAndMemberID), chattingData.toString());
        }
    }


    //소켓 종료
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {

        //httpSession이 이미 제거되었기 때문에 memberSessionService 사용 불가
        String myId = socketSessionAndMemberID.get(session.getId());
        socketService.sendLogoutMember(session, sessionMap, socketSessionAndMemberID, myId);

        //매칭 대기열에서 Member 삭제
        drowGameMatchingInfo.removeIf(s -> s.equals(myId));
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
                        RequestDTO requestDTO = new RequestDTO();
                        requestDTO.setRequest("leaveOtherMember");
                        socketService.sendMessage(wss, socketService.dtoToJson(requestDTO));
                        gameRooms.remove(wss);
                    }
                }
            }
        }
        sessionMap.remove(session.getId());
        socketSessionAndMemberID.remove(session.getId());
        super.afterConnectionClosed(session, status);
    }
}