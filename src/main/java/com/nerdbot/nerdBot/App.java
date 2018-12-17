package com.nerdbot.nerdBot;


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.json.JSONArray;
import org.json.JSONObject;


import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.entities.VoiceChannel;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import net.dv8tion.jda.core.managers.GuildController;

public class App extends ListenerAdapter
{
	static List<Nerd> nerds = new ArrayList<>();
	static List<Message> messageList = new ArrayList<>();
	static boolean isListening = false;
	static String gameState = "";
	static User player = null;
	
	//gamble
	static int bet = 0;
	
	//blackjack
	static List<String> botHand = new ArrayList<String>();
	static List<String> hand = new ArrayList<String>();
	
    public static void main( String[] args ) throws Exception
    {
        JDA nerdBot = new JDABuilder(AccountType.BOT).setToken(Ref.token).buildBlocking();
        nerdBot.addEventListener(new App());
        buildUserList();      
        
    }
    
    @Override
    public void onMessageReceived(MessageReceivedEvent evt){
    	
    	//Messages necessities
    	String[] strArgs = new String[]{};
    	int nameChange = Ref.names.length;
    	int jakeMe = Ref.jakeMe.length;
    	//Objects
    	User objUser = evt.getAuthor();
    	MessageChannel objMsgChannel = evt.getChannel();
    	Message msg = evt.getMessage();
    	GuildController controller = new GuildController(evt.getGuild());
    	VoiceChannel voicechannel = null;
    	
    	if (evt.getAuthor().isBot()){
    		return;
    	}
    	if (isListening){
    		respond(player,gameState,msg,objMsgChannel);
    		return;
    	}
    	
    	//Commands
    	if (msg.getContentRaw().charAt(0) == Ref.prefix){
    		List<User> mentions = msg.getMentionedUsers();
    		Random rand = new Random();
    		String name = Ref.names[rand.nextInt(nameChange) + 0];
    		strArgs = msg.getContentRaw().substring(1).split(" ");
    		String command = strArgs[0].toLowerCase();
    		
    		if (command.equals("hello") || command.equals("hi") || command.equals("hey")){
    			if (objUser.getId().equals("146054948931633152")){
    				objMsgChannel.sendMessage("NERDDD").queue();
    			}else{
    				objMsgChannel.sendMessage("Hey " + name + "!").queue();
    			}
    		}
    		else if (command.equals("roll")){
        		strArgs = msg.getContentRaw().substring(1).split(" ");
        		if (strArgs.length > 1){
    	    		int rollNum = Integer.valueOf(strArgs[1]);
    	    		int roll = rand.nextInt(rollNum) + 1;
    	    		objMsgChannel.sendMessage(objUser.getAsMention() + " rolled a " + roll + "!").queue();
        		}
    		}
    		else if (command.equals("jakeme")){
    			objMsgChannel.sendMessage(Ref.jakeMe[rand.nextInt(jakeMe) + 0]).queue();	
    		}
    		else if (command.equals("zimmername")){
    			try {
					objMsgChannel.sendMessage("Zimmer's current name on steam : " + getSteamName("76561197983327754")).queue();
				} catch (IOException e) {
					objMsgChannel.sendMessage("Couldn't reach steam API.").queue();
					e.printStackTrace();
				}
    		}
    		else if (command.equals("ihabname")){
    			try {
					objMsgChannel.sendMessage("Ihab's current name on steam : " + getSteamName("76561198065720165")).queue();
				} catch (IOException e) {
					objMsgChannel.sendMessage("Couldn't reach steam API.").queue();
					e.printStackTrace();
				}
    		}
    		else if (command.equals("points")){
    			for (Nerd nerd : nerds){
    				if (objUser.getName().equals(nerd.getUser())){
    					objMsgChannel.sendMessage(nerd.getUser() + " has " + nerd.getPoints() + " nerd points!").queue();
    					return;
    				}
    			}
    			objMsgChannel.sendMessage("You do not currently have any nerd points, " + name).queue();
    		}
    		else if (command.equals("givepoints")){
    			if (strArgs.length > 2){
	    			if (!objUser.getId().equals("146783075487973376")){
	    				objMsgChannel.sendMessage("Only the Bot Lord can assign points, " + name).queue();
	    			}else{
	    				User mention = mentions.get(0);
		    			for (Nerd nerd : nerds){
		    				if (mention.getId().equals(nerd.getId()) && mentions.size() > 0){
		    					objMsgChannel.sendMessage(mention.getName() + " has recieved " + strArgs[2] + " nerd points!").queue();
		    					nerd.setPoints(nerd.getPoints()+Integer.valueOf(strArgs[2]));
		    					try {
									savePoints();
								} catch (IOException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
		    					return;
		    				}
		    			}
		    			nerds.add(new Nerd(mention.getName(),mention.getId(),Integer.valueOf(strArgs[2])));
		    			objMsgChannel.sendMessage(mention.getName() + " has received " + strArgs[2] + " nerd points!").queue();
		    			try {
							savePoints();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
	    			}
    			}
    		}else if (command.equals("gamble")){
    			if (evt.getChannel().getId().equals("434197231260925953")){
            		if (strArgs.length > 1){
        	    		int bet = Integer.valueOf(strArgs[1]);
            		}
	    			objMsgChannel.sendMessage("How many points do you want to gamble?").queue();
	    			isListening = true;
	    			player = msg.getAuthor();
	    			gameState = "gamble";
    			}else{
	    			objMsgChannel.sendMessage("Please put all gambling requests in the casino channel " + name + ".").queue();

    			}
    		}else if (command.equals("blackjack")){
    			if (evt.getChannel().getId().equals("434197231260925953")){
    				if (strArgs.length > 1){
    					bet = Integer.valueOf(strArgs[1]);
    				}else{
    	    			objMsgChannel.sendMessage("Please add bet after.").queue();
    	    			return;
    				}
    				botHand.add(drawCard());
    				botHand.add(drawCard());
    				hand.add(drawCard());
    				hand.add(drawCard());
    				player = msg.getAuthor();
    				objMsgChannel.sendMessage("Bot Hand: " + stringList(botHand) + "\n" +
							player.getName() + " Hand: " + stringList(hand)).queue();
	    			isListening = true;
	    			player = msg.getAuthor();
	    			objMsgChannel.sendMessage("Hit or Stay?").queue();
	    			gameState = "blackjack";
    			}else{
	    			objMsgChannel.sendMessage("Please put all gambling requests in the casino channel " + name + ".").queue();

    			}
    		}
    		else if (command.equals("kill")){
    			if (evt.getAuthor().getId().equals("152620942927855616")){
	    			User mention = mentions.get(0);
	    			kill(evt,mention);
	    			objMsgChannel.sendMessage("Killed").queue();
    			}else{
    				objMsgChannel.sendMessage("Only the bigboi can kill people.").queue();
    			}
    		}
    		else if (command.equals("test")){
				try {
	    			objMsgChannel.sendMessage(":eggplant:").queue();
					savePoints();
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
    		}
    	messageList.add(msg);
    	}
    	if (msg.getContentRaw().contains("supplements") || msg.getContentRaw().contains("Supplements")){
    		User mention = evt.getAuthor();
    		kill(evt,mention);
    		objMsgChannel.sendMessage("Fuck off Paul.").queue();
    		
    	}
    }
    
    public void savePoints() throws FileNotFoundException, IOException{ 	
    	BufferedWriter out = null;
    	try  
    	{
    	    FileWriter fstream = new FileWriter("resources.txt");
    	    out = new BufferedWriter(fstream);
    	    for (Nerd nerd : nerds)
    	    	out.write(nerd.toString());
    	    
    	}
    	catch (IOException e)
    	{
    	    System.err.println("Error: " + e.getMessage());
    	}
    	finally
    	{
    	    if(out != null) {
    	        out.close();
    	    }
    	}
    	nerds.clear();
    	buildUserList();
    }
    
    public void respond(User author,String state, Message msg, MessageChannel objMsgChannel){
    	String id = author.getId();
    	if (state.equals("gamble")){    		
    		if (msg.getAuthor() == author){
	    		for (Nerd nerd : nerds){
	    			if (nerd.getId().equals(id)){
	    				bet = Integer.valueOf(msg.getContentRaw());
	    				if (nerd.getPoints() >= bet && (bet >= 1)){
	    					objMsgChannel.sendMessage("Got it. You are now betting "+ msg.getContentRaw() +". Type roll to continue.").queue();    					
	    					gameState = "roll";
	    				}else{
	    					objMsgChannel.sendMessage("You can't make that bet!").queue();
	    					isListening = false;
	    					return;
	    				}
	    			}
	    		}
    		}
    		
    	}else if (state.equals("roll")){
    		boolean win = gamblePlay();
    		if (win){
    			objMsgChannel.sendMessage("You won! Double the nerd points!").queue();
    			for (Nerd nerd : nerds){
	    			if (nerd.getId().equals(id)){
	    				nerd.setPoints(nerd.getPoints() + (bet * 2));
	    				objMsgChannel.sendMessage(nerd.getUser() + " : " + Integer.toString(nerd.getPoints()) + " points").queue();
	    			}
    			}
    			
    		}else{
    			objMsgChannel.sendMessage("You lost... Losing nerd points.").queue();
    			for (Nerd nerd : nerds){
	    			if (nerd.getId().equals(id)){
	    				nerd.setPoints(nerd.getPoints() - bet);
	    				objMsgChannel.sendMessage(nerd.getUser() + " : " + Integer.toString(nerd.getPoints()) + " points").queue();
	    			}
    			}
    		}
    		isListening = false;
    		try {
				savePoints();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    	}else if (state.equals("blackjack")){
    		if (msg.getAuthor() == author){
	    		if (msg.getContentRaw().equals("hit")){
	    			hand.add(drawCard());
					objMsgChannel.sendMessage("Bot Hand: " + stringList(botHand) + "\n" +
							player.getName() + " Hand: " + stringList(hand)).queue();
					if (countCards(hand) > 21){
						objMsgChannel.sendMessage("YOU BUSTED NERD.").queue();
	    				addPoints(author,-bet,objMsgChannel);
						botHand.clear();
			    		hand.clear();
						isListening = false;
					}else{
						objMsgChannel.sendMessage("Hit or Stay?").queue();
					}
					return;
	    			
	    		}else if (msg.getContentRaw().equals("stay")){
	    			while (countCards(botHand) < 17){
	    				botHand.add(drawCard());
	    			}
	    			objMsgChannel.sendMessage("Bot Hand: " + stringList(botHand) + "\n" +
							player.getName() + " Hand: " + stringList(hand)).queue();
					if ((countCards(botHand) > 21)){
						objMsgChannel.sendMessage("I BUSTED.").queue();
						addPoints(author,bet,objMsgChannel);
						isListening = false;
						botHand.clear();
			    		hand.clear();
						return;
					}
					if (countCards(botHand) > countCards(hand)){
	    				objMsgChannel.sendMessage("I WIN NERD.").queue();
	    				addPoints(author,-bet,objMsgChannel);
	    				isListening = false;
	    			}else{
	    				objMsgChannel.sendMessage("YOU WIN NERD.").queue();
	    				addPoints(author,bet,objMsgChannel);
	    				isListening = false;
	    			}
	    		}
	    		botHand.clear();
	    		hand.clear();
	    	}
    	}

    	
    }
    
    public boolean gamblePlay(){
    	Random rand = new Random();
        return rand.nextBoolean();
    }
    
    public static void buildUserList() throws FileNotFoundException, IOException{
    	String fileName = "resources.txt";

		//read file into stream, try-with-resources
    	try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
    	    String line;
    	    while ((line = br.readLine()) != null && !line.equals("")) {
    	    	String[] values = line.split(":");
    	    	Nerd newNerd = new Nerd(values[0],values[2],Integer.valueOf(values[1]));
    	    	nerds.add(newNerd);
    	    }
    	}
    }
    
    public String getSteamName(String userId) throws IOException{
    	//http://api.steampowered.com/ISteamUser/GetPlayerSummaries/v0002/?key=646EAEDE318241F0F3B87E7E3513FEDA&steamids=76561197983327754
    	URL url = new URL("http://api.steampowered.com/ISteamUser/GetPlayerSummaries/v0002/?key=646EAEDE318241F0F3B87E7E3513FEDA&steamids="+userId);
    	HttpURLConnection con = (HttpURLConnection) url.openConnection();
    	con.setRequestMethod("GET");
    	 BufferedReader in = new BufferedReader(
                 new InputStreamReader(con.getInputStream()));
         String inputLine;
         StringBuffer response = new StringBuffer();
         while ((inputLine = in.readLine()) != null) {
         	response.append(inputLine);
         }
         in.close();
         //print in String
         System.out.println(response.toString());
         //Read JSON response and print
         JSONObject myResponse = new JSONObject(response.toString());
         JSONArray jsonArray  = myResponse.getJSONObject("response").getJSONArray("players");
         String zimmer = jsonArray.getJSONObject(0).getString("personaname");
         return zimmer;
    }
    
    public void kill(MessageReceivedEvent event, User user) 
    {
        GuildController controller = new GuildController(event.getGuild());
        VoiceChannel voicechannel = null;
        MessageChannel currentChannel = event.getChannel();
        Member memberToMove = null;
        

        for(VoiceChannel channel : event.getGuild().getVoiceChannels()){
            List<Member> members =  channel.getMembers();
            for (Member member : members){
            	if (member.getUser().getId().equals(user.getId())){
            		memberToMove = member;
            	}
            }
            if(channel.getId().equalsIgnoreCase("434126400174424064")){
            	voicechannel = channel;
            }
        }

        try
        {
            controller.moveVoiceMember(memberToMove,voicechannel).queue();
        }
        catch(Exception ex)
        {
            ex.printStackTrace();
        }
    }
    
    public String drawCard(){
		Random rand = new Random();
		int chance = Ref.cards.length;
		String card = Ref.cards[rand.nextInt(chance) + 0];
		chance = Ref.faces.length;
		String face = Ref.faces[rand.nextInt(chance) + 0];
    	return card + face; 
    }
    
    public int countCards(List<String> list){
    	int score = 0;
    	for (String listItem : list){
    		char value = listItem.charAt(0);
    		switch (value){
			case '1': 	score++;
			break;
			case '2': 	score+=2;
			break;
			case '3': 	score+=3;
			break;
			case '4': 	score+=4;
			break;
			case '5': 	score+=5;
			break;
			case '6': 	score+=6;
			break;
			case '7': 	score+=7;
			break;
			case '8': 	score+=8;
			break;
			case '9': 	score+=9;
			break;
			case 'J': 	score+=10;
			break;
			case 'Q': 	score+=10;
			break;
			case 'K': 	score+=10;
			break;
    		}
    	}
    	return score;
    }
    
    public String stringList(List<String> list){
    	String printOut = "";
    	for (int i = 0; i < list.size();i++){
    		if (i < list.size()-1){
    			printOut += list.get(i) + " , ";
    		}else{
    			printOut += list.get(i);
    		}
    	}
    	return printOut;
    }
    
    public void addPoints(User user, int points,MessageChannel objMsgChannel){
		for (Nerd nerd : nerds){
			if (nerd.getId().equals(user.getId())){
				nerd.setPoints(nerd.getPoints() + points);
				objMsgChannel.sendMessage(nerd.getUser() + " : " + Integer.toString(nerd.getPoints()) + " points").queue();
			}
		}
		try {
			savePoints();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
}
