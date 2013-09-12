 serviceStatistics {
	/* The name of the metric that is the basis
	for the scale rule decision
	In the current example, the metric we use is "Total Requests Count".
	*/
	metric "Total Requests Count"

	/* (Optional)
	The sliding time range (in seconds) for
	aggregating per-instance metric samples.
	The number of samples in the time windows equals
	the time window divided by the sampling period.
	Default value: 300 */
	movingTimeRangeInSeconds 20

	/* (Optional)
	The algorithms for aggregating metric samples by instances
	and by time. Metric samples are aggregated separately
	per instance in the specified time range,and then aggregated
	again for all instances.
	Default value: Statistics.averageOfAverages
	Possible values:
	Statistics.maximumOfAverages,
	Statistics.minimumOfAverages,
	Statistics.averageOfAverages,
	Statistics.percentileOfAverages(90),
	Statistics.maximumOfMaximums,
	Statistics.minimumOfMinimums,
	Statistics.maximumThroughput.
	The following has the same effect as setting instancesStatistics
	and timeStatistics separately.
	For example:
	statistics Statistics.maximumOfAverages
	is the same as:
	timeStatistics Statistics.average
	instancesStatistics Statistics.maximum */
	statistics Statistics.maximumThroughput
}

highThreshold {
	/* The value above which the number of instances is increased */
	value 1
	/* The number of instances to increase when above threshold */
	instancesIncrease 1
}

lowThreshold {
	/* The value below which the number of instances is decreased */
	value 0.2
	/* The number of instances to decrease when below threshold */
	instancesDecrease 1
}