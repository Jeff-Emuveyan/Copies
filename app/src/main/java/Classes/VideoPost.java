package Classes;

/**
 * Created by JEFF EMUVEYAN on 1/28/2018.
 */

public class VideoPost {

    private int id;
    private String title;
    private String period;
    private String imageLink;
    private String videoLink;
    private String audioLink;
    private String numberOfLikes;
    private String numberOfCopies;
    private String userId;
    private String originalVideoPostId;
    private String videoType;
    private boolean isLiked;




    public VideoPost(int id, String title, String period, String imageLink, String videoLink, String audioLink, String numberOfLikes, String numberOfCopies, String userId, boolean isLiked) {
        this.id = id;
        this.title = title;
        this.period = period;
        this.imageLink = imageLink;
        this.videoLink = videoLink;
        this.audioLink = audioLink;
        this.numberOfLikes = numberOfLikes;
        this.numberOfCopies = numberOfCopies;
        this.userId = userId;
        this.isLiked = isLiked;
    }


    public VideoPost(int id, String title, String period, String imageLink, String videoLink, String audioLink, String numberOfLikes, String numberOfCopies, String userId, String originalVideoPostId, boolean isLiked) {
        this.id = id;
        this.title = title;
        this.period = period;
        this.imageLink = imageLink;
        this.videoLink = videoLink;
        this.audioLink = audioLink;
        this.numberOfLikes = numberOfLikes;
        this.numberOfCopies = numberOfCopies;
        this.userId = userId;
        this.originalVideoPostId = originalVideoPostId;
        this.isLiked = isLiked;
    }



    public VideoPost(String title, String period, String imageLink, String videoLink, String audioLink, String numberOfLikes, String numberOfCopies, String userId, String originalVideoPostId, String videoType) {
        this.title = title;
        this.period = period;
        this.imageLink = imageLink;
        this.videoLink = videoLink;
        this.audioLink = audioLink;
        this.numberOfLikes = numberOfLikes;
        this.numberOfCopies = numberOfCopies;
        this.userId = userId;
        this.originalVideoPostId = originalVideoPostId;
        this.videoType = videoType;
    }


    public VideoPost(int id, String title, String period, String imageLink, String videoLink, String audioLink, String numberOfLikes, String numberOfCopies, String userId, String originalVideoPostId, String videoType, boolean isLiked) {
        this.id = id;
        this.title = title;
        this.period = period;
        this.imageLink = imageLink;
        this.videoLink = videoLink;
        this.audioLink = audioLink;
        this.numberOfLikes = numberOfLikes;
        this.numberOfCopies = numberOfCopies;
        this.userId = userId;
        this.originalVideoPostId = originalVideoPostId;
        this.videoType = videoType;
        this.isLiked = isLiked;
    }


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getPeriod() {
        return period;
    }

    public void setPeriod(String period) {
        this.period = period;
    }

    public String getImageLink() {
        return imageLink;
    }

    public void setImageLink(String imageLink) {
        this.imageLink = imageLink;
    }

    public String getVideoLink() {
        return videoLink;
    }

    public void setVideoLink(String videoLink) {
        this.videoLink = videoLink;
    }

    public String getAudioLink() {
        return audioLink;
    }

    public void setAudioLink(String audioLink) {
        this.audioLink = audioLink;
    }

    public String getNumberOfLikes() {
        return numberOfLikes;
    }

    public void setNumberOfLikes(String numberOfLikes) {
        this.numberOfLikes = numberOfLikes;
    }

    public String getNumberOfCopies() {
        return numberOfCopies;
    }

    public void setNumberOfCopies(String numberOfCopies) {
        this.numberOfCopies = numberOfCopies;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public boolean isLiked() {
        return isLiked;
    }

    public void setLiked(boolean liked) {
        isLiked = liked;
    }

    public String getOriginalVideoPostId() {
        return originalVideoPostId;
    }

    public void setOriginalVideoPostId(String originalVideoPostId) {
        this.originalVideoPostId = originalVideoPostId;
    }

    public String getVideoType() {
        return videoType;
    }

    public void setVideoType(String videoType) {
        this.videoType = videoType;
    }
}
