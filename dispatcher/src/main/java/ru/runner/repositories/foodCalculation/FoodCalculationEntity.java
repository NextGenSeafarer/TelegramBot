package ru.runner.repositories.foodCalculation;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table
@Getter
@Setter
@NoArgsConstructor
@ToString
public class FoodCalculationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int Id;
    private long chatID;
    private int foodAmount;
    private String nutritionType;
    private LocalDateTime feedTime;
    private boolean isAllowedEntryFoodAmount;


}
