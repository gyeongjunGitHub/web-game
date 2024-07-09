package drowGame.drowGame.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import drowGame.drowGame.dto.ItemDTO;
import drowGame.drowGame.dto.MyItemsDTO;
import drowGame.drowGame.entity.ItemEntity;
import drowGame.drowGame.entity.MyItemsEntity;
import drowGame.drowGame.repository.StoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StoreService {
    private final StoreRepository storeRepository;
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

    public List<MyItemsDTO> getMyItems(String memberId) {
        List<MyItemsEntity> myItemsEntityList = storeRepository.getMyItems(memberId);
        List<MyItemsDTO> myItems = new ArrayList<>();
        for(MyItemsEntity m : myItemsEntityList){
            MyItemsDTO myItemsDTO = new MyItemsDTO(m);
            myItems.add(myItemsDTO);
        }
        return myItems;
    }
}
