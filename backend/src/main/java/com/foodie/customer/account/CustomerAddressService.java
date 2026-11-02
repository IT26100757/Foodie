package com.foodie.customer.account;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@Validated
public class CustomerAddressService {

    private final CustomerAccountRepository accountRepository;
    private final CustomerAddressRepository addressRepository;

    public CustomerAddressService(
            CustomerAccountRepository accountRepository,
            CustomerAddressRepository addressRepository
    ) {
        this.accountRepository = accountRepository;
        this.addressRepository = addressRepository;
    }

    @Transactional(readOnly = true)
    public List<CustomerAddressResponse> listAddresses(
            @NotNull Long customerId
    ) {
        return findAddresses(customerId).stream()
                .map(CustomerAddressResponse::from)
                .toList();
    }

    @Transactional
    public CustomerAddressResponse createAddress(
            @NotNull Long customerId,
            @NotNull @Valid CustomerAddressRequest request
    ) {
        CustomerAccount customer = lockCustomer(customerId);
        List<CustomerAddress> addresses = findAddresses(customerId);

        boolean makeDefault =
                addresses.isEmpty() || request.defaultAddress();

        if (makeDefault) {
            clearDefaults(addresses);
        }

        CustomerAddress address = new CustomerAddress(
                customer,
                request.address1().trim(),
                request.address2().trim(),
                request.address3().trim(),
                trimOptional(request.city()),
                trimOptional(request.postalCode()),
                makeDefault
        );

        CustomerAddress saved = addressRepository.saveAndFlush(address);

        return CustomerAddressResponse.from(saved);
    }

    @Transactional
    public CustomerAddressResponse updateAddress(
            @NotNull Long customerId,
            @NotNull Long addressId,
            @NotNull @Valid CustomerAddressRequest request
    ) {
        lockCustomer(customerId);

        CustomerAddress address = findOwnedAddress(customerId, addressId);
        List<CustomerAddress> addresses = findAddresses(customerId);

        // Keep the current default until another address is selected.
        boolean makeDefault =
                request.defaultAddress() || address.isDefaultAddress();

        if (makeDefault) {
            clearDefaults(addresses);
        }

        address.setAddress1(request.address1().trim());
        address.setAddress2(request.address2().trim());
        address.setAddress3(request.address3().trim());
        address.setCity(trimOptional(request.city()));
        address.setPostalCode(trimOptional(request.postalCode()));
        address.setDefaultAddress(makeDefault);

        CustomerAddress saved = addressRepository.saveAndFlush(address);

        return CustomerAddressResponse.from(saved);
    }

    @Transactional
    public void deleteAddress(
            @NotNull Long customerId,
            @NotNull Long addressId
    ) {
        lockCustomer(customerId);

        CustomerAddress address = findOwnedAddress(customerId, addressId);
        boolean wasDefault = address.isDefaultAddress();

        addressRepository.delete(address);
        addressRepository.flush();

        if (wasDefault) {
            List<CustomerAddress> remaining = findAddresses(customerId);

            if (!remaining.isEmpty()) {
                clearDefaults(remaining);
                remaining.get(0).setDefaultAddress(true);
                addressRepository.flush();
            }
        }
    }

    private CustomerAccount lockCustomer(Long customerId) {
        return accountRepository.findByIdForUpdate(customerId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "Please sign in again."
                ));
    }

    private CustomerAddress findOwnedAddress(
            Long customerId,
            Long addressId
    ) {
        return addressRepository.findByIdAndCustomer_Id(
                        addressId,
                        customerId
                )
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Delivery address not found."
                ));
    }

    private List<CustomerAddress> findAddresses(Long customerId) {
        return addressRepository
                .findAllByCustomer_IdOrderByDefaultAddressDescIdAsc(
                        customerId
                );
    }

    private void clearDefaults(List<CustomerAddress> addresses) {
        for (CustomerAddress address : addresses) {
            address.setDefaultAddress(false);
        }
    }

    private String trimOptional(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        return value.trim();
    }
}