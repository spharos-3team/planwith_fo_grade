package com.planwith.planwith_fo_grade.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "grade.kafka")
public class GradeKafkaProperties {

	private boolean consumerEnabled = true;
	private Topics topics = new Topics();

	public boolean isConsumerEnabled() {
		return consumerEnabled;
	}

	public void setConsumerEnabled(boolean consumerEnabled) {
		this.consumerEnabled = consumerEnabled;
	}

	public Topics getTopics() {
		return topics;
	}

	public void setTopics(Topics topics) {
		this.topics = topics;
	}

	public static class Topics {
		private String storyCreated = "planwith.story.created";
		private String storyDeleted = "planwith.story.deleted";
		private String followCreated = "planwith.follow.created";
		private String followRemoved = "planwith.follow.removed";
		private String likeCreated = "planwith.like.created";
		private String likeRemoved = "planwith.like.removed";
		private String memberCreated = "planwith.member.created";
		private String gradeChanged = "planwith.grade.changed";
		private String gradeRewardGranted = "planwith.grade.reward-granted";

		public String getStoryCreated() { return storyCreated; }
		public void setStoryCreated(String storyCreated) { this.storyCreated = storyCreated; }
		public String getStoryDeleted() { return storyDeleted; }
		public void setStoryDeleted(String storyDeleted) { this.storyDeleted = storyDeleted; }
		public String getFollowCreated() { return followCreated; }
		public void setFollowCreated(String followCreated) { this.followCreated = followCreated; }
		public String getFollowRemoved() { return followRemoved; }
		public void setFollowRemoved(String followRemoved) { this.followRemoved = followRemoved; }
		public String getLikeCreated() { return likeCreated; }
		public void setLikeCreated(String likeCreated) { this.likeCreated = likeCreated; }
		public String getLikeRemoved() { return likeRemoved; }
		public void setLikeRemoved(String likeRemoved) { this.likeRemoved = likeRemoved; }
		public String getMemberCreated() { return memberCreated; }
		public void setMemberCreated(String memberCreated) { this.memberCreated = memberCreated; }
		public String getGradeChanged() { return gradeChanged; }
		public void setGradeChanged(String gradeChanged) { this.gradeChanged = gradeChanged; }
		public String getGradeRewardGranted() { return gradeRewardGranted; }
		public void setGradeRewardGranted(String gradeRewardGranted) { this.gradeRewardGranted = gradeRewardGranted; }
	}
}
