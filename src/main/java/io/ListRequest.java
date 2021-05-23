package io;

public class ListRequest implements Message{
    @Override
    public MassageType getType() {
        return MassageType.LIST_REQEST;
    }
}
