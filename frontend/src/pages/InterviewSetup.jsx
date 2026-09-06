import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../hooks/useAuth';
import { interviewService } from '../services/interviewService';
import toast from 'react-hot-toast';
import './InterviewSetup.css';

const InterviewSetup = () => {
    const { user } = useAuth();
    const navigate = useNavigate();
    const [loading, setLoading] = useState(false);
    const [remaining, setRemaining] = useState(null);
    const [formData, setFormData] = useState({
        topic: 'Java',
        durationMinutes: 30,
        mode: 'TOPIC', // 'TOPIC' or 'ENGLISH'
    });

    const topics = ['Java', 'React', 'Python', 'General', 'English'];
    const durations = [15, 30, 45, 60];
    const modes = [
        { value: 'TOPIC', label: '💼 Technical Interview' },
        { value: 'ENGLISH', label: '🗣️ English Practice' },
    ];

    useEffect(() => {
        fetchRemaining();
    }, []);

    const fetchRemaining = async () => {
        try {
            const response = await interviewService.getRemainingInterviews();
            setRemaining(response.data.data);
        } catch (error) {
            console.error('Failed to fetch remaining interviews:', error);
        }
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        setLoading(true);

        try {
            const response = await interviewService.startInterview(formData);
            const sessionId = response.data.data.sessionId;
            toast.success('Interview started! 🎯');
            navigate(`/interview/session/${sessionId}`);
        } catch (error) {
            const msg = error.response?.data?.message || 'Failed to start interview';
            toast.error(msg);
            if (msg.includes('Daily limit')) {
                fetchRemaining();
            }
        } finally {
            setLoading(false);
        }
    };

    if (remaining === null) {
        return (
            <div className="interview-setup-loading">Loading...</div>
        );
    }

    return (
        <div className="interview-setup-container">
            <div className="interview-setup-box">
                <h1 className="interview-setup-title">
                    🎯 Interview Setup
                </h1>

                <div className="interview-setup-remaining">
                    <span className={`remaining-badge ${user?.role === 'ADMIN' ? 'admin' : ''}`}>
                        {user?.role === 'ADMIN' ? '♾️ Unlimited' : `${remaining} interviews remaining today`}
                    </span>
                </div>

                <form onSubmit={handleSubmit} className="interview-setup-form">
                    <div className="form-group">
                        <label>Interview Mode</label>
                        <div className="mode-buttons">
                            {modes.map((mode) => (
                                <button
                                    key={mode.value}
                                    type="button"
                                    className={`mode-btn ${formData.mode === mode.value ? 'active' : ''} ${mode.value === 'ENGLISH' ? 'english' : ''}`}
                                    onClick={() => setFormData({ ...formData, mode: mode.value })}
                                >
                                    {mode.label}
                                </button>
                            ))}
                        </div>
                    </div>

                    {formData.mode === 'TOPIC' && (
                        <div className="form-group">
                            <label>Select Topic</label>
                            <select
                                value={formData.topic}
                                onChange={(e) => setFormData({ ...formData, topic: e.target.value })}
                                className="form-select"
                            >
                                {topics.map((topic) => (
                                    <option key={topic} value={topic}>{topic}</option>
                                ))}
                            </select>
                        </div>
                    )}

                    {formData.mode === 'ENGLISH' && (
                        <div className="form-hint">
                            Practice your English speaking skills with AI conversation partner.
                            Get feedback on grammar, fluency, and vocabulary.
                        </div>
                    )}

                    <div className="form-group">
                        <label>Duration (minutes)</label>
                        <div className="duration-buttons">
                            {durations.map((dur) => (
                                <button
                                    key={dur}
                                    type="button"
                                    className={`duration-btn ${formData.durationMinutes === dur ? 'active' : ''}`}
                                    onClick={() => setFormData({ ...formData, durationMinutes: dur })}
                                >
                                    {dur}m
                                </button>
                            ))}
                        </div>
                        <p className="form-small">
                            ~{Math.floor(formData.durationMinutes / 3)} questions
                        </p>
                    </div>

                    <button
                        type="submit"
                        className="start-btn"
                        disabled={loading || remaining === 0}
                    >
                        {loading ? 'Starting...' : 'Start Interview 🚀'}
                    </button>

                    {remaining === 0 && (
                        <p className="error-text">
                            Daily limit reached! Come back tomorrow.
                        </p>
                    )}
                </form>
            </div>
        </div>
    );
};

export default InterviewSetup;