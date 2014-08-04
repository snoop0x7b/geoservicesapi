/*
 * Copyright (C) 2014 Saurabh Rane
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.geoservicesapi.services;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.Charset;

import org.json.JSONObject;
import org.json.JSONArray;

/**
 * LocationServices is a class for consuming location services using MapQuest Geo-coding API.
 * 
 * @author Saurabh Rane
 * @version 2014-07-24
*/

public class LocationServices {
	private String mapQuestApiKey;

    /**
     * Construct a LocationServices with your mapQuest api key.
     *
     * @param mapQuestApiKey
     *            Your api key for mapQuest api.
     */
	public LocationServices(String mapQuestApiKey) {
		this.mapQuestApiKey = mapQuestApiKey;
	}
	
    /**
     * Get the geo-location of an address.
     *
     * @param address
     *            Address of a location.
     * @return The JSONObject associated with geo-coordinates.
     */
	public JSONObject getCoordinatesUsingAddress(String address) {
		JSONObject result = new JSONObject();

		String apiUrl = "http://open.mapquestapi.com/geocoding/v1/address?key="+mapQuestApiKey+"&location="+address;
		
		apiUrl = apiUrl.replaceAll(" ", "%20");

		try {
			InputStream is = new URL(apiUrl).openStream();
			BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
			StringBuilder sb = new StringBuilder();
			int cp;
			while ((cp = rd.read()) != -1) {
				sb.append((char) cp);
			}

			String jsonText = sb.toString();
			JSONObject res = new JSONObject(jsonText);
			JSONObject info = res.getJSONObject("info");
			int statusCode = info.getInt("statuscode");
			if(statusCode == 0) {
				JSONArray results = res.getJSONArray("results");
				JSONObject resultObject = results.getJSONObject(0);
				JSONArray locations = resultObject.getJSONArray("locations");
				JSONObject location = locations.getJSONObject(0);
				JSONObject latLng = location.getJSONObject("latLng");
				double lat = latLng.getDouble("lat");
				double lng = latLng.getDouble("lng");
				
				JSONObject coordinates = new JSONObject();
				coordinates.put("lat", lat);
				coordinates.put("lng", lng);
				result.put("location", coordinates);
			}
			
		} catch (Exception e) {
			JSONObject error = new JSONObject();
			error.put("message", "Error processing request. Try again after some time");
			result.put("error", error);
		}
		return result;

	}
	
    /**
     * Get the geo-location of an address using its components.
     *
     * @param street
     *            Street in an address of a location.
     * @param city
     *            City in an address of a location.
     * @param state
     *            State in an address of a location.
     * @param postalCode
     *            Postal Code in an address of a location.
     * @return The JSONObject associated with geo-coordinates.
     */
	public JSONObject getCoordinatesUsingComponents(String street, String city,  String state, String postalCode) {

		JSONObject result = new JSONObject();

		String apiUrl = "http://www.mapquestapi.com/geocoding/v1/address?&key="+mapQuestApiKey+"&street="+street+"&city="+city+"&state="+state+"&postalCode="+postalCode;
		apiUrl = apiUrl.replaceAll(" ", "%20");

		try {
			InputStream is = new URL(apiUrl).openStream();
			BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
			StringBuilder sb = new StringBuilder();
			int cp;
			while ((cp = rd.read()) != -1) {
				sb.append((char) cp);
			}

			String jsonText = sb.toString();
			JSONObject res = new JSONObject(jsonText);
			JSONObject info = res.getJSONObject("info");
			int statusCode = info.getInt("statuscode");
			if(statusCode == 0) {
				JSONArray results = res.getJSONArray("results");
				JSONObject resultObject = results.getJSONObject(0);
				JSONArray locations = resultObject.getJSONArray("locations");
				JSONObject location = locations.getJSONObject(0);
				JSONObject latLng = location.getJSONObject("latLng");
				double lat = latLng.getDouble("lat");
				double lng = latLng.getDouble("lng");
				
				JSONObject coordinates = new JSONObject();
				coordinates.put("lat", lat);
				coordinates.put("lng", lng);
				result.put("location", coordinates);
			}
			
		} catch (Exception e) {
			JSONObject error = new JSONObject();
			error.put("message", "Error processing request. Try again after some time");
			result.put("error", error);
		}
		return result;
	}
	
    /**
     * Get the address of a location using its geo-coordinates.
     *
     * @param lat
     *            Latitude of the location.
     * @param lng
     *            Longitude of the location.
     * @return The JSONObject associated with address from geo-coordinates.
     */
	public JSONObject getAddress(String lat, String lng) {

		JSONObject result = new JSONObject();
		
		if(lat == null) {
			JSONObject error = new JSONObject();
			error.put("message", "One or more parameters are missing in request.");
			error.put("id", "MISSING_PARAMETER");
			error.put("field", "lat");
			result.put("error", error);
		} else if(lng == null) {
			JSONObject error = new JSONObject();
			error.put("message", "One or more parameters are missing in request.");
			error.put("id", "MISSING_PARAMETER");
			error.put("field", "lng");
			result.put("error", error);
		} else if(lat.equals("")) {
			JSONObject error = new JSONObject();
			error.put("message", "One or more parameters are invlid in request.");
			error.put("id", "INVALID_PARAMETER");
			error.put("field", "lat");
			result.put("error", error);
		} else if(lng.equals("")) {
			JSONObject error = new JSONObject();
			error.put("message", "One or more parameters are invlid in request.");
			error.put("id", "INVALID_PARAMETER");
			error.put("field", "lng");
			result.put("error", error);
		} else {
			double latd = 0;
			boolean latProblem = false;

			try {
				latd = Double.parseDouble(lat);
			} catch(Exception e){
				latProblem  = true;
			}
			
			if(latProblem) {
				JSONObject error = new JSONObject();
				error.put("message", "One or more parameters are invlid in request.");
				error.put("id", "INVALID_PARAMETER");
				error.put("field", "lat");
				result.put("error", error);
			} else {
				double lngd = 0;
				boolean lngProblem = false;

				try {
					lngd = Double.parseDouble(lng);
				} catch(Exception e){
					lngProblem  = true;
				}
				
				if(lngProblem) {
					JSONObject error = new JSONObject();
					error.put("message", "One or more parameters are invlid in request.");
					error.put("id", "INVALID_PARAMETER");
					error.put("field", "lng");
					result.put("error", error);
				} else {

					String apiUrl = "http://open.mapquestapi.com/geocoding/v1/reverse?key="+mapQuestApiKey+"&location="+latd+","+lngd;
					
					apiUrl = apiUrl.replaceAll(" ", "%20");

					try {
						InputStream is = new URL(apiUrl).openStream();
						BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
						StringBuilder sb = new StringBuilder();
						int cp;
						while ((cp = rd.read()) != -1) {
							sb.append((char) cp);
						}

						String jsonText = sb.toString();
						JSONObject res = new JSONObject(jsonText);
						JSONObject info = res.getJSONObject("info");
						int statusCode = info.getInt("statuscode");
						if(statusCode == 0) {
							JSONArray results = res.getJSONArray("results");
							JSONObject resultObject = results.getJSONObject(0);
							JSONArray locations = resultObject.getJSONArray("locations");
							JSONObject location = locations.getJSONObject(0);
							String street = "", city = "", state = "", country = "", postalCode = "";

							try {
								street = location.getString("street");
							} catch(Exception e) {
							}

							try {
								city = location.getString("adminArea5");
							} catch(Exception e) {
							}

							try {
								state = location.getString("adminArea3");
							} catch(Exception e) {
							}

							try {
								country = location.getString("adminArea1");
							} catch(Exception e) {
							}

							try {
								postalCode = location.getString("postalCode");
							} catch(Exception e) {
							}
							
							JSONObject address = new JSONObject();
							
							address.put("street", street);
							address.put("city", city);
							address.put("state", state);
							address.put("country", country);
							address.put("postalCode", postalCode);
							
							result.put("address", address);
							
							JSONObject providedLocation = new JSONObject();
							
							providedLocation.put("lat", lat);
							providedLocation.put("lng", lng);
							
							result.put("providedLocation", providedLocation);
						}
					} catch (Exception e) {
						JSONObject error = new JSONObject();
						error.put("message", "Error processing request. Try again after some time");
						result.put("error", error);
					}
				}
			}
		}
		return result;
	}
}