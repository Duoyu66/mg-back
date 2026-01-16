package com.example.mg.dao.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.mg.common.PageData;
import com.example.mg.common.R;
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

    @Override
    public R<UserDTO> findById(String id){

        // 查询用户实体
        UserEntity entity = userMapper.selectById(id);

        // 如果用户不存在，返回null或抛出异常
        if (entity == null) {
            return R.success("用户不存在，ID:@ " + id,null);
        }

        // 转换为DTO
        return R.success(UserDTO.builder()
                .id(entity.getId())
                .username(entity.getUsername())
                .email(entity.getEmail())
                .build());
    }

    @Override
    public R<PageData<UserDTO>> getUserByPage(int page, int pageSize) {
        // 创建分页对象
        Page<UserEntity> pageParam = Page.of(page, pageSize);

        // 执行分页查询
        Page<UserEntity> entityPage = userMapper.selectPage(pageParam, null);

        // 将Entity转换为DTO
        List<UserDTO> userDTOs = entityPage.getRecords().stream()
                .map(entity -> UserDTO.builder()
                        .id(entity.getId())
                        .username(entity.getUsername())
                        .email(entity.getEmail())
                        .build())
                .collect(Collectors.toList());

        // 使用转换后的DTO列表创建分页数据
        return R.page(userDTOs, entityPage.getTotal(), page, pageSize);
    }

    @Override
    public UserDTO findByUsername(String username) {
        QueryWrapper<UserEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", username);
        UserEntity entity = userMapper.selectOne(queryWrapper);
        
        if (entity == null) {
            return null;
        }
        
        return UserDTO.builder()
                .id(entity.getId())
                .username(entity.getUsername())
                .email(entity.getEmail())
                .build();
    }

    @Override
    public void save(UserEntity user) {
        if (user.getId() == null) {
            userMapper.insert(user);
        } else {
            userMapper.updateById(user);
        }
    }

    @Override
    public UserEntity getUserEntityById(String id) {
        return userMapper.selectById(id);
    }
}

