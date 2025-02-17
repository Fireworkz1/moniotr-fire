package group.fire_monitor.service.emailsender;

import javax.mail.*;
import javax.mail.internet.*;
import java.util.Properties;

public class EmailSender {
    private String host; // SMTP服务器地址
    private String port; // SMTP端口
    private String username; // 发件人邮箱
    private String password; // 发件人邮箱密码

    // 构造函数
    public EmailSender(String host, String port, String username, String password) {
        this.host = host;
        this.port = port;
        this.username = username;
        this.password = password;
    }

    public EmailSender() {
        this.host = "smtp.126.com";
        this.port = "465";
        this.username = "smart030518@126.com";
        this.password = "sMart2012";
    }

    // 发送邮件的方法
    public void sendEmail(String recipient, String title, String content) {
        // 邮件服务器配置
        Properties properties = new Properties();
        properties.put("mail.smtp.auth", "true"); // 启用身份验证
        properties.put("mail.smtp.starttls.enable", "true"); // 启用TLS
        properties.put("mail.smtp.host", host); // SMTP服务器地址
        properties.put("mail.smtp.port", port); // SMTP端口

        // 创建会话
        Session session = Session.getInstance(properties, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });

        try {
            // 创建邮件对象
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(username)); // 设置发件人
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipient)); // 设置收件人
            message.setSubject(title); // 设置邮件主题
            message.setText(content); // 设置邮件内容

            // 发送邮件
            Transport.send(message);
            System.out.println("邮件发送成功！");
        } catch (MessagingException e) {
            e.printStackTrace();
            System.err.println("邮件发送失败：" + e.getMessage());
        }
    }
}
