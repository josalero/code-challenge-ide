import { CodeOutlined, LogoutOutlined, UnorderedListOutlined } from "@ant-design/icons";
import { Avatar, Button, Layout, Menu, Typography } from "antd";
import { Link, useLocation, useNavigate } from "react-router-dom";
import { useAuth } from "../auth/useAuth";

const { Header, Content } = Layout;

export default function AppLayout({ children }: { children: React.ReactNode }) {
  const { user, logout } = useAuth();
  const location = useLocation();
  const navigate = useNavigate();

  const selectedKey = location.pathname.startsWith("/challenges") ? "challenges" : "";
  const initials = user?.email?.slice(0, 2).toUpperCase() ?? "?";

  return (
    <Layout className="min-h-screen bg-slate-950">
      <a href="#main-content" className="ctl-skip-link">
        Skip to main content
      </a>
      <Header className="sticky top-0 z-30 flex h-14 items-center gap-4 border-b border-slate-800/80 bg-slate-950/90 px-4 backdrop-blur md:px-6">
        <Link
          to="/challenges"
          className="flex shrink-0 items-center gap-2.5 text-white no-underline"
        >
          <span className="flex h-9 w-9 items-center justify-center rounded-lg bg-emerald-500/15 ring-1 ring-emerald-500/25">
            <CodeOutlined className="text-lg text-emerald-400" aria-hidden />
          </span>
          <span className="hidden sm:block">
            <Typography.Text className="!text-white block text-sm font-semibold leading-tight">
              Code Training Lab
            </Typography.Text>
            <Typography.Text className="!text-slate-500 block text-xs">
              Practice · AI coach
            </Typography.Text>
          </span>
        </Link>
        <Menu
          theme="dark"
          mode="horizontal"
          selectedKeys={[selectedKey]}
          className="min-w-0 flex-1 border-0 bg-transparent"
          items={[
            {
              key: "challenges",
              icon: <UnorderedListOutlined aria-hidden />,
              label: <Link to="/challenges">Challenges</Link>,
            },
          ]}
        />
        <div className="flex items-center gap-2 sm:gap-3">
          {user && (
            <div className="hidden items-center gap-2 md:flex">
              <Avatar
                size="small"
                className="!bg-emerald-600/30 !text-emerald-300 text-xs"
                aria-hidden
              >
                {initials}
              </Avatar>
              <Typography.Text className="!text-slate-400 max-w-[180px] truncate text-sm">
                {user.email}
              </Typography.Text>
            </div>
          )}
          <Button
            type="text"
            icon={<LogoutOutlined aria-hidden />}
            className="!text-slate-300"
            onClick={() => {
              logout();
              navigate("/login");
            }}
            aria-label="Sign out"
          >
            <span className="hidden sm:inline">Sign out</span>
          </Button>
        </div>
      </Header>
      <Content id="main-content" className="mx-auto w-full max-w-7xl px-4 py-6 md:px-6 md:py-8">
        {children}
      </Content>
    </Layout>
  );
}
