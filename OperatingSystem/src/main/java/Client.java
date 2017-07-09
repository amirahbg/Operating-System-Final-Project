


import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;

import javax.swing.*;

/**
 * A simple Swing-based client for the capitalization server.
 * It has a main frame window with a text field for entering
 * strings and a textarea to see the results of capitalizing
 * them.
 */
public class Client {

    private BufferedReader in;
    private PrintWriter out;
    private JFrame frame = new JFrame("Client Side");
    private JTextField dataField = new JTextField(40);
    private JTextArea messageArea = new JTextArea(20, 60);
    private JTextArea loggerArea = new JTextArea(20, 60);
    private JButton settingButton = new JButton("Setting");

    /**
     * Constructs the client by laying out the GUI and registering a
     * listener with the textfield so that pressing Enter in the
     * listener sends the textfield contents to the server.
     */
    public Client() {
        ArrayList<Integer> setting = (new SettingFile(ConstantValue.CONF_PATH)).getSetting();
        ConstantValue.init(setting);

        // Layout GUI
        messageArea.setEditable(false);
        loggerArea.setEditable(false);
        frame.getContentPane().add(dataField, "North");
        frame.getContentPane().add(new JScrollPane(messageArea), "Center");
        frame.getContentPane().add(new JScrollPane(loggerArea), "East");
        frame.getContentPane().add(settingButton, "West");


        // Add Listeners
        dataField.addActionListener(new ActionListener() {
            /**
             * Responds to pressing the enter key in the textfield
             * by sending the contents of the text field to the
             * server and displaying the response from the server
             * in the text area.  If the response is "." we exit
             * the whole application, which closes all sockets,
             * streams and windows.
             */
            public void actionPerformed(ActionEvent e) {
                out.println(dataField.getText());
                dataField.selectAll();
            }
        });

        settingButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String portNumber = JOptionPane.showInputDialog(
                        frame,
                        "Set port number:",
                        "Setting ",
                        JOptionPane.QUESTION_MESSAGE);

                new SettingFile(ConstantValue.CONF_PATH).
                        setPortNumber(Integer.valueOf(portNumber));
            }
        });
    }

    /**
     * Implements the connection logic by prompting the end user for
     * the server's IP address, connecting, setting up streams, and
     * consuming the welcome messages from the server.  The RequestThread
     * protocol says that the server sends three lines of text to the
     * client immediately after establishing a connection.
     */
    public void connectToServer() throws IOException {
        // Make connection and initialize streams

        Socket socket = new Socket(ConstantValue.SERVER_ADDRESS, ConstantValue.PORT_NUMBER);
        in = new BufferedReader(
                new InputStreamReader(socket.getInputStream()));
        out = new PrintWriter(socket.getOutputStream(), true);

        // Consume the initial welcoming messages from the server
        for (int i = 0; i < 3; i++) {
            messageArea.append(in.readLine() + "\n");
        }
    }

    /**
     * Runs the client application.
     */
    public static void main(String[] args) throws Exception {
        Client client = new Client();
        client.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        client.frame.pack();
        client.frame.setVisible(true);
        client.connectToServer();
        client.begin();
    }

    private void begin() {
        loggerArea.append("Logger: \n");
        new LogGrabberThread().start();
    }

    class LogGrabberThread extends Thread {
        @Override
        public void run() {
            while (true) {
                String response;
                try {
                    response = in.readLine();
                    if (response == null || response.equals("")) {
                        System.exit(0);
                    }
                } catch (IOException ex) {
                    response = "Error: " + ex;
                }
                if (response.contains("Allocated Resources")) {
                    loggerArea.append(response + "\n");
                } else {
                    messageArea.append(response + "\n");
                }
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}