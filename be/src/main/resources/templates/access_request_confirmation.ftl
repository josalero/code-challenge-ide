<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8"/>
  <title>Access request received</title>
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
    .footer {
      margin-top: 24px;
      font-size: 12px;
      color: #64748b;
    }
  </style>
</head>
<body>
  <div class="container">
    <p>Hi ${fullName},</p>

    <p>
      We received your request to access <strong>${appName}</strong>.
      An administrator will review it and email you if your account is approved.
    </p>

    <p>
      If approved, you will receive a separate email with sign-in instructions.
      You can check back later at
      <a href="${loginUrl}">${loginUrl}</a>.
    </p>

    <p class="footer">
      If you did not submit this request, you can ignore this message.
    </p>
  </div>
</body>
</html>
