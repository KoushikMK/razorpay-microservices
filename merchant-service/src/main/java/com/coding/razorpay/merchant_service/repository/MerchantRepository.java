package com.coding.razorpay.merchant_service.repository;

import com.coding.razorpay.common_lib.enums.MerchantStatus;
import com.coding.razorpay.merchant_service.entity.Merchant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface MerchantRepository extends JpaRepository<Merchant, UUID> {

    boolean existsByEmail(String email);

    List<Merchant> findByStatus(MerchantStatus merchantStatus);
}
