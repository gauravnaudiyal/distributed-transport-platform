package com.groupb.booking.repository;

import com.groupb.booking.model.entity.BookingSegment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookingSegmentRepository extends JpaRepository<BookingSegment, Long> {

    List<BookingSegment> findByBookingBookingId(String bookingId);
}
