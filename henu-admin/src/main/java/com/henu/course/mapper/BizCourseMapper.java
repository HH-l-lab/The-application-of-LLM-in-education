package com.henu.course.mapper;

import java.util.List;
import com.henu.course.domain.BizCourse;

/**
 * 在线课程资源Mapper接口
 * 
 * @author henu
 * @date 2026-03-06
 */
public interface BizCourseMapper {
    /**
     * 查询在线课程资源
     * 
     * @param courseId 在线课程资源主键
     * @return 在线课程资源
     */
    public BizCourse selectBizCourseByCourseId(Long courseId);

    /**
     * 查询在线课程资源列表
     * 
     * @param bizCourse 在线课程资源
     * @return 在线课程资源集合
     */
    public List<BizCourse> selectBizCourseList(BizCourse bizCourse);

    /**
     * 新增在线课程资源
     * 
     * @param bizCourse 在线课程资源
     * @return 结果
     */
    public int insertBizCourse(BizCourse bizCourse);

    /**
     * 修改在线课程资源
     * 
     * @param bizCourse 在线课程资源
     * @return 结果
     */
    public int updateBizCourse(BizCourse bizCourse);

    /**
     * 删除在线课程资源
     * 
     * @param courseId 在线课程资源主键
     * @return 结果
     */
    public int deleteBizCourseByCourseId(Long courseId);

    /**
     * 批量删除在线课程资源
     * 
     * @param courseIds 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteBizCourseByCourseIds(Long[] courseIds);

    /**
     * 播放量+1
     */
    public int incrementPlayCount(Long courseId);

    /**
     * 查询课程总数
     */
    public int selectCourseCount();

    /**
     * 查询所有课程总播放量
     */
    public Long selectTotalPlayCount();

    /**
     * 根据创作者ID查询总播放量
     */
    public Long selectPlayCountByCreatorId(Long creatorId);

    /**
     * 根据创作者ID查询课程数
     */
    public int selectCourseCountByCreatorId(Long creatorId);
}
