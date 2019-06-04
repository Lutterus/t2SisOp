package Channel;

import java.net.InetAddress;
import java.util.ArrayList;

import Server.ListenServer;

public class Channel {
	private String name;
	private InetAddress adm;
	private ArrayList<Users> userList;
	private ChannelList father;

	public Channel(String name, InetAddress adm, ChannelList father) {
		this.name = name;
		this.adm = adm;
		userList = new ArrayList<Users>();
		this.father = father;
	}

	public String getName() {
		return name;
	}
	
	public void addUser(Users user, ReservedWords palavrasReservadas) {
		userList.add(user);
		startThread(user, palavrasReservadas, father);
	}
	
	public void removeUser(Users user) {
		userList.remove(user);
	}
	
	public void startThread(Users user, ReservedWords palavrasReservadas, ChannelList father) {
		Thread listener = new Thread(new Listen(user, palavrasReservadas, father));
		listener.start();
	}
	
	public ArrayList<Users> getUsers(){
		return userList;
	}
	
	public String getUsersString() {
		String names = "Usuarios => ";
		for (Users user : userList) {
			if(user.getName()==null) {
				names = names + ", anonymous";
			}else {
				names = names + ", "+ user.getName();
			}
			
		}
		
		return names;
	}

	public ChannelList getFather() {
		return father;
	}
	
	public InetAddress getAdm() {
		return adm;
	}

	public void disconnectAll(ReservedWords palavrasReservadas, ChannelList channelFather) {
		for (Users user : userList) {
			String clientSentence = "Nao e mais possivel mandar mensagens, pois estre canal foi excluido pelo administrador";
			Thread replier = new Thread(new ReplyAll(clientSentence, user, "SERVER"));
			replier.start();
			try {
				replier.join();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			Thread listener = new Thread(new ListenServer(user.getSocket(), palavrasReservadas, channelFather));
			listener.start();
			user.setCurrentChannel(null);
		}
		
		userList.clear();
		
	}
	
	public void disconnectOne(Users user) {
		userList.remove(user);
	}
	
}