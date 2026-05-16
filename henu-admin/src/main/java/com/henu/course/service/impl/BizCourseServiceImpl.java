package com.henu.course.service.impl;

import java.util.List;
import com.henu.common.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.henu.course.mapper.BizCourseMapper;
import com.henu.course.domain.BizCourse;
import com.henu.course.service.IBizCourseService;

/**
 * 在线课程资源Service业务层处理
 * 
 * @author henu
 * @date 2026-03-06
 */
@Service
public class BizCourseServiceImpl implements IBizCourseService {
    @Autowired
    private BizCourseMapper bizCourseMapper;

    /**
     * 查询在线课程资源
     * 
     * @param courseId 在线课程资源主键
     * @return 在线课程资源
     */
    @Override
    public BizCourse selectBizCourseByCourseId(Long courseId) {
        return bizCourseMapper.selectBizCourseByCourseId(courseId);
    }

    /**
     * 查询在线课程资源列表
     * 
     * @param bizCourse 在线课程资源
     * @return 在线课程资源
     */
    @Override
    public List<BizCourse> selectBizCourseList(BizCourse bizCourse) {
        return bizCourseMapper.selectBizCourseList(bizCourse);
    }

    /**
     * 新增在线课程资源
     * 
     * @param bizCourse 在线课程资源
     * @return 结果
     */
    @Override
    public int insertBizCourse(BizCourse bizCourse) {
        bizCourse.setCreateTime(DateUtils.getNowDate());
        return bizCourseMapper.insertBizCourse(bizCourse);
    }

    /**
     * 修改在线课程资源
     * 
     * @param bizCourse 在线课程资源
     * @return 结果
     */
    @Override
    public int updateBizCourse(BizCourse bizCourse) {
        bizCourse.setUpdateTime(DateUtils.getNowDate());
        return bizCourseMapper.updateBizCourse(bizCourse);
    }

    /**
     * 批量删除在线课程资源
     * 
     * @param courseIds 需要删除的在线课程资源主键
     * @return 结果
     */
    @Override
    public int deleteBizCourseByCourseIds(Long[] courseIds) {
        return bizCourseMapper.deleteBizCourseByCourseIds(courseIds);
    }

    /**
     * 删除在线课程资源信息
     * 
     * @param courseId 在线课程资源主键
     * @return 结果
     */
    @Override
    public int deleteBizCourseByCourseId(Long courseId) {
        return bizCourseMapper.deleteBizCourseByCourseId(courseId);
    }

    @Override
    public int incrementPlayCount(Long courseId) {
        return bizCourseMapper.incrementPlayCount(courseId);
    }

    @Override
    public int selectCourseCount() {
        return bizCourseMapper.selectCourseCount();
    }

    @Override
    public Long selectTotalPlayCount() {
        return bizCourseMapper.selectTotalPlayCount();
    }

    @Override
    public Long selectPlayCountByCreatorId(Long creatorId) {
        return bizCourseMapper.selectPlayCountByCreatorId(creatorId);
    }

    @Override
    public int selectCourseCountByCreatorId(Long creatorId) {
        return bizCourseMapper.selectCourseCountByCreatorId(creatorId);
    }
}
