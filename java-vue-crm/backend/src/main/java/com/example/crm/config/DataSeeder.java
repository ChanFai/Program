package com.example.crm.config;

import com.example.crm.customer.ContactRequest;
import com.example.crm.customer.CustomerDetailResponse;
import com.example.crm.customer.CustomerRequest;
import com.example.crm.customer.CustomerService;
import com.example.crm.customer.CustomerStage;
import com.example.crm.customer.FollowUpRequest;
import com.example.crm.opportunity.OpportunityRequest;
import com.example.crm.opportunity.OpportunityService;
import com.example.crm.opportunity.OpportunityStage;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Configuration
public class DataSeeder {
    @Bean
    CommandLineRunner seedCustomers(CustomerService customerService, OpportunityService opportunityService) {
        return args -> {
            CustomerDetailResponse xinghe = customerService.create(new CustomerRequest(
                    "星河制造有限公司",
                    "智能制造",
                    "展会线索",
                    "林一",
                    CustomerStage.NEGOTIATION,
                    new BigDecimal("168000.00"),
                    "021-8899-1200",
                    "purchase@xinghe.example",
                    "上海市浦东新区世纪大道 100 号",
                    "关注年度设备升级项目，需要本周给出实施排期。",
                    LocalDate.now().plusDays(2),
                    List.of(new ContactRequest("周敏", "采购总监", "13800010001", "zhoumin@xinghe.example", true))
            ));

            customerService.addFollowUp(xinghe.id(), new FollowUpRequest(
                    "林一",
                    "电话",
                    "确认预算已通过，需要补充交付周期和售后 SLA。",
                    LocalDate.now().minusDays(1),
                    LocalDate.now().plusDays(2)
            ));

            CustomerDetailResponse yunshu = customerService.create(new CustomerRequest(
                    "云枢科技",
                    "企业服务",
                    "官网咨询",
                    "陈澈",
                    CustomerStage.QUALIFIED,
                    new BigDecimal("86000.00"),
                    "010-6200-7788",
                    "it@yunshu.example",
                    "北京市海淀区中关村软件园",
                    "对私有化部署和权限审计比较敏感。",
                    LocalDate.now(),
                    List.of(new ContactRequest("许宁", "技术负责人", "13900020002", "xuning@yunshu.example", true))
            ));

            customerService.addFollowUp(yunshu.id(), new FollowUpRequest(
                    "陈澈",
                    "会议",
                    "完成技术澄清，客户要求补充权限审计和私有化部署报价。",
                    LocalDate.now().minusDays(2),
                    LocalDate.now()
            ));

            CustomerDetailResponse nanchuan = customerService.create(new CustomerRequest(
                    "南川零售集团",
                    "零售连锁",
                    "老客户转介绍",
                    "林一",
                    CustomerStage.WON,
                    new BigDecimal("234000.00"),
                    "0755-3300-9088",
                    "ops@nanchuan.example",
                    "深圳市南山区科技园",
                    "一期已成交，后续可推进门店数据看板。",
                    LocalDate.now().plusWeeks(3),
                    List.of(new ContactRequest("何佳", "运营经理", "13700030003", "hejia@nanchuan.example", true))
            ));

            CustomerDetailResponse qingshi = customerService.create(new CustomerRequest(
                    "青石教育",
                    "教育培训",
                    "市场活动",
                    "苏禾",
                    CustomerStage.CONTACTED,
                    new BigDecimal("42000.00"),
                    "0571-7788-3200",
                    "admin@qingshi.example",
                    "杭州市西湖区文三路 88 号",
                    "下周安排产品演示，重点讲客户分层和自动提醒。",
                    LocalDate.now().plusDays(5),
                    List.of(new ContactRequest("宋闻", "校区负责人", "13600040004", "songwen@qingshi.example", true))
            ));

            customerService.addFollowUp(qingshi.id(), new FollowUpRequest(
                    "苏禾",
                    "微信",
                    "已发送演示议程，客户希望重点看校区线索分配和提醒能力。",
                    LocalDate.now().minusDays(1),
                    LocalDate.now().plusDays(5)
            ));

            opportunityService.create(new OpportunityRequest(
                    xinghe.id(),
                    "年度设备升级 CRM 项目",
                    OpportunityStage.NEGOTIATION,
                    new BigDecimal("168000.00"),
                    70,
                    "林一",
                    "展会线索",
                    LocalDate.now().plusDays(18),
                    "发送最终实施排期和售后 SLA。",
                    "预算已过会，卡在交付排期确认。"
            ));

            opportunityService.create(new OpportunityRequest(
                    yunshu.id(),
                    "私有化部署与权限审计",
                    OpportunityStage.PROPOSAL,
                    new BigDecimal("86000.00"),
                    45,
                    "陈澈",
                    "官网咨询",
                    LocalDate.now().plusDays(30),
                    "补充安全白皮书和部署报价。",
                    "技术负责人已认可产品能力。"
            ));

            opportunityService.create(new OpportunityRequest(
                    nanchuan.id(),
                    "门店数据看板二期",
                    OpportunityStage.WON,
                    new BigDecimal("234000.00"),
                    100,
                    "林一",
                    "老客户转介绍",
                    LocalDate.now().minusDays(4),
                    "推进验收和复购需求收集。",
                    "一期成交后转入客户成功。"
            ));

            opportunityService.create(new OpportunityRequest(
                    qingshi.id(),
                    "校区线索运营工具",
                    OpportunityStage.DISCOVERY,
                    new BigDecimal("42000.00"),
                    25,
                    "苏禾",
                    "市场活动",
                    LocalDate.now().plusDays(45),
                    "安排产品演示，确认试点校区名单。",
                    "需求明确但预算窗口未定。"
            ));
        };
    }
}
