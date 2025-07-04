package org.automation.goofish.data;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface ChatRepository extends R2dbcRepository<ChatContext, String> {

    @Query("""
        INSERT INTO chat_context (chat_id, chat_history, item_id)
        VALUES (:#{#chat.chatId}, :#{#chat.chatHistory}, :#{#chat.itemId})
        """)
    Mono<Void> insert(@Param("chat") ChatContext chat);
}
