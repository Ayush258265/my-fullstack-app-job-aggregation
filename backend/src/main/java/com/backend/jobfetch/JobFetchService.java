package com.backend.jobfetch;

import com.backend.entity.Job;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class JobFetchService {

	private final ArbeitnowClient arbeitnowClient;
	private final RemotiveClient remotiveClient;
	private final RemoteOKClient remoteOKClient;
	private final AdzunaClient adzunaClient;
	private final GreenhouseClient greenhouseClient;
	private final LeverClient leverClient;
	private final JobProcessor jobProcessor;

	public void fetchAllJobs() {
		log.info("Starting job fetch from all sources...");

		List<Job> allJobs = new ArrayList<>();

		// Fetch from each source
		allJobs.addAll(arbeitnowClient.fetchJobs());
		allJobs.addAll(remotiveClient.fetchJobs());
		allJobs.addAll(remoteOKClient.fetchJobs());
		allJobs.addAll(adzunaClient.fetchJobs());
		allJobs.addAll(greenhouseClient.fetchJobs());
		allJobs.addAll(leverClient.fetchJobs());

		log.info("Total jobs fetched: {}", allJobs.size());

		// Process and save
		if (!allJobs.isEmpty()) {
			jobProcessor.processJobs(allJobs);
		} else {
			log.warn("No jobs fetched from any source");
		}
	}
}