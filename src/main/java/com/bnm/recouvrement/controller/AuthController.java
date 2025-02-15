package  com.bnm.recouvrement.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.web.bind.annotation.*;

import com.bnm.recouvrement.Config.JwtService;
import com.bnm.recouvrement.dto.AuthRequest;
import com.bnm.recouvrement.dto.AuthResponse;
import com.bnm.recouvrement.dto.UserDto;
import com.bnm.recouvrement.service.AuthService;


@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = "http://localhost:4200")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final AuthService authService;

    @Autowired
    public AuthController(AuthenticationManager authenticationManager, JwtService jwtService, AuthService authService) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody UserDto request) {
        AuthResponse response = authService.register(request);
        return ResponseEntity.ok(response);
    }
        
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody AuthRequest authRequest) {
        System.out.println("hello from authcontroller ");
        return ResponseEntity.ok(authService.authenticate(authRequest));
    }
}