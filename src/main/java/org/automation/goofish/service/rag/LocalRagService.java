package org.automation.goofish.service.rag;

import com.alibaba.cloud.ai.advisor.DocumentRetrievalAdvisor;
import lombok.Getter;
import lombok.SneakyThrows;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.rag.retrieval.search.VectorStoreDocumentRetriever;
import org.springframework.ai.reader.TextReader;
import org.springframework.ai.reader.markdown.MarkdownDocumentReader;
import org.springframework.ai.reader.markdown.config.MarkdownDocumentReaderConfig;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.stereotype.Component;

import static java.lang.invoke.MethodHandles.lookup;

@Component
public class LocalRagService {

    private static final Logger logger = LoggerFactory.getLogger(lookup().lookupClass());

    @Getter
    DocumentRetrievalAdvisor documentRetrievalAdvisor;

    private final VectorStore vectorStore;

    @Autowired
    @SneakyThrows
    public LocalRagService(
            VectorStore vectorStore,
            ResourcePatternResolver resourcePatternResolver
    ) {
        this.vectorStore = vectorStore;
        this.documentRetrievalAdvisor = new DocumentRetrievalAdvisor(new VectorStoreDocumentRetriever(vectorStore, 0.7, 5, null));
        Resource[] knowledgeDocs = resourcePatternResolver.getResources("classpath:rag/**/*.{pdf,txt,md}");
        initialize(knowledgeDocs);
    }

    /**
     * 初始化向量存储：自动选择Reader解析文件
     */
    private void initialize(Resource[] resources) {
        TokenTextSplitter splitter = TokenTextSplitter.builder().withChunkSize(800).build();
        for (Resource resource : resources) {
            String filename = resource.getFilename();
            if (filename == null) continue;

            // 提取文件后缀（小写）
            switch (FilenameUtils.getExtension(filename)) {
                case "pdf" -> vectorStore.write(splitter.transform(new PagePdfDocumentReader(resource).read()));
                case "txt" -> vectorStore.write(splitter.transform(new TextReader(resource).read()));
                case "md" ->
                        vectorStore.write(splitter.transform(new MarkdownDocumentReader(resource, MarkdownDocumentReaderConfig.defaultConfig()).read()));
            }
        }
    }
}
