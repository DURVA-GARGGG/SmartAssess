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
                .orElseGet(() -> roleRepository.save(new Role(roleId, roleId == 1 ? "Admin" : "Student")));

        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail().trim());
        // For compatibility with the existing project's DB data
        user.setPassword(request.getPassword());
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

        boolean matches = request.getPassword().equals(user.getPassword())
                || passwordEncoder.matches(request.getPassword(), user.getPassword());

        if (!matches) {
            throw new ApiException("Invalid credentials", 401);
        }

        String roleName = user.getRole() != null ? user.getRole().getRoleName() : "Student";
        String token = jwtUtil.generateToken(user.getUserId(), user.getEmail(), roleName, user.getName());

        return new AuthResponse(token, roleName, user.getName(), user.getUserId());
    }
}
