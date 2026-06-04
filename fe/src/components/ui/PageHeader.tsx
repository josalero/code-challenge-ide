import { Typography } from "antd";
import type { ReactNode } from "react";

type Props = {
  title: string;
  description?: ReactNode;
  extra?: ReactNode;
};

export default function PageHeader({ title, description, extra }: Props) {
  return (
    <header className="mb-6 flex flex-col gap-4 border-b border-border pb-5 lg:flex-row lg:items-start lg:justify-between">
      <div className="min-w-0">
        <Typography.Title
          level={1}
          className="!mb-2 !mt-0 !text-2xl !font-semibold !leading-tight !text-foreground md:!text-3xl"
        >
          {title}
        </Typography.Title>
        {description && (
          <Typography.Paragraph className="!mb-0 max-w-3xl !text-sm !leading-relaxed !text-muted-foreground md:!text-base">
            {description}
          </Typography.Paragraph>
        )}
      </div>
      {extra && <div className="w-full shrink-0 lg:w-auto">{extra}</div>}
    </header>
  );
}
