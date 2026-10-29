
package com.foodie.wallet;

public record BankAccountResponse(
        Long id,
        String bank,
        String name,
        String number,
        String branch
) {

    public static BankAccountResponse from(
            MerchantBankAccount account) {

        return new BankAccountResponse(
                account.getId(),
                account.getBankName(),
                account.getAccountHolder(),
                "•••• " + account.getLastFour(),
                account.getBranch()
        );
    }
}
