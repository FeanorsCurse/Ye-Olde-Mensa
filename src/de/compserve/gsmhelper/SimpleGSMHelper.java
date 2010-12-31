/**
 *   Ye Olde Mensa is an android application for displaying the current
 *   mensa plans of University Oldenburg on an android mobile phone.
 *   
 *   Copyright (C) 2009/2010 Daniel Süpke
 *
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details.
 *
 *    You should have received a copy of the GNU General Public License
 *    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.compserve.gsmhelper;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

import android.telephony.TelephonyManager;
import android.telephony.gsm.GsmCellLocation;

/**
 * 
 * @author Frederik Kramer
 * 
 */

/**
 * 
 * @comment SimpleGSMHelper class comprises functionality that supports localisation of a mobile
 * device based on GSM data. It is used for a coarse localisation of the mobile phone based on the 
 * OpenCellID geolocalisation database.
 * 
 */

public class SimpleGSMHelper {
	
	private int lac, cellID, mnc, mcc = 0;
	private String iso = "";
	
	private double[] getCoordinatesFromGSM(int cellID, int lac, int mnc, int mcc) {
		
		try {
			 /* Opening Connection to OpenCellID 
			 *  geo localisation database and parsing the txt return values
			 */
			
			 double[] result = new double[2];
		     
			 /*
			  * Initiating url connection
			  */
			 
			 String urlString = "http://www.opencellid.org/cell/get?cellid="+cellID+"&mcc="+mcc+"&mnc="+mnc+"&lac="+lac+"&fmt=txt"; 
		     URL url = new URL(urlString); 
		     URLConnection conn = url.openConnection();
		     HttpURLConnection httpConn = (HttpURLConnection)conn;        
		     
		     /* 
		      * Sending request via HTTP Post command
		      */
		     
		     httpConn.setRequestMethod("POST");
		     httpConn.setDoOutput(true); 
		     httpConn.setDoInput(true);
		     httpConn.connect(); 
			
		     /*
		      * Reading input stream
		      */
			
		     InputStream is=httpConn.getInputStream();
		     StringBuffer buffer=new StringBuffer();
		     
		     /*
		      * Reading input stream byte by byte and appending result to the string buffer
		      */
		     
		     int car;
		     
		     while( (car=is.read())!= -1){
		        buffer.append((char)car);
		     }
		     
		     /*
		      * Closing stream and converting buffer to static String
		      */
		     
		     is.close();
		     httpConn.disconnect();
		     String res=buffer.toString();
		     
		     if(res.startsWith("err")){
		    	/*
			    *  In case of an error set default longitude and latitude valuse to 0.0, 0.0
			    */
		        result[0] = 0.0;
		        result[1] = 0.0;
		     	} 
		     
		     else {
		    	/*
			     *  Storing the valid result into an output array of doubles
			     */

		    	int pos=res.indexOf(',');
		        String lat=res.substring(0,pos);
		        int pos2=res.indexOf(',',pos+1);
		        String lng=res.substring(pos+1,pos2);
		        result[0] = Double.valueOf(lat).doubleValue();
		        result[1] = Double.valueOf(lng).doubleValue();
		       
		       }
		     
		  return result;
		  
		  } catch (Exception e) {
		      return null;   
		  }
	 
	}

 	public double getDistance (double[] targetCoordinates) {
 			
 			/*
 			 * This method retrieves the distance of the mobile phone
 			 * to a target location that is stored in the input double array
 			 * The array consists of a latitude value at position 0 and 
 			 * a longitude value at position 1
 			 */
 		
 			double[] sourceCoordinates = this.getCoordinatesFromGSM(this.cellID, this.lac, this.mnc, this.mcc);
 		
 			/*
 			 * Values must be converted to radial fragments to apply geometry
 			 */
 			
			double lat1 = Math.toRadians(sourceCoordinates[0]); 
			double lng1 = Math.toRadians(sourceCoordinates[1]); 
			double lat2 = Math.toRadians(targetCoordinates[0]);
			double lng2 = Math.toRadians(targetCoordinates[1]);
			
			/*
			 * Trigonometrical function to calculate the real 
			 * distance based on the earth norm elipsoid
			 */
			
			double dist = Math.abs(Math.acos(Math.sin(lat1)*Math.sin(lat2) +
						Math.cos(lat1)*Math.cos(lat2)*
						Math.cos(lng1 - lng2))) * 6370.137;
			
			// Rounding and outputting the result
			dist = Math.floor(dist);	
			return dist;
				
	}

	public void setMobileLocation(TelephonyManager manager) {
		
				try {
			 	
				/*
				 * This method sets the current location of the mobile phone based on its
				 * operative mobile state
				 */
					
			 	TelephonyManager tm = manager;
				GsmCellLocation location = (GsmCellLocation) tm.getCellLocation();
				
				// Setting mobile country iso name like e.g. "de"
				
				this.iso = tm.getSimCountryIso();
					
				if (this.iso.equalsIgnoreCase("de") && (tm.getNetworkOperator().length() == 5)) {
					
						/* 
						 * For germany the iso code must be "de" and the string comprising mobile country
						 * code (mcc) and mobile network code (mnc) must have a length of 5 digits (3 mcc and 2 mnc)
						 * furthermore the cellID and the lac is needed for the Open CellID service
						 */
					
						this.lac = location.getLac();
						this.cellID = location.getCid();
					
						/*
						 * Casting string results to trivial int values
						 */
						
						Integer mcc = Integer.parseInt(tm.getNetworkOperator().substring(0,3));
						this.mcc = mcc.intValue();
						Integer mnc = Integer.parseInt(tm.getNetworkOperator().substring(3,5));
						this.mnc = mnc.intValue();
					
					}
				else {
						// Setting the reference point to Klecken
			 			// a village south of Hamburg in order to avoid
			 			// unreasonable distance measures of more the 5000km
					
						this.iso = "de";
						this.lac = 41367;
						this.cellID = 10105;
						this.mnc = 07;
						this.mcc = 262;
					 }
				
			 	} catch (Exception e) {
			 		
			 	}
				
	}

}
