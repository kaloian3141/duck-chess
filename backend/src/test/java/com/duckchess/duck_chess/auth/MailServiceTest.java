package com.duckchess.duck_chess.auth;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class MailServiceTest 
{

    private JavaMailSender mailSender;
    private MailService service;

    @BeforeEach
    void setUp() 
    {
        mailSender = mock(JavaMailSender.class);
        service = new MailService(mailSender, "noreply@duckchess.local");
    }

    @Test
    void sendsVerificationEmailWithCorrectHeadersAndBody() 
    {
        service.sendVerificationCode("user@example.com", "123456");

        ArgumentCaptor<SimpleMailMessage> captor =
                ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(captor.capture());
        SimpleMailMessage sent = captor.getValue();

        assertThat(sent.getFrom()).isEqualTo("noreply@duckchess.local");
        assertThat(sent.getTo()).containsExactly("user@example.com");
        assertThat(sent.getSubject()).contains("verify");
        assertThat(sent.getText()).contains("123456");
    }

    @Test
    void doesNotSendWhenMailSenderIsInvokedOnceOnly() 
    {
        service.sendVerificationCode("a@b.c", "999999");
        verify(mailSender, times(1)).send(any(SimpleMailMessage.class));
        verifyNoMoreInteractions(mailSender);
    }
    @Test
    void sendsToDifferentRecipientsIndependently() 
    {
        service.sendVerificationCode("first@example.com", "111111");
        service.sendVerificationCode("second@example.com", "222222");

        ArgumentCaptor<SimpleMailMessage> captor =
                ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender, times(2)).send(captor.capture());

        assertThat(captor.getAllValues())
                .extracting(m -> m.getTo()[0])
                .containsExactly("first@example.com", "second@example.com");
        assertThat(captor.getAllValues())
                .extracting(SimpleMailMessage::getText)
                .anyMatch(t -> t.contains("111111"))
                .anyMatch(t -> t.contains("222222"));
    }

    @Test
    void propagatesMailSenderException() 
    {
        doThrow(new org.springframework.mail.MailSendException("smtp down"))
                .when(mailSender).send(any(SimpleMailMessage.class));

        assertThatThrownBy(() ->
                service.sendVerificationCode("user@example.com", "123456"))
                .isInstanceOf(org.springframework.mail.MailSendException.class);
    }

    @Test
    void codeAppearsExactlyOnceInBody() 
    {
        service.sendVerificationCode("user@example.com", "424242");

        ArgumentCaptor<SimpleMailMessage> captor =
                ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(captor.capture());

        long occurrences = captor.getValue().getText()
                .split("424242", -1).length - 1;
        assertThat(occurrences).isEqualTo(1);
    }

    @Test
    void bodyMentionsExpiryDuration() 
    {
        service.sendVerificationCode("user@example.com", "123456");

        ArgumentCaptor<SimpleMailMessage> captor =
                ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(captor.capture());

        assertThat(captor.getValue().getText()).contains("60 minutes");
    }
}