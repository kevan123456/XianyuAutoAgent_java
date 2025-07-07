package org.automation.goofish.data;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface ItemRepository extends R2dbcRepository<ItemContext, String> {

    @Query("""
            INSERT INTO item_context (item_id, item_info)
            VALUES (:#{#item.itemId}, :#{#item.itemInfo})
            """)
    Mono<Void> insert(@Param("item") ItemContext item);
}
