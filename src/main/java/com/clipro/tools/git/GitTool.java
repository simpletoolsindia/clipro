package com.clipro.tools.git;

import com.clipro.tools.Tool;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.Status;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Base class for Git tools using JGit.
 */
public abstract class GitTool implements Tool {

    protected final Path repositoryDirectory;

    public GitTool() {
        this.repositoryDirectory = Path.of(System.getProperty("user.dir"));
    }

    public GitTool(String repoDir) {
        this.repositoryDirectory = Path.of(repoDir);
    }

    protected Repository openRepository() throws IOException {
        File repoDir = repositoryDirectory.toFile();
        return new FileRepositoryBuilder()
            .findGitDir(repoDir)
            .build();
    }

    protected Git openGit() throws IOException {
        return new Git(openRepository());
    }

    protected String formatStatus(Status status) {
        StringBuilder sb = new StringBuilder();

        Set<String> modified = status.getModified();
        Set<String> added = status.getAdded();
        Set<String> removed = status.getRemoved();
        Set<String> untracked = status.getUntracked();

        if (modified.isEmpty() && added.isEmpty() && removed.isEmpty() && untracked.isEmpty()) {
            return "Nothing to commit, working tree clean.";
        }

        sb.append("Changes:\n");

        if (!modified.isEmpty()) {
            sb.append("  Modified: ");
            sb.append(String.join(", ", modified));
            sb.append("\n");
        }

        if (!added.isEmpty()) {
            sb.append("  Added: ");
            sb.append(String.join(", ", added));
            sb.append("\n");
        }

        if (!removed.isEmpty()) {
            sb.append("  Removed: ");
            sb.append(String.join(", ", removed));
            sb.append("\n");
        }

        if (!untracked.isEmpty()) {
            sb.append("  Untracked: ");
            sb.append(String.join(", ", untracked));
            sb.append("\n");
        }

        return sb.toString();
    }
}