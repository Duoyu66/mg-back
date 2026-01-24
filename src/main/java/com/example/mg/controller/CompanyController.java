package com.example.mg.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.mg.common.R;
import com.example.mg.dto.CompanyLevelVO;
import com.example.mg.entity.CompanyEntity;
import com.example.mg.mapper.CompanyMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;
import java.util.stream.Collectors;

@Tag(name = "公司", description = "公司相关接口")
@RestController
@RequestMapping("/company")
@RequiredArgsConstructor
public class CompanyController {

    private final CompanyMapper companyMapper;

    @Operation(summary = "获取公司列表（按等级分组）")
    @GetMapping("/list")
    public R<List<CompanyLevelVO>> list() {
        List<CompanyEntity> all = companyMapper.selectList(null);
        
        Map<Integer, List<CompanyEntity>> grouped = all.stream()
                .collect(Collectors.groupingBy(CompanyEntity::getLevel));
        
        List<Integer> sortedKeys = new ArrayList<>(grouped.keySet());
        sortedKeys.sort(Integer::compareTo);

        List<CompanyLevelVO> result = new ArrayList<>();
        for (Integer level : sortedKeys) {
            result.add(CompanyLevelVO.builder()
                    .level(level)
                    .companies(grouped.get(level))
                    .build());
        }
        
        return R.success(result);
    }
}
