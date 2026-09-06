import { useState, useEffect, useRef } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { useAuth } from '../hooks/useAuth';
import { interviewService } from '../services/interviewService';
import Timer from '../components/interview/Timer';
import VoiceControls from '../components/interview/VoiceControls';
import VoiceToggle from '../components/interview/VoiceToggle';  // ✅ ALREADY THERE
import EndInterviewButton from '../components/interview/EndInterviewButton';
import speechService from '../services/speechService';  // ✅ ALREADY THERE
import toast from 'react-hot-toast';
import './InterviewSession.css';

const InterviewSession = () => {
  const { sessionId } = useParams();
  const { user } = useAuth();
  const navigate = useNavigate();
  
  const [question, setQuestion] = useState(null);
  const [loading, setLoading] = useState(true);
  const [answer, setAnswer] = useState('');
  const [submitting, setSubmitting] = useState(false);
  const [feedback, setFeedback] = useState(null);
  const [sessionEnded, setSessionEnded] = useState(false);
  const [timeLeft, setTimeLeft] = useState(180);
  const [isActive, setIsActive] = useState(true);
  const [isEnglishMode, setIsEnglishMode] = useState(false);
  const [voiceEnabled, setVoiceEnabled] = useState(true);  // ✅ ALREADY THERE
  const [isPlaying, setIsPlaying] = useState(false);  // ✅ ALREADY THERE

  const answerRef = useRef('');

  useEffect(() => {
    fetchQuestion();
  }, []);

  // ============================================
  // ✅ ADD: Speak Question Function (Add This Entire Block)
  // ============================================
  const speakQuestion = (text) => {
    if (!voiceEnabled || !text) return;
    
    // Clean text for speech
    const cleanText = text.replace(/[#*_`]/g, '').replace(/\s+/g, ' ').trim();
    
    // Set female voice
    const voiceName = 'Microsoft Zira - English (United States)';
    speechService.setVoiceByName(voiceName);
    
    // Create utterance with optimal settings
    const utterance = new SpeechSynthesisUtterance(cleanText);
    utterance.pitch = 1.2;      // Natural pitch
    utterance.rate = 0.9;       // Clear and understandable
    utterance.voice = speechService.getVoices().find(v => v.name === voiceName);
    
    utterance.onstart = () => {
      setIsPlaying(true);
      console.log('🔊 Speaking question...');
    };
    
    utterance.onend = () => {
      setIsPlaying(false);
      console.log('✅ Question reading completed');
    };
    
    utterance.onerror = (event) => {
      setIsPlaying(false);
      console.error('Speech error:', event);
    };
    
    window.speechSynthesis.speak(utterance);
  };

  // ============================================
  // ✅ ADD: Read question when it changes (Add This Entire Block)
  // ============================================
  useEffect(() => {
    if (question && question.question && voiceEnabled) {
      speakQuestion(question.question);
    }
    
    return () => {
      window.speechSynthesis.cancel();
    };
  }, [question, voiceEnabled]);

  // ============================================
  // ✅ ADD: Toggle Voice Function (Add This Entire Block)
  // ============================================
  const toggleVoice = () => {
    if (voiceEnabled) {
      window.speechSynthesis.cancel();
      setIsPlaying(false);
    } else {
      if (question && question.question) {
        speakQuestion(question.question);
      }
    }
    setVoiceEnabled(!voiceEnabled);
  };

  const fetchQuestion = async () => {
    setLoading(true);
    try {
      const response = await interviewService.getNextQuestion(sessionId);
      const data = response.data.data;
      setQuestion(data);
      setTimeLeft(data.timeLimitSeconds || 180);
      setIsEnglishMode(data.mode === 'ENGLISH');
      setFeedback(null);
      setAnswer('');
      answerRef.current = '';
      setIsActive(true);
    } catch (error) {
      if (error.response?.data?.message?.includes('completed')) {
        toast('Interview already completed', { icon: 'ℹ️', style: { color: '#0284c7' } });
        navigate(`/interview/summary/${sessionId}`);
      } else {
        toast.error('Failed to load question');
      }
    } finally {
      setLoading(false);
    }
  };

  const handleSubmitAnswer = async () => {
    if (!answer.trim()) {
      toast.error('Please enter your answer');
      return;
    }
    if (sessionEnded || submitting) return;

    // ✅ ADD THIS LINE: Stop voice before submitting
    window.speechSynthesis.cancel();

    setSubmitting(true);
    try {
      const response = await interviewService.submitAnswer(
        sessionId,
        question.questionNumber,
        answer
      );
      const data = response.data.data;
      setFeedback(data);
      setSessionEnded(!!data.isComplete);
      setIsActive(!data.isComplete);

      if (data.isComplete) {
        toast.success('🎉 Interview completed!');
        setTimeout(() => {
          navigate(`/interview/summary/${sessionId}`);
        }, 3000);
      } else {
        toast.success('✅ Answer recorded!');
        setTimeout(() => {
          setFeedback(null);
          fetchQuestion();
        }, 2000);
      }
    } catch (error) {
      toast.error('Failed to submit answer');
    } finally {
      setSubmitting(false);
    }
  };

  const handleVoiceTranscript = (transcript) => {
    setAnswer(transcript);
    answerRef.current = transcript;
  };

  const handleEndSession = async () => {
    if (sessionEnded) return;
    
    // ✅ ADD THIS LINE: Stop voice before ending
    window.speechSynthesis.cancel();
    
    setSessionEnded(true);
    setIsActive(false);
    try {
      await interviewService.endSession(sessionId, 'user_requested');
      toast('Session ended', { icon: 'ℹ️', style: { color: '#0284c7' } });
      navigate(`/interview/summary/${sessionId}`);
    } catch (error) {
      const backendMsg =
        error?.response?.data?.message ||
        error?.response?.data?.error ||
        error?.message ||
        'Unknown error';
      console.error('Failed to end session:', {
        status: error?.response?.status,
        data: error?.response?.data,
        message: error?.message,
      });
      toast.error(`Failed to end session: ${backendMsg}`);
      setSessionEnded(false);
      setIsActive(true);
    }
  };

  const handleTimerEnd = () => {
    if (sessionEnded || submitting) return;
    
    // ✅ ADD THIS LINE: Stop voice on timer end
    window.speechSynthesis.cancel();
    
    toast.error('⏰ Time is up! Submitting your answer...');
    if (answer.trim()) {
      handleSubmitAnswer();
    } else {
      toast.error('No answer provided. Moving to next question...');
      setTimeout(() => fetchQuestion(), 2000);
    }
  };

  const handleSilence = () => {
    if (sessionEnded) return;
    toast.error('Are you still there?');
  };

  if (loading) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <div className="text-gray-400 text-lg">Loading question...</div>
      </div>
    );
  }

  if (!question) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <div className="text-red-500 text-lg">No question found</div>
      </div>
    );
  }

  return (
    <div className="min-h-screen flex items-center justify-center bg-gray-50 px-4 py-8">
      <div className="bg-white rounded-2xl shadow-xl p-6 max-w-3xl w-full">
        {/* Header */}
        <div className="flex justify-between items-center mb-4 pb-4 border-b-2 border-gray-100">
          <div>
            <span className="inline-block px-3 py-1 bg-blue-100 text-blue-700 rounded-full text-sm font-semibold">
              {isEnglishMode ? '🗣️ English Practice' : question.topic}
            </span>
            <span className="ml-3 text-sm text-gray-500">
              Question {question.questionNumber} of {question.totalQuestions}
            </span>
          </div>
          <div className="flex items-center gap-2">
            <VoiceToggle
              isPlaying={isPlaying}
              onToggle={toggleVoice}
              isEnabled={voiceEnabled}
            />
            <EndInterviewButton onEnd={handleEndSession} />
          </div>
        </div>

        {/* Timer */}
        <Timer
          duration={timeLeft}
          onEnd={handleTimerEnd}
          onSilence={handleSilence}
          isActive={isActive}
        />

        {/* Question with Voice Indicator */}
        <div className="my-6 p-4 bg-gray-50 rounded-xl">
          <div className="flex justify-between items-center mb-2">
            <p className="text-xs text-gray-400 uppercase font-semibold">Question</p>
            {/* ✅ ADD: Voice Playing Indicator */}
            {voiceEnabled && isPlaying && (
              <span className="text-xs text-green-500 animate-pulse font-semibold">
                🔊 Speaking...
              </span>
            )}
          </div>
          <p className="text-lg text-gray-800 leading-relaxed">{question.question}</p>
        </div>

        {/* Answer Input */}
        <div className="my-4">
          <div className="flex justify-between items-center mb-2">
            <label className="font-semibold text-gray-700">Your Answer</label>
            <VoiceControls
              onTranscript={handleVoiceTranscript}
              isListening={false}
            />
          </div>
          <textarea
            className="w-full px-4 py-3 border-2 border-gray-200 rounded-xl focus:outline-none focus:border-blue-500 resize-none min-h-[120px]"
            value={answer}
            onChange={(e) => setAnswer(e.target.value)}
            placeholder={isEnglishMode ? "Speak or type your response..." : "Type your answer here..."}
            rows={5}
            disabled={submitting || (feedback && feedback.isComplete)}
          />
        </div>

        {/* Feedback Section */}
        {feedback && (
          <div
            className={`my-4 p-4 rounded-xl border-2 ${
              feedback.isComplete
                ? 'bg-green-50 border-green-200'
                : 'bg-blue-50 border-blue-200 text-center py-4'
            }`}
          >
            {feedback.isComplete ? (
              <>
                <h4 className="text-sm font-bold text-green-700 uppercase mb-3">
                  📊 Interview Complete!
                </h4>
                <div className="grid grid-cols-1 md:grid-cols-2 gap-3">
                  <div className="bg-white p-3 rounded-lg">
                    <span className="text-xs text-gray-400 uppercase">Overall Score</span>
                    <p className="text-2xl font-bold text-blue-600">{feedback.score}/100</p>
                  </div>
                  <div className="bg-white p-3 rounded-lg md:col-span-2">
                    <span className="text-xs text-gray-400 uppercase">Detailed Feedback</span>
                    <p className="text-sm text-gray-700 whitespace-pre-wrap mt-1">
                      {feedback.contentFeedback}
                    </p>
                  </div>
                </div>
                <p className="text-center mt-3 font-semibold text-green-600">
                  🎉 Interview complete! Redirecting to summary...
                </p>
              </>
            ) : (
              <>
                <p className="text-blue-600 font-semibold">✅ Answer recorded!</p>
                <p className="text-sm text-gray-500 mt-1">Loading next question...</p>
              </>
            )}
          </div>
        )}

        {/* Submit Button */}
        {!feedback && (
          <button
            className={`w-full py-3 rounded-xl font-bold text-white transition-all ${
              submitting || !answer.trim()
                ? 'bg-gray-400 cursor-not-allowed'
                : 'bg-blue-600 hover:bg-blue-700 shadow-md hover:shadow-lg'
            }`}
            onClick={handleSubmitAnswer}
            disabled={submitting || !answer.trim()}
          >
            {submitting ? 'Submitting...' : 'Submit Answer'}
          </button>
        )}
      </div>
    </div>
  );
};

export default InterviewSession;