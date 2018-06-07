package kr.or.kpew.kieas.alertsystem;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.Properties;

import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.*;
import javax.mail.internet.*;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.plaf.nimbus.NimbusLookAndFeel;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import kr.or.kpew.kieas.common.AlertSystemProfile;
import java.io.*;
import javazoom.jl.player.*;
public class AlertSystemView implements Observer {
	public enum requestComponents
	{
		language,
		scope,
		sent,
		senderName,
		headline,
		certainty,
		urgency,
		description,
		instruction,
		contact,
		category,
		web
	}
	
	private AlertSystemController controller;

	private JFrame frame;
	private Container alertPane;
	private GridBagConstraints gbc;
	private JTextArea alertArea;
	private JScrollPane alertAreaPane;
	private JPanel buttonPane;
	private JTabbedPane mainTabbedPane;

	JTextField systemType;
	
	
	
	
	
	public AlertSystemView() {
	}
	
	public void show() {
		frame.setVisible(true);
	}

	public void init() {
		initLookAndFeel();
		initFrame();
		gbc = new GridBagConstraints();
		initAlertPane();
		initButtonPane();
		mainTabbedPane.addTab("경보메시지", alertPane);
	}

	public void setController(AlertSystemController controller) {
		this.controller = controller;
	}

	private void initLookAndFeel() {
		try {
			UIManager.setLookAndFeel(new NimbusLookAndFeel());
		} catch (UnsupportedLookAndFeelException e) {
			e.printStackTrace();
		}
	}

	private void initFrame() {
		this.frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		frame.addWindowListener(controller);

		this.mainTabbedPane = new JTabbedPane();
		Container container = frame.getContentPane();
		container.add(mainTabbedPane);

		frame.setSize(800, 800);
//		frame.setLocation(IntegratedAlertSystemMain.xLocation, IntegratedAlertSystemMain.yLocation);
//		IntegratedAlertSystemMain.xLocation += IntegratedAlertSystemMain.xIncrement;
//		IntegratedAlertSystemMain.yLocation += IntegratedAlertSystemMain.yIncrement;
		frame.setPreferredSize(new Dimension(512, 256));
	}

	private void initAlertPane() {
		alertPane = new JPanel();
		alertPane.setLayout(new GridBagLayout());

		alertArea = new JTextArea(5, 20);
		alertAreaPane = new JScrollPane(alertArea);

		alertArea.setText("");

		gbc.fill = GridBagConstraints.BOTH;
		setGbc(0, 0, 1, 1, 1, 8);
		alertPane.add(alertAreaPane, gbc);

		gbc.fill = GridBagConstraints.HORIZONTAL;
		setGbc(0, 1, 1, 1, 1, 2);

	}

	private void initButtonPane() {
		this.buttonPane = new JPanel();
		
		systemType = new JTextField(15);
		systemType.setEnabled(false);
		systemType.setText("<경보시스템 종류>");

		JButton clearButton = new JButton("Clear");
		clearButton.addActionListener(controller);
		buttonPane.add(clearButton, BorderLayout.WEST);
		JButton webButton = new JButton("WebVersion");
		webButton.addActionListener(controller);
		buttonPane.add(webButton, BorderLayout.WEST);

		buttonPane.add(systemType, BorderLayout.WEST);

		alertPane.add(buttonPane, gbc);
	}


	private void setGbc(int gridx, int gridy, int gridwidth, int gridheight, int weightx, int weighty) {
		gbc.gridx = gridx;
		gbc.gridy = gridy;
		gbc.gridwidth = gridwidth;
		gbc.gridheight = gridheight;
		gbc.weightx = weightx;
		gbc.weighty = weighty;
	}

	public void systemExit() {
		String question = "표준경보시스템 프로그램을 종료하시겠습니까?";
		String title = "프로그램 종료";

		if (JOptionPane.showConfirmDialog(frame, question, title, JOptionPane.YES_NO_OPTION,
				JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION) {
			System.exit(0);
		} else {
			System.out.println("AS: cancel exit program");
		}
	}
	
	// model의 notifyObservers(Object obj)에 의해 호출되는 함수, obj를 파라미터로 받아서 처리하면 된다.
	@Override
	public void update(Observable o, Object arg) {
		//obj를 형변환해야한다.
		// instanceof 구문? 그 class의 instance인지 묻는 
		if(arg instanceof String) {
			
			// url request
			url_request((String)arg);
			
			// 수신기 텍스트 변환 
			alertArea.setText((String)arg);
			
			// 경보음 출력 
			try {
				// mp3 파일만 재생 가능
				File file = new File("/Users/doorbw/software_class/KIEAS/IEAS_K/sound/sound.mp3"); 
				Player p = new Player(new FileInputStream(file));
				System.out.println("Sound PLAY ...");
				p.play();
				p.close();
				System.out.println("Sound PLAY SUCCESS");
			}
			catch(Exception e) {
				System.out.println("Sound play FAIL\nERROR : "+e);
			}
			// 이메일 발송
			gmailSend((String)arg);
		}
		else if(arg instanceof AlertSystemProfile) {
			AlertSystemProfile profile = (AlertSystemProfile)arg;
			frame.setTitle(profile.getSender());
			systemType.setText(profile.getType().getDescription());
		}
	}

	public void clear() {
		alertArea.setText("");
	}
	
	//함수이름: gmailSend()
	//
	public static void gmailSend(String arg) {
		Document doc = convertStringToDocument(arg);
		Element rootElement = doc.getDocumentElement();
		System.out.println(rootElement);
		// 메일 내용 설정 
		String content = "<h1>"+getString("headline", rootElement);
		content += "</h1><br>CODE: "+getString("code", rootElement)+"<br>경보발령시각: "+getString("sent", rootElement);
		if (getString("description", rootElement) != null) {
			if (getString("description", rootElement).contains("\n")) {
				content += "<br>경보발령자: "+getString("senderName", rootElement)+"<br><h2>상세내용</h2>"+getString("description", rootElement).replace("\n", "<br>");
			}else {
				content += "<br>경보발령자: "+getString("senderName", rootElement)+"<br><h2>상세내용</h2>"+getString("description", rootElement);
			}
		}
		if (getString("instruction", rootElement)!=null) {
			if (getString("instruction", rootElement).contains("\n")) {
				content += "<br><br><h2>대처방안</h2>"+getString("instruction", rootElement).replace("\n", "<br>");
			}else {
				content += "<br><br><h2>대처방안</h2>"+getString("instruction", rootElement);
			}
		}
		if(getString("contact", rootElement) != null) {
			if (getString("contact", rootElement).contains("\n")) {
				content += "<br><br><h2>Web</h2>"+getString("web", rootElement)+"<br><br><h2>Contact</h2>"+getString("contact", rootElement).replace("\n", "<br>");
			}else {
				content += "<br><br><h2>Web</h2>"+getString("web", rootElement)+"<br><br><h2>Contact</h2>"+getString("contact", rootElement);
			}
		}
		content += "<br><hr><h4># 발령된 경보에 대해 Web에서 보려면 아래 주소를 클릭하세요.</h4>"+"<a href='https://software-test-doorbw.c9users.io/'>https://software-test-doorbw.c9users.io/</a>";
	    String user = "doobw@likelion.org"; 
	    String password = "ansqjadn9";   

	    // SMTP 서버 정보를 설정
	    Properties prop = new Properties();
	    prop.put("mail.smtp.host", "smtp.gmail.com"); 
	    prop.put("mail.smtp.port", 465); 
	    prop.put("mail.smtp.auth", "true"); 
	    prop.put("mail.smtp.ssl.enable", "true"); 
	    prop.put("mail.smtp.ssl.trust", "smtp.gmail.com");
	    
	    Session session = Session.getDefaultInstance(prop, new javax.mail.Authenticator() {
	        protected PasswordAuthentication getPasswordAuthentication() {
	            return new PasswordAuthentication(user, password);
	        }
	    });

	    try {
	    	System.out.println("Sending email ...");
	        MimeMessage message = new MimeMessage(session);
	        message.setFrom(new InternetAddress(user));

	        //수신자메일주소
	        message.addRecipient(Message.RecipientType.TO, new InternetAddress("qjadn9@naver.com")); 

	        // Subject
	        message.setSubject(getString("headline", rootElement)); //메일 제목을 입력

	        // Text
	        message.setText(content, "utf-8", "html");    //메일 내용을 입력

	        // send the message
	        Transport.send(message); ////전송
	        System.out.println("Sending email SUCCESS");
	        
	    } catch (AddressException e) {
	    	System.out.println("Seding eamil address FAIL\nERROR :"+e);
	        // TODO Auto-generated catch block
	        e.printStackTrace();
	    } catch (MessagingException e) {
	    	System.out.println("Seding eamil messaging FAIL\nERROR :"+e);
	        // TODO Auto-generated catch block
	        e.printStackTrace();
	    }
	}
	
	public static void url_request(String arg) {
		
		try {
			System.out.println("URL request ...");
			URL url = new URL("https://software-test-doorbw.c9users.io/post_data/");
			URLConnection conn = url.openConnection();
			conn.setRequestProperty("Accept-Language",  "ko-kr,ko;q=0.8,en-us;q=0.5,en;q=0.3");
			conn.setDoOutput(true);
			String parameter = "";
			Document doc = convertStringToDocument(arg);
			Element rootElement = doc.getDocumentElement();
			System.out.println(rootElement);
			for(requestComponents component : requestComponents.values()) {
				try {
					String value = getString(component.toString(), rootElement);
					System.out.println("request: "+component.toString()+","+value);
					parameter += "&" + URLEncoder.encode(component.toString(), "UTF-8") + "="+ URLEncoder.encode(value, "UTF-8");
				}catch(Exception e) {
					System.out.println("parameter encoding error\nERROR: "+e);
					continue;
				}
			}
			System.out.println(parameter);
			OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
			wr.write(parameter);
			wr.flush();  // 꼭 flush를 호출해야 한다.
			BufferedReader rd = null;
			rd = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
			String line = null;
			while ((line = rd.readLine()) != null) {
			  // 로직 처리
			}
			wr.close();
			System.out.println("URL request SUCCESS");
		}catch(Exception e) {
			System.out.println("URL request FAIL\nERROR :"+e);
		}
	}
	
	protected static String getString(String tagName, Element element) {
        NodeList list = element.getElementsByTagName(tagName);
        if (list != null && list.getLength() > 0) {
            NodeList subList = list.item(0).getChildNodes();
            if (subList != null && subList.getLength() > 0) {
                return subList.item(0).getNodeValue();
            }
        }

        return null;
    }
	
	private static Document convertStringToDocument(String xmlStr) {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();  
        DocumentBuilder builder;  
        try  
        {  
            builder = factory.newDocumentBuilder();  
            Document doc = builder.parse( new InputSource( new StringReader( xmlStr ) ) ); 
            return doc;
        } catch (Exception e) {  
            e.printStackTrace();  
        } 
        return null;
    }

}



