package netty;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Message implements Serializable {

    private String content;
    private String author;
    private LocalDateTime createdAt;

    public String getDateFormatted() {
        return DateTimeFormatter.ofPattern("MM-dd-yyyy HH:mm:ss").format(createdAt);
    }

}
