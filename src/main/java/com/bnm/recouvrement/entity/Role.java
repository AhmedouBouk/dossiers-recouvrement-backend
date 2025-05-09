package com.bnm.recouvrement.entity;

import java.util.*;

import jakarta.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "roles")
public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String name;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "role_permissions",
        joinColumns = @JoinColumn(name = "role_id"),
        inverseJoinColumns = @JoinColumn(name = "permission_id")
    )
    private Set<Permission> permissions = new HashSet<>();

    @Column(nullable = false, columnDefinition = "BOOLEAN DEFAULT TRUE")
    private boolean active = true;

    public Role() {
    }

    public Role(Long id, String name, Set<Permission> permissions, boolean active) {
        this.id = id;
        this.name = name;
        this.permissions = permissions;
        this.active = active;
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Set<Permission> getPermissions() {
        return this.permissions;
    }

    public void setPermissions(Set<Permission> permissions) {
        this.permissions = permissions;
    }

    public boolean isActive() {
        return this.active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public Role id(Long id) {
        setId(id);
        return this;
    }

    public Role name(String name) {
        setName(name);
        return this;
    }

    public Role permissions(Set<Permission> permissions) {
        setPermissions(permissions);
        return this;
    }

    public Role active(boolean active) {
        setActive(active);
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this)
            return true;
        if (!(o instanceof Role)) {
            return false;
        }
        Role role = (Role) o;
        return Objects.equals(id, role.id) && Objects.equals(name, role.name) && Objects.equals(permissions, role.permissions) && Objects.equals(active, role.active);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, permissions, active);
    }

    @Override
    public String toString() {
        return "{" +
            " id='" + getId() + "'" +
            ", name='" + getName() + "'" +
            ", permissions='" + getPermissions() + "'" +
            ", active='" + isActive() + "'" +
            "}";
    }
}
