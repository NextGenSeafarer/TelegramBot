package ru.runner.repositories.foodCalculation;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FoodCalculationRepository extends CrudRepository<FoodCalculationEntity, Integer> {



}
