package Channel;

import java.net.InetAddress;
import java.net.Socket;

public class Users {
	private String name;
	private Socket socket;
	private Channel currentChannel;
	private String lastMessage;

	public Users(String name, Socket socket, Channel currentChannel) {
		this.name = null;
		this.socket = socket;
		this.currentChannel = currentChannel;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Socket getSocket() {
		return socket;
	}

	public void setCurrentChannel(Channel currentChannel) {
		this.currentChannel = currentChannel;
	}

	public Channel getCurrentChannel() {
		return currentChannel;
	}

	public String getIp() {
		InetAddress IPAddress = socket.getInetAddress();
		return IPAddress.getHostAddress();
	}

	public String getLastMessage() {
		return lastMessage;
	}

	public void setLastMessage(String lastMessage) {
		this.lastMessage = lastMessage;
	}

}
