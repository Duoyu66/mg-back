    package com.example.mg.controller;

    import cn.hutool.core.util.IdUtil;
    import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
    import com.example.mg.common.PageData;
    import com.example.mg.common.R;
    import com.example.mg.dto.PostVO;
    import com.example.mg.dto.IdCount;
    import com.example.mg.entity.PostEntity;
    import com.example.mg.entity.UserEntity;
    import com.example.mg.mapper.PostMapper;
    import com.example.mg.mapper.PostLikeMapper;
    import com.example.mg.mapper.PostCommentMapper;
    import com.example.mg.mapper.UserMapper;
    import io.swagger.v3.oas.annotations.Operation;
    import io.swagger.v3.oas.annotations.tags.Tag;
    import lombok.RequiredArgsConstructor;
    import org.springframework.web.bind.annotation.*;

    import com.example.mg.utils.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;

@Tag(name = "帖子", description = "帖子相关接口")
@RestController
@RequestMapping("/post")
@RequiredArgsConstructor
public class PostController {
    private final PostMapper postMapper;
    private final PostLikeMapper postLikeMapper;
    private final PostCommentMapper postCommentMapper;
    private final UserMapper userMapper;
    private final JwtUtil jwtUtil;
        @Operation(summary = "分页获取帖子列表")
        @GetMapping("/getAll")
        public R<PageData<PostVO>> getAll(@RequestParam(defaultValue = "1") int page,
                                          @RequestParam(defaultValue = "10") int pageSize) {
            Page<PostEntity> p = Page.of(page, pageSize);
            LambdaQueryWrapper<PostEntity> wrapper = new LambdaQueryWrapper<>();
            wrapper.orderByDesc(PostEntity::getCreateTime);
            Page<PostEntity> result = postMapper.selectPage(p, wrapper);
            java.util.List<PostEntity> records = result.getRecords();
            java.util.List<String> ids = new java.util.ArrayList<>();
            for (PostEntity e : records) ids.add(e.getId());
            java.util.Map<String, Integer> likeMap = new java.util.HashMap<>();
            java.util.Map<String, Integer> commentMap = new java.util.HashMap<>();
            if (!ids.isEmpty()) {
                java.util.List<IdCount> likeAgg = postLikeMapper.countByPostIds(ids);
                java.util.List<IdCount> commentAgg = postCommentMapper.countByPostIds(ids);
                for (IdCount ic : likeAgg) likeMap.put(ic.getPostId(), ic.getCnt() == null ? 0 : ic.getCnt());
                for (IdCount ic : commentAgg) commentMap.put(ic.getPostId(), ic.getCnt() == null ? 0 : ic.getCnt());
            }
            java.util.Set<String> userIdSet = new java.util.HashSet<>();
            for (PostEntity e : records) if (e.getUserId() != null) userIdSet.add(e.getUserId());
            java.util.Map<String, UserEntity> userMap = new java.util.HashMap<>();
            if (!userIdSet.isEmpty()) {
                java.util.List<UserEntity> users = userMapper.selectBatchIds(new java.util.ArrayList<>(userIdSet));
                for (UserEntity u : users) userMap.put(u.getId(), u);
            }
            java.util.List<PostVO> vos = new java.util.ArrayList<>(records.size());
            for (PostEntity e : records) {
                PostVO vo = new PostVO();
                vo.setId(e.getId());
                vo.setTitle(e.getTitle());
                vo.setContent(e.getContent());
                vo.setUserId(e.getUserId());
                vo.setIpAddress(e.getIpAddress());
                vo.setView(e.getView());
                vo.setCreateTime(e.getCreateTime());
                vo.setUpdateTime(e.getUpdateTime());
                vo.setLikeCount(likeMap.getOrDefault(e.getId(), 0));
                vo.setCommentCount(commentMap.getOrDefault(e.getId(), 0));
                UserEntity u = userMap.get(e.getUserId());
                if (u != null) {
                    vo.setAvatar(u.getAvatar());
                    vo.setSchool(u.getSchool());
                    vo.setSignature(u.getSignature());
                    vo.setNickname(u.getNickName());
                    vo.setVipType(u.getVipType());
                }
                vos.add(vo);
            }
            return R.page(vos, result.getTotal());
        }
        //发布帖子
        @Operation(summary = "发布帖子")
    @PostMapping("/sendPost")
    public R<Boolean> sendPost(@RequestBody PostVO vo, HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token == null || !token.startsWith("Bearer ")) {
            return R.failed("未登录");
        }
        token = token.substring(7);
        String userId = jwtUtil.getUserIdFromToken(token);

        String id = Long.toString(IdUtil.getSnowflakeNextId());
        
        PostEntity entity = new PostEntity();
        entity.setId(id);
        entity.setContent(vo.getContent());
        entity.setUserId(userId);
        
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty()) ip = request.getRemoteAddr();
        entity.setIpAddress(ip);
        
        entity.setView(0);
        // entity.setCreateTime(LocalDateTime.now());
        // entity.setUpdateTime(LocalDateTime.now());

        // 6. 保存到数据库
        postMapper.insert(entity);

        return R.success(true);
    }
    }
