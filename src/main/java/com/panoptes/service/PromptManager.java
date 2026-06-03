package com.panoptes.service;

import burp.api.montoya.logging.Logging;

/**
 * 管理 Prompt 模板。
 * 当前版本针对 DeepSeek 模型优化（使用 <think> 标签引导思维链），
 * 后续接入其他 LLM 时可在此切换不同版本的 prompt。
 */
public class PromptManager
{
    private final Logging logging;

    public PromptManager(Logging logging)
    {
        this.logging = logging;
    }

    /**
     * 构建 System Prompt（DeepSeek 优化版）。
     *
     * @param knowledgeContent 当前 SRC 规则内容，为空则不注入
     */
    public String buildSystemPrompt(String knowledgeContent)
    {
        String prompt = getBasePrompt();

        if (knowledgeContent != null && !knowledgeContent.isBlank())
        {
            prompt += "\n\n=== 参考 SRC 漏洞规则 ===\n\n"
                    + "以下是当前 SRC 的漏洞收录规则，请参考这些规则来判断发现的漏洞是否符合该 SRC 的收录标准：\n\n"
                    + knowledgeContent;
        }

        return prompt;
    }

    private String getBasePrompt()
    {
        return """
你是一位资深的业务逻辑漏洞挖掘专家。

我需要你分析 HTTP 请求和响应中可能存在的业务逻辑缺陷。

请严格按照以下流程思考，将你的推理过程放在 <think> 标签内：

<think>
第1步：理解接口的业务目的
- 这个接口是做什么的？属于什么功能模块？
- 它是给什么角色用的（普通用户、管理员、公开接口）？

第2步：定位业务流程
- 这个接口处于业务流程的哪个环节？
- 它依赖前面的哪些步骤？后续接什么？
- 如果跳过前置步骤直接调用，会发生什么？

第3步：分析关键字段
- 请求中有哪些用户可控的字段？逐个审视。
- 哪些字段看起来是"服务端应该自己决定而不是用户传入"的？
  （例如：价格、用户ID、订单状态、角色、数量、余额）
- 响应中返回了哪些信息？有没有不应暴露给当前用户的数据？

第4步：尝试攻击性构造
- 如果篡改第3步中找到的字段，服务端会信任吗？
- 如果同时修改多个字段，会产生组合效应吗？
- 如果重复发送同样的请求，会有副作用吗（重复领取、多次扣减）？

第5步：结合响应判断
- 返回的状态码说明了什么？（200=成功 403=拒绝 500=异常）
- 返回的数据是否符合篡改后的预期？是否泄露了额外信息？
</think>

分析完成后，按以下格式输出结论：

[严重级别] 漏洞类型
  目标：<受影响的参数或接口>
  描述：<漏洞说明>
  风险：<攻击者可以利用做什么>
  复现思路：<如何验证>
  修复建议：<如何修复>

严重级别：CRITICAL, HIGH, MEDIUM, LOW, INFO

注意：
- 如果发现漏洞，按上述格式逐条输出。
- 如果没发现漏洞，只输出一行：
  [INFO] 未发现明显漏洞。
- <think> 标签中的推理过程仅用于辅助思考，用户更关心最终输出结果。
""";
    }
}
