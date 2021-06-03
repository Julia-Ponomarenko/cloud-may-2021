package netty;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.stream.Collectors;

@Slf4j
public class ObjectHandler extends SimpleChannelInboundHandler<Message> {
    private String login = "DefaultUser";
    private String dir = "serverDir";
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        ctx.writeAndFlush(
                Message.builder()
                        .author("Server")
                        .createdAt(LocalDateTime.now())
                        .content("Enter the login")
                        .build()
        );
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Message message) throws Exception {
        log.debug("received: {}", message);
        ctx.writeAndFlush(message);
        if (message.getContent().startsWith("/help")){}
        else if (message.getContent().equals("/ls")) {
            String files = Files.list(Paths.get(dir + "/" + login))
                    .map(path -> path.getFileName().toString())
                    .collect(Collectors.joining("\n")) + "\n\r";
            ctx.writeAndFlush(
                    Message.builder()
                            .author("Server")
                            .createdAt(LocalDateTime.now())
                            .content(files)
                            .build()
            );

        }

        else if (message.getContent().startsWith("/cat ")) {
            String fileName = message.getContent().replaceAll("/cat ", "");
            String data = String.join("", Files.readAllLines(Paths.get(dir+ "/" + login, fileName))) + "\n\r";
            ctx.writeAndFlush( Message.builder()
                    .author("Server")
                    .createdAt(LocalDateTime.now())
                    .content(data)
                    .build()
            );
        }

        else if (message.getContent().startsWith("/mkdir ")) {
            String dirName = message.getContent().replaceAll("/mkdir ", "");
            Files.createDirectory(Paths.get(login + "/" + dirName));
            ctx.writeAndFlush( Message.builder()
                    .author("Server")
                    .createdAt(LocalDateTime.now())
                    .content("directory created")
                    .build()
            );
        }

        else if (message.getContent().startsWith("/touch ")) {
            String fileName = message.getContent().replaceAll("/touch ", "");
            Files.createFile(Paths.get(dir+ "/" + login, fileName));
            ctx.writeAndFlush(Message.builder()
                    .author("Server")
                    .createdAt(LocalDateTime.now())
                    .content("file created")
                    .build()
            );
        }

        else if (message.getContent().startsWith("/auth ")) {
            login = message.getContent().replaceAll("/auth ","");
        }

        else if (message.getContent().startsWith("/read ")) {
            String [] splittedMessage = message.getContent().split(" ");
            String fileName = splittedMessage[splittedMessage.length-1];
            String mes = message.getContent().replaceAll("/read ", "").replaceAll(" "+fileName, "");
            Files.write(Paths.get(dir + "/" + login, fileName), mes.getBytes(StandardCharsets.UTF_8));
            ctx.writeAndFlush(Message.builder()
                    .author("Server")
                    .createdAt(LocalDateTime.now())
                    .content("file updated")
                    .build()
            );
        }
        else {
            ctx.writeAndFlush(Message.builder()
                    .author("Server")
                    .createdAt(LocalDateTime.now())
                    .content("Wrong command")
                    .build()
            );
        }
    }
}
