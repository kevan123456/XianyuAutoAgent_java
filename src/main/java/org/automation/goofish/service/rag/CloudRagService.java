package org.automation.goofish.service.rag;

import com.alibaba.cloud.ai.advisor.DocumentRetrievalAdvisor;
import com.alibaba.cloud.ai.dashscope.api.DashScopeApi;
import com.alibaba.cloud.ai.dashscope.rag.DashScopeDocumentRetriever;
import com.alibaba.cloud.ai.dashscope.rag.DashScopeDocumentRetrieverOptions;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static java.lang.invoke.MethodHandles.lookup;

@Component
public class CloudRagService {

    private static final Logger logger = LoggerFactory.getLogger(lookup().lookupClass());

    DashScopeApi dashScopeApi;
    @Getter
    DocumentRetrievalAdvisor documentRetrievalAdvisor;
    DashScopeDocumentRetriever dashScopeDocumentRetriever;
    private static final String indexName = "闲鱼";

    @Autowired
    public CloudRagService(DashScopeApi dashScopeApi) {
        this.dashScopeDocumentRetriever = new DashScopeDocumentRetriever(dashScopeApi, DashScopeDocumentRetrieverOptions.builder()
                .withIndexName(indexName).build());
        this.dashScopeApi = dashScopeApi;
        this.documentRetrievalAdvisor = new DocumentRetrievalAdvisor(dashScopeDocumentRetriever);
    }
}
