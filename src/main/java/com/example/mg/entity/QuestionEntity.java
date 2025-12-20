package com.example.mg.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("question")
public class QuestionEntity {
    @TableId
    private String id;
    private String questionId;
    private String output;
    private String input;
}
