/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package patientdashboard;

/**
 *
 * @author bompe
 */
public class PatientDashBoard {
    


    public static void main(String[] args) {
        splash screen=new splash();
        screen.setVisible(true);
        try{
           
            for (int i = 0;i <= 100;i++){
                 Thread.sleep(20);
                splash.jProgressBar1.setValue(i);
                if(i==100){
                    new logIN().setVisible(true);
                    screen.dispose();
                }
            }
        }catch(Exception e){
            
        }
    }
    
}
