package com.clipro.session;

import com.clipro.llm.models.Message;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Conversation compaction for long-running sessions.
 * Summarizes old messages to save context window space.
 */
public class ConversationCompactor {

    // How many messages to keep in original form (not summarized)
    private static final int KEEP_RECENT_COUNT = 10;

    // Target message count after compaction
    private static final int TARGET_MESSAGE_COUNT = 50;

    // Summarized message prefix
    private static final String SUMMARIZED_PREFIX = "[Previous conversation summarized] ";

    /**
     * Compact a list of messages.
     * Returns a new list with old messages summarized.
     */
    public List<Message> compact(List<Message> messages) {
        if (messages == null || messages.size() <= TARGET_MESSAGE_COUNT) {
            return messages != null ? new ArrayList<>(messages) : new ArrayList<>();
        }

        List<Message> result = new ArrayList<>();

        // Keep recent messages as-is
        List<Message> recentMessages = messages.subList(
            Math.max(0, messages.size() - KEEP_RECENT_COUNT),
            messages.size()
        );
        result.addAll(recentMessages);

        // Summarize old messages
        List<Message> oldMessages = messages.subList(0, messages.size() - KEEP_RECENT_COUNT);
        if (!oldMessages.isEmpty()) {
            String summary = summarizeMessages(oldMessages);
            Message summarized = Message.system(SUMMARIZED_PREFIX + summary);
            result.add(0, summarized);
        }

        return result;
    }

    /**
     * Summarize a group of messages.
     * Creates a summary message describing the conversation context.
     */
    public String summarizeMessages(List<Message> messages) {
        if (messages == null || messages.isEmpty()) {
            return "Empty conversation.";
        }

        int userMessages = 0;
        int assistantMessages = 0;
        int toolMessages = 0;
        Set<String> topics = new LinkedHashSet<>();
        Set<String> files = new LinkedHashSet<>();

        for (Message msg : messages) {
            String role = msg.getRole();
            String content = msg.getContent();

            if (content == null) continue;

            switch (role) {
                case "user":
                    userMessages++;
                    // Extract potential topics/files from content
                    topics.addAll(extractTopics(content));
                    files.addAll(extractFilePaths(content));
                    break;
                case "assistant":
                    assistantMessages++;
                    break;
                case "tool":
                    toolMessages++;
                    break;
            }
        }

        StringBuilder summary = new StringBuilder();
        summary.append("Conversation summary (").append(messages.size()).append(" messages): ");
        summary.append(userMessages).append(" user messages, ");
        summary.append(assistantMessages).append(" assistant responses, ");
        summary.append(toolMessages).append(" tool calls.");

        if (!topics.isEmpty()) {
            summary.append(" Topics: ");
            summary.append(topics.stream().limit(5).collect(Collectors.joining(", ")));
        }

        if (!files.isEmpty()) {
            summary.append(" Files: ");
            summary.append(files.stream().limit(5).collect(Collectors.joining(", ")));
        }

        return summary.toString();
    }

    /**
     * Extract potential topics from text.
     */
    private Set<String> extractTopics(String text) {
        Set<String> topics = new LinkedHashSet<>();

        // Simple topic extraction - look for significant words
        String[] words = text.toLowerCase().split("\\s+");
        for (String word : words) {
            // Filter out common words
            if (word.length() > 5 && !isCommonWord(word)) {
                topics.add(word);
            }
            if (topics.size() >= 10) break;
        }

        return topics;
    }

    /**
     * Extract file paths from text.
     */
    private Set<String> extractFilePaths(String text) {
        Set<String> files = new LinkedHashSet<>();

        // Match common file patterns
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(
            "(?:\\./|/)[\\w./\\\\-]+\\.\\w+"
        );
        java.util.regex.Matcher matcher = pattern.matcher(text);

        while (matcher.find() && files.size() < 5) {
            files.add(matcher.group());
        }

        return files;
    }

    /**
     * Check if word is a common/stop word.
     */
    private boolean isCommonWord(String word) {
        Set<String> stopWords = Set.of(
            "the", "be", "to", "of", "and", "a", "in", "that", "have", "i",
            "it", "for", "not", "on", "with", "he", "as", "you", "do", "at",
            "this", "but", "his", "by", "from", "they", "we", "say", "her", "she",
            "or", "an", "will", "my", "one", "all", "would", "there", "their", "what",
            "so", "up", "out", "if", "about", "who", "get", "which", "go", "me",
            "when", "make", "can", "like", "time", "no", "just", "him", "know", "take",
            "people", "into", "year", "your", "good", "some", "could", "them", "see", "other",
            "than", "then", "now", "look", "only", "come", "its", "over", "think", "also",
            "back", "after", "use", "two", "how", "our", "work", "first", "well", "way",
            "even", "new", "want", "because", "any", "these", "give", "day", "most", "us"
        );
        return stopWords.contains(word);
    }

    /**
     * Calculate compaction ratio.
     */
    public double getCompactionRatio(List<Message> before, List<Message> after) {
        if (before == null || before.isEmpty()) return 1.0;
        if (after == null || after.isEmpty()) return 0.0;
        return (double) after.size() / before.size();
    }

    /**
     * Estimate tokens saved by compaction.
     */
    public int estimateTokensSaved(List<Message> before, List<Message> after) {
        if (before == null || after == null) return 0;

        int beforeTokens = estimateTokens(before);
        int afterTokens = estimateTokens(after);
        return Math.max(0, beforeTokens - afterTokens);
    }

    private int estimateTokens(List<Message> messages) {
        return messages.stream()
            .mapToInt(msg -> msg.getContent() != null ? msg.getContent().length() / 4 : 0)
            .sum();
    }
}
