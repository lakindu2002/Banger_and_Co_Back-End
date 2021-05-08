package com.lakindu.bangerandcobackend.entity;

import com.lakindu.bangerandcobackend.dto.UserDTO;

import javax.persistence.*;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.sql.Date;
import java.util.Arrays;

@Entity
@Table(name = "user_info")
public class User {
    @Id
    @Column(name = "username")
    private String username;

    @Column(name = "email_address")
    private String emailAddress;

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Column(name = "date_of_birth", nullable = false)
    private Date dateOfBirth;

    @Column(name = "user_password", nullable = false)
    private String userPassword;

    @Column(name = "contact_number", nullable = false, length = 20)
    private String contactNumber;

    @Lob //large object
    @Column(name = "profile_picture", columnDefinition = "MEDIUMBLOB")
    private byte[] profilePicture;

    @Column(name = "is_black_listed", nullable = false)
    private boolean isBlackListed;

    @ManyToOne(cascade = {CascadeType.DETACH, CascadeType.REFRESH})
    @JoinColumn(name = "role_id", nullable = false)
    private Role userRole;

    public User() {
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
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

    @Override
    public String toString() {
        return "User{" +
                "emailAddress='" + emailAddress + '\'' +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", dateOfBirth=" + dateOfBirth +
                ", userPassword='" + userPassword + '\'' +
                ", contactNumber='" + contactNumber + '\'' +
                ", profilePicture=" + Arrays.toString(profilePicture) +
                ", isBlackListed=" + isBlackListed +
                ", userRole=" + userRole +
                '}';
    }

    public static User convertDTOToEntity(UserDTO theUserDTO, Role theRole) {
        User theUser = new User();
        theUser.setBlackListed(theUserDTO.isBlackListed());
        if (theRole != null) {
            theUser.setUserRole(theRole);
        }
        if (theUserDTO.getUsername() != null) {
            theUser.setUsername(theUserDTO.getUsername());
        }
        if (theUserDTO.getFirstName() != null) {
            theUser.setFirstName(theUserDTO.getFirstName());
        }
        if (theUserDTO.getLastName() != null) {
            theUser.setLastName(theUserDTO.getLastName());
        }
        if (theUserDTO.getUserPassword() != null) {
            theUser.setUserPassword(theUserDTO.getUserPassword());
        }
        if (theUserDTO.getContactNumber() != null) {
            theUser.setContactNumber(theUserDTO.getContactNumber());
        }
        if (theUserDTO.getProfilePicture() != null) {
            theUser.setProfilePicture(theUserDTO.getProfilePicture());
        }
        if (theUserDTO.getEmailAddress() != null) {
            theUser.setEmailAddress(theUserDTO.getEmailAddress());
        }
        if (theUserDTO.getDateOfBirth() != null) {
            theUser.setDateOfBirth(theUserDTO.getDateOfBirth());
        }

        return theUser;
    }
}
