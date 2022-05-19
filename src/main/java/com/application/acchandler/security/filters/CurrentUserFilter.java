package com.application.acchandler.security.filters;

import com.application.acchandler.controllers.user.UserController;
import com.application.acchandler.model.users.Users;
import com.application.acchandler.security.auth.ApplicationUser;
import com.application.acchandler.utils.UserUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;


@Component
public class CurrentUserFilter  extends OncePerRequestFilter {


    @Autowired
    private UserUtils userUtils;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException
    {

        if (SecurityContextHolder.getContext().getAuthentication() instanceof OAuth2AuthenticationToken)
        {
            OidcUser principalUser = (OidcUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

            //STEP:1 => Check if the Unique identifier of OAuth Login response attributes is present in our table.
            String googleSubId = (String) principalUser.getClaims().get("sub");
            Users user = userUtils.getUserObjBasedOnGoogleId(googleSubId);

            //STEP:2 => If STEP-1 fails to spot a user from our table, get a user from our table
            //              whose email matches the emailID of the user logged in through OAuth login.
            if (user == null)
            {
                user = userUtils.getUserObjBasedOnEmail(principalUser.getEmail());
            }

            if (user == null)
            {
                //create a new user
                user = new Users();
                user.setEmailID(principalUser.getEmail());
                user.setGoogleId(googleSubId);
                userUtils.performValidations(null, user);
                userUtils.saveUser(user);
            }
            else
            {
                //update user with sub_id
                if (user.getGoogleId() == null)
                {
                    user.setGoogleId(googleSubId);
                    userUtils.saveUser(user);
                }
            }

            UserController.setCurrentUserContext(user);
        }
        else if (SecurityContextHolder.getContext().getAuthentication() instanceof UsernamePasswordAuthenticationToken)
        {
            System.out.println("User name - Password Auth token");

            ApplicationUser appUser = (ApplicationUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

            Users currentUser = userUtils.getUserObjBasedOnUserId(appUser.getUserId());

            //if current user phone number is not yet verified, it will be marked as verified.
            //since, when the code reach this filter, it should already have crossed UserNamePasswordAuthenticationFilter
            if (!currentUser.getIsPhoneNumberVerified())
            {
                currentUser.setIsPhoneNumberVerified(true);
                userUtils.saveUser(currentUser);
            }
            UserController.setCurrentUserContext(currentUser);
        }

        filterChain.doFilter(request, response);

        //clear ThreadLocal
        UserController.setCurrentUserContext(null);
    }
}
