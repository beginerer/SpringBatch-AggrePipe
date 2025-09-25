package com.core.example;

import org.springframework.batch.core.ChunkListener;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.stereotype.Component;

@Component
public class ProgressChunkListener implements ChunkListener{

    @Override
    public void afterChunk(ChunkContext ctx) {
        StepExecution se = ctx.getStepContext().getStepExecution();
        System.out.printf("[PROGRESS] step=%s read=%d write=%d commit=%d%n",
                se.getStepName(), se.getReadCount(), se.getWriteCount(), se.getCommitCount());

    }
}
