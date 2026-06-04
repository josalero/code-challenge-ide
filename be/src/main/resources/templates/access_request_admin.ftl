<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8"/>
  <title>Access request</title>
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
    .details {
      background: #f8fafc;
      border: 1px solid #e2e8f0;
      border-radius: 6px;
      padding: 16px;
      margin: 16px 0;
    }
    .details dt {
      font-weight: bold;
      margin-top: 8px;
    }
    .details dd {
      margin: 4px 0 0;
    }
    .message {
      white-space: pre-wrap;
      font-family: inherit;
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
    <p>Someone requested access to <strong>${appName}</strong>.</p>

    <dl class="details">
      <dt>Name</dt>
      <dd>${fullName}</dd>
      <dt>Email</dt>
      <dd>${email}</dd>
      <dt>Submitted</dt>
      <dd>${submittedAt}</dd>
      <dt>Message</dt>
      <dd class="message">${message}</dd>
    </dl>

    <p>
      Review the request and create a user account from the admin panel if you approve access.
    </p>

    <p>
      <a class="btn" href="${adminUsersUrl}">Open user management</a>
    </p>

    <p class="footer">
      This notification was sent because a visitor submitted the public access request form.
    </p>
  </div>
</body>
</html>
