package io;

import lombok.Data;

import java.util.List;
@Data
public class ListMessage implements Message{
    List<String> files;

    public ListMessage(List<String> files) {
        this.files = files;
    }
    @Override
    public MassageType getType() {
        return MassageType.LIST;
    }
}
