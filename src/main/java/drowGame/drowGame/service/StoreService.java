package drowGame.drowGame.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import drowGame.drowGame.dto.ItemDTO;
import drowGame.drowGame.dto.MyItemsDTO;
import drowGame.drowGame.entity.ItemEntity;
import drowGame.drowGame.entity.MyItemsEntity;
import drowGame.drowGame.repository.StoreRepository;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StoreService {
    private final StoreRepository storeRepository;
    private final MemberSessionService memberSessionService;
    @Transactional
    public void itemRegistration(String data) {
        ObjectMapper objectMapper = new ObjectMapper();
        ItemDTO itemDTO = new ItemDTO();
        try {
            itemDTO = objectMapper.readValue(data, ItemDTO.class);
        } catch (JsonMappingException e) {
            throw new RuntimeException(e);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        ItemEntity itemEntity = new ItemEntity(itemDTO);
        storeRepository.itemRegistration(itemEntity);
    }

    public List<ItemDTO> getItems() {
        List<ItemEntity> items = storeRepository.getItems();
        List<ItemDTO> result = new ArrayList<>();
        for(ItemEntity i : items){
            ItemDTO itemDTO = new ItemDTO(i);
            result.add(itemDTO);
        }
        return result;
    }

    public List<MyItemsDTO> getMyItems(HttpSession httpSession) {
        String myId = memberSessionService.getMemberId(httpSession.getId());
        List<MyItemsEntity> myItemsEntityList = storeRepository.getMyItems(myId);
        List<MyItemsDTO> myItems = new ArrayList<>();
        for(MyItemsEntity m : myItemsEntityList){
            MyItemsDTO myItemsDTO = new MyItemsDTO(m);
            myItems.add(myItemsDTO);
        }
        return myItems;
    }

    @Transactional
    public void buy(String item, HttpSession httpSession) {
        String myId = memberSessionService.getMemberId(httpSession.getId());
        ObjectMapper objectMapper = new ObjectMapper();
        MyItemsDTO myItemsDTO = new MyItemsDTO();
        try {
            myItemsDTO = objectMapper.readValue(item, MyItemsDTO.class);
        } catch (JsonMappingException e) {
            throw new RuntimeException(e);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        
        //게임 포인트로 구매하기
        
        //아이템 존재 체크
        List<MyItemsEntity> myItems = storeRepository.getMyItems(myId);
        boolean isAlreadyGet = false;
        String name = null;
        for(MyItemsEntity m : myItems){
            if (m.getName().equals(myItemsDTO.getName())) {
                isAlreadyGet = true;
            }
        }
        
        //가지고 있는 아이템이면 count+1
        if(isAlreadyGet){
            for(MyItemsEntity m : myItems){
                if (m.getName().equals(myItemsDTO.getName())) {
                    m.setCount(m.getCount()+1);
                }
            }
        }else{
            myItemsDTO.setCount(1);
            myItemsDTO.setMember_id(myId);
            MyItemsEntity myItemsEntity = new MyItemsEntity(myItemsDTO);
            storeRepository.buy(myItemsEntity);
        }

    }
}
