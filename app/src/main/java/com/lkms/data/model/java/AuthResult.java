package com.lkms.data.model.java;

import com.lkms.data.model.kotlin.Role;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthResult {

        private String authToken; // Authentication token (e.g., JWT)

        private Role userRole; // User's role

        private int userId; // userId (int)
}
