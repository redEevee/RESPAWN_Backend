package com.shop.respawn.domain.item;

import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter @Setter
public class Mouse extends Item{
    private String symmetry;

}
