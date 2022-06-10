package zh.tools.error;

public class ZhToolsBaseException extends RuntimeException {

	private static final long serialVersionUID = -256044999580636074L;

	public ZhToolsBaseException(String string) {
		super(string);
	}

	public ZhToolsBaseException(String message, Throwable cause) {
		super(message, cause);
	}

	public ZhToolsBaseException(Throwable cause) {
		super(cause);
	}

	public ZhToolsBaseException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public ZhToolsBaseException() {
	}
}
