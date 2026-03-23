package com.togezzer.restapi.room_users;

import com.togezzer.restapi.room.RoomEntity;
import com.togezzer.restapi.user.UserEntity;
import jakarta.persistence.*;
import lombok.*;

@Setter
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Entity
@Table(name = "room_users")
public class RoomUserEntity {

    @EmbeddedId
    private RoomUserId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("roomId")
    @JoinColumn(name = "room_id")
    private RoomEntity room;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("userId")
    @JoinColumn(name = "user_id")
    private UserEntity user;
}
