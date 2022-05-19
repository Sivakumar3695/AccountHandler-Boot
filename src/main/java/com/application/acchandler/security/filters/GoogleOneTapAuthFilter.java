package com.application.acchandler.security.filters;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.client.oidc.authentication.OidcIdTokenDecoderFactory;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUserAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtDecoderFactory;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.logging.Level;

@Component
public class GoogleOneTapAuthFilter extends OncePerRequestFilter {
    private static final String INVALID_ID_TOKEN_ERROR_CODE = "invalid_id_token";
    private static final Logger logger = LoggerFactory.getLogger(GoogleOneTapAuthFilter.class);

    private JwtDecoderFactory<ClientRegistration> jwtDecoderFactory = new OidcIdTokenDecoderFactory();
    private ClientRegistrationRepository clientRegistrationRepository;

    RequestMatcher customFilterUrl = new AntPathRequestMatcher("/tokenlogin/google");

    @Autowired
    public GoogleOneTapAuthFilter(ClientRegistrationRepository clientRegistrationRepository)
    {
        this.clientRegistrationRepository = clientRegistrationRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        if (!customFilterUrl.matches(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        String idTokenString = request.getParameter("credential");
        logger.info("Google One Tap Authentication filter");
        if (idTokenString == null){
            logger.info("No idToken found. Continue chain");
            filterChain.doFilter(request, response);
            return;
        }

        logger.info("Getting google client registration");
        ClientRegistration clientRegistration = this.clientRegistrationRepository.findByRegistrationId("google");

        logger.info("Parsing received id_token");
        GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
                new NetHttpTransport(),
                new GsonFactory())
                .setAudience(Collections.singletonList("843919078944-ej9pr652l5bamh29nsdoecmdml5d6fvb.apps.googleusercontent.com"))
                .build();

        GoogleIdToken gidToken;
        JwtDecoder jwtDecoder = this.jwtDecoderFactory.createDecoder(clientRegistration);
        Jwt jwt;
        try
        {
            gidToken = verifier.verify(idTokenString); // verify google token before proceeding.
            if (gidToken == null)
            {
                OAuth2Error invalidIdTokenError = new OAuth2Error(INVALID_ID_TOKEN_ERROR_CODE, "Invalid Google Credentials", null);
                throw new OAuth2AuthenticationException(invalidIdTokenError, invalidIdTokenError.toString());
            }
            jwt = jwtDecoder.decode(idTokenString);
        }
        catch (GeneralSecurityException e)
        {
            OAuth2Error invalidIdTokenError = new OAuth2Error(INVALID_ID_TOKEN_ERROR_CODE, e.getMessage(), null);
            throw new OAuth2AuthenticationException(invalidIdTokenError, invalidIdTokenError.toString(), e);
        }

        OidcIdToken idToken = new OidcIdToken(jwt.getTokenValue(), jwt.getIssuedAt(), jwt.getExpiresAt(), jwt.getClaims());

        logger.info("Creating userinfo");
        OidcUserInfo userInfo = new OidcUserInfo(gidToken.getPayload());

        logger.info("Setting authorities");
        Set<GrantedAuthority> authorities = new LinkedHashSet<>();
        authorities.add(new OidcUserAuthority(idToken, userInfo));

        String usernameAttribute = clientRegistration.getProviderDetails().getUserInfoEndpoint().getUserNameAttributeName();
        logger.info("Username Attribute: {}", usernameAttribute);

        logger.info("Create OidcUser");
        OidcUser oidcUser = new DefaultOidcUser(authorities, idToken, userInfo, usernameAttribute);

        logger.info("Create authResult");
        OAuth2AuthenticationToken authResult = new OAuth2AuthenticationToken(oidcUser, authorities, clientRegistration.getRegistrationId());

        logger.info("Setting Authentication");
        SecurityContextHolder.getContext().setAuthentication(authResult);
        onSuccessfulAuthentication(request, response, authResult);
//        filterChain.doFilter(request, response);
    }

    protected void onSuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response, Authentication authResult) throws IOException {

        response.setStatus(HttpStatus.OK.value());
    }

}
