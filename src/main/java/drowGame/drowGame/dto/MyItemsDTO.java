package drowGame.drowGame.dto;

import drowGame.drowGame.entity.MemberEntity;
import drowGame.drowGame.entity.MyItemsEntity;
import jakarta.persistence.Column;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MyItemsDTO {
    private Long id;
    private String name;
    private int price;
    private int count;
    private String original_file_name;
    private String stored_file_name;
    private String member_id;

    public MyItemsDTO(){}
    public MyItemsDTO(MyItemsEntity m) {
        this.id = m.getId();
        this.name = m.getName();
        this.price = m.getPrice();
        this.count = m.getCount();
        this.original_file_name = m.getOriginal_file_name();
        this.stored_file_name = m.getStored_file_name();
        this.member_id = m.getMember_id();
    }
}
