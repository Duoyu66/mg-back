package com.example.mg.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.mg.common.R;
import com.example.mg.dto.CompanyLevelVO;
import com.example.mg.dto.InterviewExperienceVO;
import com.example.mg.entity.CompanyEntity;
import com.example.mg.entity.InterviewExperienceEntity;
import com.example.mg.mapper.CompanyMapper;
import com.example.mg.mapper.InterviewExperienceMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.web.bind.annotation.RequestParam;
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
    private final InterviewExperienceMapper interviewExperienceMapper;

    @Operation(summary = "获取公司列表（按等级分组）")
    @GetMapping("/list")
    public R<List<CompanyLevelVO>> list() {
        List<CompanyEntity> all = companyMapper.selectList(new LambdaQueryWrapper<CompanyEntity>()
                .eq(CompanyEntity::getType, "target"));
        
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

    @Operation(summary = "获取该公司的面经记录")
    @GetMapping("/experiences")
    public R<List<InterviewExperienceVO>> getExperiences(@Parameter(description = "公司ID") @RequestParam String companyId) {
        List<InterviewExperienceEntity> list = interviewExperienceMapper.selectList(new LambdaQueryWrapper<InterviewExperienceEntity>()
                .select(InterviewExperienceEntity::getId,InterviewExperienceEntity::getVip, InterviewExperienceEntity::getTitle, InterviewExperienceEntity::getCreateTime)
                .eq(InterviewExperienceEntity::getCompanyId, companyId)
                .isNull(InterviewExperienceEntity::getUserId));
        
        List<InterviewExperienceVO> result = list.stream()
                .map(entity -> InterviewExperienceVO.builder()
                        .id(entity.getId())
                        .title(entity.getTitle())
                        .vip(entity.getVip())
                        .createTime(entity.getCreateTime())
                        .build())
                .collect(Collectors.toList());

        return R.success(result);
    }
    @Operation(summary = "获取面经记录详情")
    @GetMapping("/getExperience")
    public R<InterviewExperienceEntity> getExperience(@Parameter(description = "记录ID") @RequestParam String id) {
        InterviewExperienceEntity experience = interviewExperienceMapper.selectById(id);
        if (experience == null) {
            return R.failed("面经记录不存在");
        }
        return R.success(experience);
    }
    //获取该用户的面试记录通过解析token获取userId
}
