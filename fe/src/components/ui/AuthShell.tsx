import { CodeOutlined, RobotOutlined, SafetyOutlined } from "@ant-design/icons";
import { Typography } from "antd";
import type { ReactNode } from "react";
import { Link } from "react-router-dom";

type Props = {
  title: string;
  subtitle: string;
  children: ReactNode;
};

const FEATURES = [
  { icon: <SafetyOutlined />, text: "Sandboxed Java runs with real JUnit & coverage" },
  { icon: <RobotOutlined />, text: "AI coach hints — never full solutions" },
  { icon: <CodeOutlined />, text: "Monaco editor with language support" },
];

export default function AuthShell({ title, subtitle, children }: Props) {
  return (
    <div className="ctl-auth-bg flex min-h-screen flex-col lg:flex-row">
      <aside
        className="hidden flex-1 flex-col justify-between border-b border-slate-800/60 px-8 py-10 lg:flex lg:border-b-0 lg:border-r"
        aria-hidden={false}
      >
        <div>
          <Link to="/login" className="inline-flex items-center gap-2 no-underline">
            <span className="flex h-10 w-10 items-center justify-center rounded-lg bg-emerald-500/15 ring-1 ring-emerald-500/30">
              <CodeOutlined className="text-xl text-emerald-400" />
            </span>
            <span>
              <Typography.Text className="!text-white block font-semibold leading-tight">
                Code Training Lab
              </Typography.Text>
              <Typography.Text className="!text-slate-500 text-xs">
                Practice · test · improve
              </Typography.Text>
            </span>
          </Link>
          <Typography.Title level={2} className="!text-white !mt-10 !mb-3 max-w-md">
            Level up your Java with guided practice
          </Typography.Title>
          <ul className="mt-6 space-y-4">
            {FEATURES.map((f) => (
              <li key={f.text} className="flex items-start gap-3 text-slate-300">
                <span className="mt-0.5 text-emerald-400">{f.icon}</span>
                <span className="text-sm leading-relaxed">{f.text}</span>
              </li>
            ))}
          </ul>
        </div>
        <Typography.Text className="!text-slate-600 text-xs">
          Docker sandbox · hidden tests · style gates
        </Typography.Text>
      </aside>

      <main className="flex flex-1 items-center justify-center px-4 py-10 lg:max-w-lg lg:flex-none lg:px-12">
        <div className="w-full max-w-md">
          <div className="mb-6 lg:hidden">
            <Link to="/login" className="inline-flex items-center gap-2 no-underline">
              <CodeOutlined className="text-lg text-emerald-400" />
              <Typography.Text className="!text-white font-medium">
                Code Training Lab
              </Typography.Text>
            </Link>
          </div>
          <div className="ctl-auth-panel rounded-xl border border-slate-800/80 bg-slate-900/95 p-6 shadow-xl sm:p-8">
            <Typography.Title level={3} className="!text-white !mb-1">
              {title}
            </Typography.Title>
            <Typography.Paragraph className="!text-slate-400 !mb-6">
              {subtitle}
            </Typography.Paragraph>
            {children}
          </div>
        </div>
      </main>
    </div>
  );
}
