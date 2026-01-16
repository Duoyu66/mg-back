package com.example.mg.mapper;

import com.example.mg.dto.IdCount;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface PostLikeMapper {
    @Select({
            "<script>",
            "SELECT post_id AS postId, COUNT(*) AS cnt ",
            "FROM post_like ",
            "WHERE post_id IN ",
            "<foreach item='id' collection='ids' open='(' separator=',' close=')'>",
            "#{id}",
            "</foreach>",
            "GROUP BY post_id",
            "</script>"
    })
    List<IdCount> countByPostIds(@Param("ids") List<String> ids);
}
