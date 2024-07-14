package drowGame.drowGame.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import drowGame.drowGame.dto.ItemDTO;
import drowGame.drowGame.dto.MyItemsDTO;
import drowGame.drowGame.entity.ItemEntity;
import drowGame.drowGame.entity.MemberEntity;
import drowGame.drowGame.entity.MyItemsEntity;
import drowGame.drowGame.repository.MemberRepository;
import drowGame.drowGame.repository.StoreRepository;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class StoreService {
    private final StoreRepository storeRepository;
    private final MemberRepository memberRepository;
    private final MemberSessionService memberSessionService;
    @Transactional
    public boolean itemRegistration(MultipartFile itemPicture, String name, int price) throws IOException {
        String originalFileName = itemPicture.getOriginalFilename();
        String storedFileName = System.currentTimeMillis() + "_" + originalFileName;

        String windowSavePath = "C:/images/" + storedFileName;
        String ec2SavePath = "/home/images/" + storedFileName;

        File saveFile = new File(ec2SavePath);

        //폴더가 없을 경우 폴더 생성!
        if(!saveFile.exists()){
            saveFile.mkdirs();
        }

        //file 저장
        itemPicture.transferTo(saveFile);

        ItemDTO itemDTO = new ItemDTO();
        itemDTO.setName(name);
        itemDTO.setPrice(price);
        itemDTO.setOriginal_file_name(originalFileName);
        itemDTO.setStored_file_name(storedFileName);
        ItemEntity itemEntity = new ItemEntity(itemDTO);
        return storeRepository.itemRegistration(itemEntity);
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

    @Transactional
    public boolean buy(String item, HttpSession httpSession) {
        String myId = memberSessionService.getMemberId(httpSession.getId());
        ObjectMapper objectMapper = new ObjectMapper();
        MyItemsDTO myItemsDTO = new MyItemsDTO();
        try {
            myItemsDTO = objectMapper.readValue(item, MyItemsDTO.class);

        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        
        //아이템 존재 체크
        List<MyItemsEntity> myItems = memberRepository.getMyItems(myId);
        boolean isAlreadyGet = false;
        String name = null;
        for(MyItemsEntity m : myItems){
            if (m.getName().equals(myItemsDTO.getName())) {
                isAlreadyGet = true;
            }
        }

        try {
            //게임 포인트 차감
            Optional<MemberEntity> byIdOptional = memberRepository.findById(myId);
            if (byIdOptional.isPresent()){
                MemberEntity byId = byIdOptional.get();
                byId.setGame_point(byId.getGame_point() - myItemsDTO.getPrice());
            }

            //가지고 있는 아이템이면 count+1
            if(isAlreadyGet){
                for(MyItemsEntity m : myItems){
                    if (m.getName().equals(myItemsDTO.getName())) {
                        m.setCount(m.getCount()+1);
                    }
                }
                return true;
            }else{
                myItemsDTO.setCount(1);
                myItemsDTO.setMember_id(myId);
                MyItemsEntity myItemsEntity = new MyItemsEntity(myItemsDTO);
                return storeRepository.buy(myItemsEntity);
            }
        }catch (Exception e){
            return false;
        }
    }
}
