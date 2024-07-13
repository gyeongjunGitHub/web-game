package drowGame.drowGame.dto;

import drowGame.drowGame.entity.ItemEntity;
import jakarta.persistence.Column;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ItemDTO {
    private Long id;
    private String name;
    private int price;
    private String original_file_name;
    private String stored_file_name;

    public ItemDTO(){}
    public ItemDTO(ItemEntity itemEntity){
        this.id = itemEntity.getId();
        this.name = itemEntity.getName();
        this.price = itemEntity.getPrice();
        this.original_file_name = itemEntity.getOriginal_file_name();
        this.stored_file_name = itemEntity.getStored_file_name();
    }
}
