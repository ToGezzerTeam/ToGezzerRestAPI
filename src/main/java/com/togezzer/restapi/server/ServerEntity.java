package com.togezzer.restapi.server;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "servers")
public class ServerEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, updatable = false)
    private UUID uuid;

    @Column(name = "name", nullable = false, length = 255)
    @Size(min = 1, max = 255)
    private String name;

    @Column(name = "createdAt", nullable = false)
    private Instant createdAt;

    @Column(name = "createdBy", nullable = false, updatable = false)
    private String createdBy;

    @Column(name = "isPublic", nullable = false, updatable = false)
    private boolean isPublic;

    @Column(name = "logo", nullable = false, updatable = false)
    private String logo;

    @Column(name = "background", nullable = false, updatable = false)
    private String background;
}
