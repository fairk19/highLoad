/**
 * Created by Александр on 19.02.14.
 */
import java.io.*;
import java.net.*;

public class Client {
    public static void main(String args[])
    {
        try
        {
            byte buf[] = new byte[64*1024];
            int r;

            FileInputStream fis = new FileInputStream(args[0]);
            r = fis.read(buf);
            String header = new String(buf, 0, r);
            fis.close();

            String host = extract(header, "Host:", "\n");


            if(host == null)
            {
                System.out.println("invalid request:\n"+header);
                return;
            }

            int port = host.indexOf(":",0);
            if(port < 0) port = 80;
            else
            {
                port = Integer.parseInt(host.substring(port+1));
                host = host.substring(0, port);
            }

            Socket s = new Socket(host, port);

            s.getOutputStream().write(header.getBytes());

            InputStream is = s.getInputStream();

            FileOutputStream fos = new FileOutputStream(args[1]);

            r = 1;
            while(r > 0)
            {
                r = is.read(buf);
                if(r > 0)
                    fos.write(buf, 0, r);
            }

            fos.close();
            s.close();
        }
        catch(Exception e)
        {e.printStackTrace();}
    }

    protected static String extract(String str, String start, String end)
    {
        int s = str.indexOf("\n\n", 0), e;
        if(s < 0) s = str.indexOf("\r\n\r\n", 0);
        if(s > 0) str = str.substring(0, s);
        s = str.indexOf(start, 0)+start.length();
        if(s < start.length()) return null;
        e = str.indexOf(end, s);
        if(e < 0) e = str.length();
        return (str.substring(s, e)).trim();
    }
}
