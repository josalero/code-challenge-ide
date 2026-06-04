<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8"/>
  <title>Your ${appName} account</title>
  <style>
    body {
      font-family: Arial, sans-serif;
      line-height: 1.6;
      color: #333;
      background: #f5f5f5;
      margin: 0;
      padding: 24px;
    }
    .container {
      max-width: 600px;
      margin: 0 auto;
      background: #fff;
      border: 1px solid #ddd;
      border-radius: 8px;
      padding: 24px;
    }
    .credentials {
      background: #f8fafc;
      border: 1px solid #e2e8f0;
      border-radius: 6px;
      padding: 16px;
      margin: 16px 0;
    }
    .credentials dt {
      font-weight: bold;
      margin-top: 8px;
    }
    .credentials dd {
      margin: 4px 0 0;
      font-family: monospace;
    }
    .btn {
      display: inline-block;
      background: #059669;
      color: #fff !important;
      text-decoration: none;
      padding: 10px 18px;
      border-radius: 6px;
      margin-top: 8px;
    }
    .footer {
      margin-top: 24px;
      font-size: 12px;
      color: #64748b;
    }
  </style>
</head>
<body>
  <div class="container">
    <p>Dear ${fullName},</p>

    <p>
      An administrator created your <strong>${appName}</strong> account as a
      <strong>${roleLabel}</strong>. Use the credentials below to sign in.
    </p>

    <dl class="credentials">
      <dt>Email (username)</dt>
      <dd>${email}</dd>
      <dt>Temporary password</dt>
      <dd>${temporaryPassword}</dd>
    </dl>

    <p>
      On first sign-in you will be asked to choose a new password. Your new password must be at
      least 8 characters and include uppercase, lowercase, and a digit.
    </p>

    <p>
      <a class="btn" href="${loginUrl}">Sign in to ${appName}</a>
    </p>

    <p class="footer">
      For security, do not share these credentials. If you did not expect this account, contact
      your administrator.
    </p>
  </div>
</body>
</html>
