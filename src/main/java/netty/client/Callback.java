package netty.client;

import netty.Message;

@FunctionalInterface
public interface Callback {
    void call(Message message);
}
