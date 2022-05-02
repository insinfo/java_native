package demo;

import org.apache.sshd.client.SshClient;
import org.apache.sshd.client.keyverifier.AcceptAllServerKeyVerifier;
import org.apache.sshd.client.session.ClientSession;
import org.apache.sshd.sftp.client.SftpClient;
import org.apache.sshd.sftp.client.SftpClientFactory;
import org.apache.sshd.sftp.client.SftpClient.DirEntry;

import java.nio.charset.Charset;
import java.time.Duration;

public class SshdExample {
    public static void init(String host, String user, String pass, String dirToDownload, String outFile) {
        try {
            try (SshClient sshClient = SshClient.setUpDefaultClient();) {
                sshClient.setServerKeyVerifier(AcceptAllServerKeyVerifier.INSTANCE);
                sshClient.addPasswordIdentity(pass);
                sshClient.start();

                var timeout = Duration.ofSeconds(5);

                try (ClientSession sshSession = sshClient.connect(user, host, 22)
                        .verify(timeout)
                        .getSession()) {
                    //sshSession.addPasswordIdentity(pass);
                    sshSession.auth().verify(timeout);
                    // System.out.println(sshSession.auth().verify().isSuccess());
                    SftpClientFactory factory = SftpClientFactory.instance();
                    try (SftpClient sftpClient = factory.createSftpClient(sshSession)) {
                        sftpClient.setNameDecodingCharset(Charset.forName("ISO-8859-1"));
                        for (DirEntry entry : sftpClient.readDir(dirToDownload)) {
                            System.out.println(entry.getFilename());
                        }
                    }

                }
                sshClient.stop();
            }

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("SshdExample@init: " + e);
        }
    }
}
