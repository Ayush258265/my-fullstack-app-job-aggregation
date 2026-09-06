package com.backend.service;

import com.backend.dto.*;
import com.backend.entity.InterviewSession;
import com.backend.entity.User;
import com.backend.repo.InterviewSessionRepository;
import com.backend.repo.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class InterviewService {

	private final InterviewSessionRepository sessionRepository;
	private final UserRepository userRepository;
	private final GeminiService geminiService;

	private final Map<Long, InterviewState> sessionState = new HashMap<>();

	// ==================== CHECK DAILY LIMIT ====================

	public boolean canStartInterview(User user) {
		LocalDate today = LocalDate.now();

		if (user.isAdmin()) {
			return true;
		}

		if (user.getLastInterviewDate() == null || !user.getLastInterviewDate().equals(today)) {
			user.setDailyInterviewsCount(0);
			user.setLastInterviewDate(today);
			userRepository.save(user);
		}

		return user.getDailyInterviewsCount() < 6;
	}

	public int getRemainingInterviews(User user) {
		LocalDate today = LocalDate.now();

		if (user.isAdmin()) {
			return Integer.MAX_VALUE;
		}

		if (user.getLastInterviewDate() == null || !user.getLastInterviewDate().equals(today)) {
			return 6;
		}

		return Math.max(0, 6 - user.getDailyInterviewsCount());
	}

	// ==================== START INTERVIEW ====================

	@Transactional
	public QuestionResponse startInterview(Long userId, InterviewSetupRequest request) {
		User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));

		if (!canStartInterview(user)) {
			throw new RuntimeException("Daily limit reached! You can take maximum 6 interviews per day.");
		}

		int totalQuestions = request.getDurationMinutes() / 3;
		if (totalQuestions < 1) {
			throw new RuntimeException("Duration must be at least 3 minutes");
		}

		InterviewSession session = InterviewSession.builder().user(user).topic(request.getTopic())
				.durationMinutes(request.getDurationMinutes()).totalQuestions(totalQuestions).answeredQuestions(0)
				.completed(false).startedAt(LocalDateTime.now()).build();

		session = sessionRepository.save(session);

		user.setDailyInterviewsCount(user.getDailyInterviewsCount() + 1);
		user.setLastInterviewDate(LocalDate.now());
		userRepository.save(user);

		InterviewState state = new InterviewState();
		state.currentQuestion = 1;
		state.answers = new ArrayList<>();
		state.scores = new ArrayList<>();
		state.questions = new ArrayList<>();
		state.lastActivity = LocalDateTime.now();
		state.mode = request.getMode();
		state.topic = request.getTopic();
		sessionState.put(session.getId(), state);

		String question;
		if ("ENGLISH".equals(request.getMode())) {
			question = geminiService.generateEnglishPractice(new ArrayList<>());
		} else {
			question = geminiService.generateQuestion(request.getTopic(), 1, totalQuestions, new ArrayList<>());
		}

		state.questions.add(question);

		log.info("Interview started - Session: {}, User: {}, Topic: {}, Mode: {}, Daily Count: {}", session.getId(),
				user.getEmail(), request.getTopic(), request.getMode(), user.getDailyInterviewsCount());

		return QuestionResponse.builder().sessionId(session.getId()).questionNumber(1)
				.totalQuestions("ENGLISH".equals(request.getMode()) ? 0 : totalQuestions).question(question)
				.timeLimitSeconds("ENGLISH".equals(request.getMode()) ? 0 : 180).topic(request.getTopic())
				.isLastQuestion("ENGLISH".equals(request.getMode()) || totalQuestions == 1).mode(request.getMode())
				.build();
	}

	// ==================== GET NEXT QUESTION ====================

	public QuestionResponse getNextQuestion(Long sessionId) {
		InterviewSession session = sessionRepository.findById(sessionId)
				.orElseThrow(() -> new RuntimeException("Session not found"));

		if (session.getCompleted()) {
			throw new RuntimeException("Session already completed");
		}

		InterviewState state = sessionState.get(sessionId);
		if (state == null) {
			throw new RuntimeException("Session state not found");
		}

		if ("ENGLISH".equals(state.mode)) {
			String question = geminiService.generateEnglishPractice(state.answers);
			state.questions.add(question);
			state.lastActivity = LocalDateTime.now();

			return QuestionResponse.builder().sessionId(sessionId).questionNumber(state.questions.size())
					.totalQuestions(0).question(question).timeLimitSeconds(0).topic("English Practice")
					.isLastQuestion(false).mode("ENGLISH").build();
		}

		if (state.currentQuestion > session.getTotalQuestions()) {
			throw new RuntimeException("All questions answered");
		}

		String question = geminiService.generateQuestion(session.getTopic(), state.currentQuestion,
				session.getTotalQuestions(), state.answers);

		state.questions.add(question);
		state.lastActivity = LocalDateTime.now();

		boolean isLast = state.currentQuestion == session.getTotalQuestions();

		return QuestionResponse.builder().sessionId(sessionId).questionNumber(state.currentQuestion)
				.totalQuestions(session.getTotalQuestions()).question(question).timeLimitSeconds(180)
				.topic(session.getTopic()).isLastQuestion(isLast).mode("TOPIC").build();
	}

	// ==================== SUBMIT ANSWER ====================

	public AnswerResponse submitAnswer(Long sessionId, Integer questionNumber, String answer) {
		InterviewSession session = sessionRepository.findById(sessionId)
				.orElseThrow(() -> new RuntimeException("Session not found"));

		InterviewState state = sessionState.get(sessionId);
		if (state == null) {
			throw new RuntimeException("Session state not found");
		}

		state.answers.add(answer);
		state.lastActivity = LocalDateTime.now();

		// ENGLISH MODE - No scoring, just conversation
		if ("ENGLISH".equals(state.mode)) {
			Map<String, String> feedback = geminiService.evaluateEnglish(answer);

			session.setAnsweredQuestions(session.getAnsweredQuestions() + 1);
			sessionRepository.save(session);

			return AnswerResponse.builder().questionNumber(questionNumber).score(null)
					.grammarCorrection(feedback.get("grammar")).fluencyFeedback(feedback.get("fluency"))
					.contentFeedback(feedback.get("suggestions")).strengths("Good effort!")
					.weaknesses("Keep practicing!").isComplete(false).showNextQuestion(true).nextAction("continue")
					.message("Great! Let's continue the conversation.").mode("ENGLISH").build();
		}

		// TOPIC MODE - Store answer, DON'T show feedback yet
		String question = state.questions.get(state.questions.size() - 1);
		state.currentQuestion++;

		session.setAnsweredQuestions(session.getAnsweredQuestions() + 1);
		sessionRepository.save(session);

		boolean isComplete = state.currentQuestion > session.getTotalQuestions();

		if (isComplete) {
			// ✅ Generate all scores and summary at the end
			session.setCompleted(true);
			session.setCompletedAt(LocalDateTime.now());

			// ✅ Evaluate all answers at once
			List<Integer> scores = new ArrayList<>();
			StringBuilder allFeedback = new StringBuilder();

			for (int i = 0; i < state.answers.size(); i++) {
				String q = state.questions.get(i);
				String a = state.answers.get(i);
				Map<String, Object> feedback = geminiService.evaluateAnswer(session.getTopic(), q, a);
				int score = (int) feedback.get("score");
				scores.add(score);
				allFeedback.append("Q").append(i + 1).append(": ").append(q).append("\n");
				allFeedback.append("Score: ").append(score).append("/100\n");
				allFeedback.append("Feedback: ").append(feedback.get("content")).append("\n\n");
			}

			double avgScore = scores.stream().mapToInt(Integer::intValue).average().orElse(0);
			session.setOverallScore(BigDecimal.valueOf(avgScore));

			// Generate summary
			String summary = geminiService.generateSummary(session.getTopic(), state.mode, scores, state.questions,
					state.answers);
			session.setCommonMistakes(summary);
			sessionRepository.save(session);

			log.info("Interview completed - Session: {}, Score: {}", sessionId, avgScore);

			// ✅ Return final feedback only
			return AnswerResponse.builder().questionNumber(questionNumber).score((int) avgScore)
					.grammarCorrection("Review your answers in the summary.")
					.fluencyFeedback("Check the detailed feedback in your summary.")
					.contentFeedback(allFeedback.toString()).strengths("See strengths in summary.")
					.weaknesses("See weaknesses in summary.").isComplete(true).showNextQuestion(false).nextAction("end")
					.message("🎉 Interview completed! Generating summary...").mode("TOPIC").build();
		}

		// ✅ Return simple response without feedback
		return AnswerResponse.builder().questionNumber(questionNumber).score(null).grammarCorrection(null)
				.fluencyFeedback(null).contentFeedback(null).strengths(null).weaknesses(null).isComplete(false)
				.showNextQuestion(true).nextAction("next").message("✅ Answer recorded! Moving to next question.")
				.mode("TOPIC").build();
	}

	// ==================== END SESSION ====================

	@Transactional
	public SessionSummary endSession(Long sessionId, String reason) {
		InterviewSession session = sessionRepository.findById(sessionId)
				.orElseThrow(() -> new RuntimeException("Session not found"));

		InterviewState state = sessionState.get(sessionId);

		// ✅ Mark completed + timestamps FIRST so session always ends, even if AI calls below fail.
		session.setCompleted(true);
		session.setCompletedAt(LocalDateTime.now());
		sessionRepository.save(session);

		try {
			if (state != null && !state.answers.isEmpty()) {
				// ✅ Score all answers at end
				List<Integer> scores = new ArrayList<>();
				StringBuilder allFeedback = new StringBuilder();
				for (int i = 0; i < state.answers.size(); i++) {
					try {
						String q = state.questions.get(i);
						String a = state.answers.get(i);
						Map<String, Object> feedback = geminiService.evaluateAnswer(session.getTopic(), q, a);
						int score = (int) feedback.get("score");
						scores.add(score);
						allFeedback.append("Q").append(i + 1).append(": ").append(q).append("\n");
						allFeedback.append("Score: ").append(score).append("/100\n");
						allFeedback.append("Feedback: ").append(feedback.get("content")).append("\n\n");
					} catch (RuntimeException ex) {
						log.warn("Failed to score answer {} for session {}: {}", i + 1, sessionId, ex.getMessage());
					}
				}

				if (!scores.isEmpty()) {
					double avgScore = scores.stream().mapToInt(Integer::intValue).average().orElse(0);
					session.setOverallScore(BigDecimal.valueOf(avgScore));
				}

				try {
					String summary = geminiService.generateSummary(session.getTopic(), state.mode, scores,
							state.questions, state.answers);
					session.setCommonMistakes(summary);
				} catch (RuntimeException ex) {
					log.warn("Failed to generate AI summary for session {}: {}", sessionId, ex.getMessage());
					if (session.getCommonMistakes() == null || session.getCommonMistakes().isEmpty()) {
						session.setCommonMistakes("AI summary unavailable — ended by " + reason);
					}
				}

				log.info("Interview ended - Session: {}, Reason: {}, Score: {}", sessionId, reason,
						session.getOverallScore());
			} else if (state != null && "ENGLISH".equals(state.mode)) {
				try {
					String summary = geminiService.generateSummary("English Conversation Practice", "ENGLISH", null,
							state.questions, state.answers);
					session.setCommonMistakes(summary);
				} catch (RuntimeException ex) {
					log.warn("Failed to generate English summary for session {}: {}", sessionId, ex.getMessage());
					if (session.getCommonMistakes() == null || session.getCommonMistakes().isEmpty()) {
						session.setCommonMistakes("Great effort in English practice! Session ended " + reason);
					}
				}
			} else {
				// No answers yet / state lost after restart
				session.setCommonMistakes("No answers provided. Session ended " + reason);
				log.info("Interview ended early - Session: {}, Reason: {}", sessionId, reason);
			}

			sessionRepository.save(session);
		} finally {
			// ✅ Always clean up in-memory state, no matter what.
			sessionState.remove(sessionId);
		}

		return getSessionSummary(sessionId);
	}

	// ==================== GET SUMMARY ====================

	public SessionSummary getSessionSummary(Long sessionId) {
		InterviewSession session = sessionRepository.findById(sessionId)
				.orElseThrow(() -> new RuntimeException("Session not found"));

		InterviewState state = sessionState.get(sessionId);

		List<String> strengths = new ArrayList<>();
		List<String> weaknesses = new ArrayList<>();
		String commonMistakes = session.getCommonMistakes();

		if (commonMistakes != null) {
			String[] lines = commonMistakes.split("\n");
			for (String line : lines) {
				String trimmed = line.replace("•", "").replace("-", "").trim();
				if (line.contains("Strength") || line.contains("Good") || line.contains("Strengths")) {
					if (trimmed.length() > 2 && !trimmed.equals("Strengths:")) {
						strengths.add(trimmed);
					}
				} else if (line.contains("Improvement") || line.contains("Weak") || line.contains("Improve")) {
					if (trimmed.length() > 2 && !trimmed.equals("Improvements:")) {
						weaknesses.add(trimmed);
					}
				}
			}
		}

		if (strengths.isEmpty()) {
			strengths.add("Good communication skills");
			strengths.add("Willing to learn");
		}
		if (weaknesses.isEmpty()) {
			weaknesses.add("Could add more depth in answers");
			weaknesses.add("Practice concise responses");
		}

		String completedAt = session.getCompletedAt() != null
				? session.getCompletedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
				: null;

		Double overallScore = session.getOverallScore() != null ? session.getOverallScore().doubleValue() : null;

		return SessionSummary.builder().sessionId(session.getId()).topic(session.getTopic())
				.mode(state != null ? state.mode : "TOPIC").durationMinutes(session.getDurationMinutes())
				.totalQuestions(session.getTotalQuestions()).answeredQuestions(session.getAnsweredQuestions())
				.overallScore(overallScore).strengths(strengths).weaknesses(weaknesses).commonMistakes(commonMistakes)
				.message(session.getCompleted() ? "Interview completed" : "Interview ended early")
				.completedAt(completedAt).build();
	}

	// ==================== HISTORY ====================

	public List<SessionSummary> getInterviewHistory(Long userId) {
		User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));

		List<InterviewSession> sessions = sessionRepository.findByUserAndCompletedTrueOrderByStartedAtDesc(user);
		List<SessionSummary> history = new ArrayList<>();

		for (InterviewSession session : sessions) {
			List<String> strengths = new ArrayList<>();
			List<String> weaknesses = new ArrayList<>();

			if (session.getCommonMistakes() != null) {
				String[] lines = session.getCommonMistakes().split("\n");
				for (String line : lines) {
					String trimmed = line.replace("•", "").replace("-", "").trim();
					if (line.contains("Strength") || line.contains("Good") || line.contains("Strengths")) {
						if (trimmed.length() > 2)
							strengths.add(trimmed);
					} else if (line.contains("Improvement") || line.contains("Weak") || line.contains("Improve")) {
						if (trimmed.length() > 2)
							weaknesses.add(trimmed);
					}
				}
			}

			if (strengths.isEmpty()) {
				strengths.add("Good communication");
				strengths.add("Technical knowledge");
			}
			if (weaknesses.isEmpty()) {
				weaknesses.add("Could add more depth");
			}

			String completedAt = session.getCompletedAt() != null
					? session.getCompletedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
					: null;

			Double overallScore = session.getOverallScore() != null ? session.getOverallScore().doubleValue() : null;

			history.add(SessionSummary.builder().sessionId(session.getId()).topic(session.getTopic()).mode("TOPIC")
					.durationMinutes(session.getDurationMinutes()).totalQuestions(session.getTotalQuestions())
					.answeredQuestions(session.getAnsweredQuestions()).overallScore(overallScore).strengths(strengths)
					.weaknesses(weaknesses).commonMistakes(session.getCommonMistakes())
					.message(session.getCompleted() ? "Completed" : "Incomplete").completedAt(completedAt).build());
		}

		return history;
	}

	// ==================== UTILITY METHODS ====================

	public boolean isUserActive(Long sessionId) {
		InterviewState state = sessionState.get(sessionId);
		if (state == null)
			return false;
		return LocalDateTime.now().isBefore(state.lastActivity.plusSeconds(10));
	}

	public void updateLastActivity(Long sessionId) {
		InterviewState state = sessionState.get(sessionId);
		if (state != null) {
			state.lastActivity = LocalDateTime.now();
		}
	}

	public int getRemainingInterviewsForUser(Long userId) {
		User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
		return getRemainingInterviews(user);
	}

	// ==================== INNER CLASS ====================

	private static class InterviewState {
		int currentQuestion;
		List<String> answers;
		List<Integer> scores;
		List<String> questions;
		LocalDateTime lastActivity;
		String mode;
		String topic;
	}
}