import { Typography } from "antd";
import type { ReactNode } from "react";

type Props = {
  title: string;
  description?: ReactNode;
  extra?: ReactNode;
};

export default function PageHeader({ title, description, extra }: Props) {
  return (
    <header className="mb-6 flex flex-col gap-4 border-b border-slate-800/80 pb-5 lg:flex-row lg:items-start lg:justify-between">
      <div className="min-w-0">
        <Typography.Title
          level={1}
          className="!mb-2 !mt-0 !text-2xl !font-semibold !leading-tight !text-white md:!text-3xl"
        >
          {title}
        </Typography.Title>
        {description && (
          <Typography.Paragraph className="!mb-0 max-w-3xl !text-sm !leading-relaxed !text-slate-400 md:!text-base">
            {description}
          </Typography.Paragraph>
        )}
      </div>
      {extra && <div className="w-full shrink-0 lg:w-auto">{extra}</div>}
    </header>
  );
}
