//package com.backend.service;
//
//import com.fasterxml.jackson.databind.JsonNode;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.fasterxml.jackson.databind.node.ArrayNode;
//import com.fasterxml.jackson.databind.node.ObjectNode;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.http.*;
//import org.springframework.stereotype.Service;
//import org.springframework.web.client.RestTemplate;
//
//import java.util.*;
//
//@Slf4j
//@Service
//public class GeminiService {
//
//    @Value("${gemini.api.key:}")
//    private String geminiApiKey;
//
//    @Value("${gemini.api.url:https://generativelanguage.googleapis.com/v1beta/models/gemini-3.6-flash:generateContent}")
//    private String geminiApiUrl;
//
//    @Value("${gemini.model:gemini-3.6-flash}")
//    private String geminiModel;
//
//    // ✅ ADDED: Mock mode flag
//    @Value("${gemini.mock.mode:false}")
//    private boolean mockMode;
//
//    private final RestTemplate restTemplate;
//    private final ObjectMapper objectMapper;
//
//    public GeminiService() {
//        this.restTemplate = new RestTemplate();
//        this.objectMapper = new ObjectMapper();
//    }
//
//    // ==================== MOCK QUESTIONS ====================
//
//    private static final Map<String, List<String>> MOCK_QUESTIONS = new HashMap<>();
//
//    static {
//        MOCK_QUESTIONS.put("Java", Arrays.asList(
//            "Explain the difference between abstract class and interface in Java.",
//            "What is the Java memory model? Explain heap and stack.",
//            "How does garbage collection work in Java?",
//            "What is the difference between ArrayList and LinkedList?",
//            "Explain the Collections framework in Java.",
//            "What is the difference between equals() and == in Java?",
//            "Explain the Singleton design pattern in Java.",
//            "What is multithreading in Java?",
//            "What is the difference between final, finally, and finalize?",
//            "Explain the Spring Boot auto-configuration."
//        ));
//        MOCK_QUESTIONS.put("React", Arrays.asList(
//            "Explain the component lifecycle in React.",
//            "What is the difference between state and props?",
//            "How does the Virtual DOM work?",
//            "Explain React hooks and their use cases.",
//            "What is the Context API and when to use it?",
//            "What is the difference between controlled and uncontrolled components?",
//            "Explain the useEffect hook and its dependencies.",
//            "What is React Fiber and how does it work?",
//            "Explain Redux and its core principles.",
//            "What are React keys and why are they important?"
//        ));
//        MOCK_QUESTIONS.put("Python", Arrays.asList(
//            "Explain list comprehension in Python.",
//            "What is the difference between deep copy and shallow copy?",
//            "How does garbage collection work in Python?",
//            "Explain decorators in Python.",
//            "What is the Global Interpreter Lock (GIL)?",
//            "What is the difference between a tuple and a list?",
//            "Explain the use of 'self' in Python classes.",
//            "What are Python generators and how do they work?",
//            "Explain the difference between Django and Flask.",
//            "What is type hinting in Python?"
//        ));
//        MOCK_QUESTIONS.put("General", Arrays.asList(
//            "Tell me about yourself and your experience.",
//            "What is your biggest achievement?",
//            "How do you handle pressure and deadlines?",
//            "Describe a challenge you faced and how you overcame it.",
//            "Where do you see yourself in 5 years?",
//            "Why did you choose this career path?",
//            "How do you handle conflict in a team?",
//            "What are your strengths and weaknesses?",
//            "Tell me about a time you failed and what you learned.",
//            "What motivates you to do your best work?"
//        ));
//        MOCK_QUESTIONS.put("English", Arrays.asList(
//            "Describe your daily routine in detail.",
//            "Tell me about your favorite book or movie.",
//            "What are your strengths and weaknesses?",
//            "Describe a memorable travel experience.",
//            "What are your future goals?",
//            "Tell me about your best friend.",
//            "What do you like to do in your free time?",
//            "Describe your dream job.",
//            "What is the most important lesson you've learned in life?",
//            "Tell me about a time you helped someone."
//        ));
//    }
//
//    // ==================== GENERATE QUESTION ====================
//
//    /**
//     * Generate interview question - REAL-TIME using Gemini OR MOCK
//     */
//    public String generateQuestion(String topic, int questionNumber, int totalQuestions, List<String> previousAnswers) {
//        // ✅ CHECK: If mock mode is enabled, use mock questions
//        if (mockMode) {
//            log.info("🔵 Using MOCK mode for question generation");
//            return getMockQuestion(topic, questionNumber);
//        }
//
//        if (geminiApiKey == null || geminiApiKey.isEmpty() || geminiApiKey.equals("YOUR_GEMINI_API_KEY_HERE")) {
//            log.error("Gemini API key not configured!");
//            return "⚠️ Gemini API key not configured. Please add your API key to application.properties";
//        }
//
//        try {
//            String prompt = buildQuestionPrompt(topic, questionNumber, totalQuestions, previousAnswers);
//            
//            log.info("📝 Generating question for topic: {}, question: {}/{}", topic, questionNumber, totalQuestions);
//            
//            String response = callGeminiAPI(prompt);
//            
//            if (response == null) {
//                log.error("❌ Gemini API returned null response");
//                return "Sorry, I couldn't generate a question. Please try again.";
//            }
//            
//            String question = extractTextFromResponse(response);
//            log.info("✅ Generated question: {}", question);
//            return cleanQuestion(question);
//
//        } catch (Exception e) {
//            log.error("❌ Error generating question: {}", e.getMessage());
//            e.printStackTrace();
//            return "Sorry, I couldn't generate a question. Please try again.";
//        }
//    }
//
//    /**
//     * ✅ NEW: Get mock question from predefined list
//     */
//    private String getMockQuestion(String topic, int questionNumber) {
//        List<String> questions = MOCK_QUESTIONS.getOrDefault(topic, MOCK_QUESTIONS.get("General"));
//        int index = (questionNumber - 1) % questions.size();
//        String question = questions.get(index);
//        log.info("📝 Mock Question: {}", question);
//        return question;
//    }
//
//    private String buildQuestionPrompt(String topic, int questionNumber, int totalQuestions,
//            List<String> previousAnswers) {
//        StringBuilder prompt = new StringBuilder();
//
//        prompt.append("You are an expert technical interviewer conducting a ").append(topic);
//        prompt.append(" interview. Generate a realistic interview question.\n\n");
//
//        prompt.append("Interview Context:\n");
//        prompt.append("- Topic: ").append(topic).append("\n");
//        prompt.append("- Question ").append(questionNumber).append(" of ").append(totalQuestions).append("\n");
//
//        if (previousAnswers != null && !previousAnswers.isEmpty()) {
//            prompt.append("- Previous answers provided by the candidate:\n");
//            for (int i = 0; i < previousAnswers.size(); i++) {
//                String answer = previousAnswers.get(i);
//                if (answer.length() > 200) {
//                    answer = answer.substring(0, 200) + "...";
//                }
//                prompt.append("  Answer ").append(i + 1).append(": ").append(answer).append("\n");
//            }
//            prompt.append("\nBased on the candidate's previous answers, generate a relevant follow-up question.\n");
//        }
//
//        prompt.append("\nQuestion Requirements:\n");
//        prompt.append("1. Be specific and relevant to ").append(topic).append("\n");
//        prompt.append("2. Challenge the candidate appropriately\n");
//        prompt.append("3. Take approximately 2-3 minutes to answer\n");
//        prompt.append("4. Be clear and unambiguous\n");
//        prompt.append("5. Avoid yes/no questions\n");
//        prompt.append("6. Return ONLY the question text, nothing else\n");
//        prompt.append("7. Do not include question numbers or any formatting\n");
//
//        return prompt.toString();
//    }
//
//    private String cleanQuestion(String question) {
//        if (question == null)
//            return "Please answer the following question.";
//
//        question = question.replaceAll("^\\d+[\\.\\)]\\s*", "");
//        question = question.replaceAll("^Q\\d+[\\.\\)]\\s*", "");
//        question = question.replaceAll("^Question\\s*\\d+[\\.\\)]\\s*", "");
//        question = question.trim();
//
//        if (!question.endsWith("?") && !question.endsWith(".")) {
//            question = question + "?";
//        }
//
//        return question;
//    }
//
//    // ==================== CALL GEMINI API ====================
//
//    private String callGeminiAPI(String prompt) {
//        try {
//            HttpHeaders headers = new HttpHeaders();
//            headers.setContentType(MediaType.APPLICATION_JSON);
//            headers.set("x-goog-api-key", geminiApiKey);
//
//            ObjectNode root = objectMapper.createObjectNode();
//            ObjectNode content = objectMapper.createObjectNode();
//            ArrayNode parts = objectMapper.createArrayNode();
//            ObjectNode part = objectMapper.createObjectNode();
//            part.put("text", prompt);
//            parts.add(part);
//            content.set("parts", parts);
//            ArrayNode contents = objectMapper.createArrayNode();
//            contents.add(content);
//            root.set("contents", contents);
//
//            String requestBody = objectMapper.writeValueAsString(root);
//            String url = geminiApiUrl;
//
//            // ✅ ADD DETAILED LOGGING
//            log.info("=========================================");
//            log.info("🔵 CALLING GEMINI API");
//            log.info("URL: {}", url);
//            log.info("API Key (first 10 chars): {}", geminiApiKey.substring(0, Math.min(10, geminiApiKey.length())) + "...");
//            log.info("Request Body: {}", requestBody);
//            log.info("=========================================");
//
//            HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);
//            ResponseEntity<String> response = restTemplate.exchange(
//                url, HttpMethod.POST, entity, String.class
//            );
//
//            // ✅ LOG RESPONSE
//            log.info("=========================================");
//            log.info("🟢 GEMINI RESPONSE");
//            log.info("Status Code: {}", response.getStatusCode());
//            log.info("Response Body: {}", response.getBody());
//            log.info("=========================================");
//
//            if (response.getStatusCode().is2xxSuccessful()) {
//                return response.getBody();
//            } else {
//                log.error("❌ Gemini API error: {} - {}", response.getStatusCode(), response.getBody());
//                return null;
//            }
//
//        } catch (Exception e) {
//            log.error("❌ Error calling Gemini API: {}", e.getMessage());
//            e.printStackTrace();
//            return null;
//        }
//    }
//
//    private String extractTextFromResponse(String response) {
//        try {
//            if (response == null) {
//                return null;
//            }
//
//            JsonNode root = objectMapper.readTree(response);
//            JsonNode candidates = root.get("candidates");
//
//            if (candidates != null && candidates.isArray() && candidates.size() > 0) {
//                JsonNode firstCandidate = candidates.get(0);
//                JsonNode content = firstCandidate.get("content");
//
//                if (content != null) {
//                    JsonNode parts = content.get("parts");
//                    if (parts != null && parts.isArray() && parts.size() > 0) {
//                        JsonNode firstPart = parts.get(0);
//                        JsonNode text = firstPart.get("text");
//                        if (text != null) {
//                            return text.asText();
//                        }
//                    }
//                }
//            }
//
//            log.error("Could not extract text from response: {}", response);
//            return null;
//
//        } catch (Exception e) {
//            log.error("Error parsing Gemini response: {}", e.getMessage());
//            return null;
//        }
//    }
//
//    /**
//     * Check if user is active (silence detection)
//     */
//    public boolean isUserActive(String lastMessage) {
//        return lastMessage != null && !lastMessage.trim().isEmpty() && lastMessage.trim().length() > 2;
//    }
//
//    // ==================== ENGLISH PRACTICE ====================
//
//    public String generateEnglishPractice(List<String> previousMessages) {
//        // ✅ CHECK: If mock mode is enabled, use mock conversation
//        if (mockMode) {
//            log.info("🔵 Using MOCK mode for English practice");
//            return getMockEnglishResponse(previousMessages);
//        }
//
//        if (geminiApiKey == null || geminiApiKey.isEmpty() || geminiApiKey.equals("YOUR_GEMINI_API_KEY_HERE")) {
//            log.error("Gemini API key not configured!");
//            return "Hello! I'm here to help you practice English. How are you doing today?";
//        }
//
//        try {
//            String prompt = buildEnglishPrompt(previousMessages);
//            String response = callGeminiAPI(prompt);
//
//            if (response == null) {
//                return "Let's continue our conversation. Tell me something interesting about your day!";
//            }
//
//            return extractTextFromResponse(response);
//
//        } catch (Exception e) {
//            log.error("Error generating English practice: {}", e.getMessage());
//            return "Let's continue our conversation. Tell me something interesting about your day!";
//        }
//    }
//
//    private String getMockEnglishResponse(List<String> previousMessages) {
//        String[] responses = {
//            "That's interesting! Tell me more about that.",
//            "I see. How did that make you feel?",
//            "That's a great point! Can you elaborate?",
//            "Interesting perspective! What do you think about...",
//            "I understand. What would you do differently next time?",
//            "That's wonderful! How did you get started with that?",
//            "I appreciate you sharing that. What's your favorite part?",
//            "That's a valuable lesson. How has it helped you since?",
//            "Great job! What's the next step in your journey?",
//            "That's impressive! What inspired you to do that?"
//        };
//        int index = previousMessages != null ? previousMessages.size() % responses.length : 0;
//        return responses[index];
//    }
//
//    private String buildEnglishPrompt(List<String> previousMessages) {
//        StringBuilder prompt = new StringBuilder();
//
//        prompt.append("You are an English language tutor having a casual conversation with a learner.\n\n");
//        prompt.append("Your goal is to help them practice English in a natural, friendly way.\n\n");
//
//        if (previousMessages != null && !previousMessages.isEmpty()) {
//            prompt.append("Conversation History:\n");
//            for (int i = 0; i < previousMessages.size(); i++) {
//                String msg = previousMessages.get(i);
//                if (msg.length() > 300) {
//                    msg = msg.substring(0, 300) + "...";
//                }
//                prompt.append("  User: ").append(msg).append("\n");
//            }
//            prompt.append("\nContinue the conversation naturally.\n");
//        }
//
//        prompt.append("\nGuidelines:\n");
//        prompt.append("1. Keep the conversation natural and engaging\n");
//        prompt.append("2. Ask open-ended questions\n");
//        prompt.append("3. Use appropriate vocabulary for the learner's level\n");
//        prompt.append("4. Make it a two-way conversation\n");
//        prompt.append("5. Keep your response concise (2-3 sentences)\n");
//        prompt.append("6. Do NOT correct grammar in this response - only conversation\n");
//        prompt.append("7. Return ONLY the response text, nothing else\n");
//
//        return prompt.toString();
//    }
//
//    // ==================== EVALUATION ====================
//
//    public Map<String, Object> evaluateAnswer(String topic, String question, String answer) {
//        Map<String, Object> result = new HashMap<>();
//
//        // ✅ CHECK: If mock mode is enabled, use mock evaluation
//        if (mockMode) {
//            log.info("🔵 Using MOCK mode for evaluation");
//            return getMockEvaluation(answer);
//        }
//
//        if (geminiApiKey == null || geminiApiKey.isEmpty() || geminiApiKey.equals("YOUR_GEMINI_API_KEY_HERE")) {
//            log.error("Gemini API key not configured!");
//            return getDefaultEvaluation("API key not configured");
//        }
//
//        try {
//            String prompt = buildEvaluationPrompt(topic, question, answer);
//            String response = callGeminiAPI(prompt);
//
//            if (response == null) {
//                return getDefaultEvaluation("No response from AI");
//            }
//
//            return parseEvaluationResponse(response);
//
//        } catch (Exception e) {
//            log.error("Error evaluating answer: {}", e.getMessage());
//            return getDefaultEvaluation("Error evaluating answer");
//        }
//    }
//
//    /**
//     * ✅ NEW: Get mock evaluation based on answer length
//     */
//    private Map<String, Object> getMockEvaluation(String answer) {
//        Map<String, Object> result = new HashMap<>();
//        
//        int answerLength = answer != null ? answer.length() : 0;
//        int score;
//        String contentFeedback;
//        String weaknesses;
//        
//        if (answerLength > 50) {
//            score = 80 + (int)(Math.random() * 15); // 80-95
//            contentFeedback = "Good coverage of the topic. Well structured answer.";
//            weaknesses = "Could add more depth and technical details.";
//        } else if (answerLength > 20) {
//            score = 55 + (int)(Math.random() * 20); // 55-75
//            contentFeedback = "Good start, but could be more detailed.";
//            weaknesses = "More depth needed in the explanation.";
//        } else {
//            score = 30 + (int)(Math.random() * 25); // 30-55
//            contentFeedback = "Brief answer. Expand on the key points.";
//            weaknesses = "Need more detailed explanation.";
//        }
//
//        result.put("score", Math.min(score, 98));
//        result.put("grammar", "Good grammar. Minor improvements possible.");
//        result.put("fluency", "Fluent and well-structured.");
//        result.put("content", contentFeedback);
//        result.put("strengths", "Clear communication, good effort.");
//        result.put("weaknesses", weaknesses);
//
//        log.info("📊 Mock Score: {}", result.get("score"));
//        return result;
//    }
//
//    private String buildEvaluationPrompt(String topic, String question, String answer) {
//        return String.format(
//                "You are an expert interviewer evaluating a candidate's answer.\n\n" + "Topic: %s\n" + "Question: %s\n"
//                        + "Candidate's Answer: %s\n\n" + "Evaluate using this rubric (score 0-100):\n"
//                        + "1. Clarity (30%%): How clear and well-structured is the answer?\n"
//                        + "2. Relevance (30%%): How relevant is the answer to the question?\n"
//                        + "3. Completeness (25%%): Did the user cover all key points?\n"
//                        + "4. Fluency (15%%): How fluent and professional is the language?\n\n"
//                        + "Return ONLY valid JSON with these exact keys:\n" + "{\n" + "  \"score\": 85,\n"
//                        + "  \"grammar\": \"Grammar correction suggestions (1-2 sentences)\",\n"
//                        + "  \"fluency\": \"Fluency feedback (1-2 sentences)\",\n"
//                        + "  \"content\": \"Content feedback (1-2 sentences)\",\n"
//                        + "  \"strengths\": \"What was good (1-2 sentences)\",\n"
//                        + "  \"weaknesses\": \"What needs improvement (1-2 sentences)\"\n" + "}",
//                topic, question, answer);
//    }
//
//    private Map<String, Object> parseEvaluationResponse(String response) {
//        Map<String, Object> result = new HashMap<>();
//
//        try {
//            if (response == null) {
//                return getDefaultEvaluation("No response from AI");
//            }
//
//            String jsonStr = response;
//            int start = response.indexOf('{');
//            int end = response.lastIndexOf('}');
//            if (start != -1 && end != -1) {
//                jsonStr = response.substring(start, end + 1);
//            }
//
//            JsonNode root = objectMapper.readTree(jsonStr);
//
//            result.put("score", root.has("score") ? root.get("score").asInt(70) : 70);
//            result.put("grammar", root.has("grammar") ? root.get("grammar").asText() : "Good grammar.");
//            result.put("fluency", root.has("fluency") ? root.get("fluency").asText() : "Fluent response.");
//            result.put("content", root.has("content") ? root.get("content").asText() : "Good content.");
//            result.put("strengths", root.has("strengths") ? root.get("strengths").asText() : "Clear communication.");
//            result.put("weaknesses",
//                    root.has("weaknesses") ? root.get("weaknesses").asText() : "Could add more depth.");
//
//        } catch (Exception e) {
//            log.error("Error parsing evaluation: {}", e.getMessage());
//            return getDefaultEvaluation("Error parsing evaluation");
//        }
//
//        return result;
//    }
//
//    private Map<String, Object> getDefaultEvaluation(String errorMessage) {
//        Map<String, Object> result = new HashMap<>();
//        result.put("score", 0);
//        result.put("grammar", errorMessage);
//        result.put("fluency", errorMessage);
//        result.put("content", errorMessage);
//        result.put("strengths", "Please try again");
//        result.put("weaknesses", "Please try again");
//        return result;
//    }
//
//    // ==================== ENGLISH EVALUATION ====================
//
//    public Map<String, String> evaluateEnglish(String userMessage) {
//        Map<String, String> result = new HashMap<>();
//
//        // ✅ CHECK: If mock mode is enabled, use mock evaluation
//        if (mockMode) {
//            log.info("🔵 Using MOCK mode for English evaluation");
//            result.put("grammar", "Good grammar! Minor improvements possible.");
//            result.put("fluency", "Fluent and natural conversation.");
//            result.put("suggestions", "Try using more varied vocabulary.");
//            return result;
//        }
//
//        if (geminiApiKey == null || geminiApiKey.isEmpty() || geminiApiKey.equals("YOUR_GEMINI_API_KEY_HERE")) {
//            log.error("Gemini API key not configured!");
//            result.put("grammar", "Good effort!");
//            result.put("fluency", "Keep practicing!");
//            result.put("suggestions", "Try to use more varied vocabulary.");
//            return result;
//        }
//
//        try {
//            String prompt = String.format(
//                    "You are an English tutor. Analyze this user's message and provide feedback.\n\n"
//                            + "User's Message: %s\n\n" + "Return ONLY valid JSON with these exact keys:\n" + "{\n"
//                            + "  \"grammar\": \"Grammar corrections (1-2 sentences)\",\n"
//                            + "  \"fluency\": \"Fluency feedback (1-2 sentences)\",\n"
//                            + "  \"suggestions\": \"Suggestions for improvement (1-2 sentences)\"\n" + "}",
//                    userMessage);
//
//            String response = callGeminiAPI(prompt);
//
//            if (response == null) {
//                result.put("grammar", "Good effort!");
//                result.put("fluency", "Keep practicing!");
//                result.put("suggestions", "Try to use more varied vocabulary.");
//                return result;
//            }
//
//            return parseEnglishFeedback(response);
//
//        } catch (Exception e) {
//            log.error("Error evaluating English: {}", e.getMessage());
//            result.put("grammar", "Good effort!");
//            result.put("fluency", "Keep practicing!");
//            result.put("suggestions", "Try to use more varied vocabulary.");
//            return result;
//        }
//    }
//
//    private Map<String, String> parseEnglishFeedback(String response) {
//        Map<String, String> result = new HashMap<>();
//
//        try {
//            if (response == null) {
//                return result;
//            }
//
//            String jsonStr = response;
//            int start = response.indexOf('{');
//            int end = response.lastIndexOf('}');
//            if (start != -1 && end != -1) {
//                jsonStr = response.substring(start, end + 1);
//            }
//
//            JsonNode root = objectMapper.readTree(jsonStr);
//            result.put("grammar", root.has("grammar") ? root.get("grammar").asText() : "Good grammar!");
//            result.put("fluency", root.has("fluency") ? root.get("fluency").asText() : "Good fluency!");
//            result.put("suggestions", root.has("suggestions") ? root.get("suggestions").asText() : "Keep practicing!");
//
//        } catch (Exception e) {
//            log.error("Error parsing English feedback: {}", e.getMessage());
//        }
//
//        return result;
//    }
//
//    // ==================== SUMMARY ====================
//
//    public String generateSummary(String topic, String mode, List<Integer> scores, List<String> questions,
//            List<String> answers) {
//        // ✅ CHECK: If mock mode is enabled, use mock summary
//        if (mockMode) {
//            log.info("🔵 Using MOCK mode for summary generation");
//            return getMockSummary(topic, scores);
//        }
//
//        if (geminiApiKey == null || geminiApiKey.isEmpty() || geminiApiKey.equals("YOUR_GEMINI_API_KEY_HERE")) {
//            log.error("Gemini API key not configured!");
//            return "Great effort! Keep practicing to improve your skills.";
//        }
//
//        try {
//            String prompt = buildSummaryPrompt(topic, mode, scores, questions, answers);
//            String response = callGeminiAPI(prompt);
//
//            if (response == null) {
//                return "Great effort! Keep practicing to improve your skills.";
//            }
//
//            return extractTextFromResponse(response);
//
//        } catch (Exception e) {
//            log.error("Error generating summary: {}", e.getMessage());
//            return "Great effort! Keep practicing to improve your skills.";
//        }
//    }
//
//    private String getMockSummary(String topic, List<Integer> scores) {
//        double avg = scores != null && !scores.isEmpty() 
//            ? scores.stream().mapToInt(Integer::intValue).average().orElse(0) 
//            : 0;
//        
//        return String.format(
//            "Overall Score: %.1f/100\n\n" +
//            "Good understanding of %s!\n\n" +
//            "Strengths:\n" +
//            "• Clear communication\n" +
//            "• Good technical knowledge\n" +
//            "• Structured answers\n\n" +
//            "Areas for Improvement:\n" +
//            "• Add more technical depth\n" +
//            "• Practice concise answers\n" +
//            "• Use more examples\n\n" +
//            "Keep practicing to improve your skills!",
//            avg, topic
//        );
//    }
//
//    private String buildSummaryPrompt(String topic, String mode, List<Integer> scores, List<String> questions,
//            List<String> answers) {
//        StringBuilder prompt = new StringBuilder();
//
//        prompt.append("You are an expert interviewer. Summarize this interview session.\n\n");
//        prompt.append("Topic: ").append(topic).append("\n");
//        prompt.append("Mode: ").append(mode).append("\n");
//
//        if (scores != null && !scores.isEmpty()) {
//            double avg = scores.stream().mapToInt(Integer::intValue).average().orElse(0);
//            prompt.append("Average Score: ").append(avg).append("/100\n");
//            prompt.append("Scores: ").append(scores).append("\n");
//        }
//
//        if (questions != null && !questions.isEmpty()) {
//            prompt.append("\nQuestions and Answers:\n");
//            int maxAnswers = Math.min(questions.size(), answers != null ? answers.size() : 0);
//            for (int i = 0; i < maxAnswers; i++) {
//                prompt.append("Q").append(i + 1).append(": ").append(questions.get(i)).append("\n");
//                String answer = answers.get(i);
//                if (answer.length() > 300) {
//                    answer = answer.substring(0, 300) + "...";
//                }
//                prompt.append("A").append(i + 1).append(": ").append(answer).append("\n\n");
//            }
//        }
//
//        prompt.append("\nProvide a comprehensive summary with:\n");
//        prompt.append("1. Overall performance assessment (2-3 sentences)\n");
//        prompt.append("2. Top 3 Strengths (bullet points)\n");
//        prompt.append("3. Top 3 Areas for Improvement (bullet points)\n");
//        prompt.append("4. Common mistakes observed\n");
//        prompt.append("5. Final recommendation for improvement\n\n");
//        prompt.append("Make it constructive, encouraging, and professional.");
//
//        return prompt.toString();
//    }
//}

package com.backend.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Slf4j
@Service
public class GeminiService {

	@Value("${groq.api.key:}")
	private String groqApiKey;

	@Value("${groq.api.url:https://api.groq.com/openai/v1/chat/completions}")
	private String groqApiUrl;

	@Value("${groq.model:llama-3.1-8b-instant}") // ✅ CHANGE DEFAULT
	private String groqModel;

	// ✅ NEW: Mock mode flag (same as GeminiService)
	@Value("${groq.mock.mode:false}")
	private boolean mockMode;

	private final RestTemplate restTemplate;
	private final ObjectMapper objectMapper;

	public GeminiService() {
		this.restTemplate = new RestTemplate();
		this.objectMapper = new ObjectMapper();
	}

	// ==================== MOCK QUESTIONS (SAME AS GEMINI) ====================

	private static final Map<String, List<String>> MOCK_QUESTIONS = new HashMap<>();

	static {
		MOCK_QUESTIONS.put("Java", Arrays.asList("Explain the difference between abstract class and interface in Java.",
				"What is the Java memory model? Explain heap and stack.", "How does garbage collection work in Java?",
				"What is the difference between ArrayList and LinkedList?",
				"Explain the Collections framework in Java.", "What is the difference between equals() and == in Java?",
				"Explain the Singleton design pattern in Java.", "What is multithreading in Java?",
				"What is the difference between final, finally, and finalize?",
				"Explain the Spring Boot auto-configuration."));
		MOCK_QUESTIONS.put("React",
				Arrays.asList("Explain the component lifecycle in React.",
						"What is the difference between state and props?", "How does the Virtual DOM work?",
						"Explain React hooks and their use cases.", "What is the Context API and when to use it?",
						"What is the difference between controlled and uncontrolled components?",
						"Explain the useEffect hook and its dependencies.", "What is React Fiber and how does it work?",
						"Explain Redux and its core principles.", "What are React keys and why are they important?"));
		MOCK_QUESTIONS.put("Python", Arrays.asList("Explain list comprehension in Python.",
				"What is the difference between deep copy and shallow copy?",
				"How does garbage collection work in Python?", "Explain decorators in Python.",
				"What is the Global Interpreter Lock (GIL)?", "What is the difference between a tuple and a list?",
				"Explain the use of 'self' in Python classes.", "What are Python generators and how do they work?",
				"Explain the difference between Django and Flask.", "What is type hinting in Python?"));
		MOCK_QUESTIONS.put("General", Arrays.asList("Tell me about yourself and your experience.",
				"What is your biggest achievement?", "How do you handle pressure and deadlines?",
				"Describe a challenge you faced and how you overcame it.", "Where do you see yourself in 5 years?",
				"Why did you choose this career path?", "How do you handle conflict in a team?",
				"What are your strengths and weaknesses?", "Tell me about a time you failed and what you learned.",
				"What motivates you to do your best work?"));
		MOCK_QUESTIONS.put("English",
				Arrays.asList("Describe your daily routine in detail.", "Tell me about your favorite book or movie.",
						"What are your strengths and weaknesses?", "Describe a memorable travel experience.",
						"What are your future goals?", "Tell me about your best friend.",
						"What do you like to do in your free time?", "Describe your dream job.",
						"What is the most important lesson you've learned in life?",
						"Tell me about a time you helped someone."));
	}

	// ==================== GENERATE QUESTION ====================

	/**
	 * Generate interview question - REAL-TIME using Groq OR MOCK
	 */
	public String generateQuestion(String topic, int questionNumber, int totalQuestions, List<String> previousAnswers) {
		// ✅ SAME: Mock mode check
		if (mockMode) {
			log.info("🔵 Using MOCK mode for question generation");
			return getMockQuestion(topic, questionNumber);
		}

		// ✅ CHANGED: Groq API key check
		if (groqApiKey == null || groqApiKey.isEmpty() || groqApiKey.equals("YOUR_GROQ_API_KEY_HERE")) {
			log.error("Groq API key not configured!");
			return "⚠️ Groq API key not configured. Please add your API key to application.properties";
		}

		try {
			String prompt = buildQuestionPrompt(topic, questionNumber, totalQuestions, previousAnswers);

			log.info("📝 Generating question for topic: {}, question: {}/{}", topic, questionNumber, totalQuestions);

			// ✅ CHANGED: Call Groq API instead of Gemini
			String response = callGroqAPI(prompt);

			if (response == null) {
				log.error("❌ Groq API returned null response");
				return "Sorry, I couldn't generate a question. Please try again.";
			}

			String question = extractTextFromResponse(response);
			log.info("✅ Generated question: {}", question);
			return cleanQuestion(question);

		} catch (Exception e) {
			log.error("❌ Error generating question: {}", e.getMessage());
			e.printStackTrace();
			return "Sorry, I couldn't generate a question. Please try again.";
		}
	}

	/**
	 * ✅ SAME: Get mock question from predefined list
	 */
	private String getMockQuestion(String topic, int questionNumber) {
		List<String> questions = MOCK_QUESTIONS.getOrDefault(topic, MOCK_QUESTIONS.get("General"));
		int index = (questionNumber - 1) % questions.size();
		String question = questions.get(index);
		log.info("📝 Mock Question: {}", question);
		return question;
	}

	// ==================== BUILD PROMPTS (SAME AS GEMINI) ====================

	private String buildQuestionPrompt(String topic, int questionNumber, int totalQuestions,
			List<String> previousAnswers) {
		StringBuilder prompt = new StringBuilder();

		prompt.append("You are an expert technical interviewer conducting a ").append(topic);
		prompt.append(" interview. Generate a single, clear interview question.\n\n");

		prompt.append("Interview Context:\n");
		prompt.append("- Topic: ").append(topic).append("\n");
		prompt.append("- Question ").append(questionNumber).append(" of ").append(totalQuestions).append("\n");

		if (previousAnswers != null && !previousAnswers.isEmpty()) {
			prompt.append("- Previous answers provided by the candidate:\n");
			for (int i = 0; i < previousAnswers.size(); i++) {
				String answer = previousAnswers.get(i);
				if (answer.length() > 200) {
					answer = answer.substring(0, 200) + "...";
				}
				prompt.append("  Answer ").append(i + 1).append(": ").append(answer).append("\n");
			}
			prompt.append("\nBased on the candidate's previous answers, generate a relevant follow-up question.\n");
		}

		prompt.append("\nCRITICAL RULES (MUST FOLLOW):\n");
		prompt.append("1. Return ONLY the question text, nothing else\n");
		prompt.append("2. Do NOT include tables, markdown, or any formatting\n");
		prompt.append("3. Do NOT include bullet points or numbered lists\n");
		prompt.append("4. Do NOT include sections like 'Why this question', 'Rationale', or 'Example'\n");
		prompt.append("5. Do NOT include code blocks or inline code\n");
		prompt.append("6. Keep the question clear, concise, and professional (2-3 sentences max)\n");
		prompt.append("7. The question should take approximately 2-3 minutes to answer\n");
		prompt.append("8. Avoid yes/no questions\n");
		prompt.append("9. Return ONLY the question text, nothing else\n");
		prompt.append("10. Example: 'Explain the difference between abstract class and interface in Java.'\n");

		return prompt.toString();
	}

	private String cleanQuestion(String question) {
		if (question == null)
			return "Please answer the following question.";

		// ✅ Remove markdown formatting
		question = question.replaceAll("\\*\\*([^*]+)\\*\\*", "$1"); // Remove bold
		question = question.replaceAll("\\*([^*]+)\\*", "$1"); // Remove italic
		question = question.replaceAll("`([^`]+)`", "$1"); // Remove inline code
		question = question.replaceAll("```[\\s\\S]*?```", ""); // Remove code blocks

		// ✅ Remove tables (markdown tables with | and ---)
		question = question.replaceAll("(?m)^\\s*\\|.*\\|\\s*$", ""); // Remove table rows
		question = question.replaceAll("(?m)^\\s*\\|-+\\|\\s*$", ""); // Remove table separators

		// ✅ Remove entire sections after common headers
		String[] removeAfter = { "Rationale:", "Reasoning:", "Why this question:", "Explanation:", "Example:",
				"When to choose which:", "Typical usage scenarios:", "Conclusion:" };
		for (String header : removeAfter) {
			int index = question.indexOf(header);
			if (index != -1) {
				question = question.substring(0, index);
			}
		}

		// ✅ Remove extra content after common patterns
		question = question.replaceAll("(?i)\\|\\s*Aspect\\s*\\|.*", "");
		question = question.replaceAll("(?i)\\|\\s*HashMap\\s*\\|.*", "");
		question = question.replaceAll("(?i)\\|\\s*Hashtable\\s*\\|.*", "");

		// ✅ Remove question numbers and labels
		question = question.replaceAll("^\\d+[\\.\\)]\\s*", "");
		question = question.replaceAll("^Q\\d+[\\.\\)]\\s*", "");
		question = question.replaceAll("^Question\\s*\\d+[\\.\\)]\\s*", "");

		// ✅ Remove "Generated Interview Question" prefix
		question = question.replaceAll("(?i)^\\s*Generated\\s+Interview\\s+Question\\s*", "");
		question = question.replaceAll("(?i)^\\s*Interview\\s+Question\\s*", "");

		// ✅ Remove extra whitespace and newlines
		question = question.replaceAll("\\s+", " ").trim();

		// ✅ Remove "---" separators
		question = question.replaceAll("---", "");

		// ✅ If question is still too long or contains table-like content, extract the
		// first meaningful sentence
		if (question.length() > 500 || question.contains("|")) {
			// Try to extract just the question part
			String[] sentences = question.split("[.!?]");
			StringBuilder cleanQuestion = new StringBuilder();
			for (String sentence : sentences) {
				String trimmed = sentence.trim();
				// Skip if it looks like table data or metadata
				if (!trimmed.isEmpty() && !trimmed.contains("|") && !trimmed.contains("Aspect")
						&& !trimmed.contains("HashMap")) {
					cleanQuestion.append(trimmed).append(".");
					if (cleanQuestion.length() > 50)
						break;
				}
			}
			if (cleanQuestion.length() > 0) {
				question = cleanQuestion.toString();
			}
		}

		// ✅ Ensure it ends with a question mark
		if (!question.endsWith("?") && !question.endsWith(".")) {
			question = question + "?";
		}

		return question;
	}

	// ==================== CALL GROQ API (CHANGED) ====================

	/**
	 * ✅ CHANGED: Call Groq API with OpenAI-compatible format
	 */
	private String callGroqAPI(String prompt) {
		try {
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			// ✅ CHANGED: Groq uses Bearer token authentication
			headers.set("Authorization", "Bearer " + groqApiKey);

			// ✅ CHANGED: Groq uses OpenAI chat completion format
			ObjectNode root = objectMapper.createObjectNode();
			root.put("model", groqModel);
			root.put("temperature", 0.7);
			root.put("max_tokens", 500);

			// ✅ CHANGED: Messages array format
			ArrayNode messages = objectMapper.createArrayNode();
			ObjectNode userMessage = objectMapper.createObjectNode();
			userMessage.put("role", "user");
			userMessage.put("content", prompt);
			messages.add(userMessage);
			root.set("messages", messages);

			String requestBody = objectMapper.writeValueAsString(root);

			// ✅ CHANGED: URL doesn't need query parameter
			String url = groqApiUrl;

			log.info("=========================================");
			log.info("🔵 CALLING GROQ API");
			log.info("URL: {}", url);
			log.info("Model: {}", groqModel);
			log.info("Request Body: {}", requestBody);
			log.info("=========================================");

			HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);
			ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);

			log.info("=========================================");
			log.info("🟢 GROQ RESPONSE");
			log.info("Status Code: {}", response.getStatusCode());
			log.info("Response Body: {}", response.getBody());
			log.info("=========================================");

			if (response.getStatusCode().is2xxSuccessful()) {
				return response.getBody();
			} else {
				log.error("❌ Groq API error: {} - {}", response.getStatusCode(), response.getBody());
				return null;
			}

		} catch (Exception e) {
			log.error("❌ Error calling Groq API: {}", e.getMessage());
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * ✅ CHANGED: Extract text from Groq's OpenAI-compatible response
	 */
	private String extractTextFromResponse(String response) {
		try {
			if (response == null) {
				return null;
			}

			JsonNode root = objectMapper.readTree(response);

			// ✅ CHANGED: Groq uses OpenAI response format
			JsonNode choices = root.get("choices");
			if (choices != null && choices.isArray() && choices.size() > 0) {
				JsonNode firstChoice = choices.get(0);
				JsonNode message = firstChoice.get("message");
				if (message != null && message.has("content")) {
					String content = message.get("content").asText();
					// ✅ SAME: Clean the response
					return content.trim();
				}
			}

			log.error("Could not extract text from Groq response: {}", response);
			return null;

		} catch (Exception e) {
			log.error("Error parsing Groq response: {}", e.getMessage());
			return null;
		}
	}

	/**
	 * Check if user is active (silence detection) - SAME AS GEMINI
	 */
	public boolean isUserActive(String lastMessage) {
		return lastMessage != null && !lastMessage.trim().isEmpty() && lastMessage.trim().length() > 2;
	}

	// ==================== ENGLISH PRACTICE (SAME AS GEMINI) ====================

	public String generateEnglishPractice(List<String> previousMessages) {
		if (mockMode) {
			log.info("🔵 Using MOCK mode for English practice");
			return getMockEnglishResponse(previousMessages);
		}

		if (groqApiKey == null || groqApiKey.isEmpty() || groqApiKey.equals("YOUR_GROQ_API_KEY_HERE")) {
			log.error("Groq API key not configured!");
			return "Hello! I'm here to help you practice English. How are you doing today?";
		}

		try {
			String prompt = buildEnglishPrompt(previousMessages);
			String response = callGroqAPI(prompt);

			if (response == null) {
				return "Let's continue our conversation. Tell me something interesting about your day!";
			}

			return extractTextFromResponse(response);

		} catch (Exception e) {
			log.error("Error generating English practice: {}", e.getMessage());
			return "Let's continue our conversation. Tell me something interesting about your day!";
		}
	}

	private String getMockEnglishResponse(List<String> previousMessages) {
		String[] responses = { "That's interesting! Tell me more about that.", "I see. How did that make you feel?",
				"That's a great point! Can you elaborate?", "Interesting perspective! What do you think about...",
				"I understand. What would you do differently next time?",
				"That's wonderful! How did you get started with that?",
				"I appreciate you sharing that. What's your favorite part?",
				"That's a valuable lesson. How has it helped you since?",
				"Great job! What's the next step in your journey?",
				"That's impressive! What inspired you to do that?" };
		int index = previousMessages != null ? previousMessages.size() % responses.length : 0;
		return responses[index];
	}

	private String buildEnglishPrompt(List<String> previousMessages) {
		StringBuilder prompt = new StringBuilder();

		prompt.append("You are an English language tutor having a casual conversation with a learner.\n\n");
		prompt.append("Your goal is to help them practice English in a natural, friendly way.\n\n");

		if (previousMessages != null && !previousMessages.isEmpty()) {
			prompt.append("Conversation History:\n");
			for (int i = 0; i < previousMessages.size(); i++) {
				String msg = previousMessages.get(i);
				if (msg.length() > 300) {
					msg = msg.substring(0, 300) + "...";
				}
				prompt.append("  User: ").append(msg).append("\n");
			}
			prompt.append("\nContinue the conversation naturally.\n");
		}

		prompt.append("\nGuidelines:\n");
		prompt.append("1. Keep the conversation natural and engaging\n");
		prompt.append("2. Ask open-ended questions\n");
		prompt.append("3. Use appropriate vocabulary for the learner's level\n");
		prompt.append("4. Make it a two-way conversation\n");
		prompt.append("5. Keep your response concise (2-3 sentences)\n");
		prompt.append("6. Do NOT correct grammar in this response - only conversation\n");
		prompt.append("7. Return ONLY the response text, nothing else\n");

		return prompt.toString();
	}

	// ==================== EVALUATION (SAME AS GEMINI) ====================

	public Map<String, Object> evaluateAnswer(String topic, String question, String answer) {
		Map<String, Object> result = new HashMap<>();

		if (mockMode) {
			log.info("🔵 Using MOCK mode for evaluation");
			return getMockEvaluation(answer);
		}

		if (groqApiKey == null || groqApiKey.isEmpty() || groqApiKey.equals("YOUR_GROQ_API_KEY_HERE")) {
			log.error("Groq API key not configured!");
			return getDefaultEvaluation("API key not configured");
		}

		try {
			String prompt = buildEvaluationPrompt(topic, question, answer);
			String response = callGroqAPI(prompt);

			if (response == null) {
				return getDefaultEvaluation("No response from AI");
			}

			return parseEvaluationResponse(response);

		} catch (Exception e) {
			log.error("Error evaluating answer: {}", e.getMessage());
			return getDefaultEvaluation("Error evaluating answer");
		}
	}

	private Map<String, Object> getMockEvaluation(String answer) {
		Map<String, Object> result = new HashMap<>();

		int answerLength = answer != null ? answer.length() : 0;
		int score;
		String contentFeedback;
		String weaknesses;

		if (answerLength > 50) {
			score = 80 + (int) (Math.random() * 15);
			contentFeedback = "Good coverage of the topic. Well structured answer.";
			weaknesses = "Could add more depth and technical details.";
		} else if (answerLength > 20) {
			score = 55 + (int) (Math.random() * 20);
			contentFeedback = "Good start, but could be more detailed.";
			weaknesses = "More depth needed in the explanation.";
		} else {
			score = 30 + (int) (Math.random() * 25);
			contentFeedback = "Brief answer. Expand on the key points.";
			weaknesses = "Need more detailed explanation.";
		}

		result.put("score", Math.min(score, 98));
		result.put("grammar", "Good grammar. Minor improvements possible.");
		result.put("fluency", "Fluent and well-structured.");
		result.put("content", contentFeedback);
		result.put("strengths", "Clear communication, good effort.");
		result.put("weaknesses", weaknesses);

		log.info("📊 Mock Score: {}", result.get("score"));
		return result;
	}

	private String buildEvaluationPrompt(String topic, String question, String answer) {
		return String.format(
				"You are an expert interviewer evaluating a candidate's answer.\n\n" + "Topic: %s\n" + "Question: %s\n"
						+ "Candidate's Answer: %s\n\n" + "Evaluate using this rubric (score 0-100):\n"
						+ "1. Clarity (30%%): How clear and well-structured is the answer?\n"
						+ "2. Relevance (30%%): How relevant is the answer to the question?\n"
						+ "3. Completeness (25%%): Did the user cover all key points?\n"
						+ "4. Fluency (15%%): How fluent and professional is the language?\n\n"
						+ "Return ONLY valid JSON with these exact keys:\n" + "{\n" + "  \"score\": 85,\n"
						+ "  \"grammar\": \"Grammar correction suggestions (1-2 sentences)\",\n"
						+ "  \"fluency\": \"Fluency feedback (1-2 sentences)\",\n"
						+ "  \"content\": \"Content feedback (1-2 sentences)\",\n"
						+ "  \"strengths\": \"What was good (1-2 sentences)\",\n"
						+ "  \"weaknesses\": \"What needs improvement (1-2 sentences)\"\n" + "}",
				topic, question, answer);
	}

	/**
	 * ✅ CHANGED: Parse Groq's OpenAI-compatible response
	 */
	private Map<String, Object> parseEvaluationResponse(String response) {
		Map<String, Object> result = new HashMap<>();

		try {
			if (response == null) {
				return getDefaultEvaluation("No response from AI");
			}

			String jsonStr = response;
			int start = response.indexOf('{');
			int end = response.lastIndexOf('}');
			if (start != -1 && end != -1) {
				jsonStr = response.substring(start, end + 1);
			}

			JsonNode root = objectMapper.readTree(jsonStr);

			result.put("score", root.has("score") ? root.get("score").asInt(70) : 70);
			result.put("grammar", root.has("grammar") ? root.get("grammar").asText() : "Good grammar.");
			result.put("fluency", root.has("fluency") ? root.get("fluency").asText() : "Fluent response.");
			result.put("content", root.has("content") ? root.get("content").asText() : "Good content.");
			result.put("strengths", root.has("strengths") ? root.get("strengths").asText() : "Clear communication.");
			result.put("weaknesses",
					root.has("weaknesses") ? root.get("weaknesses").asText() : "Could add more depth.");

		} catch (Exception e) {
			log.error("Error parsing evaluation: {}", e.getMessage());
			return getDefaultEvaluation("Error parsing evaluation");
		}

		return result;
	}

	private Map<String, Object> getDefaultEvaluation(String errorMessage) {
		Map<String, Object> result = new HashMap<>();
		result.put("score", 0);
		result.put("grammar", errorMessage);
		result.put("fluency", errorMessage);
		result.put("content", errorMessage);
		result.put("strengths", "Please try again");
		result.put("weaknesses", "Please try again");
		return result;
	}

	// ==================== ENGLISH EVALUATION (SAME AS GEMINI) ====================

	public Map<String, String> evaluateEnglish(String userMessage) {
		Map<String, String> result = new HashMap<>();

		if (mockMode) {
			log.info("🔵 Using MOCK mode for English evaluation");
			result.put("grammar", "Good grammar! Minor improvements possible.");
			result.put("fluency", "Fluent and natural conversation.");
			result.put("suggestions", "Try using more varied vocabulary.");
			return result;
		}

		if (groqApiKey == null || groqApiKey.isEmpty() || groqApiKey.equals("YOUR_GROQ_API_KEY_HERE")) {
			log.error("Groq API key not configured!");
			result.put("grammar", "Good effort!");
			result.put("fluency", "Keep practicing!");
			result.put("suggestions", "Try to use more varied vocabulary.");
			return result;
		}

		try {
			String prompt = String.format(
					"You are an English tutor. Analyze this user's message and provide feedback.\n\n"
							+ "User's Message: %s\n\n" + "Return ONLY valid JSON with these exact keys:\n" + "{\n"
							+ "  \"grammar\": \"Grammar corrections (1-2 sentences)\",\n"
							+ "  \"fluency\": \"Fluency feedback (1-2 sentences)\",\n"
							+ "  \"suggestions\": \"Suggestions for improvement (1-2 sentences)\"\n" + "}",
					userMessage);

			String response = callGroqAPI(prompt);

			if (response == null) {
				result.put("grammar", "Good effort!");
				result.put("fluency", "Keep practicing!");
				result.put("suggestions", "Try to use more varied vocabulary.");
				return result;
			}

			return parseEnglishFeedback(response);

		} catch (Exception e) {
			log.error("Error evaluating English: {}", e.getMessage());
			result.put("grammar", "Good effort!");
			result.put("fluency", "Keep practicing!");
			result.put("suggestions", "Try to use more varied vocabulary.");
			return result;
		}
	}

	private Map<String, String> parseEnglishFeedback(String response) {
		Map<String, String> result = new HashMap<>();

		try {
			if (response == null) {
				return result;
			}

			String jsonStr = response;
			int start = response.indexOf('{');
			int end = response.lastIndexOf('}');
			if (start != -1 && end != -1) {
				jsonStr = response.substring(start, end + 1);
			}

			JsonNode root = objectMapper.readTree(jsonStr);
			result.put("grammar", root.has("grammar") ? root.get("grammar").asText() : "Good grammar!");
			result.put("fluency", root.has("fluency") ? root.get("fluency").asText() : "Good fluency!");
			result.put("suggestions", root.has("suggestions") ? root.get("suggestions").asText() : "Keep practicing!");

		} catch (Exception e) {
			log.error("Error parsing English feedback: {}", e.getMessage());
		}

		return result;
	}

	// ==================== SUMMARY (SAME AS GEMINI) ====================

	public String generateSummary(String topic, String mode, List<Integer> scores, List<String> questions,
			List<String> answers) {
		if (mockMode) {
			log.info("🔵 Using MOCK mode for summary generation");
			return getMockSummary(topic, scores);
		}

		if (groqApiKey == null || groqApiKey.isEmpty() || groqApiKey.equals("YOUR_GROQ_API_KEY_HERE")) {
			log.error("Groq API key not configured!");
			return "Great effort! Keep practicing to improve your skills.";
		}

		try {
			String prompt = buildSummaryPrompt(topic, mode, scores, questions, answers);
			String response = callGroqAPI(prompt);

			if (response == null) {
				return "Great effort! Keep practicing to improve your skills.";
			}

			return extractTextFromResponse(response);

		} catch (Exception e) {
			log.error("Error generating summary: {}", e.getMessage());
			return "Great effort! Keep practicing to improve your skills.";
		}
	}

	private String getMockSummary(String topic, List<Integer> scores) {
		double avg = scores != null && !scores.isEmpty()
				? scores.stream().mapToInt(Integer::intValue).average().orElse(0)
				: 0;

		return String.format("Overall Score: %.1f/100\n\n" + "Good understanding of %s!\n\n" + "Strengths:\n"
				+ "• Clear communication\n" + "• Good technical knowledge\n" + "• Structured answers\n\n"
				+ "Areas for Improvement:\n" + "• Add more technical depth\n" + "• Practice concise answers\n"
				+ "• Use more examples\n\n" + "Keep practicing to improve your skills!", avg, topic);
	}

	private String buildSummaryPrompt(String topic, String mode, List<Integer> scores, List<String> questions,
			List<String> answers) {
		StringBuilder prompt = new StringBuilder();

		prompt.append("You are an expert interviewer. Summarize this interview session.\n\n");
		prompt.append("Topic: ").append(topic).append("\n");
		prompt.append("Mode: ").append(mode).append("\n");

		if (scores != null && !scores.isEmpty()) {
			double avg = scores.stream().mapToInt(Integer::intValue).average().orElse(0);
			prompt.append("Average Score: ").append(avg).append("/100\n");
			prompt.append("Scores: ").append(scores).append("\n");
		}

		if (questions != null && !questions.isEmpty()) {
			prompt.append("\nQuestions and Answers:\n");
			int maxAnswers = Math.min(questions.size(), answers != null ? answers.size() : 0);
			for (int i = 0; i < maxAnswers; i++) {
				prompt.append("Q").append(i + 1).append(": ").append(questions.get(i)).append("\n");
				String answer = answers.get(i);
				if (answer.length() > 300) {
					answer = answer.substring(0, 300) + "...";
				}
				prompt.append("A").append(i + 1).append(": ").append(answer).append("\n\n");
			}
		}

		prompt.append("\nProvide a comprehensive summary with:\n");
		prompt.append("1. Overall performance assessment (2-3 sentences)\n");
		prompt.append("2. Top 3 Strengths (bullet points)\n");
		prompt.append("3. Top 3 Areas for Improvement (bullet points)\n");
		prompt.append("4. Common mistakes observed\n");
		prompt.append("5. Final recommendation for improvement\n\n");
		prompt.append("Make it constructive, encouraging, and professional.");

		return prompt.toString();
	}
}
