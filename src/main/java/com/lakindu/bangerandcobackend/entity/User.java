package com.lakindu.bangerandcobackend.entity;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class User {
    @Id
    private String emailAddress;
}
