package com.evm.backend.service;

import com.evm.backend.dto.request.CustomerFilterRequest;
import com.evm.backend.dto.request.CustomerRequest;
import com.evm.backend.dto.response.CustomerDetailResponse;
import com.evm.backend.dto.response.CustomerListResponse;
import org.springframework.data.domain.Page;

/**
 * Service interface for Customer operations
 * CRUD operations for Customer management
 */
public interface CustomerService {

    /**
     * Get all customers with filtering and pagination
     *
     * @param filterRequest Filter parameters
     * @return Page of CustomerListResponse
     */
    Page<CustomerListResponse> getAllCustomers(CustomerFilterRequest filterRequest);

    /**
     * Get customer by ID
     *
     * @param customerId Customer ID
     * @return CustomerDetailResponse
     */
    CustomerDetailResponse getCustomerById(Long customerId);

    /**
     * Get customer by email
     *
     * @param email Email
     * @return CustomerDetailResponse
     */
    CustomerDetailResponse getCustomerByEmail(String email);

    /**
     * Get customer by phone number
     *
     * @param phoneNumber Phone number
     * @return CustomerDetailResponse
     */
    CustomerDetailResponse getCustomerByPhoneNumber(String phoneNumber);

    /**
     * Create a new customer
     *
     * @param request Customer request
     * @return CustomerDetailResponse of created customer
     */
    CustomerDetailResponse createCustomer(CustomerRequest request);

    /**
     * Update an existing customer
     *
     * @param customerId Customer ID to update
     * @param request Customer request with updated data
     * @return CustomerDetailResponse of updated customer
     */
    CustomerDetailResponse updateCustomer(Long customerId, CustomerRequest request);

    /**
     * Delete a customer
     *
     * @param customerId Customer ID to delete
     */
    void deleteCustomer(Long customerId);
}