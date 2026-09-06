-- =====================================================================
-- V20__seed_notification_templates.sql
-- Global MVP notification templates for Sprint 4 lifecycle events.
-- Depends on: V8__notifications_integrations_audit.sql, V9 seed languages
-- =====================================================================

INSERT INTO notification_templates (tenant_id, code, channel, subject_template, body_template, language_id)
SELECT NULL, t.code, 'EMAIL', t.subject_template, t.body_template, l.id
FROM languages l
CROSS JOIN (
    VALUES
        ('ACCOUNT_CREATED', 'Welcome {{firstName}}', 'Hello {{firstName}}, your CAP account is ready. Sign in at {{loginUrl}}.'),
        ('PASSWORD_RESET', 'Password reset successful', 'Hello {{firstName}}, your password was reset successfully.'),
        ('ASSESSMENT_ASSIGNED', 'New assessment assigned', 'Hello {{firstName}}, assessment configuration {{groupName}} ({{groupCode}}) has been assigned to you.'),
        ('ASSESSMENT_COMPLETED', 'Assessment submitted', 'Hello {{firstName}}, your assessment {{assessmentCode}} was submitted successfully.'),
        ('REPORT_PUBLISHED', 'Your career report is ready', 'Hello {{firstName}}, your career report is ready: {{reportUrl}}')
) AS t(code, subject_template, body_template)
WHERE l.code = 'en'
  AND NOT EXISTS (
      SELECT 1
      FROM notification_templates nt
      WHERE nt.tenant_id IS NULL
        AND nt.code = t.code
        AND nt.channel = 'EMAIL'
        AND nt.language_id = l.id
  );

INSERT INTO notification_templates (tenant_id, code, channel, subject_template, body_template, language_id)
SELECT NULL, t.code, 'EMAIL', t.subject_template, t.body_template, l.id
FROM languages l
CROSS JOIN (
    VALUES
        ('ACCOUNT_CREATED', 'स्वागत है {{firstName}}', 'नमस्ते {{firstName}}, आपका CAP खाता तैयार है। {{loginUrl}} पर साइन इन करें।'),
        ('PASSWORD_RESET', 'पासवर्ड रीसेट सफल', 'नमस्ते {{firstName}}, आपका पासवर्ड सफलतापूर्वक रीसेट हो गया।'),
        ('ASSESSMENT_ASSIGNED', 'नया मूल्यांकन असाइन', 'नमस्ते {{firstName}}, मूल्यांकन कॉन्फ़िगरेशन {{groupName}} ({{groupCode}}) आपको असाइन किया गया है।'),
        ('ASSESSMENT_COMPLETED', 'मूल्यांकन जमा', 'नमस्ते {{firstName}}, आपका मूल्यांकन {{assessmentCode}} सफलतापूर्वक जमा हो गया।'),
        ('REPORT_PUBLISHED', 'आपकी करियर रिपोर्ट तैयार', 'नमस्ते {{firstName}}, आपकी करियर रिपोर्ट तैयार है: {{reportUrl}}')
) AS t(code, subject_template, body_template)
WHERE l.code = 'hi'
  AND NOT EXISTS (
      SELECT 1
      FROM notification_templates nt
      WHERE nt.tenant_id IS NULL
        AND nt.code = t.code
        AND nt.channel = 'EMAIL'
        AND nt.language_id = l.id
  );
