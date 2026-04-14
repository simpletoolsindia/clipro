package com.clipro.tools.skill;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Map;
import java.util.Collections;

class SkillToolTest {

    @Test
    void shouldListSkills() {
        SkillTool tool = new SkillTool();
        String result = (String) tool.execute(Map.of("action", "list"));
        // Should not crash even with no skills
        assertNotNull(result);
    }

    @Test
    void shouldReturnErrorForMissingSkill() {
        SkillTool tool = new SkillTool();
        String result = (String) tool.execute(Map.of(
            "action", "execute",
            "name", "nonexistent-skill"
        ));
        assertTrue(result.contains("not found") || result.contains("Usage"));
    }

    @Test
    void shouldReturnSkillInfo() {
        SkillTool tool = new SkillTool();
        String result = (String) tool.execute(Map.of(
            "action", "info",
            "name", ""
        ));
        // Should show usage for empty name
        assertTrue(result.contains("Usage"));
    }

    @Test
    void shouldHaveCorrectNameAndDescription() {
        SkillTool tool = new SkillTool();
        assertEquals("skill", tool.getName());
        assertTrue(tool.getDescription().contains("skill"));
    }

    @Test
    void shouldHaveValidParameters() {
        SkillTool tool = new SkillTool();
        Object params = tool.getParameters();
        assertNotNull(params);
        assertTrue(params instanceof Map);
    }
}