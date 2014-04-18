/**
 * Created by alexandr on 12.04.14.
 */
import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class LoggerFactory
{
    public static Logger getLogger(String loggerName, String fileName)
    {
        Logger logger = Logger.getLogger(loggerName);
        try {
            FileHandler logFile = new FileHandler(fileName);
            logFile.setFormatter(new SimpleFormatter());
            logger.addHandler(logFile);
            logger.setUseParentHandlers(false);
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return logger;
    }
}
