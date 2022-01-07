package PamguardMVC.debug;

import java.io.PrintStream;
import java.util.Locale;

public class Debug {
	
	public static PrintStream out;
	public static PrintStream err;
	
	{
		out = new DebugOut(System.out);
		err = new DebugOut(System.err);
	}
	
	public static final String flag = "-debugout";
	
	private static boolean printDebug = false;

	private class DebugOut extends PrintStream {

		private PrintStream printStream;

		public DebugOut(PrintStream printStream) {
			super(printStream);
			this.printStream = printStream;
		}

		@Override
		public void println(boolean x) {
			if (printDebug) {
				printStream.println(x);
			}
		}

		@Override
		public void println(char x) {
			if (printDebug) {
				printStream.println(x);
			}
		}

		@Override
		public void println(int x) {
			if (printDebug) {
				printStream.println(x);
			}
		}

		@Override
		public void println(long x) {
			if (printDebug) {
				printStream.println(x);
			}
		}

		@Override
		public void println(float x) {
			if (printDebug) {
				printStream.println(x);
			}
		}

		@Override
		public void println(double x) {
			if (printDebug) {
				printStream.println(x);
			}
		}

		@Override
		public void println(char[] x) {
			if (printDebug) {
				printStream.println(x);
			}
		}

		@Override
		public void println(String x) {
			if (printDebug) {
				printStream.println(x);
			}
		}

		@Override
		public void println(Object x) {
			if (printDebug) {
				printStream.println(x);
			}
		}

		@Override
		public PrintStream printf(String format, Object... args) {
			if (printDebug) {
				return printStream.printf(format, args);
			}
			else {
				return null;
			}
		}

		@Override
		public PrintStream printf(Locale l, String format, Object... args) {
			if (printDebug) {
				return printStream.printf(l, format, args);
			}
			else {
				return null;
			}
		}

		@Override
		public void flush() {
			printStream.flush();
		}

		@Override
		public boolean checkError() {
			return printStream.checkError();
		}


		@Override
		public void write(int b) {
			if (printDebug) {
				printStream.write(b);
			}
		}

		@Override
		public void write(byte[] buf, int off, int len) {
			if (printDebug) {
				printStream.write(buf, off, len);
			}
		}

		@Override
		public void print(boolean b) {
			if (printDebug) {
				printStream.print(b);
			}
		}

		@Override
		public void print(char c) {
			if (printDebug) {
				printStream.print(c);
			}
		}

		@Override
		public void print(int i) {
			if (printDebug) {
				printStream.print(i);
			}
		}

		@Override
		public void print(long l) {
			if (printDebug) {
				printStream.print(l);
			}
		}

		@Override
		public void print(float f) {
			if (printDebug) {
				printStream.print(f);
			}
		}

		@Override
		public void print(double d) {
			if (printDebug) {
				printStream.print(d);
			}
		}

		@Override
		public void print(char[] s) {
			if (printDebug) {
				printStream.print(s);
			}
		}

		@Override
		public void print(String s) {
			if (printDebug) {
				printStream.print(s);
			}
		}

		@Override
		public void print(Object obj) {
			if (printDebug) {
				printStream.print(obj);
			}
		}

		@Override
		public void println() {
			if (printDebug) {
				printStream.println();
			}
		}

		@Override
		public PrintStream format(String format, Object... args) {
			if (printDebug) {
				return printStream.format(format, args);
			}
			else {
				return null;
			}
		}

		@Override
		public PrintStream format(Locale l, String format, Object... args) {
			if (printDebug) {
				return printStream.format(l, format, args);
			}
			else {
				return null;
			}
		}

		@Override
		public PrintStream append(CharSequence csq) {
			if (printDebug) {
				return printStream.append(csq);
			}
			else {
				return null;
			}
		}

		@Override
		public PrintStream append(CharSequence csq, int start, int end) {
			if (printDebug) {
				return printStream.append(csq, start, end);
			}
			else {
				return null;
			}
		}

		@Override
		public PrintStream append(char c) {
			if (printDebug) {
				return printStream.append(c);
			}
			else {
				return null;
			}
		}
		
	}

	/**
	 * @return the printDebug
	 */
	public static boolean isPrintDebug() {
		return printDebug;
	}

	/**
	 * @param printDebug the printDebug to set
	 */
	public static void setPrintDebug(boolean printDebug) {
		new Debug();
		Debug.printDebug = printDebug;
	}
}
