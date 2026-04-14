package com.clipro.ui.components;

import com.clipro.ui.Terminal;
import com.clipro.agent.SubAgent;
import static com.clipro.agent.SubAgent.AgentState.*;
import java.util.*;

/**
 * L-03: Teammate View for agent team visualization.
 * Shows team members with status and progress bars.
 */
public class TeammateView {

    public static class TeamMember {
        public final String id;
        public final String name;
        public SubAgent.AgentState state;
        public String currentTask;
        public int progress; // 0-100
        public String lastMessage;

        public TeamMember(String id, String name) {
            this.id = id;
            this.name = name;
            this.state = SubAgent.AgentState.IDLE;
            this.progress = 0;
        }
    }

    public static String render(List<TeamMember> members) {
        if (members.isEmpty()) {
            return Terminal.dim("No team members active");
        }

        StringBuilder sb = new StringBuilder();
        int width = Math.min(Terminal.getColumns(), 60);

        sb.append("┌─ Team Members ────────────────────────────────────────────────────┐\n");

        for (TeamMember member : members) {
            String statusIcon = getStatusIcon(member.state);
            String statusColor = getStatusColor(member.state);
            String progressBar = renderProgressBar(member.progress);

            sb.append("│ ").append(statusIcon).append(" ").append(truncate(member.name, 15));
            sb.append(" ").append(statusColor).append(member.state.name()).append(Terminal.RESET);

            if (member.currentTask != null && !member.currentTask.isEmpty()) {
                sb.append("\n│   ").append(Terminal.dim(truncate(member.currentTask, 45)));
            }

            sb.append("\n│   Progress: ").append(progressBar).append(" ").append(member.progress).append("%\n");
            sb.append("│───────────────────────────────────────────────────────────────────────│\n");
        }

        sb.append("└───────────────────────────────────────────────────────────────────────┘\n");
        return sb.toString();
    }

    private static String getStatusIcon(SubAgent.AgentState state) {
        return switch (state) {
            case THINKING -> Terminal.cyan("💭");
            case ACTING -> Terminal.yellow("⚡");
            case IDLE -> Terminal.green("✓");
            case ERROR -> Terminal.red("✗");
            default -> Terminal.dim("?");
        };
    }

    private static String getStatusColor(SubAgent.AgentState state) {
        return switch (state) {
            case THINKING -> Terminal.cyan("");
            case ACTING -> Terminal.yellow("");
            case IDLE -> Terminal.green("");
            case ERROR -> Terminal.red("");
            default -> Terminal.dim("");
        };
    }

    private static String renderProgressBar(int progress) {
        int width = 20;
        int filled = (progress * width) / 100;

        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (int i = 0; i < width; i++) {
            if (i < filled) {
                if (progress < 30) sb.append("\033[32m▓\033[0m");
                else if (progress < 70) sb.append("\033[33m▓\033[0m");
                else sb.append("\033[32m▓\033[0m");
            } else {
                sb.append("\033[90m░\033[0m");
            }
        }
        sb.append("]");
        return sb.toString();
    }

    private static String truncate(String s, int max) {
        if (s == null) return "";
        if (s.length() <= max) return s;
        return s.substring(0, max - 2) + "..";
    }
}
