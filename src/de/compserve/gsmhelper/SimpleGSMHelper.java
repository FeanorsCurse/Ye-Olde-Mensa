package de.compserve.gsmhelper;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

import android.content.Context;
import android.telephony.TelephonyManager;
import android.telephony.gsm.GsmCellLocation;

import android.app.Activity;

public class SimpleGSMHelper extends Activity {
	
	private int lac, cellID, mnc, mcc = 0;
	private String iso,mess = "";
	
private double[] getCoordinatesFromGSM(int cellID, int lac, int mnc, int mcc) {
		
		try {
			 // Opening Connection to OpenCellID
			 String urlString = "http://www.opencellid.org/cell/get?cellid="+cellID+"&mcc="+mcc+"&mnc="+mnc+"&lac="+lac+"&fmt=txt";
			 double[] result = new double[2];
		     
		     URL url = new URL(urlString); 
		     URLConnection conn = url.openConnection();
		     HttpURLConnection httpConn = (HttpURLConnection)conn;        
		     httpConn.setRequestMethod("POST");
		     httpConn.setDoOutput(true); 
		     httpConn.setDoInput(true);
		     httpConn.connect(); 
			
			
		     InputStream is=httpConn.getInputStream();
		     StringBuffer buffer=new StringBuffer();
		     
		     int car;
		     
		     while( (car=is.read())!= -1){
		        buffer.append((char)car);
		     }
		     
		     is.close();
		     httpConn.disconnect();
		     String res=buffer.toString();
		     
		     if(res.startsWith("err")){
		        result[0] = 0.0;
		        result[1] = 0.0;
		     	} 
		     
		     else {
		      
		       int pos=res.indexOf(',');
		       String lat=res.substring(0,pos);
		       int pos2=res.indexOf(',',pos+1);
		       String lng=res.substring(pos+1,pos2);
		       result[0] = Double.valueOf(lat).doubleValue();
		       result[1] = Double.valueOf(lng).doubleValue();
		       }
		     
		  return result;
		  
		  } catch (Exception e) {
		      e.toString();
		      return null;   
		  }
	 
	}

 	private double getDistance (double[] sourceCoordinates, double[] targetCoordinates) {
				
				double lat1 = Math.toRadians(sourceCoordinates[0]); 
				double lng1 = Math.toRadians(sourceCoordinates[1]); 
				double lat2 = Math.toRadians(targetCoordinates[0]);
				double lng2 = Math.toRadians(targetCoordinates[1]);
				
				double dist = Math.abs(Math.acos(Math.sin(lat1)*Math.sin(lat2) +
						Math.cos(lat1)*Math.cos(lat2)*
						Math.cos(lng1 - lng2))) * 6370.137;
				
				return dist;
				
	}

	private void getGsmPresets() {
			 	try {
			 		
				TelephonyManager tm  =  (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE); 
				GsmCellLocation location = (GsmCellLocation) tm.getCellLocation();
				
				//Fake IDs for testing or if no GSM cell available
					
				this.lac = location.getLac();
				this.cellID = location.getCid();
				this.iso = tm.getSimCountryIso();
					
				if (this.iso == "de") {
					// immer noch Schrott !
					// irgendwas stimmt mit der Abfrage oben noch nicht
					// länge der variable 2 und inhalt de, dennoch kein einsprung in die routine
					if (tm.getNetworkOperator().length() == 5) {
						Integer mcc = Integer.parseInt(tm.getNetworkOperator().substring(0,2));
						this.mcc = mcc.intValue();
						Integer mnc = Integer.parseInt(tm.getNetworkOperator().substring(3,4));
						this.mnc = mnc.intValue();
						this.mess = "eins";
						}	
					else {
						Integer mcc = Integer.parseInt(tm.getNetworkOperator().substring(0,2));
						this.mcc = mcc.intValue();
						Integer mnc = Integer.parseInt(tm.getNetworkOperator().substring(3,4));
						this.mnc = mnc.intValue();
						this.mess = "zwei";
						}
					}
				else {
						this.mnc = this.iso.length();
					 }
				
			 	} catch (Exception e) {
			 		
			 		// Setting the reference point to Klecken
			 		// a village south of Hamburg i order to avoid
			 		// unreasonable distance measures of more the 5000km
			 		
			 		this.iso = "de";
			 		this.lac = 41367;
					this.cellID = 10105;
					this.mnc = 07;
					this.mcc = 262;
					this.mess = "drei";
			 	}
				
	}

}
