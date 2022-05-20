# AccountHandler-Boot
A simple application with
1. Basic User Account handling
2. Custom Securtiy configuration written on top of Spring Security


# Basic User Account Handling
1. Facilitates easy sign in with Phone number and OTP option.
2. Easy login with "sign-in with  Google option". Also, login page will load with customized sign-in button based on the previous logged in Google account due to the Google One-Tap sign-in implementation.
3. Onboards users quickly without pesting them to provide details that they can update later.
4. Multiple session handling with all-in-one place. Users can handle remote sessions easily by viewing their IP address, location and terminating sessions from remote devices.

 
# What to expect in the future?
1. More simplified sign-in options:
  a. Sign-in with Facebook
2. We are on path to couple this App as an Account Handler and User Session management in our multi-app microservice architecture. Hence, we will soon provide REST API options to show an user with the list of Application that he/she uses and the list of other applications that they can expore.
3. Implementing OTP handling to verify user's mobile number and email address which will improve the app's security and reliability. Currently, due to the lack of OTP verification, we don't recomment users to provide sensitive information. However, in order to test the application, use random mobile number (for instance, 9876543210) and the default OTP (1234).
4. **IMPORTANT FEATURE**: Our roadmap includes a critical feature to facilitate the utilization the logged-in session in AccountsHandler across all other applications from our app bundle. Moreover, further extensions of this work might focus on providing Identity service to other external applications also.
5. Click [here](https://github.com/Sivakumar3695/AccountHandler-Boot/projects/1#column-18741064) to check our next set of features, enhancements, nice-to-have(s), issue-fixes and more.

# How to run this application?
1. Download and extract this file to src/main/resources
2. Use [this](https://github.com/Sivakumar3695/AccountHandler-Boot/files/8732003/application-dev.txt) application-dev.properties file and update property fields according to the development environment.
3. Build Command: _mvn package -Dspring.profiles.active=dev_
4. Run command: _java -Dspring.profiles.active=dev -jar target/*.jar_
