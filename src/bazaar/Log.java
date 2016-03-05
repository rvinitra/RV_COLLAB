/**
 * 
 */
package bazaar;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author root
 *
 */
public final class Log {
    public final static Logger l = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
    public static Level info=Level.INFO;
    public static Level finest=Level.FINEST;
    public Log() {
	l.setLevel(Level.INFO);
	l.setLevel(Level.FINEST);
    }
}
