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
import drowGame.drowGame.repository.MemberRepository;
import drowGame.drowGame.repository.QuizRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
public class SocketService {
    
    //나중에 정리 필요
    private final FriendRepository friendRepository;
    private final ChattingRepository chattingRepository;
    private final MemberRepository memberRepository;
    private final QuizRepository quizRepository;

    public void sendLogoutMember(WebSocketSession webSocketSession,
                                 HashMap<String, WebSocketSession> sessionMap,
                                 ConcurrentHashMap<String, String> socketSessionAndMemberID,
                                 String myId) {

        // socket sessionMap 순회
        for(String memberKey : sessionMap.keySet()){
            WebSocketSession wss = sessionMap.get(memberKey);

            // 소캣에 등록된 Member 아이디와 myId가 같지 않으면 (자기 자신 제외)
            if(!myId.equals(socketSessionAndMemberID.get(memberKey))){
                for (String membersKey : sessionMap.keySet()) {
                    // 메시지를 받는 사람 ID와 보내려는 member ID가 동일하지 않으면 전송
                    if (!socketSessionAndMemberID.get(memberKey).equals(socketSessionAndMemberID.get(membersKey))) {
                        String memberInfo = "{\"logOutMember\" : \"" + socketSessionAndMemberID.get(membersKey) + "\"}";
                        sendMessage(wss, memberInfo);
                    }
                }
            }
        }
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

    public void sendLoginMemberList(WebSocketSession webSocketSession,
                                    HashMap<String, WebSocketSession> sessionMap,
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
                        String memberInfo = "{\"loginMember\" : \"" + socketSessionAndMemberID.get(membersKey) + "\"}";
                        sendMessage(wss, memberInfo);
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
                             HashMap<String, WebSocketSession> sessionMap,
                             ConcurrentHashMap<String, String> socketSessionAndMemberID){

        for(String key : sessionMap.keySet()){
            if(receiver.equals(socketSessionAndMemberID.get(key))){
                return sessionMap.get(key);
            }
        }
        return null;
    }

    public void sendMessage(WebSocketSession wss, String message){
        try{
            wss.sendMessage(new TextMessage(message));
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void sendMessageSameRoomId(WebSocketSession session, ConcurrentHashMap<WebSocketSession, GameRoom> gameRooms, RequestDTO requestDTO){
        //같은 roomId를 가진 유저에게 전송
        int myRoomId = gameRooms.get(session).getRoomId();
        for (WebSocketSession wss : gameRooms.keySet()){
            if(myRoomId == gameRooms.get(wss).getRoomId()){
                sendMessage(wss, dtoToJson(requestDTO));
            }
        }
    }
    public void sendMessageSameRoomIdNotMe(WebSocketSession session, ConcurrentHashMap<WebSocketSession, GameRoom> gameRooms, RequestDTO requestDTO){
        //자기 자신 제외 같은 roomId를 가진 유저에게 전송
        int myRoomId = gameRooms.get(session).getRoomId();
        for (WebSocketSession wss : gameRooms.keySet()){
            //roomId는 같고 자기 자신 제외
            if(gameRooms.get(wss).getRoomId() == myRoomId && !wss.equals(session)){
                sendMessage(wss, dtoToJson(requestDTO));
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
    public ChattingDTO chatContentSave(ChattingDTO chattingDTO) {
        ChattingEntity chattingEntity = new ChattingEntity(chattingDTO);
        ChattingEntity saveResult = chattingRepository.chatContentSave(chattingEntity);
        ChattingDTO result = new ChattingDTO(saveResult);
        return result;
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


    @Transactional
    public void addFriend(RequestDTO requestDTO, String myId) {
        if(Boolean.parseBoolean(requestDTO.getData())){
            FriendId friendId = new FriendId();
            FriendId friendId1 = new FriendId();
            FriendEntity friendEntity = new FriendEntity();
            FriendEntity friendEntity1 = new FriendEntity();

            friendId.setMember_id(myId);
            friendId.setFriend_id(requestDTO.getReceiver());
            friendEntity.setId(friendId);

            friendId1.setMember_id(requestDTO.getReceiver());
            friendId1.setFriend_id(myId);
            friendEntity1.setId(friendId1);

            friendRepository.addFriend(friendEntity, friendEntity1);
        }
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
}
