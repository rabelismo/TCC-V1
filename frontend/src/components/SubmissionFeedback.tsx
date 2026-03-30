import { useState } from "react";
import { CheckCircle2, XCircle, ChevronDown, ChevronUp, Trophy } from "lucide-react";
import type { SubmissionResult } from "../types";

interface SubmissionFeedbackProps {
  result: SubmissionResult;
}

export default function SubmissionFeedback({ result }: SubmissionFeedbackProps) {
  const [expandedId, setExpandedId] = useState<number | null>(null);

  const toggle = (id: number) => {
    setExpandedId(expandedId === id ? null : id);
  };

  const percentage = result.totalCount > 0
    ? Math.round((result.passedCount / result.totalCount) * 100)
    : 0;

  return (
    <div className="submission-feedback">
      <div className={`feedback-header ${result.allPassed ? "all-passed" : "has-failures"}`}>
        {result.allPassed ? (
          <>
            <Trophy size={20} />
            <span>Todos os critérios foram atendidos!</span>
          </>
        ) : (
          <>
            <span className="feedback-score">
              {result.passedCount}/{result.totalCount}
            </span>
            <span>critérios atendidos</span>
          </>
        )}
      </div>

      <div className="feedback-progress-bar">
        <div
          className={`feedback-progress-fill ${result.allPassed ? "complete" : ""}`}
          style={{ width: `${percentage}%` }}
        />
      </div>

      <ul className="feedback-criteria-list">
        {result.criteria.map((criterion) => (
          <li key={criterion.criterionId} className="feedback-criterion">
            <button
              className="criterion-row"
              onClick={() => !criterion.passed && criterion.hint ? toggle(criterion.criterionId) : undefined}
            >
              <span className="criterion-icon">
                {criterion.passed
                  ? <CheckCircle2 size={18} className="icon-pass" />
                  : <XCircle size={18} className="icon-fail" />}
              </span>
              <span className={`criterion-desc ${criterion.passed ? "passed" : "failed"}`}>
                {criterion.description}
              </span>
              {!criterion.passed && criterion.hint && (
                <span className="criterion-chevron">
                  {expandedId === criterion.criterionId
                    ? <ChevronUp size={16} />
                    : <ChevronDown size={16} />}
                </span>
              )}
            </button>

            {expandedId === criterion.criterionId && criterion.hint && (
              <div className="criterion-hint">
                <strong>Dica:</strong> {criterion.hint}
              </div>
            )}
          </li>
        ))}
      </ul>
    </div>
  );
}
