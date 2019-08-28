package uren.com.myduties.models;

import java.util.ArrayList;
import java.util.List;

public class Group {
    private String groupid = null;
    private String name = null;
    private String groupPhotoUrl = null;
    private long createAt;
    private String groupAdmin = null;
    private List<String> memberIdList = new ArrayList<>();
    private String type;

    public Group() {
    }

    public Group(String groupid, String name, String groupPhotoUrl, long createAt, String groupAdmin,
                  List<String> memberIdList, String type) {
        this.groupid = groupid;
        this.name = name;
        this.groupPhotoUrl = groupPhotoUrl;
        this.createAt = createAt;
        this.groupAdmin = groupAdmin;
        this.memberIdList = memberIdList;
        this.type = type;
    }

    public Group(String groupid) {
        this.groupid = groupid;
    }

    public String getGroupid() {
        return this.groupid;
    }

    public void setGroupid(String groupid) {
        this.groupid = groupid;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getGroupPhotoUrl() {
        return this.groupPhotoUrl;
    }

    public void setGroupPhotoUrl(String groupPhotoUrl) {
        this.groupPhotoUrl = groupPhotoUrl;
    }

    public long getCreateAt() {
        return createAt;
    }

    public void setCreateAt(long createAt) {
        this.createAt = createAt;
    }

    public List<String> getMemberIdList() {
        return memberIdList;
    }

    public void setMemberIdList(List<String> memberIdList) {
        this.memberIdList = memberIdList;
    }

    public String getGroupAdmin() {
        return this.groupAdmin;
    }

    public void setGroupAdmin(String groupAdmin) {
        this.groupAdmin = groupAdmin;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
