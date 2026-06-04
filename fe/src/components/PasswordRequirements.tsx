import { useQuery } from "@tanstack/react-query";
import { apiFetch } from "../api/client";
import type { PasswordRequirementsResponse } from "../api/types";
import { ApiPaths } from "../domain/constants";

const FALLBACK_REQUIREMENTS = [
  "At least 8 characters long",
  "At least one uppercase letter (A–Z)",
  "At least one lowercase letter (a–z)",
  "At least one digit (0–9)",
  "Must not be the same as your email address",
];

type Props = {
  title?: string;
  className?: string;
};

export default function PasswordRequirements({
  title = "Password requirements",
  className = "",
}: Props) {
  const query = useQuery({
    queryKey: ["password-requirements"],
    queryFn: () =>
      apiFetch<PasswordRequirementsResponse>(ApiPaths.AUTH_PASSWORD_REQUIREMENTS),
    staleTime: 60_000,
  });

  const requirements = query.data?.requirements ?? FALLBACK_REQUIREMENTS;

  return (
    <div
      className={`rounded-md border border-border bg-muted/40 px-4 py-3 text-sm text-muted-foreground ${className}`}
      aria-label={title}
    >
      <p className="mb-2 font-medium text-foreground">{title}</p>
      <ul className="mb-0 list-disc space-y-1 pl-5">
        {requirements.map((item) => (
          <li key={item}>{item}</li>
        ))}
      </ul>
    </div>
  );
}
