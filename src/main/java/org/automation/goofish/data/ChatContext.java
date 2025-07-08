package org.automation.goofish.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Table("chat_context")
public class ChatContext {
    @Id
    private String chatId;
    String chatHistory;
    String chatAnalysis;
    private String itemId;
}
