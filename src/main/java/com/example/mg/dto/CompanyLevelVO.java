package com.example.mg.dto;

import com.example.mg.entity.CompanyEntity;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class CompanyLevelVO {
    private int level;
    private List<CompanyEntity> companies;
}
