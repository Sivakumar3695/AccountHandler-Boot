package com.application.acchandler.controllers.user;

import com.application.acchandler.model.Users;
import com.application.acchandler.model.UserRepository;
import com.application.acchandler.security.auth.ApplicationUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
public class UserController {

    private UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserController(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/")
    public String getHome(){
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        String name = "";
        if (principal != null){
            name = principal.toString();
        }
        System.out.println(name);
        return "Welcome Home " + name;
    }

    @PostMapping("/sendOTP")
    public ResponseEntity<String> sendOtp(){
        System.out.println("Hey, you are not logged in");

        String phoneNumber = "8012289764";
        Users user = userRepository.findByPhoneNumber(phoneNumber);

        if (user == null){
            user = new Users();
            user.setPhoneNumber(Long.valueOf(phoneNumber));
            user.setLatestOtp(passwordEncoder.encode(String.valueOf(1234)));
            user.setOtpExpirationTime(System.currentTimeMillis() + (10 * 60 * 1000)); // 10 minutes from the current time.
            userRepository.save(user);

            System.out.println("New used row created and otp process resumed");
        }
        else {
            user.setLatestOtp(passwordEncoder.encode(String.valueOf(1234)));
            user.setOtpExpirationTime(System.currentTimeMillis() + (10 * 60 * 1000)); // 10 minutes from the current time.
            userRepository.save(user);

            System.out.println("User already exists. Hence, only the OTP column has been updated.");
        }

        System.out.println("Otp sent to the user. Process Done!");
        return ResponseEntity.ok()
                .body("OTP send to your mobile");
    }

    @GetMapping("/getMyDetails")
    public ResponseEntity<Object> getCurrentUserInfo() throws Exception
    {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        System.out.println(principal);

        Users user = userRepository.findById(((ApplicationUser) principal).getUserId())
                .get();

        Map<String, Object> responseMap = new HashMap<String, Object>();
        responseMap.put("display_name", user.getDisplayName());
        responseMap.put("nick_name", user.getNickName());
        responseMap.put("phone_number", user.getPhoneNumber());
        responseMap.put("gender_code", GenderEnum.getGenderCode(user.getGender()));
        responseMap.put("gender_display_name", GenderEnum.getGenderDisplayName(user.getGender()));
        responseMap.put("email_id", user.getEmailID());
        return ResponseEntity.ok()
                .body(responseMap);

    }

    @GetMapping("/user")
    public String getUser(){
        return "Hello user";
    }

    @GetMapping("/normaluser")
    public String getNormalUser(){
        return "Hello get user";
    }

    @GetMapping("/superuser")
    public String getSuperUser(){
        return "Hello super user";
    }
}
