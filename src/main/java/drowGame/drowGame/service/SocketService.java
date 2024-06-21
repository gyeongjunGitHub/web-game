package drowGame.drowGame.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import drowGame.drowGame.Handler.GameRoomHandler;
import drowGame.drowGame.dto.ChattingDTO;
import drowGame.drowGame.dto.FriendDTO;
import drowGame.drowGame.dto.RequestDTO;
import drowGame.drowGame.entity.ChattingEntity;
import drowGame.drowGame.entity.FriendEntity;
import drowGame.drowGame.entity.FriendId;
import drowGame.drowGame.repository.ChattingRepository;
import drowGame.drowGame.repository.FriendRepository;
import drowGame.drowGame.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.Request;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

@Service
@RequiredArgsConstructor
public class SocketService {
    
    //나중에 정리 필요
    private final FriendRepository friendRepository;
    private final ChattingRepository chattingRepository;
    private final MemberRepository memberRepository;


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

    ////////////////////////
    //안쓰는 함수 나중에 제거//
    ////////////////////////
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

    public String dtoToJson(ChattingDTO chattingDTO){
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.writeValueAsString(chattingDTO);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
    public String dtoToJson(RequestDTO requestDTO){
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.writeValueAsString(requestDTO);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
    public String dtoToJson(FriendDTO friendDTO){
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.writeValueAsString(friendDTO);
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


    public void matching(ConcurrentLinkedQueue<String> drowGameMatchingInfo) {
        System.out.println("drowGame matching member size : " + drowGameMatchingInfo.size());
        if(drowGameMatchingInfo.size() == 2){
            System.out.println("매치 인원 충족!");
        }

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
}
