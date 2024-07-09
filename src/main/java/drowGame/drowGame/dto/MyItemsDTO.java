package drowGame.drowGame.dto;

import drowGame.drowGame.entity.MemberEntity;
import drowGame.drowGame.entity.MyItemsEntity;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MyItemsDTO {
    private Long id;
    private String name;
    private int price;
    private String member_id;

    public MyItemsDTO(){}
    public MyItemsDTO(MyItemsEntity m) {
        this.id = m.getId();
        this.name = m.getName();
        this.price = m.getPrice();
        this.member_id = m.getMember_id();
    }
}
