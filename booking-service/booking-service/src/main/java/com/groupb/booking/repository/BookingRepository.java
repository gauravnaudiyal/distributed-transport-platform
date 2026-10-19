package com.groupb.booking.repository;

import com.groupb.booking.model.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, String> {

    Optional<Booking> findByIdempotencyKey(String idempotencyKey);
}
