package com.panoptes.service;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * SRC 知识库服务。
 * 管理 knowledge/ 目录下的 Markdown 规则文件，
 * 支持选择当前使用的 SRC 规则，并读取其内容注入到 Prompt 中。
 */
public class KnowledgeBaseService
{
    private static final String CONFIG_FILE = "config.json";
    private static final String MD_EXT = ".md";

    private final Path knowledgeDir;
    private final Gson gson;
    private final String defaultSrcName;

    public KnowledgeBaseService(Path knowledgeDir)
    {
        this.knowledgeDir = knowledgeDir;
        this.gson = new Gson();
        this.defaultSrcName = "通用";
        ensureDirExists();
    }

    // ── 目录管理 ──

    private void ensureDirExists()
    {
        try
        {
            Files.createDirectories(knowledgeDir);
        }
        catch (IOException e)
        {
            // ignore
        }
    }

    // ── 配置读写 ──

    public String getActiveSrc()
    {
        Path configPath = knowledgeDir.resolve(CONFIG_FILE);
        if (Files.exists(configPath))
        {
            try
            {
                String content = Files.readString(configPath);
                JsonObject json = gson.fromJson(content, JsonObject.class);
                if (json.has("activeSrc") && !json.get("activeSrc").getAsString().isBlank())
                {
                    return json.get("activeSrc").getAsString();
                }
            }
            catch (Exception e)
            {
                // fall through
            }
        }
        return defaultSrcName;
    }

    public void setActiveSrc(String srcName)
    {
        Path configPath = knowledgeDir.resolve(CONFIG_FILE);
        try
        {
            JsonObject json;
            if (Files.exists(configPath))
            {
                String content = Files.readString(configPath);
                json = gson.fromJson(content, JsonObject.class);
            }
            else
            {
                json = new JsonObject();
            }
            json.addProperty("activeSrc", srcName);
            Files.writeString(configPath, gson.toJson(json));
        }
        catch (Exception e)
        {
            // ignore
        }
    }

    // ── 可用 SRC 列表 ──

    public List<String> listAvailableSrcs()
    {
        ensureDirExists();
        try
        {
            return Files.list(knowledgeDir)
                    .filter(p -> p.toString().endsWith(MD_EXT))
                    .map(p -> p.getFileName().toString().replace(MD_EXT, ""))
                    .sorted()
                    .collect(Collectors.toList());
        }
        catch (IOException e)
        {
            return Collections.singletonList(defaultSrcName);
        }
    }

    // ── 读取当前 SRC 规则内容 ──

    public String getActiveSrcContent()
    {
        String srcName = getActiveSrc();
        Path mdFile = knowledgeDir.resolve(srcName + MD_EXT);
        if (Files.exists(mdFile))
        {
            try
            {
                return Files.readString(mdFile);
            }
            catch (IOException e)
            {
                return "(读取失败: " + e.getMessage() + ")";
            }
        }
        return "(未找到 " + srcName + ".md 文件，请在 knowledge/ 目录下创建)";
    }

    /**
     * 判断当前 SRC 是否有效（对应的 .md 文件存在）。
     */
    public boolean isActiveSrcValid()
    {
        String srcName = getActiveSrc();
        return Files.exists(knowledgeDir.resolve(srcName + MD_EXT));
    }
}
