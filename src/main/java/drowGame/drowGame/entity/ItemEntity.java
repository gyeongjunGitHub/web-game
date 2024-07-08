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

    public ItemEntity(){}
    public ItemEntity(ItemDTO itemDTO){
        this.name = itemDTO.getName();
        this.price = itemDTO.getPrice();
    }
}
