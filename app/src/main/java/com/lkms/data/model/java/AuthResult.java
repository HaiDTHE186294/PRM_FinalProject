package com.lkms.data.model.java;

import com.lkms.data.model.Role;
import lombok.Data;

@Data
public class AuthResult {

        private String authToken; // Authentication token (e.g., JWT)

        private Role userRole; // User's role

        private int userId; // userId (int)
}
