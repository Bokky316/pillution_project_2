package com.javalab.student.repository;

import com.javalab.student.entity.ProductIngredient;
import com.javalab.student.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductIngredientRepository extends JpaRepository<ProductIngredient, Long> {
    //Optional<ProductIngredient> findByIngredientName(String ingredientName);
    List<ProductIngredient> findByProductsContaining(Product product);

    @Query("SELECT pi FROM ProductIngredient pi JOIN pi.products p WHERE p = :product")
    List<ProductIngredient> findByProduct(@Param("product") Product product);

    @Query("SELECT pi FROM ProductIngredient pi WHERE pi.ingredientName = :ingredientName")
    List<ProductIngredient> findByIngredientName(@Param("ingredientName") String ingredientName);
}