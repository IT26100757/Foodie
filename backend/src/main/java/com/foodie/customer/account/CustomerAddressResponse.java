package com.foodie.customer.account;

public record CustomerAddressResponse(
        Long id,
        String address1,
        String address2,
        String address3,
        String city,
        String postalCode,
        boolean defaultAddress
) {

    public static CustomerAddressResponse from(CustomerAddress address) {
        return new CustomerAddressResponse(
                address.getId(),
                address.getAddress1(),
                address.getAddress2(),
                address.getAddress3(),
                address.getCity(),
                address.getPostalCode(),
                address.isDefaultAddress()
        );
    }

    @Override
    public String toString() {
        return "CustomerAddressResponse[address details hidden]";
    }
}