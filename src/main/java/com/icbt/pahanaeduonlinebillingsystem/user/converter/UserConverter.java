package com.icbt.pahanaeduonlinebillingsystem.user.converter;

import com.icbt.pahanaeduonlinebillingsystem.user.dto.UserDTO;
import com.icbt.pahanaeduonlinebillingsystem.user.entity.UserEntity;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Thisara Kavishka
 * @date 2025-07-30
 * @since 1.0
 */
public class UserConverter {

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
}
