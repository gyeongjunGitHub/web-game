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
    private final FriendRepository friendRepository;
    private final ChattingRepository chattingRepository;
    private final QuizRepository quizRepository;

    public void sendLogoutMember(WebSocketSession webSocketSession,
                                 ConcurrentHashMap<String, WebSocketSession> sessionMap,
                                 ConcurrentHashMap<String, String> socketSessionAndMemberID,
                                 String myId) {
        // socket sessionMap 순회
        for(String memberKey : sessionMap.keySet()){
            WebSocketSession wss = sessionMap.get(memberKey);
            // 소캣에 등록된 Member 아이디와 myId가 같지 않으면 (자기 자신 제외)
            String memberId = socketSessionAndMemberID.get(memberKey);
            if(!myId.equals(memberId)){
                for (String membersKey : sessionMap.keySet()) {
                    // 메시지를 받는 사람 ID와 보내려는 member ID가 동일하지 않으면 전송
                    if (!memberId.equals(socketSessionAndMemberID.get(membersKey))) {
                        StringBuilder memberInfo = new StringBuilder();
                        memberInfo.append("{\"logOutMember\" : \"");
                        memberInfo.append(socketSessionAndMemberID.get(membersKey));
                        memberInfo.append("\"}");
                        sendMessage(wss, memberInfo.toString());
                    }
                }
            }
        }
    }
    public void sendLoginMemberList(WebSocketSession webSocketSession,
                                    ConcurrentHashMap<String, WebSocketSession> sessionMap,
                                    ConcurrentHashMap<String, String> socketSessionAndMemberID,
                                    String myId) {

        // socket sessionMap 순회
        for(String loginMemberKey : sessionMap.keySet()){
            WebSocketSession wss = sessionMap.get(loginMemberKey);

            // 소캣에 등록된 Member 아이디와 myId가 같지 않으면 (자기 자신 제외)
            if(!myId.equals(socketSessionAndMemberID.get(loginMemberKey))){
                for (String membersKey : sessionMap.keySet()) {
                    // 메시지를 받는 사람 ID와 보내려는 member ID가 동일하지 않으면 전송
                    if (!socketSessionAndMemberID.get(loginMemberKey).equals(socketSessionAndMemberID.get(membersKey))) {
                        StringBuilder memberInfo = new StringBuilder();
                        memberInfo.append("{\"loginMember\" : \"");
                        memberInfo.append(socketSessionAndMemberID.get(membersKey));
                        memberInfo.append("\"}");
                        sendMessage(wss, memberInfo.toString());
                    }
                }
            }else{ // 자기 자신에게 전송
                for(String membersKey : sessionMap.keySet()){
                    // 자기 자신 ID 제외 전송
                    if (!myId.equals(socketSessionAndMemberID.get(membersKey))){
                        String memberInfo = "{\"loginMember\" : \"" + socketSessionAndMemberID.get(membersKey) + "\"}";
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
    public String dtoToJson(Object object){
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
    @Transactional
    public ChattingDTO chatContentSave(String myId, SocketRequest socketRequest) {
        ChattingDTO chattingDTO = new ChattingDTO();
        chattingDTO.setSender(myId);
        chattingDTO.setReceiver(socketRequest.getReceiver());
        chattingDTO.setContent(socketRequest.getData());
        ChattingEntity chattingEntity = new ChattingEntity(chattingDTO);
        ChattingEntity saveResult = chattingRepository.chatContentSave(chattingEntity);
        return new ChattingDTO(saveResult);
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

    public void nextTurn(SocketRequest socketRequest, WebSocketSession session, ConcurrentHashMap<WebSocketSession, GameRoom> gameRooms){
        int turn = Integer.parseInt(socketRequest.getData()) + 1;
        if(turn > 2){ turn = 1; } //사이클 종료
        QuizDTO quiz = getQuiz();
        socketRequest.setAnswer(quiz.getAnswer());
        socketRequest.setQuiz(quiz.getQuiz());
        socketRequest.setYourTurn(turn);
        sendMessageSameRoom(0, session, gameRooms, socketRequest);
    }
    public void sendFriendList(String myId, SocketRequest socketRequest, ConcurrentHashMap<String, WebSocketSession> sessionMap,
                               ConcurrentHashMap<String, String> socketSessionAndMemberID){
        //친구 목록 전송
        List<FriendDTO> friendDTOList = getFriendList(myId);
        List<FriendDTO> receiverFriendDTOList = getFriendList(socketRequest.getReceiver());

        List<String> friendList = new ArrayList<>();
        List<String> receiverFriendList = new ArrayList<>();

        //친구 리스트 순회
        for (FriendDTO f : friendDTOList) {
            //친구 ID가 socketSessionAndMemberID안에 존재한다면
            if(socketSessionAndMemberID.containsValue(f.getFriend_id())){
                f.setStatus("online");
            }
            friendList.add(dtoToJson(f));
        }
        for (FriendDTO f : receiverFriendDTOList) {
            //친구 ID가 socketSessionAndMemberID안에 존재한다면
            if(socketSessionAndMemberID.containsValue(f.getFriend_id())){
                f.setStatus("online");
            }
            receiverFriendList.add(dtoToJson(f));
        }

        if(!friendList.isEmpty()){
            sendMessage(findReceiverSession(myId, sessionMap, socketSessionAndMemberID), friendList.toString());
        }
        if(!friendList.isEmpty()){
            sendMessage(findReceiverSession(socketRequest.getReceiver(), sessionMap, socketSessionAndMemberID), receiverFriendList.toString());
        }
    }
}
