package com.smartexam.service.impl;

import com.smartexam.config.JwtUtil;
import com.smartexam.dto.AuthRequest;
import com.smartexam.dto.AuthResponse;
import com.smartexam.dto.RegisterRequest;
import com.smartexam.entity.Role;
import com.smartexam.entity.User;
import com.smartexam.exception.ApiException;
import com.smartexam.repository.RoleRepository;
import com.smartexam.repository.UserRepository;
import com.smartexam.service.AuthService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthServiceImpl(UserRepository userRepository,
                           RoleRepository roleRepository,
                           PasswordEncoder passwordEncoder,
                           JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    @Override
    @Transactional
    public void register(RegisterRequest request) {
        if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
            throw new ApiException("Email is required");
        }
        if (userRepository.existsByEmail(request.getEmail().trim())) {
            throw new ApiException("User already exists with this email");
        }

        Integer roleId = request.getRole_id() != null ? request.getRole_id() : 2;
        Role role = roleRepository.findById(roleId)
                .orElseGet(() -> roleRepository.save(new Role(roleId, roleId == 1 ? "Admin" : roleId == 3 ? "Proctor" : "Student")));

        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail().trim());
        if (request.getPassword() == null || request.getPassword().isEmpty()) {
            throw new ApiException("Password is required");
        }
        // Store a BCrypt hash, never the plain password
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(role);

        userRepository.save(user);
    }

    @Override
    public AuthResponse login(AuthRequest request) {
        if (request.getEmail() == null || request.getPassword() == null) {
            throw new ApiException("Invalid credentials", 401);
        }

        User user = userRepository.findByEmail(request.getEmail().trim())
                .orElseThrow(() -> new ApiException("Invalid credentials", 401));

        // New accounts store BCrypt hashes ("$2..."); older seed accounts may still be plain text
        String stored = user.getPassword() != null ? user.getPassword() : "";
        boolean matches = stored.startsWith("$2")
                ? passwordEncoder.matches(request.getPassword(), stored)
                : request.getPassword().equals(stored);

        if (!matches) {
            throw new ApiException("Invalid credentials", 401);
        }

        String roleName = user.getRole() != null ? user.getRole().getRoleName() : "Student";
        String token = jwtUtil.generateToken(user.getUserId(), user.getEmail(), roleName, user.getName());

        return new AuthResponse(token, roleName, user.getName(), user.getUserId());
    }
}
