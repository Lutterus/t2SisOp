package Channel;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class ReplyOne implements Runnable {
	private String echo;
	private Users user;

	public ReplyOne(String echo, Users user, String name) {
		this.echo = setEcho(echo, name);
		this.user = user;

	}

	private String setEcho(String echo, String name) {
		if (name == null) {
			echo = "anonymous: " + echo;
		} else if (name.contentEquals("SERVER")) {
			echo = "FROM SERVER: " + echo;
		} else {
			echo = name + ": " + echo;
		}

		return echo;
	}

	@Override
	public void run() {
		DataOutputStream outToClient = null;
		try {
			outToClient = new DataOutputStream(user.getSocket().getOutputStream());
		} catch (IOException e1) {
			System.err.println("Erro na thread de envio, ao obter endereço do cliente");
			e1.printStackTrace();
		}
		try {
			outToClient.writeBytes(echo);
		} catch (IOException e) {
			System.err.println("Erro na thread de envio, ao enviar a mensagem para o cliente");
			e.printStackTrace();
		}

	}

}
