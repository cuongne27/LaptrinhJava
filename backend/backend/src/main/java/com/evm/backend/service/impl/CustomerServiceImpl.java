package com.evm.backend.service.impl;

import com.evm.backend.dto.request.CustomerFilterRequest;
import com.evm.backend.dto.request.CustomerRequest;
import com.evm.backend.dto.response.CustomerDetailResponse;
import com.evm.backend.dto.response.CustomerListResponse;
import com.evm.backend.entity.Customer;
import com.evm.backend.exception.ResourceNotFoundException;
import com.evm.backend.repository.CustomerRepository;
import com.evm.backend.service.CustomerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

/**
 * Implementation of CustomerService
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;

    @Override
    public Page<CustomerListResponse> getAllCustomers(CustomerFilterRequest filterRequest) {
        log.debug("Getting all customers with filters: {}", filterRequest);

        Pageable pageable = buildPageable(filterRequest);

        Page<Customer> customersPage = customerRepository.findCustomersWithFilters(
                filterRequest.getSearchKeyword(),
                filterRequest.getCustomerType(),
                pageable
        );

        return customersPage.map(this::convertToListResponse);
    }

    @Override
    public CustomerDetailResponse getCustomerById(Long customerId) {
        log.debug("Getting customer by id: {}", customerId);

        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Customer not found with id: " + customerId));

        return convertToDetailResponse(customer);
    }

    @Override
    public CustomerDetailResponse getCustomerByEmail(String email) {
        log.debug("Getting customer by email: {}", email);

        Customer customer = customerRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Customer not found with email: " + email));

        return convertToDetailResponse(customer);
    }

    @Override
    public CustomerDetailResponse getCustomerByPhoneNumber(String phoneNumber) {
        log.debug("Getting customer by phone number: {}", phoneNumber);

        Customer customer = customerRepository.findByPhoneNumber(phoneNumber)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Customer not found with phone number: " + phoneNumber));

        return convertToDetailResponse(customer);
    }

    @Override
    @Transactional
    public CustomerDetailResponse createCustomer(CustomerRequest request) {
        log.info("Creating customer: {}", request.getFullName());

        // Validate email uniqueness (if provided)
        if (request.getEmail() != null && !request.getEmail().isEmpty()) {
            if (customerRepository.existsByEmail(request.getEmail())) {
                throw new IllegalArgumentException(
                        "Email already exists: " + request.getEmail());
            }
        }

        // Validate phone number uniqueness
        if (customerRepository.existsByPhoneNumber(request.getPhoneNumber())) {
            throw new IllegalArgumentException(
                    "Phone number already exists: " + request.getPhoneNumber());
        }

        // Create customer
        Customer customer = Customer.builder()
                .fullName(request.getFullName())
                .phoneNumber(request.getPhoneNumber())
                .email(request.getEmail())
                .address(request.getAddress())
                .customerType(request.getCustomerType())
                .history(request.getHistory())
                .createdAt(OffsetDateTime.now())
                .build();

        Customer savedCustomer = customerRepository.save(customer);

        log.info("Customer created successfully with id: {}", savedCustomer.getId());

        return convertToDetailResponse(savedCustomer);
    }

    @Override
    @Transactional
    public CustomerDetailResponse updateCustomer(Long customerId, CustomerRequest request) {
        log.info("Updating customer id: {}", customerId);

        // Find existing customer
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Customer not found with id: " + customerId));

        // Validate email uniqueness (if changed)
        if (request.getEmail() != null && !request.getEmail().isEmpty()) {
            if (customerRepository.existsByEmailAndIdNot(request.getEmail(), customerId)) {
                throw new IllegalArgumentException(
                        "Email already exists: " + request.getEmail());
            }
        }

        // Validate phone number uniqueness (if changed)
        if (customerRepository.existsByPhoneNumberAndIdNot(request.getPhoneNumber(), customerId)) {
            throw new IllegalArgumentException(
                    "Phone number already exists: " + request.getPhoneNumber());
        }

        // Update fields
        customer.setFullName(request.getFullName());
        customer.setPhoneNumber(request.getPhoneNumber());
        customer.setEmail(request.getEmail());
        customer.setAddress(request.getAddress());
        customer.setCustomerType(request.getCustomerType());
        customer.setHistory(request.getHistory());

        Customer updatedCustomer = customerRepository.save(customer);

        log.info("Customer updated successfully: {}", customerId);

        return convertToDetailResponse(updatedCustomer);
    }

    @Override
    @Transactional
    public void deleteCustomer(Long customerId) {
        log.info("Deleting customer id: {}", customerId);

        // Check if customer exists
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Customer not found with id: " + customerId));

        // Check if customer has associated data
        Long ordersCount = customerRepository.countOrdersByCustomerId(customerId);
        Long ticketsCount = customerRepository.countSupportTicketsByCustomerId(customerId);

        if (ordersCount > 0 || ticketsCount > 0) {
            throw new IllegalStateException(
                    String.format("Cannot delete customer. It has %d orders and %d support tickets",
                            ordersCount, ticketsCount));
        }

        // Delete customer
        customerRepository.deleteById(customerId);

        log.info("Customer deleted successfully: {}", customerId);
    }

    /**
     * Convert Customer entity to CustomerListResponse
     */
    private CustomerListResponse convertToListResponse(Customer customer) {
        // Get statistics
        Long totalOrders = customerRepository.countOrdersByCustomerId(customer.getId());
        Long totalSupportTickets = customerRepository.countSupportTicketsByCustomerId(customer.getId());

        return CustomerListResponse.builder()
                .id(customer.getId())
                .fullName(customer.getFullName())
                .phoneNumber(customer.getPhoneNumber())
                .email(customer.getEmail())
                .address(customer.getAddress())
                .customerType(customer.getCustomerType())
                .createdAt(customer.getCreatedAt())
                .totalOrders(totalOrders)
                .totalSupportTickets(totalSupportTickets)
                .build();
    }

    /**
     * Convert Customer entity to CustomerDetailResponse
     */
    private CustomerDetailResponse convertToDetailResponse(Customer customer) {
        // Get statistics
        Long totalOrders = customerRepository.countOrdersByCustomerId(customer.getId());
        Long totalSupportTickets = customerRepository.countSupportTicketsByCustomerId(customer.getId());
        Long openTickets = customerRepository.countOpenTicketsByCustomerId(customer.getId());
        Long closedTickets = customerRepository.countClosedTicketsByCustomerId(customer.getId());

        return CustomerDetailResponse.builder()
                .id(customer.getId())
                .fullName(customer.getFullName())
                .phoneNumber(customer.getPhoneNumber())
                .email(customer.getEmail())
                .address(customer.getAddress())
                .customerType(customer.getCustomerType())
                .history(customer.getHistory())
                .createdAt(customer.getCreatedAt())
                .totalOrders(totalOrders)
                .totalSupportTickets(totalSupportTickets)
                .openTickets(openTickets)
                .closedTickets(closedTickets)
                .build();
    }

    /**
     * Build Pageable with sorting
     */
    private Pageable buildPageable(CustomerFilterRequest filterRequest) {
        int page = filterRequest.getPage() != null ? filterRequest.getPage() : 0;
        int size = filterRequest.getSize() != null ? filterRequest.getSize() : 20;

        if (size > 100) {
            size = 100;
        }

        Sort sort = Sort.unsorted();
        if (filterRequest.getSortBy() != null) {
            switch (filterRequest.getSortBy()) {
                case "name_asc":
                    sort = Sort.by(Sort.Direction.ASC, "fullName");
                    break;
                case "name_desc":
                    sort = Sort.by(Sort.Direction.DESC, "fullName");
                    break;
                case "created_asc":
                    sort = Sort.by(Sort.Direction.ASC, "createdAt");
                    break;
                case "created_desc":
                    sort = Sort.by(Sort.Direction.DESC, "createdAt");
                    break;
                default:
                    sort = Sort.by(Sort.Direction.DESC, "createdAt");
            }
        }

        return PageRequest.of(page, size, sort);
    }
}