package Channel;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class ReplyAll implements Runnable {
	private String echo;
	private ArrayList<Users> userList;

	public ReplyAll(String echo, Users user, String name) {
		this.echo = setEcho(echo, name);
		this.userList = getUsers(user);

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

	private ArrayList<Users> getUsers(Users user) {
		Channel currentChannel = user.getCurrentChannel();
		userList = currentChannel.getUsers();
		return userList;
	}

	@Override
	public void run() {
		for (Users user : userList) {
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
				Channel currentChannel = user.getCurrentChannel();
				currentChannel.removeUser(user);
				e.printStackTrace();
			}
		}

	}

}
