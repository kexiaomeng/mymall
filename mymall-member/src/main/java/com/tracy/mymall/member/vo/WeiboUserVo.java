package com.tracy.mymall.member.vo;

import java.util.List;

public class WeiboUserVo {
    private String id;

    private String screen_name;

    private String name;

    private String province;

    private String city;

    private String location;

    private String description;

    private String url;

    private String profile_image_url;

    private String domain;

    private String gender;

    private String followers_count;

    private String friends_count;

    private String statuses_count;

    private String favourites_count;

    private String created_at;

    private boolean following;

    private boolean allow_all_act_msg;

    private boolean geo_enabled;

    private boolean verified;

    private Status status;

    private boolean allow_all_comment;

    private String avatar_large;

    private String verified_reason;

    private boolean follow_me;

    private String online_status;

    private String bi_followers_count;

    public void setId(String id){
        this.id = id;
    }
    public String getId(){
        return this.id;
    }
    public void setScreen_name(String screen_name){
        this.screen_name = screen_name;
    }
    public String getScreen_name(){
        return this.screen_name;
    }
    public void setName(String name){
        this.name = name;
    }
    public String getName(){
        return this.name;
    }
    public void setProvince(String province){
        this.province = province;
    }
    public String getProvince(){
        return this.province;
    }
    public void setCity(String city){
        this.city = city;
    }
    public String getCity(){
        return this.city;
    }
    public void setLocation(String location){
        this.location = location;
    }
    public String getLocation(){
        return this.location;
    }
    public void setDescription(String description){
        this.description = description;
    }
    public String getDescription(){
        return this.description;
    }
    public void setUrl(String url){
        this.url = url;
    }
    public String getUrl(){
        return this.url;
    }
    public void setProfile_image_url(String profile_image_url){
        this.profile_image_url = profile_image_url;
    }
    public String getProfile_image_url(){
        return this.profile_image_url;
    }
    public void setDomain(String domain){
        this.domain = domain;
    }
    public String getDomain(){
        return this.domain;
    }
    public void setGender(String gender){
        this.gender = gender;
    }
    public String getGender(){
        return this.gender;
    }
    public void setFollowers_count(String followers_count){
        this.followers_count = followers_count;
    }
    public String getFollowers_count(){
        return this.followers_count;
    }
    public void setFriends_count(String friends_count){
        this.friends_count = friends_count;
    }
    public String getFriends_count(){
        return this.friends_count;
    }
    public void setStatuses_count(String statuses_count){
        this.statuses_count = statuses_count;
    }
    public String getStatuses_count(){
        return this.statuses_count;
    }
    public void setFavourites_count(String favourites_count){
        this.favourites_count = favourites_count;
    }
    public String getFavourites_count(){
        return this.favourites_count;
    }
    public void setCreated_at(String created_at){
        this.created_at = created_at;
    }
    public String getCreated_at(){
        return this.created_at;
    }
    public void setFollowing(boolean following){
        this.following = following;
    }
    public boolean getFollowing(){
        return this.following;
    }
    public void setAllow_all_act_msg(boolean allow_all_act_msg){
        this.allow_all_act_msg = allow_all_act_msg;
    }
    public boolean getAllow_all_act_msg(){
        return this.allow_all_act_msg;
    }
    public void setGeo_enabled(boolean geo_enabled){
        this.geo_enabled = geo_enabled;
    }
    public boolean getGeo_enabled(){
        return this.geo_enabled;
    }
    public void setVerified(boolean verified){
        this.verified = verified;
    }
    public boolean getVerified(){
        return this.verified;
    }
    public void setStatus(Status status){
        this.status = status;
    }
    public Status getStatus(){
        return this.status;
    }
    public void setAllow_all_comment(boolean allow_all_comment){
        this.allow_all_comment = allow_all_comment;
    }
    public boolean getAllow_all_comment(){
        return this.allow_all_comment;
    }
    public void setAvatar_large(String avatar_large){
        this.avatar_large = avatar_large;
    }
    public String getAvatar_large(){
        return this.avatar_large;
    }
    public void setVerified_reason(String verified_reason){
        this.verified_reason = verified_reason;
    }
    public String getVerified_reason(){
        return this.verified_reason;
    }
    public void setFollow_me(boolean follow_me){
        this.follow_me = follow_me;
    }
    public boolean getFollow_me(){
        return this.follow_me;
    }
    public void setOnline_status(String online_status){
        this.online_status = online_status;
    }
    public String getOnline_status(){
        return this.online_status;
    }
    public void setBi_followers_count(String bi_followers_count){
        this.bi_followers_count = bi_followers_count;
    }
    public String getBi_followers_count(){
        return this.bi_followers_count;
    }


    public static class Status {
        private String created_at;

        private String id;

        private String text;

        private String source;

        private boolean favorited;

        private boolean truncated;

        private String in_reply_to_status_id;

        private String in_reply_to_user_id;

        private String in_reply_to_screen_name;

        private String geo;

        private String mid;


        private String reposts_count;

        private String comments_count;

        public void setCreated_at(String created_at){
            this.created_at = created_at;
        }
        public String getCreated_at(){
            return this.created_at;
        }
        public void setId(String id){
            this.id = id;
        }
        public String getId(){
            return this.id;
        }
        public void setText(String text){
            this.text = text;
        }
        public String getText(){
            return this.text;
        }
        public void setSource(String source){
            this.source = source;
        }
        public String getSource(){
            return this.source;
        }
        public void setFavorited(boolean favorited){
            this.favorited = favorited;
        }
        public boolean getFavorited(){
            return this.favorited;
        }
        public void setTruncated(boolean truncated){
            this.truncated = truncated;
        }
        public boolean getTruncated(){
            return this.truncated;
        }
        public void setIn_reply_to_status_id(String in_reply_to_status_id){
            this.in_reply_to_status_id = in_reply_to_status_id;
        }
        public String getIn_reply_to_status_id(){
            return this.in_reply_to_status_id;
        }
        public void setIn_reply_to_user_id(String in_reply_to_user_id){
            this.in_reply_to_user_id = in_reply_to_user_id;
        }
        public String getIn_reply_to_user_id(){
            return this.in_reply_to_user_id;
        }
        public void setIn_reply_to_screen_name(String in_reply_to_screen_name){
            this.in_reply_to_screen_name = in_reply_to_screen_name;
        }
        public String getIn_reply_to_screen_name(){
            return this.in_reply_to_screen_name;
        }
        public void setGeo(String geo){
            this.geo = geo;
        }
        public String getGeo(){
            return this.geo;
        }
        public void setMid(String mid){
            this.mid = mid;
        }
        public String getMid(){
            return this.mid;
        }

        public void setReposts_count(String reposts_count){
            this.reposts_count = reposts_count;
        }
        public String getReposts_count(){
            return this.reposts_count;
        }
        public void setComments_count(String comments_count){
            this.comments_count = comments_count;
        }
        public String getComments_count(){
            return this.comments_count;
        }

    }

}

