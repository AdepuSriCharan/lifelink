package com.recipientservice.repository;

import com.recipientservice.model.ReceiveRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ReceiveRequestRepository extends JpaRepository<ReceiveRequest, Long> {
    List<ReceiveRequest> findAllByRecipientId(UUID recipientId);
}
