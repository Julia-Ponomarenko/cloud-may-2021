package nio;

import io.FileObject;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.Set;
import java.util.stream.Collectors;

public class Server {

    private ByteBuffer buffer;
    private ServerSocketChannel serverSocketChannel;
    private Selector selector;
    private String dir = "serverDir";
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(Server.class);

    public Server() throws Exception {
        buffer = ByteBuffer.allocate(100);
        serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.bind(new InetSocketAddress(8189));
        serverSocketChannel.configureBlocking(false);
        selector = Selector.open();
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
        log.debug("Server started");

        while (serverSocketChannel.isOpen()) {

            selector.select(); // block

            Set<SelectionKey> selectionKeys = selector.selectedKeys();

            Iterator<SelectionKey> keyIterator = selectionKeys.iterator();

            while (keyIterator.hasNext()) {
                SelectionKey key = keyIterator.next();
                if (key.isAcceptable()) {
                    handleAccept(key);
                }
                if (key.isReadable()) {
                    handleRead(key);
                }
                keyIterator.remove();
            }
        }
    }

    private void handleRead(SelectionKey key) throws IOException {
        SocketChannel channel = (SocketChannel) key.channel();
        try {
            StringBuilder s = new StringBuilder();
            int r;
            while (true) {
                r = channel.read(buffer);
                if (r == -1) {
                    channel.close();
                    return;
                }
                if (r == 0) {
                    break;
                }
                buffer.flip();
                while (buffer.hasRemaining()) {
                    s.append((char) buffer.get());
                }
                buffer.clear();
            }

            String message = s.toString().trim();
            log.debug("received message", message);
            if (message.equals("Welcome to server!")) {}
            else if (message.equals("ls")) {
                String files = Files.list(Paths.get(dir))
                        .map(path -> path.getFileName().toString())
                        .collect(Collectors.joining("\n")) + "\n\r";
                channel.write(ByteBuffer.wrap(files.getBytes(StandardCharsets.UTF_8)));
            } else if (message.startsWith("cat ")) {
                String fileName = message.replaceAll("cat ", "");
                String data = String.join("", Files.readAllLines(Paths.get(dir, fileName))) + "\n\r";
                channel.write(ByteBuffer.wrap(data.getBytes(StandardCharsets.UTF_8)));}
            else if (message.startsWith("mkdir ")) {
                String dirName = message.replaceAll("mkdir ", "");
                Files.createDirectory(Paths.get(dirName));
                channel.write(ByteBuffer.wrap("directory created".getBytes(StandardCharsets.UTF_8)));}
            else if (message.startsWith("touch ")) {
                String fileName = message.replaceAll("touch ", "");
                Files.createFile(Paths.get(dir, fileName));
                channel.write(ByteBuffer.wrap("file created".getBytes(StandardCharsets.UTF_8)));

            }
            else if (message.startsWith("read ")) {
                String [] splittedMessage = message.split(" ");
                String fileName = splittedMessage[splittedMessage.length-1];
                String mes = message.replaceAll("read ", "").replaceAll(" "+fileName, "");
                Files.write(Paths.get(dir, fileName), mes.getBytes(StandardCharsets.UTF_8));
                channel.write(ByteBuffer.wrap("file updated".getBytes(StandardCharsets.UTF_8)));
            }
            else {
                channel.write(ByteBuffer.wrap("Wrong command\n\r".getBytes(StandardCharsets.UTF_8)));
            }
        } catch (Exception e) {
            log.error("Exception while read: ", e);
            try {
                channel.write(ByteBuffer.wrap("Wrong command\n\r".getBytes(StandardCharsets.UTF_8)));
            } catch (IOException ioException) {
                log.error("Exception while write response: ", e);
            }
        }
    }


    private void handleAccept(SelectionKey key) throws IOException {
        SocketChannel channel = serverSocketChannel.accept();
        channel.write(ByteBuffer.wrap("Welcome to server!".getBytes(StandardCharsets.UTF_8)));
        channel.configureBlocking(false);
        channel.register(selector, SelectionKey.OP_READ, "Hello world");
    }
}
