package com.icbt.pahanaeduonlinebillingsystem.user.converter;

import com.icbt.pahanaeduonlinebillingsystem.common.constant.Role;
import com.icbt.pahanaeduonlinebillingsystem.user.dto.UserDTO;
import com.icbt.pahanaeduonlinebillingsystem.user.entity.UserEntity;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Thisara Kavishka
 * @date 2025-07-30
 * @since 1.0
 */
public class UserMapper {

    public static UserEntity toEntity(UserDTO userDTO) {

        if (userDTO == null) {
            return null;
        }

        UserEntity userEntity = new UserEntity();
        userEntity.setId(userDTO.getId());
        userEntity.setUsername(userDTO.getUsername());
        userEntity.setPassword(userDTO.getPassword());
        userEntity.setSalt(userDTO.getSalt());
        userEntity.setRole(userDTO.getRole());

        userEntity.setCreatedBy(userDTO.getCreatedBy());
        userEntity.setCreatedAt(userDTO.getCreatedAt());
        userEntity.setUpdatedBy(userDTO.getUpdatedBy());
        userEntity.setUpdatedAt(userDTO.getUpdatedAt());
        userEntity.setDeletedBy(userDTO.getDeletedBy());
        userEntity.setDeletedAt(userDTO.getDeletedAt());

        return userEntity;
    }

    public static UserDTO toDto(UserEntity userEntity) {

        if (userEntity == null) {
            return null;
        }

        UserDTO userDTO = new UserDTO();
        userDTO.setId(userEntity.getId());
        userDTO.setUsername(userEntity.getUsername());
        userDTO.setPassword(userEntity.getPassword());
        userDTO.setSalt(userEntity.getSalt());
        userDTO.setRole(userEntity.getRole());

        userDTO.setCreatedBy(userEntity.getCreatedBy());
        userDTO.setCreatedAt(userEntity.getCreatedAt());
        userDTO.setUpdatedBy(userEntity.getUpdatedBy());
        userDTO.setUpdatedAt(userEntity.getUpdatedAt());
        userDTO.setDeletedBy(userEntity.getDeletedBy());
        userDTO.setDeletedAt(userEntity.getDeletedAt());

        return userDTO;
    }

    public static List<UserDTO> toDTOList(List<UserEntity> entities) {
        List<UserDTO> dtos = new ArrayList<>();
        if (entities != null) {
            for (UserEntity userEntity : entities) {
                dtos.add(toDto(userEntity));
            }
        }
        return dtos;
    }

    public static Map<String, Object> toMap(UserDTO dto, String createdByUsername, String updatedByUsername, String deletedByUsername) {
        if (dto == null) return null;
        Map<String, Object> map = new HashMap<>();
        map.put("id", dto.getId());
        map.put("username", dto.getUsername());
        map.put("role", dto.getRole().name()); // Send role as String
        // Use provided usernames, fallback to ID if username not found, or '-' if null
        map.put("createdBy", createdByUsername != null ? createdByUsername : (dto.getCreatedBy() != null ? String.valueOf(dto.getCreatedBy()) : "-"));
        map.put("createdAt", dto.getCreatedAt());
        map.put("updatedBy", updatedByUsername != null ? updatedByUsername : (dto.getUpdatedBy() != null ? String.valueOf(dto.getUpdatedBy()) : "-"));
        map.put("updatedAt", dto.getUpdatedAt());
        map.put("deletedBy", deletedByUsername != null ? deletedByUsername : (dto.getDeletedBy() != null ? String.valueOf(dto.getDeletedBy()) : "-"));
        map.put("deletedAt", dto.getDeletedAt());
        return map;
    }

    public static UserEntity mapResultSetToUserEntity(ResultSet rs) throws SQLException {
        UserEntity user = new UserEntity();
        user.setId(rs.getInt("id"));
        user.setUsername(rs.getString("username"));
        user.setPassword(rs.getString("password")); // Hashed password
        user.setSalt(rs.getString("salt"));
        user.setRole(Role.valueOf(rs.getString("role"))); // Convert String to Enum

        // Audit fields
        user.setCreatedBy(rs.getObject("created_by", Integer.class)); // Handles NULL
        user.setCreatedAt(rs.getTimestamp("created_at"));
        user.setUpdatedBy(rs.getObject("updated_by", Integer.class));
        user.setUpdatedAt(rs.getTimestamp("updated_at"));
        user.setDeletedBy(rs.getObject("deleted_by", Integer.class));
        user.setDeletedAt(rs.getTimestamp("deleted_at"));
        return user;
    }
}
