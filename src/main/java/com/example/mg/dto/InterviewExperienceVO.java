package com.example.mg.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InterviewExperienceVO {
    private String id;
    private String title;
    private String vip;
    private LocalDateTime createTime;
}
