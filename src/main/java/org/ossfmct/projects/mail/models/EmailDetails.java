package org.ossfmct.projects.mail.models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EmailDetails implements Serializable {

    private String msgBody = "";

    private List<MultipartFile> attachments = new ArrayList<>();

    public boolean isValid() {
        return !msgBody.isEmpty();
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof EmailDetails)) return false;
        if (this == object) return true;
        EmailDetails other = (EmailDetails) object;
        if (this.msgBody.equals(other.msgBody)
            && this.attachments.equals(other.attachments)) return true;

        return false;
    }

    @Override
    public String toString() {
        return "EmailDetails{"
            + "msgBody='" + (msgBody.isEmpty() ? "" : msgBody) + "'"
            + ", attachments=" + (attachments.isEmpty() ? "[]" : attachments.toString())
            + "}";
    }
}