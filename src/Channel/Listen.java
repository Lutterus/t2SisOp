package Channel;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import Server.ListenServer;

public class Listen implements Runnable {
	private Socket connectionSocket;
	private BufferedReader inFromClient;
	private String clientSentence;
	private ReservedWords palavrasReservadas;
	private Users user;
	private boolean pending = true;
	private ChannelList channelFather;

	public Listen(Users user, ReservedWords palavrasReservadas, ChannelList channelFather) {
		this.connectionSocket = user.getSocket();
		this.palavrasReservadas = palavrasReservadas;
		this.user = user;
		this.channelFather = channelFather;
		inFromClient = createStreamIn();
	}

	private BufferedReader createStreamIn() {
		try {
			inFromClient = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
			return inFromClient;
		} catch (IOException e) {
			System.err.println("Erro durante criação da stream de entrada de dados");
			e.printStackTrace();
		}
		return inFromClient;
	}

	@Override
	public void run() {
		while (pending == true) {
			try {
				clientSentence = inFromClient.readLine();
			} catch (IOException e) {
				System.err.println("Erro ao ler a mensagem enviado pelo cliente");
				pending = false;
				e.printStackTrace();
				
			}
			if (palavrasReservadas.isReserved(clientSentence)) {
				clientSentence = executeCommand(connectionSocket) + '\n';
				Thread replier = new Thread(new ReplyOne(clientSentence, user, "SERVER"));
				replier.start();
			} else {
				if (clientSentence.contentEquals("/quit")) {
					pending = false;
					clientSentence = clientSentence + '\n';
					Thread replier = new Thread(new ReplyOne(clientSentence, user, "SERVER"));
					replier.start();
					try {
						replier.join();
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					Channel currentChannel = user.getCurrentChannel();
					currentChannel.disconnectOne(user, palavrasReservadas, channelFather, 1);
					user.setCurrentChannel(null);
				}else if(clientSentence.contentEquals("/part")) {
					break;
				} else {
					clientSentence = clientSentence + '\n';
					Thread replier = new Thread(new ReplyAll(clientSentence, user, user.getName()));
					replier.start();
				}

			}
		}

	}

	private String executeCommand(Socket connectionSocket) {
		if (clientSentence.startsWith("/nick ") && wordcount(clientSentence) == 2) {
			clientSentence = commandNick();
			return clientSentence;

		} else if (clientSentence.startsWith("/join ") && wordcount(clientSentence) == 2) {
			clientSentence = commandJoin();
			return clientSentence;

		} else if (clientSentence.startsWith("/create") && wordcount(clientSentence) == 2) {
			clientSentence = commandCreate();
			return clientSentence;

		} else if (clientSentence.contentEquals("/list")) {
			clientSentence = commandList();
			return clientSentence;

		} else if (clientSentence.contentEquals("/part")) {
			clientSentence = commandPart();
			return clientSentence;

		} else if (clientSentence.contentEquals("/names")) {
			clientSentence = commandNames();
			return clientSentence;

		} else if (clientSentence.startsWith("/remove") && wordcount(clientSentence) == 2) {
			clientSentence = commandRemove();
			return clientSentence;

		} else if (clientSentence.startsWith("/msg") && wordcount(clientSentence) > 2) {
			clientSentence = commandMSG();
			return clientSentence;

		} else if (clientSentence.startsWith("/kick") && wordcount(clientSentence) == 3) {
			clientSentence = commandKick();
			return clientSentence;
		}

		return "Comando usado de forma incorreta";

	}

	private String commandKick() {
		String[] str_array = clientSentence.split(" ");
		String channelName = str_array[1];
		String userName = str_array[2];
		if (channelFather.isAdm(channelName, connectionSocket.getInetAddress())) {
			if (userName != "anonymous") {
				if (channelFather.isInChannel(user, userName)) {
					Users victim = channelFather.getUserInChannel(user, userName);
					channelFather.disconnectOneByName(channelName, victim, 0);
				} else {
					return "O usuario '" + userName + "' nao esta conectado no canal '" + channelName + "'";
				}
			}
			return "Usuario removido do canal";
		} else {
			return "Nao e possivel executar esta acao, pois voce nao e o administrador deste canal";
		}
	}

	private String commandMSG() {
		clientSentence = clientSentence.replace("/msg ", "");
		String[] str_array = clientSentence.split(" ");
		String userName = str_array[0];
		if (channelFather.isInChannel(user, userName)) {
			String answer = "[MENSAGEM PRIVADA] ";
			for (int i = 1; i < str_array.length; i++) {
				answer = answer + str_array[i] + " ";
			}
			String dest = answer + "\n";
			Users tempUser = channelFather.getUserInChannel(user, userName);
			Thread replier = new Thread(new ReplyOne(dest, tempUser, userName));
			replier.start();
			try {
				replier.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			return answer;
		} else {
			return "Nao foi possivel encontrar um usuario com o nome '" + userName + "'";
		}
	}

	private String commandNames() {
		String answer = user.getCurrentChannel().getUsersString();
		return answer;
	}

	private String commandRemove() {
		clientSentence = clientSentence.replace("/remove ", "");
		if (channelFather.channelExist(clientSentence)) {
			if (channelFather.isAdm(clientSentence, connectionSocket.getInetAddress())) {
				String answer = "Canal Removido";
				channelFather.disconnectAll(clientSentence);
				channelFather.removeChannel(clientSentence);
				return answer;
			} else {
				String answer = "Nao e possivel executar esta acao, pois voce nao e o administrador deste canal";
				return answer;
			}

		} else {
			String answer = "Nao encontramos um servidor com o nome '" + clientSentence + "'";
			return answer;
		}
	}

	private String commandPart() {
		Thread listener = new Thread(new ListenServer(connectionSocket, palavrasReservadas, channelFather, user));
		listener.start();
		Channel currentChannel = user.getCurrentChannel();
		currentChannel.disconnectOne(user, palavrasReservadas, channelFather, 1);
		user.setCurrentChannel(null);
		return "/part";
	}

	private String commandList() {
		String answer = channelFather.toString();
		return answer;
	}

	private String commandCreate() {
		clientSentence = clientSentence.replace("/create ", "");
		if (channelFather.channelExist(clientSentence)) {
			String answer = "Já existe um canal com o nome '" + clientSentence + "'";
			return answer;
		} else {
			channelFather.addChannel(clientSentence, connectionSocket.getInetAddress());
			String answer = "Criado um canal com o nome '" + clientSentence + "', para o acessar digite '/join "
					+ clientSentence + "'";
			return answer;
		}
	}

	private String commandJoin() {
		clientSentence = clientSentence.replace("/join ", "");
		if (channelFather.channelExist(clientSentence)) {
			channelFather.transferUser(clientSentence, connectionSocket, user);
			String answer = "Conectado ao canal '" + clientSentence + "'";
			pending = false;
			return answer;
		} else {
			String answer = "O canal '" + clientSentence + "' nao existe";
			return answer;
		}
	}

	private String commandNick() {
		clientSentence = clientSentence.replace("/nick ", "");
		if (clientSentence.contentEquals("'anonymous'")) {
			return "Nao e possivel alterar o nome de usuario para anonymous, visto que esta e uma palavra reservada";
		} else if (channelFather.isNameFree(clientSentence) == false) {
			return "Este nome esta sendo usado por outro usuario";
		} else {
			String alert = "";
			if (user.getName() == null) {
				alert = "O usuario 'anonymous' alterou seu 'NICK' para '" + clientSentence + "'\n";
			} else {
				alert = "O usuario " + user.getName() + " alterou seu 'NICK' para '" + clientSentence + "'\n";
			}

			Thread replier = new Thread(new ReplyAll(alert, user, "SERVER"));
			replier.start();
			try {
				replier.join();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			user.setName(clientSentence);
			return "NICK alterado com sucesso para " + clientSentence;
		}

	}

	static int wordcount(String string) {
		int count = 0;

		char ch[] = new char[string.length()];
		for (int i = 0; i < string.length(); i++) {
			ch[i] = string.charAt(i);
			if (((i > 0) && (ch[i] != ' ') && (ch[i - 1] == ' ')) || ((ch[0] != ' ') && (i == 0)))
				count++;
		}
		return count;
	}

}
