import { Alert, Button, Typography } from "antd";
import { Copy } from "lucide-react";
import type { CreateUserResponse } from "../../api/types";

type Props = {
  created: CreateUserResponse;
  title: string;
  onCopy?: () => void;
};

export default function UserCreatedResultAlert({ created, title, onCopy }: Props) {
  return (
    <Alert
      type="success"
      showIcon
      className="mb-4"
      message={title}
      description={
        <div className="space-y-2 text-sm">
          <p>
            <strong>{created.fullName}</strong> ({created.email}) was created as{" "}
            <strong>{created.role === "ADMIN" ? "administrator" : "learner"}</strong>.
          </p>
          {created.welcomeEmailSent ? (
            <p>
              A welcome email with the temporary password was sent to{" "}
              <strong>{created.email}</strong>.
            </p>
          ) : (
            <Alert
              type="warning"
              showIcon
              className="!mb-0"
              message="Welcome email was not sent"
              description="Mail is not configured on the server. Share the temporary password manually."
            />
          )}
          <p>
            Temporary password:{" "}
            <Typography.Text code copyable>
              {created.temporaryPassword}
            </Typography.Text>
          </p>
          {onCopy && (
            <Button
              type="default"
              size="small"
              icon={<Copy className="size-4" aria-hidden />}
              onClick={() => void onCopy()}
            >
              Copy sign-in details
            </Button>
          )}
        </div>
      }
    />
  );
}
