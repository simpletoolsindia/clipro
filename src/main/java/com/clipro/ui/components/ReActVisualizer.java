package com.clipro.ui.components;

import java.util.ArrayList;
import java.util.List;

/**
 * ReAct visualization panel.
 * Shows the agent's reasoning process: Thought → Action → Observation
 */
public class ReActVisualizer {

    private final List<ReActStep> steps;
    private static final int MAX_STEPS = 50;

    public ReActVisualizer() {
        this.steps = new ArrayList<>();
    }

    public void addThink(String thought) {
        steps.add(new ReActStep(ReActStep.Type.THINK, thought));
        trimSteps();
    }

    public void addAction(String action) {
        steps.add(new ReActStep(ReActStep.Type.ACTION, action));
        trimSteps();
    }

    public void addObserve(String observation) {
        steps.add(new ReActStep(ReActStep.Type.OBSERVE, observation));
        trimSteps();
    }

    public void addResult(String result) {
        steps.add(new ReActStep(ReActStep.Type.RESULT, result));
        trimSteps();
    }

    public void clear() {
        steps.clear();
    }

    public List<ReActStep> getSteps() {
        return new ArrayList<>(steps);
    }

    public String render() {
        if (steps.isEmpty()) {
            return "ReAct: (no activity yet)";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("ReAct Steps:\n");
        sb.append("─".repeat(50)).append("\n");

        for (ReActStep step : steps) {
            sb.append(step.render()).append("\n");
        }

        return sb.toString();
    }

    public String renderCompact() {
        if (steps.isEmpty()) return "";

        StringBuilder sb = new StringBuilder();
        for (ReActStep step : steps) {
            sb.append("[").append(step.getType().getIcon()).append("] ");
        }
        return sb.toString();
    }

    private void trimSteps() {
        while (steps.size() > MAX_STEPS) {
            steps.remove(0);
        }
    }
}