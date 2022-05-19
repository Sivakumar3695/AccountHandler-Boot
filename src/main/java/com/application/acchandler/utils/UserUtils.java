package com.application.acchandler.utils;

import com.application.acchandler.controllers.user.GenderEnum;
import com.application.acchandler.model.users.UserRepository;
import com.application.acchandler.model.users.Users;
import com.application.acchandler.exceptions.CustomException;
import com.google.common.base.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Component
public class UserUtils {

    private static final Logger logger = LoggerFactory.getLogger(UserUtils.class);
    private final UserRepository userRepository;
    private static final String PHONE_NUMBER_REGEX = "[1-9][0-9]{9}";
    private static final String EMAIL_REGEX = "[^\\s@]+@[^\\s@]+\\.[^\\s@]+";

    @Autowired
    public UserUtils(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public Users getUserObjBasedOnUserId(Integer userId)
    {
        return userRepository.findById(userId).get();
    }

    public Users getUserObjBasedOnGoogleId(String googleId)
    {
        return userRepository.findByGoogleId(googleId);
    }

    public Users getUserObjBasedOnEmail(String email)
    {
        return userRepository.findByEmail(email);
    }

    public Users getUserObjBasedOnPhoneNumber(String phoneNumber)
    {
        return userRepository.findByPhoneNumber(phoneNumber);
    }

    public void saveUser(Users user)
    {
        userRepository.save(user);
    }

    public Map<String, Object> getUserDetailsResponseMap(Users user) throws Exception
    {
        Map<String, Object> userDetails = new HashMap<String, Object>();
        userDetails.put("display_name", user.getDisplayName());
        userDetails.put("nick_name", user.getNickName());
        userDetails.put("phone_number", user.getPhoneNumber());
        userDetails.put("gender_code", GenderEnum.getGenderCode(user.getGender()));
        userDetails.put("gender_display_name", GenderEnum.getGenderDisplayName(user.getGender()));
        userDetails.put("email_id", user.getEmailID());

        Map<String, Object> responseMap = new HashMap<>();
        responseMap.put("incomplete_details_exist", isIncompleteDetailsExists(user));

        responseMap.put("is_phone_number_verification_pending",
                user.getPhoneNumber() != null && !user.getIsPhoneNumberVerified());
        responseMap.put("is_email_verification_pending",
                user.getEmailID() != null && !user.getIsEmailVerified());

        responseMap.put("user_details", userDetails);

        return responseMap;
    }

    public void performValidations(Users prevUserInstance, Users user) throws CustomException
    {
        //if prevUserInstance is null, then it is user creation.
        if (prevUserInstance != null && (user.getDisplayName() == null  || Strings.isNullOrEmpty(user.getDisplayName().trim())))
        {
            logger.info("Invalid display name has been provided for the userID:{1}", user.getUserID());
            throw new CustomException("Please provide a valid display name");
        }
        if (user.getPhoneNumber() == null)
        {
            if (user.getGoogleId() == null) {
                logger.info("Both user phone number and Google ID are null for the userID:{1}", user.getUserID());
                throw new CustomException("Phone Number cannot be empty");
            }

            if (prevUserInstance != null && prevUserInstance.getPhoneNumber() != null)
            {
                logger.info("User phone number has been removed by the user with Id:{1}", user.getUserID());
                throw new CustomException("Phone Number cannot be empty");
            }
        }
        String phoneNumber = String.valueOf(user.getPhoneNumber());
        if (!Strings.isNullOrEmpty(phoneNumber))
        {
            if (userRepository.findByPhoneNumberForDuplicationCheck(phoneNumber, user.getUserID()) != null)
            {
                logger.info("Provided phone number already exists. Current userId:{1}", user.getUserID());
                throw new CustomException("User with the provided phone number already exists.");
            }
            if (!phoneNumber.matches(PHONE_NUMBER_REGEX))
            {
                logger.info("Invalid phone number. Current userId:{1}", user.getUserID());
                throw new CustomException("Please provide a valid phone number.");
            }
        }
        if (!Strings.isNullOrEmpty(user.getEmailID()))
        {
            if (userRepository.findByEmailIdForDuplicationCheck(user.getEmailID(), user.getUserID()) != null)
            {
                logger.info("Provided email Id already exists. Current userId:{1}", user.getUserID());
                throw new CustomException("User with the provided emailID already exists.");
            }
            if (!user.getEmailID().matches(EMAIL_REGEX))
            {
                logger.info("Invalid email ID. Current userId:{1}", user.getUserID());
                throw new CustomException("Please provide a valid Email address.");
            }
        }
    }

    public static void updateUserInstanceWithRequestPayLoad(Users user, Map<String, Object> payload) throws Exception
    {
        String[] keys = payload.keySet().toArray(new String[0]);

        for (String key : keys)
        {
            if (key.equals("display_name"))
                user.setDisplayName(payload.get(key).toString());
            else if (key.equals("nick_name"))
                user.setNickName(payload.get(key).toString());
            else if (key.equals("phone_number"))
            {
                Long newVal = Long.valueOf(String.valueOf(payload.get(key)));
                if (user.getPhoneNumber() != newVal)
                {
                    user.setIsPhoneNumberVerified(false);
                }
                user.setPhoneNumber(newVal);
            }
            else if (key.equals("email_id"))
            {
                String newVal = (String) payload.get(key);
                if (!Objects.equals(user.getEmailID(), newVal))
                {
                    user.setIsEmailVerified(false);
                }
                user.setEmailID(newVal);
            }
            else if (key.equals("gender"))
            {
                int genderType = GenderEnum.getGenderType(payload.get(key).toString());
                user.setGender(genderType);
            }
        }
    }

    private static boolean isIncompleteDetailsExists(Users user){
        return user.getDisplayName() == null || user.getPhoneNumber() == null || user.getDisplayName().isEmpty();
    }

}
