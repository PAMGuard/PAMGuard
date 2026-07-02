package PamController.command;

public class JVMHeap extends ExtCommand{

	public JVMHeap() {
		super("JVMHeap", true);
		// TODO Auto-generated constructor stub
	}

	@Override
	public String execute(String command) {
		// TODO Auto-generated method stub
		return String.valueOf(Runtime.getRuntime().totalMemory());
	}

}
