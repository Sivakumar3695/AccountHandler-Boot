package com.application.acchandler.model;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends CrudRepository<Users, Integer> {

    @Query(value = "select * from users where phone_number= ?1", nativeQuery = true)
    Users findByPhoneNumber(String phoneNumber);
}
