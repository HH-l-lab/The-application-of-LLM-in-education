package com.henu.web.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 仪表盘统计专用 Mapper
 */
@Mapper
public interface DashboardMapper {
    @Select("SELECT COUNT(*) FROM sys_user WHERE del_flag = '0'")
    int selectUserCount();

    @Select("SELECT COUNT(*) FROM biz_experiment_record")
    int selectRecordCount();

    @Select("SELECT COUNT(*) FROM biz_course_order")
    int selectOrderCount();

    @Select("SELECT IFNULL(SUM(profit_amount), 0) FROM biz_creator_settlement WHERE creator_id = #{creatorId}")
    double selectRevenueByCreatorId(@Param("creatorId") Long creatorId);

    @Select("SELECT IFNULL(SUM(profit_amount), 0) FROM biz_creator_settlement WHERE creator_id = #{creatorId} AND settlement_status = '0'")
    double selectPendingRevenueByCreatorId(@Param("creatorId") Long creatorId);

    @Select("SELECT IFNULL(SUM(profit_amount), 0) FROM biz_creator_settlement WHERE creator_id = #{creatorId} AND settlement_status = '1'")
    double selectSettledRevenueByCreatorId(@Param("creatorId") Long creatorId);

    @Select("SELECT COUNT(*) FROM biz_course_order WHERE course_id IN (SELECT course_id FROM biz_course WHERE creator_id = #{creatorId})")
    int selectOrderCountByCreatorId(@Param("creatorId") Long creatorId);

    // ===== 学情诊断：按学科统计 =====

    // 用户某学科的实验次数
    @Select("SELECT COUNT(*) FROM biz_experiment_record r JOIN biz_course c ON r.course_id = c.course_id WHERE r.student_id = #{userId} AND c.course_subject = #{subject}")
    int selectUserExpCountBySubject(@Param("userId") Long userId, @Param("subject") String subject);

    // 用户某学科的平均分
    @Select("SELECT IFNULL(AVG(r.score), 0) FROM biz_experiment_record r JOIN biz_course c ON r.course_id = c.course_id WHERE r.student_id = #{userId} AND c.course_subject = #{subject} AND r.score IS NOT NULL")
    double selectUserAvgScoreBySubject(@Param("userId") Long userId, @Param("subject") String subject);

    // 用户某学科购买的课程数
    @Select("SELECT COUNT(*) FROM biz_course_order o JOIN biz_course c ON o.course_id = c.course_id WHERE o.buyer_id = #{userId} AND o.pay_status = '1' AND c.course_subject = #{subject}")
    int selectUserCourseCountBySubject(@Param("userId") Long userId, @Param("subject") String subject);

    // 全平台某学科的人均实验次数
    @Select("SELECT IFNULL(COUNT(*) * 1.0 / NULLIF((SELECT COUNT(DISTINCT r2.student_id) FROM biz_experiment_record r2 JOIN biz_course c2 ON r2.course_id = c2.course_id WHERE c2.course_subject = #{subject}), 0), 0) FROM biz_experiment_record r JOIN biz_course c ON r.course_id = c.course_id WHERE c.course_subject = #{subject}")
    double selectAvgExpCountBySubject(@Param("subject") String subject);

    // 全平台某学科的平均分
    @Select("SELECT IFNULL(AVG(r.score), 0) FROM biz_experiment_record r JOIN biz_course c ON r.course_id = c.course_id WHERE c.course_subject = #{subject} AND r.score IS NOT NULL")
    double selectAvgScoreBySubject(@Param("subject") String subject);

    // 全平台某学科的人均课程购买数
    @Select("SELECT IFNULL(COUNT(*) * 1.0 / NULLIF((SELECT COUNT(DISTINCT o2.buyer_id) FROM biz_course_order o2 JOIN biz_course c2 ON o2.course_id = c2.course_id WHERE o2.pay_status = '1' AND c2.course_subject = #{subject}), 0), 0) FROM biz_course_order o JOIN biz_course c ON o.course_id = c.course_id WHERE o.pay_status = '1' AND c.course_subject = #{subject}")
    double selectAvgCourseCountBySubject(@Param("subject") String subject);

    // ===== 按学科+年级维度统计 =====

    @Select("SELECT COUNT(*) FROM biz_experiment_record r JOIN biz_course c ON r.course_id = c.course_id WHERE r.student_id = #{userId} AND c.course_subject = #{subject} AND c.course_grade = #{grade}")
    int selectUserExpBySubjectGrade(@Param("userId") Long userId, @Param("subject") String subject, @Param("grade") String grade);

    @Select("SELECT IFNULL(AVG(r.score), 0) FROM biz_experiment_record r JOIN biz_course c ON r.course_id = c.course_id WHERE r.student_id = #{userId} AND c.course_subject = #{subject} AND c.course_grade = #{grade} AND r.score IS NOT NULL")
    double selectUserScoreBySubjectGrade(@Param("userId") Long userId, @Param("subject") String subject, @Param("grade") String grade);

    @Select("SELECT COUNT(*) FROM biz_course_order o JOIN biz_course c ON o.course_id = c.course_id WHERE o.buyer_id = #{userId} AND o.pay_status = '1' AND c.course_subject = #{subject} AND c.course_grade = #{grade}")
    int selectUserCourseBySubjectGrade(@Param("userId") Long userId, @Param("subject") String subject, @Param("grade") String grade);

    @Select("SELECT IFNULL(AVG(sub.cnt), 0) FROM (SELECT COUNT(*) cnt FROM biz_experiment_record r JOIN biz_course c ON r.course_id = c.course_id WHERE c.course_subject = #{subject} AND c.course_grade = #{grade} GROUP BY r.student_id) sub")
    double selectAvgExpBySubjectGrade(@Param("subject") String subject, @Param("grade") String grade);

    @Select("SELECT IFNULL(AVG(r.score), 0) FROM biz_experiment_record r JOIN biz_course c ON r.course_id = c.course_id WHERE c.course_subject = #{subject} AND c.course_grade = #{grade} AND r.score IS NOT NULL")
    double selectAvgScoreBySubjectGrade(@Param("subject") String subject, @Param("grade") String grade);

    @Select("SELECT IFNULL(AVG(sub.cnt), 0) FROM (SELECT COUNT(*) cnt FROM biz_course_order o JOIN biz_course c ON o.course_id = c.course_id WHERE o.pay_status = '1' AND c.course_subject = #{subject} AND c.course_grade = #{grade} GROUP BY o.buyer_id) sub")
    double selectAvgCourseBySubjectGrade(@Param("subject") String subject, @Param("grade") String grade);
}
