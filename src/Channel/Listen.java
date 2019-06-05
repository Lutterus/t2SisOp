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
				clientSentence = executeCommand(clientSentence, connectionSocket) + '\n';
				if (clientSentence.contains("/part")) {
					clientSentence = "Desconectado do canal" + "\n";
					Thread replier = new Thread(new ReplyOne(clientSentence, user, "SERVER"));
					replier.start();
					try {
						replier.join();
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

				} else {
					Thread replier = new Thread(new ReplyOne(clientSentence, user, "SERVER"));
					replier.start();
				}
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
					currentChannel.disconnectOne(user);
					user.setCurrentChannel(null);
				} else {
					clientSentence = clientSentence + '\n';
					Thread replier = new Thread(new ReplyAll(clientSentence, user, user.getName()));
					replier.start();
				}

			}
		}

	}

	private String executeCommand(String clientSentence, Socket connectionSocket) {
		if (clientSentence.startsWith("/nick ") && wordcount(clientSentence) == 2) {
			clientSentence = clientSentence.replace("/nick ", "");
			user.setName(clientSentence);
			return "NICK alterado com sucesso para ";
		} else if (clientSentence.startsWith("/join ") && wordcount(clientSentence) == 2) {
			clientSentence = clientSentence.replace("/join ", "");
			if (channelFather.channelExist(clientSentence)) {
				channelFather.transferUser(clientSentence, connectionSocket);
				String answer = "Conectado ao canal '" + clientSentence + "'";
				pending = false;
				return answer;
			} else {
				String answer = "O canal '" + clientSentence + "' nao existe";
				return answer;
			}
		} else if (clientSentence.startsWith("/create") && wordcount(clientSentence) == 2) {
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
		} else if (clientSentence.contentEquals("/list")) {
			String answer = channelFather.toString();
			return answer;
		} else if (clientSentence.contentEquals("/part")) {
			Thread listener = new Thread(new ListenServer(connectionSocket, palavrasReservadas, channelFather));
			listener.start();
			pending = false;
			Channel currentChannel = user.getCurrentChannel();
			currentChannel.disconnectOne(user);
			user.setCurrentChannel(null);
			return "/part";
		} else if (clientSentence.contentEquals("/names")) {
			String answer = user.getCurrentChannel().getUsersString();
			return answer;
		} else if (clientSentence.startsWith("/remove") && wordcount(clientSentence) == 2) {
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
		} else if (clientSentence.startsWith("/msg") && wordcount(clientSentence) > 2) {
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
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				return answer;
			} else {
				return "Nao foi possivel encontrar um usuario com o nome '" + userName + "'";
			}
			// String echo2 = str_array[1];
			// System.out.println("echo1: " + echo1);
			// System.out.println("echo2: "+ echo2);
			// System.out.println("tamanho: " + str_array.length);
			// if() {

			// }
		}

		return "Comando usado de forma incorreta";

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
