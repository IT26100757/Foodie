package com.foodie.service;

import com.foodie.dto.PendingMerchantResponse;
import com.foodie.model.Merchant;
import com.foodie.repository.MerchantRepository;
import com.foodie.event.MerchantApprovedEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AdminMerchantService {

    private final MerchantRepository merchantRepository;
    private final ApplicationEventPublisher eventPublisher;

    public AdminMerchantService(
            MerchantRepository merchantRepository,
            ApplicationEventPublisher eventPublisher) {

        this.merchantRepository = merchantRepository;
        this.eventPublisher = eventPublisher;
    }

    @Transactional(readOnly = true)
    public List<PendingMerchantResponse> getPendingMerchants() {

        return merchantRepository.findByVerificationStatus(
                        Merchant.VerificationStatus.PENDING
                )
                .stream()
                .map(PendingMerchantResponse::from)
                .toList();
    }


    @Transactional
    public void approveMerchant(Long merchantId) {

        Merchant merchant = merchantRepository.findById(merchantId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Merchant not found")
                );

        if (merchant.getVerificationStatus()
                != Merchant.VerificationStatus.PENDING) {

            throw new IllegalStateException(
                    "Only pending merchants can be approved"
            );
        }

        merchant.setVerificationStatus(
                Merchant.VerificationStatus.APPROVED
        );

        merchantRepository.save(merchant);

        eventPublisher.publishEvent(
                new MerchantApprovedEvent(
                        merchant.getEmail(),
                        merchant.getFirstName()
                )
        );
    }


    @Transactional
    public void rejectMerchant(Long merchantId) {

        Merchant merchant = merchantRepository.findById(merchantId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Merchant not found"));

        if (merchant.getVerificationStatus()
                != Merchant.VerificationStatus.PENDING) {

            throw new IllegalStateException(
                    "Only pending merchants can be rejected");
        }

        merchant.setVerificationStatus(
                Merchant.VerificationStatus.REJECTED);

        merchantRepository.save(merchant);
    }
}