package drowGame.drowGame.dto;

import drowGame.drowGame.entity.ItemEntity;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ItemDTO {
    private Long id;
    private String name;
    private int price;

    public ItemDTO(){}
    public ItemDTO(ItemEntity itemEntity){
        this.id = itemEntity.getId();
        this.name = itemEntity.getName();
        this.price = itemEntity.getPrice();
    }
}
