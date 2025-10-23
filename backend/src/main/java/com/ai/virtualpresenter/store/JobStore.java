package com.ai.virtualpresenter.store;

import com.ai.virtualpresenter.model.GenerationJob;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class JobStore {

    private final Map<String, GenerationJob> jobs = new ConcurrentHashMap<>();

    public void save(GenerationJob job) {
        jobs.put(job.getJobId(), job);
    }

    public Optional<GenerationJob> findById(String jobId) {
        return Optional.ofNullable(jobs.get(jobId));
    }
}
