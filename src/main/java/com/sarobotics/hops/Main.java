package com.sarobotics.hops;

import com.sarobotics.actions.Action;
import com.sarobotics.actions.ActionHW;
import com.sarobotics.actions.ActionSimulator;
import com.sarobotics.bmp280.BMP280HW;
import com.sarobotics.bmp280.BMP280;
import com.sarobotics.bmp280.BMP280Simulator;
import com.sarobotics.utils.InvalidOpenParachuteAltitude;

public class Main {

  public static void main(String... args) throws Exception, InvalidOpenParachuteAltitude {

    if (args.length >= 2) {
      BMP280 bmp280;
      Action action;
      int actualAltitude, burstAltitude, openParachuteAltitude;

      if ( args[0].equalsIgnoreCase("sim") ) {
        burstAltitude = Integer.parseInt(args[1]);
        openParachuteAltitude = Integer.parseInt(args[2]);
        bmp280 = new BMP280Simulator();
        actualAltitude = (int) bmp280.getAltitudeInMeter();
        action = new ActionSimulator(actualAltitude + burstAltitude, actualAltitude + openParachuteAltitude);
        //action = new ActionHW(actualAltitude + burstAltitude, actualAltitude + openParachuteAltitude);
      } else {
        burstAltitude = Integer.parseInt(args[0]);
        openParachuteAltitude = Integer.parseInt(args[1]);
        bmp280 = new BMP280HW();
        actualAltitude = (int) bmp280.getAltitudeInMeter();
        action = new ActionHW(actualAltitude + burstAltitude, actualAltitude + openParachuteAltitude);
      }

      final AltitudeController ac = new AltitudeController(actualAltitude);

      int counter = 1;

      while (true) {
        int altit = (int) bmp280.getAltitudeInMeter();
        if (altit != 3733) { //Hardcoded controllo su errore altitudine. Forse da imputare al BMP280HW un po' vecchiotto.
          ac.settaAltitudineAttuale(altit);
          if (ac.isGoingUp()) {
            System.out.println(counter++ + "\t  UP\t\t" + ac.print());
            if (action.canDetach(ac.getActualAltitude()) && action.getDeveAncoraScoppiare()) {
              action.setDeveAncoraScoppiare(false);
              action.sganciaSonda();
            }
          } else if (ac.isGoingDown()) {
            System.out.println(counter++ + "\t DOWN\t\t" + ac.print());
            if (action.canOpenParachute(ac.getActualAltitude())) {
              action.setIlParacaduteSiDeveAncoraAprire(false);
              action.apriParacadute();
            }
          }
//          else {
//            //Qui il sistema è stazionario
//          }
          Thread.sleep(10);
        }
      }

    } else {
      System.out.println("Manca qualcosa: esempio di esecuzione \njava -jar hops.jar 100 70\njava -jar hops.jar sim 100 7\nOppure se abbiamo problemi con la libreria pi4j aggiungere il seguente parametro alla VM: java -Dpi4j.linking=dynamic -jar.....blablabla ");
      System.exit(0);
    }
  }
}
