package com.open.su;

import io.quarkus.hibernate.reactive.panache.PanacheEntityBase;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.util.Date;
import java.util.UUID;

@MappedSuperclass
public class Entity extends PanacheEntityBase {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    UUID id;
    @Column(name = "created_at")
    @CreationTimestamp
    Date createdAt;
    @Column(name = "updated_at")
    @UpdateTimestamp
    Date updatedAt;
}
