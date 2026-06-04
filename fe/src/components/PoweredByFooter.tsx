const ITJOB_URL = "https://itjobopportunities.io";

export default function PoweredByFooter() {
  return (
    <span className="inline-flex items-center gap-1">
      Powered by{" "}
      <a
        href={ITJOB_URL}
        target="_blank"
        rel="noopener noreferrer"
        className="font-medium text-emerald-600 underline-offset-2 hover:underline dark:text-emerald-400"
      >
        ItJobOpportunities
      </a>
    </span>
  );
}
