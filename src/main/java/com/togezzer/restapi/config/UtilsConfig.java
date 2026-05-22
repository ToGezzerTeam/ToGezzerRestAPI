package com.togezzer.restapi.config;

import com.togezzer.restapi.message.service.MessageUtils;
import com.togezzer.restapi.room.RoomRepository;
import com.togezzer.restapi.room_users.RoomUserRepository;
import com.togezzer.restapi.user.UserRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UtilsConfig {
    @Bean
    public MessageUtils messageUtils(RoomRepository roomRepository, UserRepository userRepository, RoomUserRepository roomUserRepository) {
        return new MessageUtils(roomRepository, userRepository, roomUserRepository);
    }
}
