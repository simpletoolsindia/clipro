package com.clipro.session;

import com.clipro.llm.models.Message;

import java.io.*;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * History manager for conversation history.
 * Supports search, persistence, and context management.
 */
public class HistoryManager {

    private static final String DEFAULT_DIR = System.getProperty("user.home") + "/.clipro/history";
    private static final int MAX_MESSAGES = 1000;
    private static final DateTimeFormatter FILE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd_HHmmss");

    private final Path historyDir;
    private final ConcurrentLinkedQueue<Message> messages;
    private String currentSessionId;

    public HistoryManager() {
        this(DEFAULT_DIR);
    }

    public HistoryManager(String historyDir) {
        this.historyDir = Paths.get(historyDir);
        this.messages = new ConcurrentLinkedQueue<>();
        this.currentSessionId = LocalDateTime.now().format(FILE_FORMAT);
        ensureDirectory();
    }

    private void ensureDirectory() {
        try {
            Files.createDirectories(historyDir);
        } catch (IOException e) {
            // Ignore - will use memory only
        }
    }

    public void addMessage(Message message) {
        messages.add(message);
        while (messages.size() > MAX_MESSAGES) {
            messages.poll();
        }
    }

    public void addUser(String content) {
        addMessage(Message.user(content));
    }

    public void addAssistant(String content) {
        addMessage(Message.assistant(content));
    }

    public void addSystem(String content) {
        addMessage(Message.system(content));
    }

    public void addToolResult(String content, String toolCallId) {
        addMessage(Message.tool(content, toolCallId));
    }

    public List<Message> getMessages() {
        return new ArrayList<>(messages);
    }

    public List<Message> getRecentMessages(int count) {
        return messages.stream()
            .skip(Math.max(0, messages.size() - count))
            .collect(Collectors.toList());
    }

    public void clear() {
        messages.clear();
        currentSessionId = LocalDateTime.now().format(FILE_FORMAT);
    }

    public int size() {
        return messages.size();
    }

    public List<Message> search(String query) {
        return search(query, false);
    }

    public List<Message> search(String query, boolean regex) {
        List<Message> results = new ArrayList<>();
        Pattern pattern;
        try {
            pattern = regex ? Pattern.compile(query, Pattern.CASE_INSENSITIVE)
                          : Pattern.compile(Pattern.quote(query), Pattern.CASE_INSENSITIVE);
        } catch (Exception e) {
            pattern = Pattern.compile(Pattern.quote(query), Pattern.CASE_INSENSITIVE);
        }
        for (Message msg : messages) {
            if (msg.getContent() != null && pattern.matcher(msg.getContent()).find()) {
                results.add(msg);
            }
        }
        return results;
    }

    public void save() {
        save(currentSessionId);
    }

    public void save(String sessionId) {
        Path file = historyDir.resolve(sessionId + ".json");
        try (PrintWriter writer = new PrintWriter(Files.newBufferedWriter(file))) {
            writer.println("{");
            writer.println("  \"sessionId\": \"" + sessionId + "\",");
            writer.println("  \"messages\": [");
            Iterator<Message> it = messages.iterator();
            while (it.hasNext()) {
                Message msg = it.next();
                writer.println("    {");
                writer.println("      \"role\": \"" + msg.getRole() + "\",");
                writer.println("      \"content\": " + jsonEscape(msg.getContent()));
                writer.println("    }" + (it.hasNext() ? "," : ""));
            }
            writer.println("  ]");
            writer.println("}");
        } catch (IOException e) {
            // Ignore
        }
    }

    public List<String> listSessions() {
        List<String> sessions = new ArrayList<>();
        try (var stream = Files.list(historyDir)) {
            stream.filter(f -> f.toString().endsWith(".json"))
                  .map(f -> f.getFileName().toString().replace(".json", ""))
                  .sorted(Comparator.reverseOrder())
                  .forEach(sessions::add);
        } catch (IOException e) {
            // Ignore
        }
        return sessions;
    }

    public void deleteSession(String sessionId) {
        Path file = historyDir.resolve(sessionId + ".json");
        try {
            Files.deleteIfExists(file);
        } catch (IOException e) {
            // Ignore
        }
    }

    private String jsonEscape(String s) {
        if (s == null) return "null";
        return "\"" + s.replace("\\", "\\\\")
                      .replace("\"", "\\\"")
                      .replace("\n", "\\n")
                      .replace("\r", "\\r")
                      .replace("\t", "\\t") + "\"";
    }

    public String getCurrentSessionId() {
        return currentSessionId;
    }

    public Path getHistoryDir() {
        return historyDir;
    }
}
