CREATE TABLE `user_events` (
  `id` bigint(20) NOT NULL,
  `userid` varchar(50) DEFAULT NULL,
  `fullName` varchar(100) DEFAULT NULL,
  `gender` varchar(10) DEFAULT NULL,
  `relationshipStatus` varchar(20) DEFAULT NULL,
  `activityTimestamp` timestamp NULL DEFAULT NULL,
  `activityType` varchar(30) DEFAULT NULL,
  `activityMetadata` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;