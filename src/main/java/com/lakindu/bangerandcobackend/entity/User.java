package com.lakindu.bangerandcobackend.entity;

import javax.persistence.*;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import java.sql.Date;

@Entity
@Table(name = "user_info")
public class User {
    @Id
    @Email(message = "Please Provide a Valid Email Address")
    @NotBlank(message = "Provide a Valid Email Address")
    @Column(name = "email_address")
    private String emailAddress;

    @NotBlank(message = "Please Provide a Valid First Name")
    @Column(name = "first_name", nullable = false)
    private String firstName;

    @NotBlank(message = "Please Provide a Valid Last Name")
    @Column(name = "last_name", nullable = false)
    private String lastName;

    @NotBlank(message = "Please Provide a Valid Date of Birth")
    @Column(name = "date_of_birth", nullable = false)
    private Date dateOfBirth;

    @NotBlank(message = "Please Provide a Valid Password")
    @Column(name = "user_password", nullable = false)
    private String userPassword;

    @NotBlank(message = "Please Provide a Valid Contact Number")
    @Column(name = "contact_number", nullable = false, length = 20)
    private String contactNumber;

    @Column(name = "profile_picture")
    private byte[] profilePicture;

    @Column(name = "is_black_listed", nullable = false)
    private boolean isBlackListed;

    @ManyToOne(cascade = {CascadeType.DETACH, CascadeType.MERGE, CascadeType.REFRESH})
    @JoinColumn(name = "role_id", nullable = false)
    private Role userRole;

    public User() {
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public Date getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(Date dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getUserPassword() {
        return userPassword;
    }

    public void setUserPassword(String userPassword) {
        this.userPassword = userPassword;
    }

    public String getContactNumber() {
        return contactNumber;
    }

    public void setContactNumber(String contactNumber) {
        this.contactNumber = contactNumber;
    }

    public byte[] getProfilePicture() {
        return profilePicture;
    }

    public void setProfilePicture(byte[] profilePicture) {
        this.profilePicture = profilePicture;
    }

    public boolean isBlackListed() {
        return isBlackListed;
    }

    public void setBlackListed(boolean blackListed) {
        isBlackListed = blackListed;
    }

    public Role getUserRole() {
        return userRole;
    }

    public void setUserRole(Role userRole) {
        this.userRole = userRole;
    }
}
