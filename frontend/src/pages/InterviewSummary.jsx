import { useState, useEffect } from 'react';
import { useParams, Link } from 'react-router-dom';
import { interviewService } from '../services/interviewService';
import { FaArrowLeft, FaCheckCircle, FaExclamationCircle } from 'react-icons/fa';
import toast from 'react-hot-toast';
import './InterviewSummary.css';

const InterviewSummary = () => {
    const { sessionId } = useParams();
    const [summary, setSummary] = useState(null);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        fetchSummary();
    }, [sessionId]);

    const fetchSummary = async () => {
        setLoading(true);
        try {
            const response = await interviewService.getSummary(sessionId);
            setSummary(response.data.data);
        } catch (error) {
            toast.error('Failed to load summary');
        } finally {
            setLoading(false);
        }
    };

    if (loading) {
        return (
            <div className="summary-loading">
                <div className="text-gray-400 text-lg">Loading summary...</div>
            </div>
        );
    }

    if (!summary) {
        return (
            <div className="summary-error">
                <div className="text-red-500 text-lg">No summary found</div>
            </div>
        );
    }

    return (
        <div className="summary-container">
            <div className="summary-box">
                <h1 className="summary-title">📊 Interview Summary</h1>

                {/* Score */}
                <div className="summary-score-section">
                    <div className="summary-score-circle">
                        <span className="summary-score-number">
                            {summary.overallScore ? Math.round(summary.overallScore) : 'N/A'}
                        </span>
                        <span className="summary-score-label">/ 100</span>
                    </div>
                    <div className="summary-score-details">
                        <p><strong>{summary.topic}</strong> • {summary.mode}</p>
                        <p>{summary.answeredQuestions} of {summary.totalQuestions} questions answered</p>
                        <p>{summary.durationMinutes} minutes</p>
                    </div>
                </div>

                {/* Strengths */}
                <div className="summary-section">
                    <h3 className="summary-section-title">
                        <FaCheckCircle className="summary-icon green" /> Strengths
                    </h3>
                    <ul className="summary-list">
                        {summary.strengths && summary.strengths.length > 0 ? (
                            summary.strengths.map((s, i) => (
                                <li key={i}>{s}</li>
                            ))
                        ) : (
                            <li>No strengths recorded</li>
                        )}
                    </ul>
                </div>

                {/* Weaknesses */}
                <div className="summary-section">
                    <h3 className="summary-section-title">
                        <FaExclamationCircle className="summary-icon orange" /> Areas for Improvement
                    </h3>
                    <ul className="summary-list">
                        {summary.weaknesses && summary.weaknesses.length > 0 ? (
                            summary.weaknesses.map((w, i) => (
                                <li key={i}>{w}</li>
                            ))
                        ) : (
                            <li>No areas recorded</li>
                        )}
                    </ul>
                </div>

                {/* Common Mistakes */}
                {summary.commonMistakes && (
                    <div className="summary-section">
                        <h3 className="summary-section-title">💡 Common Mistakes</h3>
                        <p className="summary-text">{summary.commonMistakes}</p>
                    </div>
                )}

                {/* Message */}
                {summary.message && (
                    <div className="summary-message">
                        {summary.message}
                    </div>
                )}

                {/* Actions */}
                <div className="summary-actions">
                    <Link to="/interview/setup" className="summary-btn primary">
                        Start New Interview
                    </Link>
                    <Link to="/interview/history" className="summary-btn secondary">
                        View History
                    </Link>
                    <Link to="/" className="summary-btn secondary">
                        Home
                    </Link>
                </div>
            </div>
        </div>
    );
};

export default InterviewSummary;  // ✅ EXPORT DEFAULT