package Channel;

import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;

public class ChannelList {
	private ArrayList<Channel> channelList;
	private ReservedWords palavrasReservadas;
	private ChannelList selfReference;

	public ChannelList(ReservedWords palavrasReservadas) {
		channelList = new ArrayList<Channel>();
		this.palavrasReservadas = palavrasReservadas;
	}

	public void transferUser(String channelName, Socket connectionSocket, Users user) {
		for (Channel channel : channelList) {
			if (channel.getName().contentEquals(channelName)) {
				user.setCurrentChannel(channel);
				channel.addUser(user, palavrasReservadas);
			}
		}
	}

	public void addChannel(String name, InetAddress admIp) {
		Channel c = new Channel(name, admIp, selfReference);
		channelList.add(c);
	}

	public void addChannelDefault(String string) {
		Channel c = new Channel(string, null, selfReference);
		channelList.add(c);
	}

	public boolean channelExist(String name) {
		for (Channel channel : channelList) {
			if (channel.getName().contentEquals(name)) {
				return true;
			}
		}
		return false;
	}

	public boolean isAdm(String nameChannel, InetAddress connectionSocket) {
		for (Channel channel : channelList) {
			if (channel.getName().contentEquals(nameChannel)) {
				if (channel.getAdm() == connectionSocket) {
					return true;
				} else {
					return false;
				}

			}
		}
		return false;
	}

	public String toString() {
		String list = "Canais => ";
		for (Channel channel : channelList) {
			list = list + ", " + channel.getName();
		}
		return list;
	}

	public void setSelfReference(ChannelList cl) {
		this.selfReference = cl;
	}

	public ChannelList getSelfReference() {
		return selfReference;
	}

	public void disconnectAll(String name) {
		for (Channel channel : channelList) {
			if (channel.getName().contentEquals(name)) {
				channel.disconnectAll(palavrasReservadas, selfReference);
			}
		}
	}

	public void disconnectOneByName(String name, Users user, int type) {
		for (Channel channel : channelList) {
			if (channel.getName().contentEquals(name)) {
				channel.disconnectOne(user, palavrasReservadas, selfReference);
			}
		}
	}
	
	public void disconnectOne(Users user, int type) {
		Channel channel = user.getCurrentChannel();
		System.out.println("nome do canal " + user.getCurrentChannel().getName());
		channel.disconnectOne(user, palavrasReservadas, selfReference);
	}

	public void removeChannel(String nameChannel) {
		Channel ch = null;
		for (Channel channel : channelList) {
			if (channel.getName().contentEquals(nameChannel)) {
				ch = channel;
			}
		}

		channelList.remove(ch);

	}

	public boolean isInChannel(Users user, String userName) {
		Channel currentChannel = user.getCurrentChannel();
		if (currentChannel.isInChannel(userName)) {
			return true;
		}
		return false;
	}

	public Users getUserInChannel(Users user, String userName) {
		Channel currentChannel = user.getCurrentChannel();
		return currentChannel.getUser(userName);
	}

	public boolean isNameFree(String name) {
		for (Channel channel : channelList) {
			ArrayList<Users> tempChannelList = channel.getUsers();
			for (Users user : tempChannelList) {
				if (user.getName() != null && user.getName().contentEquals(name)) {
					return false;
				}
			}
		}

		return true;
	}
	
	public void disconnectAnonymous(Users adm) {
		adm.getCurrentChannel().cleanAnonymous(palavrasReservadas, selfReference);
	}
	
	public void cleanChannels(Users user) {
		ArrayList<String> channelsName = new ArrayList<String>();
		for (Channel channel : channelList) {
			if(channel.getAdm()==user.getSocket().getInetAddress()) {
				disconnectAll(channel.getName());
				channelsName.add(channel.getName());
			}
		}
		
		for (String string : channelsName) {
			removeChannel(string);
		}
	}
}
