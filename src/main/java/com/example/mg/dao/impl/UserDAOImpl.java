package com.example.mg.dao.impl;

import com.example.mg.dao.UserDAO;
import com.example.mg.dto.UserDTO;
import com.example.mg.entity.UserEntity;
import com.example.mg.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.stream.Collectors;

/**
 * UserDAO 默认实现
 */
@Repository
@RequiredArgsConstructor
public class UserDAOImpl implements UserDAO {

    private final UserMapper userMapper;

    @Override
    public List<UserDTO> findAll() {
        List<UserEntity> entities = userMapper.selectList(null);
        return entities.stream()
                .map(entity -> UserDTO.builder()
                        .id(entity.getId())
                        .username(entity.getUsername())
                        .email(entity.getEmail())
                        .build())
                .collect(Collectors.toList());
    }
}

