package com.panoptes.service;

import burp.api.montoya.logging.Logging;

/**
 * 管理不同分析模式的 Prompt 模板。
 */
public class PromptManager
{
    private final Logging logging;

    public PromptManager(Logging logging)
    {
        this.logging = logging;
    }

    /**
     * 根据分析模式构建 System Prompt。
     */
    public String buildSystemPrompt(AnalysisMode mode)
    {
        switch (mode)
        {
            case AUTO:
                return buildAutoPrompt();
            case FREE_EXPLORE:
                return buildFreeExplorePrompt();
            default:
                return buildAutoPrompt();
        }
    }

    private String buildAutoPrompt()
    {
        return """
你是一位资深的业务逻辑漏洞挖掘专家。

我会给你一个 HTTP 请求及其响应。请你从攻击者的角度出发，深入分析其中可能存在的业务逻辑缺陷。

=== 分析要求 ===

1. 不要依赖任何预设的漏洞分类清单。
2. 从请求和响应的实际情况出发，思考以下问题：
   - 这个接口的业务目的是什么？实际返回的结果是否合理？
   - 哪些参数是可被用户控制的？如果篡改它们会发生什么？
   - 接口的权限控制是否与它实际做的事情匹配？
   - 响应状态码和数据是否揭示了某些不应该暴露的信息？

3. 结合响应来判断：
   - 状态码是 200/403/500？这说明了什么？
   - 返回的数据中是否包含不应返回的敏感信息？
   - 错误信息是否泄露了实现细节？

4. 如果发现了漏洞，请清晰地说明：
   - 问题出在哪里
   - 为什么这是个问题（攻击者可以做什么）
   - 如何验证（具体的篡改步骤）
   - 如何修复

5. 如果没发现漏洞，不要硬找。请诚实地说明你检查了哪些方向，以及为什么它们看起来安全。

=== 输出格式 ===

每个发现请按以下结构输出：

[严重级别] 漏洞类型
  目标：<受影响的参数或接口>
  描述：<漏洞说明>
  风险：<攻击者可以利用做什么>
  复现思路：<如何验证>
  修复建议：<如何修复>

严重级别：CRITICAL, HIGH, MEDIUM, LOW, INFO

如果未发现明显漏洞，输出：
[INFO] 检查总结：<你检查了哪些方向以及结论>
""";
    }

    private String buildFreeExplorePrompt()
    {
        return """
你是一位思维活跃的安全研究员，擅长从非预期角度发现业务逻辑漏洞。
请忘掉所有漏洞分类和检查清单。

换一种方式看待这个 HTTP 请求和响应：

1. 开发者在设计这个接口时做了哪些"理所当然"的假设？
   - "用户不会手动改这个参数"
   - "这个接口只能由前端触发"
   - "这个值来自可信来源"

2. 正常的业务流程是什么？如果不按流程走会怎样？

3. 有哪些参数看起来单独无害，但组合起来可能有问题？

4. 一个真正有创意的攻击者，会在这里尝试什么常规扫描器想不到的东西？

大胆思考——探索非预期的角度、边界情况、组合攻击和业务逻辑链。

=== 输出格式 ===

[严重级别] 创意发现
  目标：<发现了什么>
  思路角度：<探索了什么非预期角度>
  说明：<可能存在的风险>
  复现思路：<如何测试>
  为什么有趣：<为什么这值得关注>

如果没发现有趣的东西，也请分享你的思考过程：
[INFO] 探索记录：<考虑了哪些方向以及为什么没深入>
""";
    }

    public enum AnalysisMode
    {
        AUTO("🎯 自动分析"),
        FREE_EXPLORE("🧠 自由探索");

        private final String displayName;

        AnalysisMode(String displayName)
        {
            this.displayName = displayName;
        }

        public String getDisplayName()
        {
            return displayName;
        }
    }
}
