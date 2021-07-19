package com.lakindu.bangerandcobackend.entity;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name = "user_role")
public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "role_id")
    private int roleId;

    @Column(nullable = false, name = "role_name", length = 50)
    private String roleName;

    @OneToMany(mappedBy = "userRole", fetch = FetchType.LAZY, cascade = {CascadeType.DETACH, CascadeType.REFRESH})
    private List<User> usersInEachRole;

    public Role() {
    }

    public List<User> getUsersInEachRole() {
        return usersInEachRole;
    }

    public void setUsersInEachRole(List<User> usersInEachRole) {
        this.usersInEachRole = usersInEachRole;
    }

    public int getRoleId() {
        return roleId;
    }

    public void setRoleId(int roleId) {
        this.roleId = roleId;
    }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    @Override
    public String toString() {
        return "Role{" +
                "roleId=" + roleId +
                ", roleName='" + roleName + '\'' +
                '}';
    }
}
