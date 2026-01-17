package com.xiaowang.xwpicturebackend.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xiaowang.xwpicturebackend.exception.BusinessException;
import com.xiaowang.xwpicturebackend.exception.ErrorCode;
import com.xiaowang.xwpicturebackend.exception.ThrowUtils;
import com.xiaowang.xwpicturebackend.model.dto.space.SpaceAddRequest;
import com.xiaowang.xwpicturebackend.model.dto.space.SpaceQueryRequest;
import com.xiaowang.xwpicturebackend.model.entity.Picture;
import com.xiaowang.xwpicturebackend.model.entity.Space;
import com.xiaowang.xwpicturebackend.model.entity.User;
import com.xiaowang.xwpicturebackend.model.enums.SpaceLevelEnum;
import com.xiaowang.xwpicturebackend.model.vo.PictureVO;
import com.xiaowang.xwpicturebackend.model.vo.SpaceVO;
import com.xiaowang.xwpicturebackend.model.vo.UserVO;
import com.xiaowang.xwpicturebackend.service.UserService;
import com.xiaowang.xwpicturebackend.service.SpaceService;
import com.xiaowang.xwpicturebackend.mapper.SpaceMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

/**
 * @author wangjialeNB
 * @description 针对表【space(空间)】的数据库操作Service实现
 * @createDate 2026-01-17 11:24:31
 */
@Service
public class SpaceServiceImpl extends ServiceImpl<SpaceMapper, Space> implements SpaceService {

    private final UserService userService;
    private final TransactionTemplate transactionTemplate;

    public SpaceServiceImpl(UserService userService, TransactionTemplate transactionTemplate) {
        this.userService = userService;
        this.transactionTemplate = transactionTemplate;
    }

    // 每个用户只能创建一个空间，用锁来实现
    private final Map<String, Lock> userLockMap = new ConcurrentHashMap<>();

    @Override
    public long addSpace(SpaceAddRequest spaceAddRequest, User loginUser) {
        // 填充参数
        Space space = new Space();
        BeanUtil.copyProperties(spaceAddRequest, space);
        if (StrUtil.isBlank(space.getSpaceName())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "空间名称不能为空");
        }
        if (space.getSpaceLevel() == null) {
            space.setSpaceLevel(SpaceLevelEnum.COMMON.getValue());
        }
        this.fillSpaceBySpaceLevel(space);
        // 校验参数
        this.validSpace(space, true);
        // 校验权限，普通用户只能创建普通空间
        long userId = loginUser.getId();
        space.setUserId(userId);
        if (SpaceLevelEnum.COMMON.getValue().equals(space.getSpaceLevel()) && !userService.isAdmin(loginUser)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权限创建指定级别的空间");
        }
        // 每个用户只能创建一个空间
        // 给每个用户加一把锁
        String userIdStr = String.valueOf(userId);
        Lock userLock = userLockMap.computeIfAbsent(userIdStr, k -> new ReentrantLock());
        userLock.lock();
        try {
            // 开启事务
            Long newSpaceId = transactionTemplate.execute(status -> {
                // 判断用户是否已经创建了空间
                boolean exists = this.lambdaQuery().eq(Space::getUserId, userId).exists();
                ThrowUtils.throwIf(exists, ErrorCode.OPERATION_ERROR, "每个用户只能创建一个空间");
                // 创建空间
                boolean result = this.save(space);
                ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR, "创建空间失败");
                return space.getId();
            });
            // 事务提交失败，返回-1
            return Optional.ofNullable(newSpaceId).orElse(-1L);
        } finally {
            userLock.unlock();
            userLockMap.remove(userIdStr);
        }
    }

    /**
     * 校验空间是否合法
     *
     * @param space 空间对象
     * @param isAdd 是否为新增操作
     */
    @Override
    public void validSpace(Space space, boolean isAdd) {
        ThrowUtils.throwIf(space == null, ErrorCode.PARAMS_ERROR, "空间不能为空");
        String spaceName = space.getSpaceName();
        Integer spaceLevel = space.getSpaceLevel();
        SpaceLevelEnum spaceLevelEnum = SpaceLevelEnum.getEnumByValue(spaceLevel);
        // 新增空间的时候，要做一个参数校验
        if (isAdd) {
            ThrowUtils.throwIf(StrUtil.isBlank(spaceName), ErrorCode.PARAMS_ERROR, "空间名称不能为空");
            ThrowUtils.throwIf(spaceLevelEnum == null, ErrorCode.PARAMS_ERROR, "空间等级不存在");
        }
        // 修改空间的时候，要做一个参数校验
        if (StrUtil.isNotBlank(spaceName) && spaceName.length() > 30) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "空间名称长度不能超过30");
        }
        if (spaceLevel != null && spaceLevelEnum == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "空间等级不存在");
        }

    }

    /**
     * 获取空间VO
     *
     * @param space   空间实体对象
     * @param request HTTP请求对象（用于获取当前登录用户信息）
     * @return 空间VO对象，包含空间实体属性和关联用户信息
     */
    @Override
    public SpaceVO getSpaceVO(Space space, HttpServletRequest request) {
        // 转换封装类
        SpaceVO spaceVO = SpaceVO.objToVo(space);
        // 关联查询用户信息
        Long userId = space.getUserId();
        if (userId != null && userId > 0) {
            User user = userService.getById(userId);
            UserVO userVO = userService.getUserVO(user);
            spaceVO.setUser(userVO);
        }
        return spaceVO;
    }

    /**
     * 获取分页后的空间VO列表
     *
     * @param spacePage 包含Space实体对象的分页对象（MyBatis Plus提供）
     * @param request   HTTP请求对象（用于获取当前登录用户信息）
     * @return 包含SpaceVO对象的分页对象，包含空间实体属性和关联用户信息
     */
    @Override
    public Page<SpaceVO> getSpaceVOPage(Page<Space> spacePage, HttpServletRequest request) {
        // 1. 获取Space分页对象中的具体数据列表
        List<Space> spaceList = spacePage.getRecords();

        // 2. 初始化SpaceVO分页对象，复用原分页的当前页、每页条数、总记录数（保证分页参数一致性）
        Page<SpaceVO> spaceVOPage = new Page<>(spacePage.getCurrent(), spacePage.getSize(), spacePage.getTotal());

        // 3. 空值校验：如果Space列表为空，直接返回空的VO分页对象（避免后续空指针）
        if (CollUtil.isEmpty(spaceList)) {
            return spaceVOPage;
        }

        // 4. 将Space实体列表转换为SpaceVO列表（基础属性转换）
        List<SpaceVO> spaceVOList = spaceList.stream().map(SpaceVO::objToVo)  // 调用SpaceVO的静态方法完成单个对象属性映射
                .collect(Collectors.toList());

        // 5. 提取所有Space关联的用户ID（去重），用于批量查询用户信息（减少数据库查询次数）
        Set<Long> userIds = spaceList.stream().map(Space::getUserId).collect(Collectors.toSet());

        // 6. 批量查询用户信息，并按用户ID分组（key：用户ID，value：对应用户列表）
        //    注：groupingBy保证同一个用户ID的用户数据聚合，此处取第一个是因为用户ID唯一
        Map<Long, List<User>> userIdUserListMap = userService.listByIds(userIds).stream().collect(Collectors.groupingBy(User::getId));

        // 7. 遍历SpaceVO列表，为每个VO填充关联的用户VO信息
        spaceVOList.forEach(spaceVO -> {
            // 获取当前SpaceVO关联的用户ID
            Long userId = spaceVO.getUserId();
            // 初始化用户对象（默认null，避免空指针）
            User user = null;
            // 如果用户ID在查询结果中存在，取第一个用户对象（用户ID唯一）
            if (userIdUserListMap.containsKey(userId)) {
                user = userIdUserListMap.get(userId).get(0);
            }
            // 将用户对象转换为UserVO并设置到SpaceVO中
            spaceVO.setUser(userService.getUserVO(user));
        });

        // 8. 将填充好用户信息的SpaceVO列表设置到分页对象中
        spaceVOPage.setRecords(spaceVOList);

        // 9. 返回最终的SpaceVO分页对象
        return spaceVOPage;
    }

    /**
     * 获取空间查询条件包装器
     *
     * @param spaceQueryRequest 空间查询请求对象，包含查询参数（如ID、用户ID、空间名称、等级等）
     * @return 基于MyBatis Plus的QueryWrapper对象，用于构建SQL查询条件
     */
    @Override
    public QueryWrapper<Space> getQueryWrapper(SpaceQueryRequest spaceQueryRequest) {
        if (spaceQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "查询参数为空");
        }


        Long id = spaceQueryRequest.getId();
        Long userId = spaceQueryRequest.getUserId();
        String spaceName = spaceQueryRequest.getSpaceName();
        Integer spaceLevel = spaceQueryRequest.getSpaceLevel();
        String sortField = spaceQueryRequest.getSortField();
        String sortOrder = spaceQueryRequest.getSortOrder();


        QueryWrapper<Space> queryWrapper = new QueryWrapper<>();


        queryWrapper.eq(ObjectUtil.isNotNull(id), "id", id);
        queryWrapper.eq(ObjectUtil.isNotNull(userId), "userId", userId);
        queryWrapper.eq(ObjectUtil.isNotNull(spaceLevel), "spaceLevel", spaceLevel);
        queryWrapper.like(StrUtil.isNotBlank(spaceName), "spaceName", spaceName);


        queryWrapper.orderBy(StrUtil.isNotEmpty(sortField), StrUtil.equals(sortOrder, "ascend"), sortField);
        return queryWrapper;
    }

    /**
     * 根据空间等级填充空间的最大大小和最大数量
     *
     * @param space 要填充的空间实体对象
     */
    @Override
    public void fillSpaceBySpaceLevel(Space space) {
        SpaceLevelEnum spaceLevelEnum = SpaceLevelEnum.getEnumByValue(space.getSpaceLevel());
        if (spaceLevelEnum != null) {
            // 如果没有设置最大大小或数量，默认使用枚举值
            long maxSize = spaceLevelEnum.getMaxSize();
            if (space.getMaxSize() == null) {
                space.setMaxSize(maxSize);
            }
            long maxCount = spaceLevelEnum.getMaxCount();
            if (space.getMaxCount() == null) {
                space.setMaxCount(maxCount);
            }
        }
    }
}




