package com.shivaraj.hireflow.application;

import java.util.Map;

/** Number of applications in each stage for one job, used for the recruiter dashboard. */
public record PipelineSummary(Long jobId, long total, Map<Stage, Long> byStage) {
}
