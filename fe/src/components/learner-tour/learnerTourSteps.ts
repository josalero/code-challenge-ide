import type { TourProps } from "antd";

export const LEARNER_CATALOG_TOUR_STEP_COUNT = 3;

function tourTarget(id: string): () => HTMLElement {
  return () =>
    document.querySelector<HTMLElement>(`[data-learner-tour="${id}"]`) ?? document.body;
}

export function learnerCatalogTourSteps(): TourProps["steps"] {
  return [
    {
      title: "Welcome to Code Training Lab",
      description:
        "This short tour shows how to browse challenges, open the coding workspace, run tests, "
        + "and submit your solution.",
      placement: "center",
      target: null,
    },
    {
      title: "Find your next challenge",
      description:
        "Search by title, filter by language or progress, and reset filters when you want to see "
        + "the full catalog again.",
      target: tourTarget("challenge-filters"),
    },
    {
      title: "Open a challenge",
      description:
        "Pick any exercise to enter the workspace. Your progress is tracked so you can continue "
        + "where you left off.",
      target: tourTarget("challenge-list"),
      nextButtonProps: {
        children: "Got it",
      },
    },
  ];
}

type WorkspaceTourOptions = {
  hasTimedSession: boolean;
};

export function learnerWorkspaceTourSteps({
  hasTimedSession,
}: WorkspaceTourOptions): TourProps["steps"] {
  const steps: TourProps["steps"] = [
    {
      title: "Coding workspace",
      description:
        "Each challenge opens in a full IDE: problem instructions, editor, run controls, and "
        + "output tabs for tests and feedback.",
      placement: "center",
      target: null,
    },
    {
      title: "Read the problem",
      description:
        "Start here for the prompt, examples, and public test cases. Expand the panel on mobile "
        + "with the Problem tab.",
      target: tourTarget("problem-panel"),
    },
    {
      title: "Write your solution",
      description:
        "Edit the starter code in the Solution tab. Use Custom tests to try edge cases before you "
        + "submit.",
      target: tourTarget("editor"),
    },
  ];

  if (hasTimedSession) {
    steps.push({
      title: "Start the timed attempt",
      description:
        "Timed challenges stay read-only until you press Start test. The countdown begins only "
        + "after you are ready to code.",
      target: tourTarget("start-test"),
    });
  }

  steps.push(
    {
      title: "Run your code",
      description:
        "Practice runs execute in Docker without locking your editor. Use ⌘/Ctrl+Enter as a "
        + "keyboard shortcut.",
      target: tourTarget("run-action"),
    },
    {
      title: "Submit for scoring",
      description:
        "Submit runs hidden tests and unlocks AI feedback. After a successful submit, use Redo to "
        + "practice again.",
      target: tourTarget("submit-action"),
    },
    {
      title: "Review results",
      description:
        "Tests, compiler output, analysis, and AI coach feedback live in the output panel. On "
        + "mobile, switch to the Output tab.",
      target: tourTarget("output-panel"),
      nextButtonProps: {
        children: "Done",
      },
    },
  );

  return steps;
}
