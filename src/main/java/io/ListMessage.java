package io;

import java.util.List;

import lombok.Data;

@Data
public class ListMessage implements Message {

    List<String> files;

    public ListMessage(List<String> files) {
        this.files = files;
    }

    @Override
    public MessageType getType() {
        return MessageType.LIST;
    }

}
