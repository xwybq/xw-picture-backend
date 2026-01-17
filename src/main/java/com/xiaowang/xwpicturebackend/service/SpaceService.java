package com.xiaowang.xwpicturebackend.service;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.xiaowang.xwpicturebackend.model.dto.space.SpaceAddRequest;
import com.xiaowang.xwpicturebackend.model.dto.space.SpaceQueryRequest;
import com.xiaowang.xwpicturebackend.model.entity.Space;
import com.xiaowang.xwpicturebackend.model.entity.User;
import com.xiaowang.xwpicturebackend.model.vo.SpaceVO;


import javax.servlet.http.HttpServletRequest;

/**
 * @author wangjialeNB
 * @description 针对表【space(空间)】的数据库操作Service
 * @createDate 2026-01-17 11:24:31
 */
public interface SpaceService extends IService<Space> {

    /**
     * 添加空间
     *
     * @param spaceAddRequest 空间添加请求
     * @param loginUser       登录用户
     * @return 空间ID
     */
    long addSpace(SpaceAddRequest  spaceAddRequest, User loginUser);


    /**
     * 校验空间是否合法（新增/更新时调用）
     *
     * @param space 空间
     * @param isAdd 是否为新增
     */
    void validSpace(Space space, boolean isAdd);


    /**
     * 获取空间VO
     *
     * @param space   空间
     * @param request 请求
     * @return 空间VO
     */
    SpaceVO getSpaceVO(Space space, HttpServletRequest request);

    /**
     * 获取空间VO分页
     *
     * @param spacePage 空间分页
     * @return 空间VO分页
     */
    Page<SpaceVO> getSpaceVOPage(Page<Space> spacePage, HttpServletRequest request);

    /**
     * 获取查询Wrapper
     *
     * @param spaceQueryRequest 空间查询请求
     * @return 查询Wrapper
     */
    QueryWrapper<Space> getQueryWrapper(SpaceQueryRequest spaceQueryRequest);

    /**
     * 根据空间等级填充空间信息
     *
     * @param space 空间
     */
     void fillSpaceBySpaceLevel(Space space);


}
