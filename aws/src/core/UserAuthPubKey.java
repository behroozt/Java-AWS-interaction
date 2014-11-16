package core;

/* -*-mode:java; c-basic-offset:2; indent-tabs-mode:nil -*- */

import com.jcraft.jsch.*;
import java.awt.*;
//import java.io.ByteArrayOutputStream;
import java.io.InputStream;
//import java.io.OutputStream;

import javax.swing.*;

public class UserAuthPubKey{
	public void main (String[] args) {
		//RunCommand ("/home/behrooz/.ec2/ec2-keypair","ec2-107-20-118-71.compute-1.amazonaws.com","ls");
	}
	
	public static void RunCommand(String keypair, String host, String command){

    try{
      JSch jsch=new JSch();
      //jsch.addIdentity("/Users/behrooz/.ec2/ec2-keypair");
      jsch.addIdentity(keypair);
      //String host=null;
      
      //host = "ec2-50-19-145-70.compute-1.amazonaws.com";
      String user = "ec2-user";

      Session session=jsch.getSession(user, host, 22);

      // username and passphrase will be given via UserInfo interface.
      UserInfo ui=new MyUserInfo();
      session.setUserInfo(ui);
      session.connect();

      Channel channel=session.openChannel("exec");
      ((ChannelExec)channel).setCommand(command);

      InputStream in=channel.getInputStream();
      channel.connect();
      byte[] tmp=new byte[1024];
      while(true){
        while(in.available()>0){
          int i=in.read(tmp, 0, 1024);
          if(i<0)break;
          System.out.print(new String(tmp, 0, i));
        }
        if(channel.isClosed()){
          System.out.println("exit-status: "+channel.getExitStatus());
          break;
        }
        try{Thread.sleep(1000);}catch(Exception ee){}
      }
      channel.disconnect();
      session.disconnect();
      System.out.println("DONE");
  }catch(Exception e){
      e.printStackTrace();
  }
      

  }


  public static class MyUserInfo implements UserInfo, UIKeyboardInteractive{
    public String getPassword(){ return null; }
    public boolean promptYesNo(String str){
    	return true;
//      Object[] options={ "yes", "no" };
//      int foo=JOptionPane.showOptionDialog(null, 
//             str,
//             "Warning", 
//             JOptionPane.DEFAULT_OPTION, 
//             JOptionPane.WARNING_MESSAGE,
//             null, options, options[0]);
//       return foo==0;
    }
  
    String passphrase;
    JTextField passphraseField=(JTextField)new JPasswordField(20);

    public String getPassphrase(){ return passphrase; }
    public boolean promptPassphrase(String message){
      Object[] ob={passphraseField};
      int result=
	JOptionPane.showConfirmDialog(null, ob, message,
				      JOptionPane.OK_CANCEL_OPTION);
      if(result==JOptionPane.OK_OPTION){
        passphrase=passphraseField.getText();
        return true;
      }
      else{ return false; }
    }
    public boolean promptPassword(String message){ return true; }
    public void showMessage(String message){
      JOptionPane.showMessageDialog(null, message);
    }
    final GridBagConstraints gbc = 
      new GridBagConstraints(0,0,1,1,1,1,
                             GridBagConstraints.NORTHWEST,
                             GridBagConstraints.NONE,
                             new Insets(0,0,0,0),0,0);
    private Container panel;
    public String[] promptKeyboardInteractive(String destination,
                                              String name,
                                              String instruction,
                                              String[] prompt,
                                              boolean[] echo){
      panel = new JPanel();
      panel.setLayout(new GridBagLayout());

      gbc.weightx = 1.0;
      gbc.gridwidth = GridBagConstraints.REMAINDER;
      gbc.gridx = 0;
      panel.add(new JLabel(instruction), gbc);
      gbc.gridy++;

      gbc.gridwidth = GridBagConstraints.RELATIVE;

      JTextField[] texts=new JTextField[prompt.length];
      for(int i=0; i<prompt.length; i++){
        gbc.fill = GridBagConstraints.NONE;
        gbc.gridx = 0;
        gbc.weightx = 1;
        panel.add(new JLabel(prompt[i]),gbc);

        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weighty = 1;
        if(echo[i]){
          texts[i]=new JTextField(20);
        }
        else{
          texts[i]=new JPasswordField(20);
        }
        panel.add(texts[i], gbc);
        gbc.gridy++;
      }

      if(JOptionPane.showConfirmDialog(null, panel, 
                                       destination+": "+name,
                                       JOptionPane.OK_CANCEL_OPTION,
                                       JOptionPane.QUESTION_MESSAGE)
         ==JOptionPane.OK_OPTION){
        String[] response=new String[prompt.length];
        for(int i=0; i<prompt.length; i++){
          response[i]=texts[i].getText();
        }
	return response;
      }
      else{
        return null;  // cancel
      }
    }
  }
}
