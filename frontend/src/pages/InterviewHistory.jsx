import { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { interviewService } from '../services/interviewService';
import { FaCalendar, FaClock, FaStar } from 'react-icons/fa';
import toast from 'react-hot-toast';
import './InterviewHistory.css';

const InterviewHistory = () => {
    const [history, setHistory] = useState([]);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        fetchHistory();
    }, []);

    const fetchHistory = async () => {
        setLoading(true);
        try {
            const response = await interviewService.getHistory();
            setHistory(response.data.data || []);
        } catch (error) {
            toast.error('Failed to load history');
        } finally {
            setLoading(false);
        }
    };

    if (loading) {
        return (
            <div className="history-loading">
                <div className="text-gray-400 text-lg">Loading history...</div>
            </div>
        );
    }

    return (
        <div className="history-container">
            <div className="history-box">
                <h1 className="history-title">📚 Interview History</h1>

                {history.length === 0 ? (
                    <div className="history-empty">
                        <p>No interviews completed yet.</p>
                        <Link to="/interview/setup" className="history-start-btn">
                            Start Your First Interview 🚀
                        </Link>
                    </div>
                ) : (
                    <div className="history-list">
                        {history.map((session) => (
                            <div key={session.sessionId} className="history-card">
                                <div className="history-card-header">
                                    <span className="history-topic">{session.topic}</span>
                                    <span className="history-score">
                                        <FaStar className="star-icon" />
                                        {session.overallScore ? Math.round(session.overallScore) : 'N/A'}
                                    </span>
                                </div>
                                <div className="history-card-details">
                                    <span className="history-detail">
                                        <FaCalendar className="detail-icon" />
                                        {session.completedAt ? new Date(session.completedAt).toLocaleDateString() : 'N/A'}
                                    </span>
                                    <span className="history-detail">
                                        <FaClock className="detail-icon" />
                                        {session.durationMinutes} min
                                    </span>
                                    <span className="history-detail">
                                        {session.answeredQuestions}/{session.totalQuestions} answered
                                    </span>
                                </div>
                                <Link
                                    to={`/interview/summary/${session.sessionId}`}
                                    className="history-view-btn"
                                >
                                    View Summary →
                                </Link>
                            </div>
                        ))}
                    </div>
                )}
            </div>
        </div>
    );
};

export default InterviewHistory;  // ✅ Make sure this is at the end