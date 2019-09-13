package uren.com.myduties.models;

import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Group {
    private String groupid = null;
    private String name = null;
    private String groupPhotoUrl = null;
    private long createAt;
    private String groupAdmin = null;
    private List<User> memberList = new ArrayList<>();

    public Group(String groupid) {
        this.groupid = groupid;
    }


    /*public Group() {
    }

    public Group(String groupid, String name, String groupPhotoUrl, long createAt, String groupAdmin,
                  List<User> memberList) {
        this.groupid = groupid;
        this.name = name;
        this.groupPhotoUrl = groupPhotoUrl;
        this.createAt = createAt;
        this.groupAdmin = groupAdmin;
        this.memberList = memberList;
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

    public List<User> getMemberList() {
        return memberList;
    }

    public void setMemberList(List<User> memberList) {
        this.memberList = memberList;
    }

    public String getGroupAdmin() {
        return this.groupAdmin;
    }

    public void setGroupAdmin(String groupAdmin) {
        this.groupAdmin = groupAdmin;
    }*/

}
