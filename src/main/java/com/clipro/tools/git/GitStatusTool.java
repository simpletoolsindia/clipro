package com.clipro.tools.git;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.Status;
import org.eclipse.jgit.api.errors.GitAPIException;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Git status tool using JGit.
 */
public class GitStatusTool extends GitTool {

    @Override
    public String getName() {
        return "git_status";
    }

    @Override
    public String getDescription() {
        return "Show git working tree status.";
    }

    @Override
    public Object getParameters() {
        return Map.of(
            "type", "object",
            "properties", Map.of(),
            "required", List.of()
        );
    }

    @Override
    public String execute(Map<String, Object> args) {
        try {
            try (Git git = openGit()) {
                Status status = git.status().call();
                StringBuilder sb = new StringBuilder();

                Set<String> modified = status.getModified();
                Set<String> added = status.getAdded();
                Set<String> removed = status.getRemoved();
                Set<String> untracked = status.getUntracked();

                if (modified.isEmpty() && added.isEmpty() && removed.isEmpty() && untracked.isEmpty()) {
                    return "Nothing to commit, working tree clean.";
                }

                if (!modified.isEmpty()) {
                    sb.append(" M ").append(String.join(" M ", modified)).append("\n");
                }
                if (!added.isEmpty()) {
                    sb.append(" A ").append(String.join(" A ", added)).append("\n");
                }
                if (!removed.isEmpty()) {
                    sb.append(" D ").append(String.join(" D ", removed)).append("\n");
                }
                if (!untracked.isEmpty()) {
                    sb.append("?? ").append(String.join("?? ", untracked)).append("\n");
                }

                return sb.toString();
            }
        } catch (IOException e) {
            return "Error: Not a git repository";
        } catch (GitAPIException e) {
            return "Error: " + e.getMessage();
        }
    }
}