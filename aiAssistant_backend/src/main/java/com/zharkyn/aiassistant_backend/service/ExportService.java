package com.zharkyn.aiassistant_backend.service;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfWriter;
import com.zharkyn.aiassistant_backend.model.Interview;
import com.zharkyn.aiassistant_backend.model.Message;
import com.zharkyn.aiassistant_backend.repository.InterviewRepository;
import com.zharkyn.aiassistant_backend.repository.MessageRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;

@Service
@Slf4j
public class ExportService {

    private final InterviewRepository interviewRepository;
    private final MessageRepository messageRepository;

    @Autowired
    public ExportService(InterviewRepository interviewRepository, 
                        MessageRepository messageRepository) {
        this.interviewRepository = interviewRepository;
        this.messageRepository = messageRepository;
    }

    public byte[] exportToCSV(List<String> interviewIds) throws ExecutionException, InterruptedException {
        log.info("Exporting {} interviews to CSV", interviewIds.size());
        
        StringBuilder csv = new StringBuilder();
        csv.append("Interview ID,Position,Company,Date,Status,Question,Answer\n");
        
        for (String interviewId : interviewIds) {
            Interview interview = interviewRepository.getById(interviewId);
            if (interview == null) {
                log.warn("Interview not found: {}", interviewId);
                continue;
            }
            
            List<Message> messages = messageRepository.getBySessionId(interviewId);
            
            String baseInfo = String.format("%s,%s,%s,%s,%s",
                    escapeCSV(interviewId),
                    escapeCSV(interview.getPosition()),
                    escapeCSV(interview.getCompany()),
                    formatDate(interview.getCreatedAt()),
                    escapeCSV(interview.getStatus())
            );
            
            Message currentQuestion = null;
            for (Message message : messages) {
                if ("assistant".equals(message.getRole())) {
                    currentQuestion = message;
                } else if ("user".equals(message.getRole()) && currentQuestion != null) {
                    csv.append(baseInfo)
                       .append(",")
                       .append(escapeCSV(currentQuestion.getContent()))
                       .append(",")
                       .append(escapeCSV(message.getContent()))
                       .append("\n");
                    currentQuestion = null;
                }
            }
        }
        
        return csv.toString().getBytes();
    }

    public byte[] exportToPDFHtml(List<String> interviewIds) throws ExecutionException, InterruptedException, DocumentException {
        log.info("Exporting {} interviews to PDF", interviewIds.size());
        
        Document document = new Document(PageSize.A4);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        
        try {
            PdfWriter.getInstance(document, baos);
            document.open();

            Font titleFont = new Font(Font.FontFamily.HELVETICA, 18, Font.BOLD);
            Font headerFont = new Font(Font.FontFamily.HELVETICA, 14, Font.BOLD);
            Font normalFont = new Font(Font.FontFamily.HELVETICA, 12, Font.NORMAL);
            Font boldFont = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD);

            Paragraph mainTitle = new Paragraph("Interviews Export Report", titleFont);
            mainTitle.setAlignment(Element.ALIGN_CENTER);
            mainTitle.setSpacingAfter(30);
            document.add(mainTitle);

            for (String interviewId : interviewIds) {
                Interview interview = interviewRepository.getById(interviewId);
                if (interview == null) {
                    log.warn("Interview not found: {}", interviewId);
                    continue;
                }
                
                List<Message> messages = messageRepository.getBySessionId(interviewId);

                document.add(new Paragraph("Position: " + interview.getPosition(), headerFont));
                document.add(new Paragraph("Company: " + interview.getCompany(), normalFont));
                document.add(new Paragraph("Date: " + formatDate(interview.getCreatedAt()), normalFont));
                document.add(new Paragraph("Status: " + interview.getStatus(), normalFont));
                document.add(Chunk.NEWLINE);

                Paragraph dialogTitle = new Paragraph("Conversation", headerFont);
                dialogTitle.setSpacingBefore(10);
                dialogTitle.setSpacingAfter(10);
                document.add(dialogTitle);

                for (Message message : messages) {
                    Paragraph messagePara = new Paragraph();
                    
                    if ("user".equals(message.getRole())) {
                        messagePara.add(new Chunk("Candidate: ", boldFont));
                    } else {
                        messagePara.add(new Chunk("Interviewer: ", boldFont));
                    }
                    
                    messagePara.add(new Chunk(message.getContent(), normalFont));
                    messagePara.setSpacingAfter(10);
                    document.add(messagePara);
                }

                document.add(Chunk.NEWLINE);
                document.add(new Paragraph("_".repeat(80)));
                document.add(Chunk.NEWLINE);
                document.add(Chunk.NEWLINE);
            }

            document.close();
            log.info("PDF generated successfully");
            
            return baos.toByteArray();
            
        } catch (DocumentException e) {
            log.error("Error generating PDF", e);
            throw e;
        }
    }

    public byte[] exportInterviewToPDF(String interviewId) throws ExecutionException, InterruptedException, DocumentException {
        log.info("Exporting interview to PDF: {}", interviewId);

        Interview interview = interviewRepository.getById(interviewId);
        if (interview == null) {
            throw new IllegalArgumentException("Interview not found: " + interviewId);
        }

        List<Message> messages = messageRepository.getBySessionId(interviewId);

        Document document = new Document(PageSize.A4);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        
        try {
            PdfWriter.getInstance(document, baos);
            document.open();

            Font titleFont = new Font(Font.FontFamily.HELVETICA, 18, Font.BOLD);
            Font headerFont = new Font(Font.FontFamily.HELVETICA, 14, Font.BOLD);
            Font normalFont = new Font(Font.FontFamily.HELVETICA, 12, Font.NORMAL);
            Font boldFont = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD);

            Paragraph title = new Paragraph("Interview Report", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(20);
            document.add(title);

            document.add(new Paragraph("Position: " + interview.getPosition(), headerFont));
            document.add(new Paragraph("Company: " + interview.getCompany(), normalFont));
            document.add(new Paragraph("Date: " + formatDate(interview.getCreatedAt()), normalFont));
            document.add(new Paragraph("Status: " + interview.getStatus(), normalFont));
            document.add(Chunk.NEWLINE);

            document.add(new Paragraph("_".repeat(80)));
            document.add(Chunk.NEWLINE);

            Paragraph dialogTitle = new Paragraph("Interview Conversation", headerFont);
            dialogTitle.setSpacingBefore(10);
            dialogTitle.setSpacingAfter(10);
            document.add(dialogTitle);

            for (Message message : messages) {
                Paragraph messagePara = new Paragraph();
                
                if ("user".equals(message.getRole())) {
                    messagePara.add(new Chunk("Candidate: ", boldFont));
                } else {
                    messagePara.add(new Chunk("Interviewer: ", boldFont));
                }
                
                messagePara.add(new Chunk(message.getContent(), normalFont));
                messagePara.setSpacingAfter(10);
                document.add(messagePara);
            }

            document.add(Chunk.NEWLINE);
            document.add(new Paragraph("_".repeat(80)));
            document.add(Chunk.NEWLINE);

            if (interview.getFeedback() != null && !interview.getFeedback().isEmpty()) {
                Paragraph feedbackTitle = new Paragraph("Feedback", headerFont);
                feedbackTitle.setSpacingBefore(10);
                feedbackTitle.setSpacingAfter(10);
                document.add(feedbackTitle);
                
                Paragraph feedback = new Paragraph(interview.getFeedback(), normalFont);
                document.add(feedback);
            }

            document.close();
            log.info("PDF generated successfully for interview: {}", interviewId);
            
            return baos.toByteArray();
            
        } catch (DocumentException e) {
            log.error("Error generating PDF", e);
            throw e;
        }
    }

    public String saveDraft(String interviewId) throws ExecutionException, InterruptedException {
        log.info("Saving draft for interview: {}", interviewId);
        
        Interview interview = interviewRepository.getById(interviewId);
        if (interview == null) {
            throw new IllegalArgumentException("Interview not found: " + interviewId);
        }
        
        String userId = getCurrentUserId();
        if (!interview.getUserId().equals(userId)) {
            throw new IllegalArgumentException("Unauthorized access to interview");
        }
        
        interview.setStatus("draft");
        interviewRepository.update(interviewId, interview);
        
        log.info("Draft saved successfully for interview: {}", interviewId);
        return "Draft saved successfully";
    }

    public String pauseInterview(String interviewId) throws ExecutionException, InterruptedException {
        log.info("Pausing interview: {}", interviewId);
        
        Interview interview = interviewRepository.getById(interviewId);
        if (interview == null) {
            throw new IllegalArgumentException("Interview not found: " + interviewId);
        }
        
        String userId = getCurrentUserId();
        if (!interview.getUserId().equals(userId)) {
            throw new IllegalArgumentException("Unauthorized access to interview");
        }
        
        interview.setStatus("paused");
        interviewRepository.update(interviewId, interview);
        
        log.info("Interview paused successfully: {}", interviewId);
        return "Interview paused successfully";
    }

    private String escapeCSV(String value) {
        if (value == null) {
            return "";
        }
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    private String formatDate(Long timestamp) {
        if (timestamp == null) {
            return "N/A";
        }
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return sdf.format(new Date(timestamp));
    }

    private String getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("User not authenticated");
        }
        return authentication.getName();
    }
}