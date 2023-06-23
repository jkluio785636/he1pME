package org.yolok.he1pME.repository;


import org.socialsignin.spring.data.dynamodb.repository.EnableScan;
import org.springframework.data.repository.CrudRepository;
import org.yolok.he1pME.entity.TwitchNotification;

@EnableScan
public interface TwitchNotificationRepository extends CrudRepository<TwitchNotification, String> {

}
