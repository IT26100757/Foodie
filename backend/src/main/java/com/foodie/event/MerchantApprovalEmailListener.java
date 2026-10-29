
package com.foodie.event;

import com.foodie.service.MerchantEmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class MerchantApprovalEmailListener {

    private static final Logger log =
            LoggerFactory.getLogger(MerchantApprovalEmailListener.class);

    private final MerchantEmailService merchantEmailService;

    public MerchantApprovalEmailListener(
            MerchantEmailService merchantEmailService) {
        this.merchantEmailService = merchantEmailService;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onMerchantApproved(MerchantApprovedEvent event) {

        try {
            merchantEmailService.sendApprovalEmail(
                    event.email(),
                    event.firstName()
            );

            log.info("Merchant approval email sent successfully");

        } catch (Exception ex) {
            log.error(
                    "Failed to send merchant approval email",
                    ex
            );
        }
    }
}
