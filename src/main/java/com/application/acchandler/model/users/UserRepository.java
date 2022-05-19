package com.application.acchandler.model.users;

import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends CrudRepository<Users, Integer> {

    @Query(value = "select * from users where phone_number= ?", nativeQuery = true)
    Users findByPhoneNumber(String phoneNumber);

    @Query(value = "select * from users where phone_number= ? and user_id != ?", nativeQuery = true)
    Users findByPhoneNumberForDuplicationCheck(String phoneNumber, Integer userId);

    @Query(value = "select * from users where email_id= ?", nativeQuery = true)
    Users findByEmail(String email);

    @Query(value = "select * from users where email_id= ? and user_id != ?", nativeQuery = true)
    Users findByEmailIdForDuplicationCheck(String email, Integer userId);

    @Query(value = "select * from users where google_id= ?", nativeQuery = true)
    Users findByGoogleId(String googleId);
}
