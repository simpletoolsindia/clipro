package com.clipro.tools.git;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Git diff tool using JGit.
 */
public class GitDiffTool extends GitTool {

    @Override
    public String getName() {
        return "git_diff";
    }

    @Override
    public String getDescription() {
        return "Show changes between commits or working tree.";
    }

    @Override
    public Object getParameters() {
        return Map.of(
            "type", "object",
            "properties", Map.of(
                "file", Map.of("type", "string", "description", "Specific file to diff")
            ),
            "required", List.of()
        );
    }

    @Override
    public String execute(Map<String, Object> args) {
        String file = (String) args.get("file");

        try {
            try (Git git = openGit()) {
                Repository repo = git.getRepository();

                try (ByteArrayOutputStream out = new ByteArrayOutputStream();
                     DiffFormatter formatter = new DiffFormatter(out)) {
                    formatter.setRepository(repo);
                    formatter.setContext(3);

                    List<DiffEntry> diffs = git.diff().call();

                    StringBuilder sb = new StringBuilder();

                    for (DiffEntry entry : diffs) {
                        if (file != null && !entry.getOldPath().contains(file) && !entry.getNewPath().contains(file)) {
                            continue;
                        }

                        sb.append("diff --git a/").append(entry.getOldPath())
                          .append(" b/").append(entry.getNewPath()).append("\n");
                        sb.append(entry.getChangeType().name()).append("\n\n");

                        formatter.format(entry);
                        sb.append(out.toString()).append("\n");
                        out.reset();
                    }

                    return sb.length() > 0 ? sb.toString() : "No changes detected.";
                }
            }
        } catch (IOException e) {
            return "Error: " + e.getMessage();
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }
}