package com.clipro.tools.git;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.AddCommand;
import org.eclipse.jgit.api.Status;
import org.eclipse.jgit.api.RebaseResult;
import org.eclipse.jgit.api.errors.GitAPIException;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Git commit tool using JGit.
 */
public class GitCommitTool extends GitTool {

    @Override
    public String getName() {
        return "git_commit";
    }

    @Override
    public String getDescription() {
        return "Commit changes with auto-staging.";
    }

    @Override
    public Object getParameters() {
        return Map.of(
            "type", "object",
            "properties", Map.of(
                "message", Map.of("type", "string", "description", "Commit message (required)")
            ),
            "required", List.of("message")
        );
    }

    @Override
    public String execute(Map<String, Object> args) {
        String message = (String) args.get("message");

        if (message == null || message.isEmpty()) {
            return "Error: message is required";
        }

        try {
            try (Git git = openGit()) {
                Status status = git.status().call();

                AddCommand addCmd = git.add();
                for (String path : status.getModified()) {
                    addCmd.addFilepattern(path);
                }
                for (String path : status.getUntracked()) {
                    addCmd.addFilepattern(path);
                }

                if (status.getModified().isEmpty() && status.getUntracked().isEmpty()) {
                    return "Nothing to commit.";
                }

                addCmd.call();

                org.eclipse.jgit.lib.ObjectId commitId = git.commit()
                    .setMessage(message)
                    .call();

                return "Committed: " + commitId.getName().substring(0, 7) + "\n" +
                       "Message: " + message;
            }
        } catch (IOException e) {
            return "Error: Not a git repository";
        } catch (GitAPIException e) {
            return "Error: " + e.getMessage();
        }
    }
}