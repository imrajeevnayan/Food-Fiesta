package com.example.demo.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.demo.entities.Driver;
import com.example.demo.entities.Driver.Status;

import jakarta.persistence.LockModeType;

public interface DriverRepository extends JpaRepository<Driver, Long> {

    List<Driver> findByStatus(Status status);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select d from Driver d where d.id = :id")
    Optional<Driver> findByIdForUpdate(@Param("id") long id);
}
