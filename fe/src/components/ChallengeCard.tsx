import { ArrowRightOutlined } from "@ant-design/icons";
import { Tag, Typography } from "antd";
import { Link } from "react-router-dom";
import type { ChallengeSummary } from "../api/types";
import { ProgressState } from "../domain/constants";
import { difficultyColor } from "../utils/difficulty";
import {
  formatLanguageLabel,
  languageTagColor,
  runnerPipelineLabel,
} from "../utils/languageRuntimes";

type Props = {
  challenge: ChallengeSummary;
  progressState?: string;
};

function actionLabel(state?: string): string {
  if (state === ProgressState.PASSED) {
    return "Review challenge";
  }
  if (state === ProgressState.ATTEMPTED || state === ProgressState.FAILED) {
    return "Continue challenge";
  }
  return "Start challenge";
}

function progressTagColor(state?: string): string {
  if (state === ProgressState.PASSED) {
    return "success";
  }
  if (state === ProgressState.ATTEMPTED) {
    return "processing";
  }
  if (state === ProgressState.FAILED) {
    return "error";
  }
  return "default";
}

function progressLabel(state?: string): string {
  if (!state || state === ProgressState.NOT_STARTED) {
    return "Not started";
  }
  if (state === ProgressState.PASSED) {
    return "Passed";
  }
  if (state === ProgressState.ATTEMPTED) {
    return "In progress";
  }
  if (state === ProgressState.FAILED) {
    return "Needs work";
  }
  return state;
}

export default function ChallengeCard({ challenge, progressState }: Props) {
  const action = actionLabel(progressState);

  return (
    <article className="ctl-challenge-card group relative flex h-full min-h-[168px] flex-col rounded-lg border border-slate-700/60 bg-slate-900/70 p-4 transition-colors hover:border-emerald-500/50 hover:bg-slate-800/70">
      <div className="mb-3 flex flex-wrap items-center gap-1.5">
        {challenge.language && (
          <Tag color={languageTagColor(challenge.language)} className="!m-0 !text-xs">
            {formatLanguageLabel(challenge.language)}
          </Tag>
        )}
        <Tag color={difficultyColor(challenge.difficulty)} className="!m-0 !text-xs">
          {challenge.difficulty}
        </Tag>
        <Tag color={progressTagColor(progressState)} className="!m-0 !text-xs">
          {progressLabel(progressState)}
        </Tag>
      </div>

      <Typography.Title
        level={3}
        className="!mb-1 !mt-0 line-clamp-2 !text-base !font-semibold !leading-snug !text-slate-50"
      >
        <Link
          to={`/challenges/${challenge.slug}`}
          className="!text-slate-50 no-underline after:absolute after:inset-0 hover:!text-emerald-300 focus-visible:outline focus-visible:outline-2 focus-visible:outline-emerald-400"
        >
          {challenge.title}
        </Link>
      </Typography.Title>

      <Typography.Text className="!text-slate-500 block truncate !text-xs">
        {challenge.slug}
      </Typography.Text>

      <Typography.Text className="!mt-3 block line-clamp-2 !text-sm !leading-relaxed !text-slate-400">
        {runnerPipelineLabel(challenge.language)}
      </Typography.Text>

      <span className="mt-auto flex items-center gap-1 pt-4 text-sm font-medium text-emerald-400/90 group-hover:text-emerald-300">
        {action}
        <ArrowRightOutlined className="text-xs transition-transform group-hover:translate-x-0.5" />
      </span>
    </article>
  );
}
