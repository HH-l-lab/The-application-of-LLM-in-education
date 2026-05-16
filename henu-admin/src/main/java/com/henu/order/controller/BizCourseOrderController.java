package com.henu.order.controller;

import java.util.List;
import javax.servlet.http.HttpServletResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.henu.common.annotation.Log;
import com.henu.common.core.controller.BaseController;
import com.henu.common.core.domain.AjaxResult;
import com.henu.common.enums.BusinessType;
import com.henu.order.domain.BizCourseOrder;
import com.henu.order.service.IBizCourseOrderService;
import com.henu.common.utils.poi.ExcelUtil;
import com.henu.common.core.page.TableDataInfo;
import com.henu.common.utils.SecurityUtils;
import com.henu.course.domain.BizCourse;
import com.henu.course.service.IBizCourseService;
import com.henu.common.utils.DateUtils;
import com.henu.message.domain.BizUserMessage;
import com.henu.message.service.IBizUserMessageService;
import com.henu.web.controller.message.MessageWebSocket;
import com.henu.settlement.domain.BizCreatorSettlement;
import com.henu.settlement.service.IBizCreatorSettlementService;
import com.henu.system.service.ISysConfigService;
import java.util.UUID;
import java.math.BigDecimal;

/**
 * 课程交易订单Controller
 * 
 * @author henu
 * @date 2026-03-02
 */
@RestController
@RequestMapping("/order/order")
public class BizCourseOrderController extends BaseController {

    @Autowired
    private ISysConfigService configService;

    @Autowired
    private IBizCourseOrderService bizCourseOrderService;

    @Autowired
    private IBizCourseService bizCourseService;

    @Autowired
    private IBizUserMessageService bizUserMessageService;

    @Autowired
    private IBizCreatorSettlementService bizCreatorSettlementService;

    /**
     * 学伴自习室 - 校验当前用户是否有某课程的阅读/进入权限
     */
    @GetMapping("/checkAccess/{courseId}")
    public AjaxResult checkAccess(@PathVariable("courseId") Long courseId) {
        Long userId = SecurityUtils.getUserId();
        String username = SecurityUtils.getUsername();

        BizCourse course = bizCourseService.selectBizCourseByCourseId(courseId);
        if (course == null) {
            return error("课程不存在");
        }

        // 1. 若为免费课程（或未设置价格），直接放行
        if (course.getPrice() == null || course.getPrice().compareTo(java.math.BigDecimal.ZERO) == 0) {
            return success(true);
        }

        // 2. 如果当前用户是超级管理员或普通管理员，直接放行
        if (SecurityUtils.isAdmin(userId) || SecurityUtils.hasRole("admin") || SecurityUtils.hasRole("ladmin")) {
            return success(true);
        }

        // 3. 如果当前用户就是该课程的创作者（上传者），直接放行
        if (username.equals(course.getCreateBy()) || userId.equals(course.getCreatorId())) {
            return success(true);
        }

        // 3. 检查当前用户是否购买过此课程，并且支付状态为已支付('1')
        BizCourseOrder query = new BizCourseOrder();
        query.setCourseId(courseId);
        query.setBuyerId(userId);
        query.setPayStatus("1");
        List<BizCourseOrder> orders = bizCourseOrderService.selectBizCourseOrderList(query);
        if (orders != null && !orders.isEmpty()) {
            return success(true);
        }

        // 不满足任何放行条件
        return success(false);
    }

    /**
     * 学伴自习室 - 模拟一键购买课程
     */
    @PostMapping("/buy")
    public AjaxResult buyCourse(@RequestBody BizCourseOrder orderRq) {
        Long courseId = orderRq.getCourseId();
        Long userId = SecurityUtils.getUserId();

        BizCourse course = bizCourseService.selectBizCourseByCourseId(courseId);
        if (course == null) {
            return error("购买失败，课程不存在");
        }

        // 判断是否已购买过此课程且支付成功，防止重复购买
        BizCourseOrder query = new BizCourseOrder();
        query.setCourseId(courseId);
        query.setBuyerId(userId);
        query.setPayStatus("1");
        List<BizCourseOrder> existOrders = bizCourseOrderService.selectBizCourseOrderList(query);
        if (existOrders != null && !existOrders.isEmpty()) {
            return error("您已经购买过此课程，无需重复购买");
        }

        BizCourseOrder order = new BizCourseOrder();
        // 随机生成 32 位无需中划线的订单流水号
        order.setOrderNo(UUID.randomUUID().toString().replace("-", ""));
        order.setBuyerId(userId);
        order.setCourseId(courseId);
        order.setAmount(course.getPrice());
        order.setPayStatus("1"); // 假设购买即支付成功
        order.setCreateTime(DateUtils.getNowDate());
        order.setPayTime(DateUtils.getNowDate());

        bizCourseOrderService.insertBizCourseOrder(order);

        // ====== 自动生成创作者收益结算流水（分成） ======
        if (course.getCreatorId() != null && course.getPrice() != null) {
            BizCreatorSettlement settlement = new BizCreatorSettlement();
            settlement.setCreatorId(course.getCreatorId());
            settlement.setOrderId(order.getOrderId());
            BigDecimal shareRate = getCreatorShareRate();
            settlement.setProfitAmount(course.getPrice().multiply(shareRate));
            settlement.setSettlementStatus("0"); // 待结算
            settlement.setCreateTime(DateUtils.getNowDate());
            bizCreatorSettlementService.insertBizCreatorSettlement(settlement);
        }

        // 自动发送站内信通知
        BizUserMessage msg = new BizUserMessage();
        msg.setUserId(userId);
        msg.setTitle("课程购买成功凭证");
        msg.setContent("尊敬的用户您好，您已成功购买实验课程《" + course.getCourseTitle() + "》，支付金额为 ¥" + course.getPrice() + "。订单流水号："
                + order.getOrderNo() + "。您现在可以进自习室畅游学习了！");
        msg.setType("2"); // 2代表交易凭证
        msg.setIsRead("0");
        msg.setRelatedId(order.getOrderId());
        msg.setCreateTime(DateUtils.getNowDate());
        msg.setCreateBy("system");
        bizUserMessageService.insertBizUserMessage(msg);
        MessageWebSocket.sendMessageToUser(userId.toString(), "NEW_MESSAGE");

        return success("购买成功");
    }

    /**
     * 获取当前登录用户的订单列表（我的订单）
     */
    @GetMapping("/user/list")
    public TableDataInfo userList(BizCourseOrder bizCourseOrder) {
        startPage();
        bizCourseOrder.setBuyerId(SecurityUtils.getUserId());
        List<BizCourseOrder> list = bizCourseOrderService.selectBizCourseOrderList(bizCourseOrder);
        return getDataTable(list);
    }

    /**
     * 查询课程交易订单列表
     */
    @PreAuthorize("@ss.hasPermi('order:order:list')")
    @GetMapping("/list")
    public TableDataInfo list(BizCourseOrder bizCourseOrder) {
        startPage();
        List<BizCourseOrder> list = bizCourseOrderService.selectBizCourseOrderList(bizCourseOrder);
        return getDataTable(list);
    }

    /**
     * 导出课程交易订单列表
     */
    @PreAuthorize("@ss.hasPermi('order:order:export')")
    @Log(title = "课程交易订单", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, BizCourseOrder bizCourseOrder) {
        List<BizCourseOrder> list = bizCourseOrderService.selectBizCourseOrderList(bizCourseOrder);
        ExcelUtil<BizCourseOrder> util = new ExcelUtil<BizCourseOrder>(BizCourseOrder.class);
        util.exportExcel(response, list, "课程交易订单数据");
    }

    /**
     * 获取课程交易订单详细信息
     */
    @PreAuthorize("@ss.hasPermi('order:order:query')")
    @GetMapping(value = "/{orderId}")
    public AjaxResult getInfo(@PathVariable("orderId") Long orderId) {
        return success(bizCourseOrderService.selectBizCourseOrderByOrderId(orderId));
    }

    /**
     * 新增课程交易订单
     */
    @PreAuthorize("@ss.hasPermi('order:order:add')")
    @Log(title = "课程交易订单", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody BizCourseOrder bizCourseOrder) {
        return toAjax(bizCourseOrderService.insertBizCourseOrder(bizCourseOrder));
    }

    /**
     * 修改课程交易订单
     */
    @PreAuthorize("@ss.hasPermi('order:order:edit')")
    @Log(title = "课程交易订单", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody BizCourseOrder bizCourseOrder) {
        return toAjax(bizCourseOrderService.updateBizCourseOrder(bizCourseOrder));
    }

    /**
     * 删除课程交易订单
     */
    @PreAuthorize("@ss.hasPermi('order:order:remove')")
    @Log(title = "课程交易订单", businessType = BusinessType.DELETE)
    @DeleteMapping("/{orderIds}")
    public AjaxResult remove(@PathVariable Long[] orderIds) {
        return toAjax(bizCourseOrderService.deleteBizCourseOrderByOrderIds(orderIds));
    }

    /**
     * 从 sys_config 读取创作者分润比例，默认 50%
     * configKey = sys.settlement.shareRate，值为百分比整数如 "50"
     */
    private BigDecimal getCreatorShareRate() {
        String val = configService.selectConfigByKey("sys.settlement.shareRate");
        try {
            if (val != null && !val.isEmpty()) {
                return new BigDecimal(val).divide(new BigDecimal("100"));
            }
        } catch (Exception ignored) {}
        return new BigDecimal("0.50"); // 默认 50%
    }
}
