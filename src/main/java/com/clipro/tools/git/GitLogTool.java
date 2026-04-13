package com.clipro.tools.git;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.LogCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.revwalk.RevCommit;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Git log tool using JGit.
 * Shows commit history with hash, message, author, date.
 */
public class GitLogTool extends GitTool {

    private static final int DEFAULT_LIMIT = 20;

    @Override
    public String getName() {
        return "git_log";
    }

    @Override
    public String getDescription() {
        return "Show commit logs. Format: hash, message, author, date. Default: 20 commits.";
    }

    @Override
    public Object getParameters() {
        return Map.of(
            "type", "object",
            "properties", Map.of(
                "limit", Map.of(
                    "type", "integer",
                    "description", "Number of commits to show",
                    "default", DEFAULT_LIMIT
                ),
                "file", Map.of(
                    "type", "string",
                    "description", "Show commits affecting specific file"
                )
            ),
            "required", List.of()
        );
    }

    @Override
    public String execute(Map<String, Object> args) {
        int limit = DEFAULT_LIMIT;
        Object limitObj = args.get("limit");
        if (limitObj instanceof Number) {
            limit = ((Number) limitObj).intValue();
        }

        String file = (String) args.get("file");

        try {
            try (Git git = openGit()) {
                LogCommand logCmd = git.log();

                if (file != null) {
                    logCmd.addPath(file);
                }

                Iterable<RevCommit> commits = logCmd.setMaxCount(limit).call();

                StringBuilder sb = new StringBuilder();
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");

                int count = 0;
                for (RevCommit commit : commits) {
                    String hash = commit.getId().getName().substring(0, 7);
                    String message = commit.getFullMessage().split("\n")[0];
                    String author = commit.getAuthorIdent().getName();
                    Date date = new Date(commit.getCommitTime() * 1000L);

                    sb.append(String.format("%s | %s | %s | %s\n",
                        hash,
                        message.length() > 60 ? message.substring(0, 60) + "..." : message,
                        author,
                        dateFormat.format(date)
                    ));

                    count++;
                }

                if (count == 0) {
                    return "No commits found.";
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