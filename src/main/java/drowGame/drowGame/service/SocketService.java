package drowGame.drowGame.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import drowGame.drowGame.dto.*;
import drowGame.drowGame.entity.ChattingEntity;
import drowGame.drowGame.entity.FriendEntity;
import drowGame.drowGame.entity.FriendId;
import drowGame.drowGame.entity.QuizEntity;
import drowGame.drowGame.repository.ChattingRepository;
import drowGame.drowGame.repository.FriendRepository;
import drowGame.drowGame.repository.QuizRepository;
import drowGame.drowGame.socket.SocketSessionManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
public class SocketService {
    
    //나중에 정리 필요
    private final SocketSessionManager sm;
    private final MemberSessionService memberSessionService;

    private final FriendRepository friendRepository;
    private final ChattingRepository chattingRepository;
    private final QuizRepository quizRepository;


    public void addSessionInfo(WebSocketSession session) {
        sm.addSessionMap(session.getId(), session);
        sm.addMemberIdMap(session.getId(), memberSessionService.getMemberId((String) session.getAttributes().get("httpSessionId")));
    }
    public void removeSessionInfo(WebSocketSession session) {
        sm.removeSessionMap(session);
        sm.removeMemberIdMap(session);
    }
    public WebSocketSession duplicateLoginCheck(WebSocketSession session){
        String myId = (String) session.getAttributes().get("httpSessionId");
        for(String s : sm.getMemberIdMap().keySet()){
            if(myId.equals(sm.getMemberIdMap().get(s))){
                SocketRequest socketRequest = new SocketRequest();
                socketRequest.setRequest("duplicateLogin");
                sendMessage(sm.getSessionMap().get(s), dtoToJson(socketRequest));
                return sm.getSessionMap().get(s); //중복 로그인
            }
        }
        return null;
    }
    public void sendFriendInfo(WebSocketSession session){
        String myId = sm.getMyId(session);
        List<FriendDTO> friendDTOList = getFriendList(myId);
        List<String> friendList = new ArrayList<>();
        for (FriendDTO f : friendDTOList) {
            if(sm.getMemberIdMap().containsValue(f.getFriend_id())){
                f.setStatus("online");
            }
            friendList.add(dtoToJson(f));
        }
        if(!friendList.isEmpty()){
            sendMessage(findReceiverSession(myId), friendList.toString());
        }
    }
    public WebSocketSession findReceiverSession(String receiver){
        for(String key : sm.getSessionMap().keySet()){
            String memberId = sm.getMemberIdMap().get(key);
            if(receiver.equals(memberId)){
                return sm.getSessionMap().get(key);
            }
        }
        return null;
    }
    public void sendChattingData(WebSocketSession session){
        String myId = sm.getMyId(session);
        List<ChattingDTO> result = getChattingData(myId);
        List<String> chattingData = new ArrayList<>();
        for(ChattingDTO c : result){
            chattingData.add(dtoToJson(c));
        }
        if(!chattingData.isEmpty()){
            sendMessage(findReceiverSession(myId), chattingData.toString());
        }
    }
    public void sendLogoutMember(WebSocketSession session) {
        String myId = sm.getMyId(session);
        // socket sessionMap 순회
        for(String memberKey : sm.getSessionMap().keySet()){
            WebSocketSession wss = sm.getSessionMap().get(memberKey);
            // 소캣에 등록된 Member 아이디와 myId가 같지 않으면 (자기 자신 제외)
            String memberId = sm.getMemberId(memberKey);
            if(!myId.equals(memberId)){
                for (String membersKey : sm.getSessionMap().keySet()) {
                    // 메시지를 받는 사람 ID와 보내려는 member ID가 동일하지 않으면 전송
                    if (!memberId.equals(sm.getMemberId(membersKey))) {
                        StringBuilder memberInfo = new StringBuilder();
                        memberInfo.append("{\"logOutMember\" : \"");
                        memberInfo.append(sm.getMemberId(membersKey));
                        memberInfo.append("\"}");
                        sendMessage(wss, memberInfo.toString());
                    }
                }
            }
        }
    }
    public void sendLoginMemberList(WebSocketSession session) {
        String myId = sm.getMyId(session);

        // socket sessionMap 순회
        for(String loginMemberKey : sm.getSessionMap().keySet()){
            WebSocketSession wss = sm.getSessionMap().get(loginMemberKey);

            // 소캣에 등록된 Member 아이디와 myId가 같지 않으면 (자기 자신 제외)
            if(!myId.equals(sm.getMemberId(loginMemberKey))){
                for (String membersKey : sm.getSessionMap().keySet()) {
                    // 메시지를 받는 사람 ID와 보내려는 member ID가 동일하지 않으면 전송
                    if (!sm.getMemberId(loginMemberKey).equals(sm.getMemberId(membersKey))) {
                        StringBuilder memberInfo = new StringBuilder();
                        memberInfo.append("{\"loginMember\" : \"");
                        memberInfo.append(sm.getMemberId(membersKey));
                        memberInfo.append("\"}");
                        sendMessage(wss, memberInfo.toString());
                    }
                }
            }else{ // 자기 자신에게 전송
                for(String membersKey : sm.getSessionMap().keySet()){
                    // 자기 자신 ID 제외 전송
                    if (!myId.equals(sm.getMemberId(membersKey))){
                        String memberInfo = "{\"loginMember\" : \"" + sm.getMemberId(membersKey) + "\"}";
                        sendMessage(wss, memberInfo);
                    }else{
                        String myIdInfo = "{\"myId\" : \"" + myId + "\"}";
                        sendMessage(wss, myIdInfo);
                    }
                }
            }
        }
    }

    public WebSocketSession findReceiverSession(String receiver,
                                                ConcurrentHashMap<String, WebSocketSession> sessionMap,
                                                ConcurrentHashMap<String, String> socketSessionAndMemberID){
        for(String key : sessionMap.keySet()){
            String memberId = socketSessionAndMemberID.get(key);
            if(receiver.equals(memberId)){
                return sessionMap.get(key);
            }
        }
        return null;
    }
    public List<FriendDTO> getFriendList(String myId) {
        List<FriendEntity> friendList = friendRepository.findFriendList(myId);
        List<FriendDTO> friendDTOList = new ArrayList<>();
        for (FriendEntity f : friendList){
            FriendDTO friendDTO = new FriendDTO();
            friendDTO.setMember_id(f.getId().getMember_id());
            friendDTO.setFriend_id(f.getId().getFriend_id());
            friendDTOList.add(friendDTO);
        }
        return friendDTOList;
    }
    public void sendMessage(WebSocketSession wss, String message){
        try{
            wss.sendMessage(new TextMessage(message));
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    @Transactional
    public void sendChatting(WebSocketSession session, SocketRequest socketRequest){
        String myId = sm.getMyId(session);
        ChattingDTO chattingDTO = new ChattingDTO();
        chattingDTO.setSender(myId);
        chattingDTO.setReceiver(socketRequest.getReceiver());
        chattingDTO.setContent(socketRequest.getData());
        ChattingEntity chattingEntity = new ChattingEntity(chattingDTO);

        ChattingDTO saveResult = new ChattingDTO(chattingRepository.chatContentSave(chattingEntity));
        saveResult.setRequest("sendMessage");

        WebSocketSession[] sessions = new WebSocketSession[2];
        sessions[0] = findReceiverSession(saveResult.getReceiver());
        sessions[1] = findReceiverSession(myId);
        for(WebSocketSession wss : sessions){
            if(wss != null){
                sendMessage(wss, dtoToJson(saveResult));
            }
        }
    }
    @Transactional
    public void addFriend(SocketRequest socketRequest, String myId) {
        if(Boolean.parseBoolean(socketRequest.getData())){
            FriendId friendId = new FriendId();
            FriendId friendId1 = new FriendId();
            FriendEntity friendEntity = new FriendEntity();
            FriendEntity friendEntity1 = new FriendEntity();

            friendId.setMember_id(myId);
            friendId.setFriend_id(socketRequest.getReceiver());
            friendEntity.setId(friendId);

            friendId1.setMember_id(socketRequest.getReceiver());
            friendId1.setFriend_id(myId);
            friendEntity1.setId(friendId1);

            friendRepository.addFriend(friendEntity, friendEntity1);
        }
    }
    public String dtoToJson(Object object){
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
    public List<ChattingDTO> getChattingData(String myId) {
        List<ChattingEntity> chattingData = chattingRepository.getChattingData(myId);
        List<ChattingDTO> chattingDTOList = new ArrayList<>();
        for(ChattingEntity c : chattingData){
            ChattingDTO chattingDTO = new ChattingDTO(c);
            chattingDTOList.add(chattingDTO);
        }
        return chattingDTOList;
    }
    public QuizDTO getQuiz() {
        int min = 1;
        int max = 7955;
        int randomQuizNumber = ThreadLocalRandom.current().nextInt(min, max + 1);

        QuizEntity quiz = quizRepository.getQuiz(randomQuizNumber);

        QuizDTO quizDTO = new QuizDTO();
        quizDTO.setNum(quiz.getNum());
        quizDTO.setQuiz(quiz.getQuiz());
        quizDTO.setAnswer(quiz.getAnswer());
        return quizDTO;
    }
    public SocketRequest socketRequestMapping(String msg) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        SocketRequest socketRequest = new SocketRequest();
        return objectMapper.readValue(msg, SocketRequest.class);
    }
    public void addFriendRequest(WebSocketSession session, SocketRequest socketRequest){
        String myId = sm.getMyId(session);
        socketRequest.setSender(myId);
        WebSocketSession receiverSession = findReceiverSession(socketRequest.getReceiver());
        sendMessage(receiverSession, dtoToJson(socketRequest));
    }
    public void sendFriendList(WebSocketSession session, SocketRequest socketRequest){
        String myId = sm.getMyId(session);
        //친구 목록 전송
        List<FriendDTO> friendDTOList = getFriendList(myId);
        List<FriendDTO> receiverFriendDTOList = getFriendList(socketRequest.getReceiver());

        List<String> friendList = new ArrayList<>();
        List<String> receiverFriendList = new ArrayList<>();

        //친구 리스트 순회
        for (FriendDTO f : friendDTOList) {
            //친구 ID가 socketSessionAndMemberID안에 존재한다면
            if(sm.getMemberIdMap().containsValue(f.getFriend_id())){
                f.setStatus("online");
            }
            friendList.add(dtoToJson(f));
        }
        for (FriendDTO f : receiverFriendDTOList) {
            //친구 ID가 socketSessionAndMemberID안에 존재한다면
            if(sm.getMemberIdMap().containsValue(f.getFriend_id())){
                f.setStatus("online");
            }
            receiverFriendList.add(dtoToJson(f));
        }

        if(!friendList.isEmpty()){
            sendMessage(findReceiverSession(myId), friendList.toString());
        }
        if(!friendList.isEmpty()){
            sendMessage(findReceiverSession(socketRequest.getReceiver()), receiverFriendList.toString());
        }
    }

    //gameRoom 수정 필요
    public void nextTurn(SocketRequest socketRequest, WebSocketSession session, ConcurrentHashMap<WebSocketSession, GameRoom> gameRooms){
        int turn = Integer.parseInt(socketRequest.getData()) + 1;
        if(turn > 2){ turn = 1; } //사이클 종료
        QuizDTO quiz = getQuiz();
        socketRequest.setAnswer(quiz.getAnswer());
        socketRequest.setQuiz(quiz.getQuiz());
        socketRequest.setYourTurn(turn);
        sendMessageSameRoom(0, session, gameRooms, socketRequest);
    }
    public void gameStart(SocketRequest socketRequest, WebSocketSession session, ConcurrentHashMap<WebSocketSession, GameRoom> gameRooms){
        QuizDTO quiz = getQuiz();
        socketRequest.setAnswer(quiz.getAnswer());
        socketRequest.setQuiz(quiz.getQuiz());
        socketRequest.setYourTurn(1);
        sendMessageSameRoom(0, session, gameRooms, socketRequest);
    }
    public void sendMessageSameRoom(int num, WebSocketSession session, ConcurrentHashMap<WebSocketSession, GameRoom> gameRooms, SocketRequest socketRequest) {
        if(num == 0){
            //같은 roomId를 가진 유저에게 전송
            int myRoomId = gameRooms.get(session).getRoomId();
            for (WebSocketSession wss : gameRooms.keySet()) {
                if (myRoomId == gameRooms.get(wss).getRoomId()) {
                    sendMessage(wss, dtoToJson(socketRequest));
                }
            }
        }
        if(num == 1){
            //자기 자신 제외 같은 roomId를 가진 유저에게 전송
            int myRoomId = gameRooms.get(session).getRoomId();
            for (WebSocketSession wss : gameRooms.keySet()){
                //roomId는 같고 자기 자신 제외
                if(gameRooms.get(wss).getRoomId() == myRoomId && !wss.equals(session)){
                    sendMessage(wss, dtoToJson(socketRequest));
                }
            }
        }
    }
}
