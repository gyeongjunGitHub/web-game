package drowGame.drowGame.entity;

import drowGame.drowGame.dto.ItemDTO;
import jakarta.persistence.*;
import lombok.Getter;

@Entity
@Table(name = "item")
@Getter
public class ItemEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String name;

    @Column
    private int price;

    @Column(name = "original_file_name")
    private String original_file_name;

    @Column(name = "stored_file_name")
    private String stored_file_name;

    public ItemEntity(){}
    public ItemEntity(ItemDTO itemDTO){
        this.name = itemDTO.getName();
        this.price = itemDTO.getPrice();
        this.original_file_name = itemDTO.getOriginal_file_name();
        this.stored_file_name = itemDTO.getStored_file_name();
    }
}
