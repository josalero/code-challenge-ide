import { Typography } from "antd";
import type { ReactNode } from "react";

type Props = {
  title: string;
  description?: ReactNode;
  extra?: ReactNode;
};

export default function PageHeader({ title, description, extra }: Props) {
  return (
    <header className="mb-6 flex flex-col gap-3 sm:flex-row sm:items-start sm:justify-between">
      <div className="min-w-0">
        <Typography.Title level={2} className="!text-white !mb-1 !mt-0">
          {title}
        </Typography.Title>
        {description && (
          <Typography.Paragraph className="!text-slate-400 !mb-0 max-w-2xl">
            {description}
          </Typography.Paragraph>
        )}
      </div>
      {extra && <div className="shrink-0">{extra}</div>}
    </header>
  );
}
