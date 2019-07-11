package com.runhang.shadow.client.device.repository;

import com.runhang.shadow.client.device.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface VendingRepository extends JpaRepository<Vending, Integer> {

    @Modifying
    @Query("update Vending set name = :name where id = :id")
    void updateName(@Param("id") int id, @Param("name") String name);

}