package com.example.ecommerce.marketplace.web.controller;

import com.example.ecommerce.marketplace.domain.retailer.Retailer;
import com.example.ecommerce.marketplace.domain.retailer.RetailerRepository;
import com.example.ecommerce.marketplace.domain.supplier.Supplier;
import com.example.ecommerce.marketplace.domain.supplier.SupplierRepository;
import com.example.ecommerce.marketplace.domain.user.User;
import com.example.ecommerce.marketplace.domain.user.UserRepository;
import com.example.ecommerce.marketplace.domain.user.UserRole;
import com.example.ecommerce.marketplace.service.auth.JwtService;
import com.example.ecommerce.marketplace.web.common.GlobalExceptionHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    private MockMvc mockMvc;

    private ObjectMapper objectMapper;

    @Mock
    private UserRepository userRepository;

    @Mock
    private SupplierRepository supplierRepository;

    @Mock
    private RetailerRepository retailerRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private UserController userController;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();
        mockMvc = MockMvcBuilders.standaloneSetup(userController)
            .setControllerAdvice(new GlobalExceptionHandler())
            .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
            .build();
    }

    @Nested
    @DisplayName("POST /api/v1/users/register/supplier - Register Supplier")
    class RegisterSupplierTests {

        @Test
        @DisplayName("Should return 201 CREATED when supplier is registered successfully")
        void registerSupplier_ShouldReturn201_WhenSuccess() throws Exception {
            Map<String, Object> request = new HashMap<>();
            request.put("username", "supplier_user");
            request.put("password", "SecurePass1!");
            request.put("name", "Test Supplier Co");
            request.put("email", "supplier@example.com");
            request.put("businessLicense", "BL123456789");
            request.put("phone", "+1234567890");
            request.put("address", "123 Business St");

            when(userRepository.existsByUsername("supplier_user")).thenReturn(false);
            when(supplierRepository.existsByEmail("supplier@example.com")).thenReturn(false);
            when(supplierRepository.existsByBusinessLicense("BL123456789")).thenReturn(false);
            when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");

            Supplier savedSupplier = new Supplier(
                1L, "Test Supplier Co", "supplier@example.com", "+1234567890",
                "123 Business St", null, null, "BL123456789", null, false
            );
            when(supplierRepository.save(any(Supplier.class))).thenReturn(savedSupplier);

            User savedUser = new User();
            savedUser.setId(1L);
            savedUser.setUsername("supplier_user");
            savedUser.setRole(UserRole.SUPPLIER);
            savedUser.setEntityId(1L);
            when(userRepository.save(any(User.class))).thenReturn(savedUser);

            mockMvc.perform(post("/api/v1/users/register/supplier")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Registration successful"))
                .andExpect(jsonPath("$.username").value("supplier_user"))
                .andExpect(jsonPath("$.role").value("SUPPLIER"))
                .andExpect(jsonPath("$.entityName").value("Test Supplier Co"));

            verify(supplierRepository, times(1)).save(any(Supplier.class));
            verify(userRepository, times(1)).save(any(User.class));
        }

        @Test
        @DisplayName("Should return 400 when username is already taken")
        void registerSupplier_ShouldReturn400_WhenUsernameExists() throws Exception {
            Map<String, Object> request = new HashMap<>();
            request.put("username", "existing_user");
            request.put("password", "SecurePass1!");
            request.put("name", "Test Supplier Co");
            request.put("email", "supplier@example.com");
            request.put("businessLicense", "BL123456789");

            when(userRepository.existsByUsername("existing_user")).thenReturn(true);

            mockMvc.perform(post("/api/v1/users/register/supplier")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Username 'existing_user' is already taken. Please choose a different username."));

            verify(supplierRepository, never()).save(any(Supplier.class));
            verify(userRepository, never()).save(any(User.class));
        }

        @Test
        @DisplayName("Should return 400 when email is already registered")
        void registerSupplier_ShouldReturn400_WhenEmailExists() throws Exception {
            Map<String, Object> request = new HashMap<>();
            request.put("username", "new_supplier");
            request.put("password", "SecurePass1!");
            request.put("name", "Test Supplier Co");
            request.put("email", "existing@example.com");
            request.put("businessLicense", "BL123456789");

            when(userRepository.existsByUsername("new_supplier")).thenReturn(false);
            when(supplierRepository.existsByEmail("existing@example.com")).thenReturn(true);

            mockMvc.perform(post("/api/v1/users/register/supplier")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Email 'existing@example.com' is already registered. Please use a different email address."));

            verify(supplierRepository, never()).save(any(Supplier.class));
        }
    }

    @Nested
    @DisplayName("POST /api/v1/users/register/retailer - Register Retailer")
    class RegisterRetailerTests {

        @Test
        @DisplayName("Should return 201 CREATED when retailer is registered successfully")
        void registerRetailer_ShouldReturn201_WhenSuccess() throws Exception {
            Map<String, Object> request = new HashMap<>();
            request.put("username", "retailer_user");
            request.put("password", "SecurePass1!");
            request.put("name", "Test Retailer Shop");
            request.put("email", "retailer@example.com");
            request.put("businessLicense", "RL987654321");
            request.put("phone", "+9876543210");
            request.put("address", "456 Retail Ave");
            request.put("creditLimit", 10000.0);

            when(userRepository.existsByUsername("retailer_user")).thenReturn(false);
            when(retailerRepository.existsByEmail("retailer@example.com")).thenReturn(false);
            when(retailerRepository.existsByBusinessLicense("RL987654321")).thenReturn(false);
            when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");

            Retailer savedRetailer = new Retailer();
            savedRetailer.setId(1L);
            savedRetailer.setName("Test Retailer Shop");
            savedRetailer.setEmail("retailer@example.com");
            when(retailerRepository.save(any(Retailer.class))).thenReturn(savedRetailer);

            User savedUser = new User();
            savedUser.setId(1L);
            savedUser.setUsername("retailer_user");
            savedUser.setRole(UserRole.RETAILER);
            savedUser.setEntityId(1L);
            when(userRepository.save(any(User.class))).thenReturn(savedUser);

            mockMvc.perform(post("/api/v1/users/register/retailer")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Registration successful"))
                .andExpect(jsonPath("$.username").value("retailer_user"))
                .andExpect(jsonPath("$.role").value("RETAILER"))
                .andExpect(jsonPath("$.entityName").value("Test Retailer Shop"));

            verify(retailerRepository, times(1)).save(any(Retailer.class));
            verify(userRepository, times(1)).save(any(User.class));
        }

        @Test
        @DisplayName("Should return 400 when business license is already registered")
        void registerRetailer_ShouldReturn400_WhenBusinessLicenseExists() throws Exception {
            Map<String, Object> request = new HashMap<>();
            request.put("username", "new_retailer");
            request.put("password", "SecurePass1!");
            request.put("name", "Test Retailer Shop");
            request.put("email", "retailer@example.com");
            request.put("businessLicense", "EXISTING_LICENSE");

            when(userRepository.existsByUsername("new_retailer")).thenReturn(false);
            when(retailerRepository.existsByEmail("retailer@example.com")).thenReturn(false);
            when(retailerRepository.existsByBusinessLicense("EXISTING_LICENSE")).thenReturn(true);

            mockMvc.perform(post("/api/v1/users/register/retailer")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Business license 'EXISTING_LICENSE' is already registered. Each business license can only be used once."));

            verify(retailerRepository, never()).save(any(Retailer.class));
            verify(userRepository, never()).save(any(User.class));
        }
    }
}

