package com.podosoftware.competency.job;

import java.util.List;

import architecture.common.user.Company;

public interface JobManager {

	public abstract int getJobCount(Company company);
	
	public abstract List<Job> getJobs(Company company) ;
	
	public abstract List<Job> getJobs(Company company, int startIndex, int numResults) ;
	
	
	public abstract int getJobCount(Company company, Classification classify);
	
	public abstract List<Job> getJobs(Company company, Classification classify) ;
	
	public abstract List<Job> getJobs(Company company, Classification classify, int startIndex, int numResults) ;	
	
	
	public abstract void saveOrUpdate(Job job) throws JobNotFoundException ;
	
	public abstract Job getJob(long jobId) throws JobNotFoundException;
	
	public void batchUpdate(List<Job> jobs) ;
}