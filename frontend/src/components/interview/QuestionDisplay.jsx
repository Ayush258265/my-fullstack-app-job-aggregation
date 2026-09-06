import { FaLightbulb } from 'react-icons/fa';

const QuestionDisplay = ({ question, questionNumber, totalQuestions, topic }) => {
    return (
        <div className="bg-gradient-to-r from-blue-50 to-indigo-50 rounded-xl p-6 mb-4 border border-blue-100">
            <div className="flex items-start gap-3">
                <div className="bg-blue-100 rounded-full p-2 mt-1">
                    <FaLightbulb className="text-blue-600" size={18} />
                </div>
                <div className="flex-1">
                    <div className="flex justify-between items-center mb-2">
                        <span className="text-xs font-semibold text-blue-600 uppercase">
                            {topic} • Question {questionNumber} of {totalQuestions}
                        </span>
                    </div>
                    <p className="text-lg text-gray-800 leading-relaxed">
                        {question}
                    </p>
                </div>
            </div>
        </div>
    );
};

export default QuestionDisplay;