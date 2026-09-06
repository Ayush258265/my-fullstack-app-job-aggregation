import { useState, useEffect, useRef } from 'react';
import { FaClock } from 'react-icons/fa';
import './Timer.css';

const Timer = ({ duration, onEnd, onSilence, isActive }) => {
    const [timeLeft, setTimeLeft] = useState(duration);
    const [isWarning, setIsWarning] = useState(false);
    const silenceTimer = useRef(null);
    const hasEnded = useRef(false);

    useEffect(() => {
        setTimeLeft(duration);
        setIsWarning(false);
        hasEnded.current = false;
    }, [duration]);

    useEffect(() => {
        if (!isActive) return;

        const interval = setInterval(() => {
            setTimeLeft((prev) => {
                if (prev <= 1) {
                    clearInterval(interval);
                    if (!hasEnded.current) {
                        hasEnded.current = true;
                        onEnd();
                    }
                    return 0;
                }

                // Warning at 30 seconds
                if (prev === 30) {
                    setIsWarning(true);
                }

                return prev - 1;
            });
        }, 1000);

        // Silence detection (10 seconds)
        silenceTimer.current = setTimeout(() => {
            onSilence();
        }, 10000);

        return () => {
            clearInterval(interval);
            clearTimeout(silenceTimer.current);
        };
    }, [isActive, onEnd, onSilence, duration]);

    const formatTime = (seconds) => {
        const mins = Math.floor(seconds / 60);
        const secs = seconds % 60;
        return `${mins}:${secs.toString().padStart(2, '0')}`;
    };

    const getColor = () => {
        if (timeLeft <= 10) return 'timer-danger';
        if (isWarning) return 'timer-warning';
        return 'timer-ok';
    };

    return (
        <div className="timer-bar">
            <div className="timer-left">
                <FaClock className={getColor()} />
                <span className={`timer-value ${getColor()}`}>
                    {formatTime(timeLeft)}
                </span>
                {isWarning && (
                    <span className="timer-alert">⏰ Time running out!</span>
                )}
            </div>
            <div className="timer-hint">
                {timeLeft > 30 ? '⏳ Take your time' : '🗣️ Wrap up your answer'}
            </div>
        </div>
    );
};

export default Timer;