package com.example.aimlchatbot.AimlChatbot;

import java.io.File;

import javax.servlet.ServletContext;

import org.alicebot.ab.Bot;
import org.alicebot.ab.Chat;
import org.alicebot.ab.History;
import org.alicebot.ab.MagicBooleans;
import org.alicebot.ab.MagicStrings;
import org.apache.commons.lang3.StringUtils;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import org.springframework.web.context.ServletContextAware;
import org.springframework.web.util.HtmlUtils;

@Controller
public class GreetingController implements ServletContextAware {

	private ServletContext servletContext;
	
	private static final boolean TRACE_MODE = false;
	Bot bot;
	Chat chatSession;
	
	private static boolean InitFlag = true;
	
	public void botInit() {
		//String resourcesPath = servletContext.getRealPath("/src/main/resources");
		File currDir = new File(".");
		String path = currDir.getAbsolutePath();
		path = path.substring(0, path.length() - 2);
		System.out.println(path);
		String resourcesPath = path.concat("/src/main/resources");
		System.out.println(resourcesPath);
		MagicBooleans.trace_mode = TRACE_MODE;
		bot = new Bot("super", resourcesPath);
		chatSession = new Chat(bot);
		bot.brain.nodeStats();
	}
	
	@MessageMapping("/talktorobot")
	@SendTo("/topic/greetings")
	public Greeting greeting(HelloMessage message) throws Exception {
		String response = "";
		try {
			if(InitFlag==true) {
				botInit();
				InitFlag = false;
			}
			
			String textLine = message.getMessage();

			System.out.println("Human : " + textLine);
			if ((textLine == null) || (textLine.length() < 1))
				textLine = MagicStrings.null_input;
			if (textLine.equals("q")) {
				System.exit(0);
			} else if (textLine.equals("wq")) {
				bot.writeQuit();
				System.exit(0);
			} else {
				String requestmsg = textLine;
				if (MagicBooleans.trace_mode)
					System.out.println("STATE=NA:THAT=" + ((History) chatSession.thatHistory.get(0)).get(0) + ":TOPIC=" + chatSession.predicates.get("topic"));
				response = chatSession.multisentenceRespond(requestmsg);
				while (response.contains("&lt;"))
					response = response.replace("&lt;", "<");
				while (response.contains("&gt;"))
					response = response.replace("&gt;", ">");
				
				
				response = executeDefault(response);

				System.out.println("Robot : " + response);
			}
		}
		catch(Exception ex) {
			ex.printStackTrace();
		}
		Thread.sleep(1000); // simulated delay
	    return new Greeting(HtmlUtils.htmlEscape(response));
	}

	private String executeDefault(String response) {
		if(response.trim().contains("<oob><url>")){
			response = setOOBUrl(response);
		}
		if(response.trim().contains("<oob><search>")){
			response = setOOBSearch(response);
		}
		return response;
	}
	
	private String setOOBUrl(String response)
	{
		String bettext = StringUtils.substringBetween(response, "<oob><url>", "</url></oob>");
		response = response.replace("<oob><url>", "<a href=\"");
		response = response.replace("</url></oob>", "\" target=\"_blank\">" + bettext + "</a>");
		return response;
	}
	
	private String setOOBSearch(String response)
	{
		String bettext = StringUtils.substringBetween(response, "<oob><search>", "</search></oob>");
		response = response.replace("<oob><search>", "<br/><a href=\"https://www.google.com/search?q=");
		response = response.replace("</search></oob>", "\" target=\"_blank\"><i>Click Here to View Result for "+bettext+".</i></a>");
		return response;
	}
	
	@Override
	public void setServletContext(ServletContext servletContext) {
		// TODO Auto-generated method stub
		this.servletContext = servletContext;
	}
	
}
