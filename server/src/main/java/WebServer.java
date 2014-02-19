import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.DateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * Created by Александр on 19.02.14.
 */
public class WebServer extends Thread{
    Socket socket;

    public static void main(String args[])
    {
        try
        {
            ServerSocket server = new ServerSocket(80, 0,
                    InetAddress.getByName("localhost"));

            System.out.println("server is started");

            while(true)
            {
                new WebServer(server.accept());
            }
        }
        catch(Exception e)
        {System.out.println("init error: "+e);}
    }

    public WebServer(Socket socket)
    {
        this.socket = socket;

        setDaemon(true);
        setPriority(NORM_PRIORITY);
        start();
    }

    public void run()
    {
        try
        {
            InputStream inputStream = socket.getInputStream();
            OutputStream outputStream = socket.getOutputStream();

            byte buf[] = new byte[64*1024];
            int readBuf = inputStream.read(buf);
            String request = new String(buf, 0, readBuf);
            String path = getPath(request);

            if(path == null)
            {
                String response = "HTTP/1.1 405 Method Not Allowed\n";

                DateFormat dateFormat = DateFormat.getTimeInstance();
                dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
                response = response + "Date: " + dateFormat.format(new Date()) + "\n";

                response = response
                        + "Connection: close\n"
                        + "WebServer: WEBServer\n";

                outputStream.write(response.getBytes());
                socket.close();
                return;
            }

            File file = new File(path);
            boolean fileIsExist = !file.exists();

            if(!fileIsExist)
                if(file.isDirectory())
                {
                    if(path.lastIndexOf("" + File.separator) == path.length()-1)
                        path = path + "index.html";
                    else
                        path = path + File.separator + "index.html";
                    file = new File(path);
                    fileIsExist = !file.exists();
                }

            if(fileIsExist)
            {
                String response = "HTTP/1.1 404 Not Found\n";
                DateFormat dateFormat = DateFormat.getTimeInstance();
                dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
                response = response + "Date: " + dateFormat.format(new Date()) + "\n";
                response = response
                        + "Content-Type: text/plain\n"
                        + "Connection: close\n"
                        + "WebServer: WEBServer\n"
                        + "Pragma: no-cache\n\n";

                response = response + "File " + path + " not found!";
                outputStream.write(response.getBytes());
                socket.close();
                return;
            }

            String mime = "text/plain";

            readBuf = path.lastIndexOf(".");
            if(readBuf > 0)
            {
                String ext = path.substring(readBuf);
                if(ext.equalsIgnoreCase(".html"))
                    mime = "text/html";
                else if(ext.equalsIgnoreCase(".js"))
                    mime = "text/javascript";
                else if(ext.equalsIgnoreCase(".css"))
                    mime = "text/css";
                else if(ext.equalsIgnoreCase(".gif"))
                    mime = "image/gif";
                else if(ext.equalsIgnoreCase(".jpg"))
                    mime = "image/jpeg";
                else if(ext.equalsIgnoreCase(".jpeg"))
                    mime = "image/jpeg";
                else if(ext.equalsIgnoreCase(".png"))
                    mime = "image/png";
                else if(ext.equalsIgnoreCase(".swf"))
                    mime = "image/swf";

            }

            String response = "HTTP/1.1 200 OK\n";
            DateFormat dateFormat = DateFormat.getTimeInstance();
            dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
            response = response + "Last-Modified: " + dateFormat.format(new Date(file.lastModified())) + "\n";
            response = response + "Content-Length: " + file.length() + "\n";
            response = response + "Content-Type: " + mime + "\n";
            response = response
                    + "Connection: close\n"
                    + "WebServer: WEBServer\n\n";
            outputStream.write(response.getBytes());

            String isHead = extract(request, "HEAD ", " ");
            if(isHead == null)
            {
                FileInputStream fileInputStream = new FileInputStream(path);
                readBuf = 1;
                while(readBuf > 0)
                {
                    readBuf = fileInputStream.read(buf);
                    if(readBuf > 0) outputStream.write(buf, 0, readBuf);
                }
                fileInputStream.close();
            }
            socket.close();
        }
        catch(Exception e)
        {e.printStackTrace();}
    }

    private String getPath(String header)
    {
        String URIGet = extract(header, "GET ", " ");
        String URIHead = extract(header, "HEAD ", " ");
        String path, URI;
        if(URIGet != null)
        {
            path = URIGet;
            URI = URIGet;
        }

        else if (URIHead != null)
        {
            path = URIHead;
            URI = URIHead;
        }
        else
            return null;

        path.toLowerCase();
        if(path.indexOf("http://", 0) == 0)
        {
            URI = URI.substring(7);
            URI = URI.substring(URI.indexOf("/", 0));
        }
        else if(path.indexOf("/", 0) == 0)
            URI = URI.substring(1);

        int i = URI.indexOf("?");
        if(i > 0) URI = URI.substring(0, i);
        i = URI.indexOf("#");
        if(i > 0) URI = URI.substring(0, i);

        path = "." + File.separator;
        char a;
        for(i = 0; i < URI.length(); i++)
        {
            a = URI.charAt(i);
            if(a == '/')
                path = path + File.separator;
            else
                path = path + a;
        }
        return path;
    }

    private String extract(String str, String start, String end)
    {
        int resultStart = str.indexOf("\n\n", 0), resultEnd;
        if(resultStart < 0) resultStart = str.indexOf("\r\n\r\n", 0);
        if(resultStart > 0) str = str.substring(0, resultStart);
        resultStart = str.indexOf(start, 0) + start.length();
        if(resultStart < start.length()) return null;
        resultEnd = str.indexOf(end, resultStart);
        if(resultEnd < 0) resultEnd = str.length();
        return (str.substring(resultStart, resultEnd)).trim();
    }
}
