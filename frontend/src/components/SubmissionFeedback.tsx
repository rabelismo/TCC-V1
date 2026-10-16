import { useState } from "react";
import { CheckCircle2, XCircle, ChevronDown, Trophy } from "lucide-react";
import type { SubmissionResult } from "../types";

interface SubmissionFeedbackProps {
  result: SubmissionResult;
}

export default function SubmissionFeedback({ result }: SubmissionFeedbackProps) {
  const [expandedId, setExpandedId] = useState<number | null>(null);

  const toggle = (id: number) => {
    setExpandedId(expandedId === id ? null : id);
  };

  const ratio = result.totalCount > 0 ? result.passedCount / result.totalCount : 0;

  return (
    <div className="submission-feedback">
      <div
        className={`feedback-header ${result.allPassed ? "all-passed" : "has-failures"}`}
        role="status"
        aria-live="polite"
      >
        {result.allPassed ? (
          <>
            <Trophy size={18} strokeWidth={1.75} aria-hidden="true" />
            <span>Todos os critérios atendidos. Módulo concluído.</span>
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

      <div
        className="feedback-progress-bar"
        role="progressbar"
        aria-valuenow={result.passedCount}
        aria-valuemin={0}
        aria-valuemax={result.totalCount}
        aria-label="Critérios atendidos"
      >
        <div
          className={`feedback-progress-fill ${result.allPassed ? "complete" : ""}`}
          style={{ transform: `scaleX(${ratio})` }}
        />
      </div>

      <ul className="feedback-criteria-list">
        {result.criteria.map((criterion) => {
          const hasHint = !criterion.passed && !!criterion.hint;
          const isOpen = expandedId === criterion.criterionId;
          const hintId = `hint-${criterion.criterionId}`;

          const content = (
            <>
              <span className="criterion-icon">
                {criterion.passed ? (
                  <CheckCircle2 size={17} strokeWidth={1.75} className="icon-pass" aria-hidden="true" />
                ) : (
                  <XCircle size={17} strokeWidth={1.75} className="icon-fail" aria-hidden="true" />
                )}
                <span className="visually-hidden">
                  {criterion.passed ? "Atendido:" : "Não atendido:"}
                </span>
              </span>
              <span className={`criterion-desc ${criterion.passed ? "passed" : "failed"}`}>
                {criterion.description}
              </span>
              {hasHint && (
                <span className={`criterion-chevron ${isOpen ? "open" : ""}`} aria-hidden="true">
                  <ChevronDown size={16} strokeWidth={1.75} />
                </span>
              )}
            </>
          );

          return (
            <li key={criterion.criterionId} className="feedback-criterion">
              {hasHint ? (
                <button
                  type="button"
                  className="criterion-row"
                  onClick={() => toggle(criterion.criterionId)}
                  aria-expanded={isOpen}
                  aria-controls={hintId}
                >
                  {content}
                </button>
              ) : (
                <div className="criterion-row">{content}</div>
              )}

              {hasHint && isOpen && (
                <div className="criterion-hint" id={hintId}>
                  <strong>Dica:</strong> {criterion.hint}
                </div>
              )}
            </li>
          );
        })}
      </ul>
    </div>
  );
}
