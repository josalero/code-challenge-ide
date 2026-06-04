import { Alert, Button } from "antd";
import { Link } from "react-router-dom";
import AuthShell from "../components/ui/AuthShell";

export default function AdminBootstrapSetupPage() {
  return (
    <AuthShell
      title="Administrator setup required"
      subtitle="No users exist yet. Create the initial admin account before managing learners."
    >
      <Alert
        type="info"
        showIcon
        className="mb-4"
        message="First-time deployment"
        description="After setup, sign in as admin to create user accounts from the Users page."
      />
      <Link to="/register">
        <Button type="primary" block>
          Set up admin account
        </Button>
      </Link>
      <p className="mb-0 mt-6 text-center text-sm text-muted-foreground">
        Already created an account?{" "}
        <Link
          to="/login"
          className="font-medium text-emerald-600 hover:text-emerald-700 dark:text-emerald-400 dark:hover:text-emerald-300"
        >
          Sign in
        </Link>
      </p>
    </AuthShell>
  );
}
