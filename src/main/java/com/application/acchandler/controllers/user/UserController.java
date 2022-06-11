package com.application.acchandler.controllers.user;

import com.application.acchandler.exceptions.CustomException;
import com.application.acchandler.model.DeviceDetails.DeviceDetailsJdbcOpHandler;
import com.application.acchandler.model.sessionObj.SessionJdbcTemplateOpHandler;
import com.application.acchandler.model.sessionObj.SessionObj;
import com.application.acchandler.model.users.Users;
import com.application.acchandler.storage.StorageService;
import com.application.acchandler.utils.UserUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.session.SessionRepository;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class UserController {

    private static ThreadLocal<Users> currentUserContext = new ThreadLocal<>();

    public static void setCurrentUserContext(Users user)
    {
        currentUserContext.set(user);
    }

    private final PasswordEncoder passwordEncoder;
    private final StorageService storageService;
    private final DeviceDetailsJdbcOpHandler deviceDetailsJdbcOpHandler;
    private final SessionJdbcTemplateOpHandler sessionJdbcTemplateOpHandler;
    private final SessionRepository sessionRepository;
    private final SessionRegistry sessionRegistry;
    private OAuth2AuthorizedClientService authorizedClientService;
    private UserUtils userUtils;

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Autowired
    public UserController(
            PasswordEncoder passwordEncoder,
            StorageService storageService,
            DeviceDetailsJdbcOpHandler deviceDetailsJdbcOpHandler,
            SessionJdbcTemplateOpHandler sessionJdbcTemplateOpHandler,
            SessionRepository sessionRepository,
            SessionRegistry sessionRegistry,
            OAuth2AuthorizedClientService authorizedClientService,
            UserUtils userUtils
    ) {
        this.passwordEncoder = passwordEncoder;
        this.storageService = storageService;
        this.deviceDetailsJdbcOpHandler = deviceDetailsJdbcOpHandler;
        this.sessionJdbcTemplateOpHandler = sessionJdbcTemplateOpHandler;
        this.sessionRepository = sessionRepository;
        this.sessionRegistry = sessionRegistry;
        this.authorizedClientService = authorizedClientService;
        this.userUtils = userUtils;
    }

    @GetMapping("/")
    public ResponseEntity<String> getHome()
    {
        return ResponseEntity.ok().build();
    }

    @PostMapping("/send-otp")
    public void sendOtp(@RequestParam String phoneNumber) throws Exception
    {

        Users user = userUtils.getUserObjBasedOnPhoneNumber(phoneNumber);

        if (user == null){
            logger.info("sendOTP process for a new User. Hence, creating a new user row");

            user = new Users();
            user.setPhoneNumber(Long.valueOf(phoneNumber));
            user.setLatestOtp(passwordEncoder.encode(String.valueOf(1234)));
            user.setOtpExpirationTime(System.currentTimeMillis() + (10 * 60 * 1000)); // 10 minutes from the current time.
            userUtils.performValidations(null, user);
            userUtils.saveUser(user);

            logger.info("New user created.");
        }
        else {
            user.setLatestOtp(passwordEncoder.encode(String.valueOf(1234)));
            user.setOtpExpirationTime(System.currentTimeMillis() + (10 * 60 * 1000)); // 10 minutes from the current time.
            userUtils.saveUser(user);

            logger.info("User already exists. Hence, only the OTP column has been updated.");
        }

        logger.info("Otp sent to the user. Process Done!");
        return;
    }

    @GetMapping("/users/me")
    public Map<String, Object> getCurrentUserInfo() throws Exception
    {
        return userUtils.getUserDetailsResponseMap(currentUserContext.get());
    }

    @GetMapping(value = "/sessions", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> getActiveSessions()
    {
        List<Map> sessionDeviceList =
                deviceDetailsJdbcOpHandler.getDeviceDetailsByPrincipalName(currentUserContext.get().getPossibleSessionPrincipalNames());

        Map responseMap = new HashMap<>();
        responseMap.put("device_details_list", sessionDeviceList);

        return responseMap;
    }

    @PutMapping("/users/me")
    public Map<String, Object> updateUserDetails(@RequestBody Map<String, Object> payload) throws Exception{

        Users user = currentUserContext.get();
        Users prevUserInstance = new Users(user);

        UserUtils.updateUserInstanceWithRequestPayLoad(user, payload);

        userUtils.performValidations(prevUserInstance, user);
        userUtils.saveUser(user);

        return userUtils.getUserDetailsResponseMap(user);
    }

    @GetMapping(value = "/users/me/profile-picture")
    @ResponseBody
    public ResponseEntity<byte[]> getProfilePicture() throws Exception
    {
        Users currentUser = currentUserContext.get();
        String fileName = currentUser.getProfilePicture();
        String extension = fileName.split("\\.")[1];
        if (fileName == null || fileName.isBlank())
            fileName = "default-profile-icon.jpg";

        byte[] file = storageService.loadAsResource(currentUser, fileName);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.valueOf("image/"+ extension));
        String respFileName = "Profile-Picture." + extension;
        headers.setContentDisposition(ContentDisposition.attachment().filename(respFileName).build());
        ResponseEntity<byte[]> responseEntity = new ResponseEntity<>(file, headers, HttpStatus.OK);

        return responseEntity;
    }

    @PostMapping("/profile-picture")
    public void updateProfilePicture(@RequestParam("file") MultipartFile file) throws Exception
    {
        String fileName = null;
        Users user = currentUserContext.get();

        logger.info(file.getName());
        logger.info(file.getContentType());
        fileName = "Profile-Pic-" + user.getUserID().toString() + "-" + System.currentTimeMillis()
                + "." + file.getContentType().split("/")[1];
        logger.info(fileName);
        fileName = storageService.store(user, file, fileName);

        user.setProfilePicture(fileName);
        userUtils.saveUser(user);
    }

    @PostMapping("/devices/{deviceId}/logout")
    public void signOutFromDevice(@PathVariable String deviceId) throws Exception
    {
        SessionObj sessionObj =
                sessionJdbcTemplateOpHandler.findSessionDetailsByPrimaryIdAndPrincipalName(
                        deviceId,
                        currentUserContext.get().getPossibleSessionPrincipalNames()
                );

        if (sessionObj == null){
            throw new CustomException("Sorry, you cannot perform SignOut.");
        }

        sessionRepository.deleteById(sessionObj.getSession_id());

        return;
    }
}
