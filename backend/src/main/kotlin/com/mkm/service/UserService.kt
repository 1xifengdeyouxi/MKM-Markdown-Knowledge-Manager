package com.mkm.service

import com.mkm.dto.LoginRequest
import com.mkm.dto.RegisterRequest
import com.mkm.dto.TokenResponse
import com.mkm.model.User
import com.mkm.repository.UserRepository
import com.mkm.security.JwtTokenProvider
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service

@Service
class UserService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val authenticationManager: AuthenticationManager,
    private val jwtTokenProvider: JwtTokenProvider
) {
    fun register(request: RegisterRequest): TokenResponse {
        require(!userRepository.existsByUsername(request.username)) { "Username already exists" }
        if (request.email != null) {
            require(!userRepository.existsByEmail(request.email)) { "Email already exists" }
        }
        val user = userRepository.save(
            User(
                username = request.username,
                password = passwordEncoder.encode(request.password),
                email = request.email
            )
        )
        return TokenResponse(token = jwtTokenProvider.generateToken(user.username), username = user.username)
    }

    fun login(request: LoginRequest): TokenResponse {
        authenticationManager.authenticate(
            UsernamePasswordAuthenticationToken(request.username, request.password)
        )
        return TokenResponse(
            token = jwtTokenProvider.generateToken(request.username),
            username = request.username
        )
    }
}
