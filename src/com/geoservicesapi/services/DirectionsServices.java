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
 * DirectionsServices is a class for consuming directions services using MapQuest Directions API.
 * 
 * @author Saurabh Rane
 * @version 2014-07-24
*/

public class DirectionsServices {
	
	private String mapQuestApiKey;

    /**
     * Construct a DirectionsServices with your mapQuest api key.
     *
     * @param mapQuestApiKey
     *            Your api key for mapQuest api.
     */
	public DirectionsServices(String mapQuestApiKey) {
		this.mapQuestApiKey = mapQuestApiKey;
	}
	
    /**
     * Get the route from a source to a destination.
     *
     * @param source
     *            A key string of the format "lat, lng".
     * @param destination
     *            A key string of the format "lat, lng".
     * @return The JSONObject associated with the route.
     */
	public JSONObject getRoute(String source, String destination) {
		JSONObject result = new JSONObject();
		
		if(source == null) {
			JSONObject error = new JSONObject();
			error.put("message", "One or more parameters are missing in request.");
			error.put("id", "MISSING_PARAMETER");
			error.put("field", "source");
			result.put("error", error);
		} else if(destination == null) {
			JSONObject error = new JSONObject();
			error.put("message", "One or more parameters are missing in request.");
			error.put("id", "MISSING_PARAMETER");
			error.put("field", "destination");
			result.put("error", error);
		} else if(!source.contains(",")) {
			JSONObject error = new JSONObject();
			error.put("message", "One or more parameters are invlid in request.");
			error.put("id", "INVALID_PARAMETER");
			error.put("field", "source");
			result.put("error", error);
		} else if(!destination.contains(",")) {
			JSONObject error = new JSONObject();
			error.put("message", "One or more parameters are invlid in request.");
			error.put("id", "INVALID_PARAMETER");
			error.put("field", "destination");
			result.put("error", error);
		} else {
			String slat = "", slng = "", dlat = "", dlng = "";
			
			String[] src = source.split(",");
			String[] dest = destination.split(",");
			if(!source.endsWith(",") && !source.startsWith(",")) {
				slat = src[0];
				slng = src[1];
			} 
			if(!destination.endsWith(",") && !destination.startsWith(",")) {
				dlat = dest[0];
				dlng = dest[1];
			}
			
			if(slat.trim().equals("") || slng.trim().equals("")) {
				JSONObject error = new JSONObject();
				error.put("message", "One or more parameters are invlid in request.");
				error.put("id", "INVALID_PARAMETER");
				error.put("field", "source");
				result.put("error", error);
			} else if(dlat.trim().equals("") || dlng.trim().equals("")) {
				JSONObject error = new JSONObject();
				error.put("message", "One or more parameters are invlid in request.");
				error.put("id", "INVALID_PARAMETER");
				error.put("field", "destination");
				result.put("error", error);
			} else {

				String apiUrl = "http://open.mapquestapi.com/directions/v2/route?key="+mapQuestApiKey+"&avoids=Toll%20road&from="+slat+","+slng+"&to="+dlat+","+dlng+"&routeType=fastest";
				
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
					
					JSONObject info = (JSONObject) res.getJSONObject("info");
					int statusCode = info.getInt("statuscode");
					if(statusCode == 0) {
						JSONObject route = (JSONObject) res.getJSONObject("route");
						boolean hasTollRoad = route.getBoolean("hasTollRoad");
						boolean hasCountryCross = route.getBoolean("hasCountryCross");
						boolean hasFerry = route.getBoolean("hasFerry");
						double distance = route.getDouble("distance");
						double fuelUsed = route.getDouble("fuelUsed");
						String formattedTime = route.getString("formattedTime");
						
						JSONArray legs = (JSONArray) route.getJSONArray("legs");
						JSONObject steps = new JSONObject();
						steps.put("hasTollRoad", hasTollRoad);
						steps.put("hasCountryCross", hasCountryCross);
						steps.put("hasFerry", hasFerry);
						steps.put("distance", distance);
						steps.put("fuelUsed", fuelUsed);
						steps.put("formattedTime", formattedTime);

						if(legs.length() > 0) {
							JSONObject leg = (JSONObject) legs.get(0);
							JSONArray maneuvers = (JSONArray) leg.getJSONArray("maneuvers");
							JSONArray directions = new JSONArray();
							String[] turnTypes = {"straight","slight right","right","sharp right","reverse","sharp left","left","slight left","right u-turn","left u-turn","right merge","left merge","right on ramp","left on ramp","right off ramp","left off ramp","right fork","left fork","straight fork","take transit","transfer transit","port transit","enter transit","exit transit"};
							for(int i=0; i<maneuvers.length(); i++) {
								JSONObject maneuver = (JSONObject) maneuvers.get(i);
								String narrative = maneuver.getString("narrative");
								String url = "";
								int turnType = maneuver.getInt("turnType");
								String transportMode = maneuver.getString("transportMode");
								if(i!=maneuvers.length()-1) {
									url = maneuver.getString("mapUrl");
								}
								String iconUrl = maneuver.getString("iconUrl");
								double dis = maneuver.getDouble("distance");
								String time = maneuver.getString("formattedTime");
								String directionName = "";
								try {
									directionName = maneuver.getString("directionName");
								} catch(Exception ignore) {
								}

								JSONObject man = new JSONObject();
								man.put("narrative", narrative);
								man.put("url", url);
								man.put("distance", dis);
								man.put("time", time);
								if(turnType == -1) {
									man.put("turnType", "end");
								} else {
									man.put("turnType", turnTypes[turnType]);
								}
								man.put("transportMode", transportMode);
								man.put("direction", directionName);
								man.put("iconUrl",	iconUrl);
								directions.put(i, man);
							}
							steps.put("directions", directions);
						}
						result.put("route", steps);
					}
				} catch (Exception e) {
					JSONObject error = new JSONObject();
					error.put("message", "Error processing request. Try again after some time");
					result.put("error", error);
				}
			}
		}
		return result;
	}
	
    /**
     * Get the mid point of a route from a source to a destination.
     *
     * @param source
     *            A key string of the format (lat, lng).
     * @param destination
     *            A key string of the format (lat, lng).
     * @return The JSONObject associated with the route.
     */
	public JSONObject getMidpoint(String source, String destination) {
				
		JSONObject result = new JSONObject();
		
		if(source == null) {
			JSONObject error = new JSONObject();
			error.put("message", "One or more parameters are missing in request.");
			error.put("id", "MISSING_PARAMETER");
			error.put("field", "source");
			result.put("error", error);
		} else if(destination == null) {
			JSONObject error = new JSONObject();
			error.put("message", "One or more parameters are missing in request.");
			error.put("id", "MISSING_PARAMETER");
			error.put("field", "destination");
			result.put("error", error);
		} else if(!source.contains(",")) {
			JSONObject error = new JSONObject();
			error.put("message", "One or more parameters are invlid in request.");
			error.put("id", "INVALID_PARAMETER");
			error.put("field", "source");
			result.put("error", error);
		} else if(!destination.contains(",")) {
			JSONObject error = new JSONObject();
			error.put("message", "One or more parameters are invlid in request.");
			error.put("id", "INVALID_PARAMETER");
			error.put("field", "destination");
			result.put("error", error);
		} else {
			String slat = "", slng = "", dlat = "", dlng = "";
			
			String[] src = source.split(",");
			String[] dest = destination.split(",");
			if(!source.endsWith(",") && !source.startsWith(",")) {
				slat = src[0];
				slng = src[1];
			} 
			if(!destination.endsWith(",") && !destination.startsWith(",")) {
				dlat = dest[0];
				dlng = dest[1];
			}
			
			if(slat.trim().equals("") || slng.trim().equals("")) {
				JSONObject error = new JSONObject();
				error.put("message", "One or more parameters are invlid in request.");
				error.put("id", "INVALID_PARAMETER");
				error.put("field", "source");
				result.put("error", error);
			} else if(dlat.trim().equals("") || dlng.trim().equals("")) {
				JSONObject error = new JSONObject();
				error.put("message", "One or more parameters are invlid in request.");
				error.put("id", "INVALID_PARAMETER");
				error.put("field", "destination");
				result.put("error", error);
			} else {

				String apiUrl = "http://open.mapquestapi.com/directions/v2/route?unit=k&key="+mapQuestApiKey+"&avoids=Toll%20road&from="+slat+","+slng+"&to="+dlat+","+dlng+"&routeType=fastest";
				
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
					
					JSONObject info = (JSONObject) res.getJSONObject("info");
					int statusCode = info.getInt("statuscode");
					if(statusCode == 0) {
						JSONObject route = (JSONObject) res.getJSONObject("route");
						double distance = route.getDouble("distance");
						double mid = distance/2;
						
						JSONArray legs = (JSONArray) route.getJSONArray("legs");
						
						JSONObject midpoint = new JSONObject();

						if(legs.length()>0) {
							JSONObject leg = (JSONObject) legs.get(0);
							JSONArray maneuvers = (JSONArray) leg.getJSONArray("maneuvers");
							
							double startLat = 0, startLng = 0, endLat = 0, endLng = 0;
							double oldDistance = 0;
							double distanceTillNow = 0;
							
							int index = 0;
							
							for(; index < maneuvers.length() && distanceTillNow < mid; index++) {
								JSONObject maneuver = (JSONObject) maneuvers.get(index);
								double maneuverDistance = maneuver.getDouble("distance");
								
								JSONObject startPoint = maneuver.getJSONObject("startPoint");
								startLat = startPoint.getDouble("lat");
								startLng = startPoint.getDouble("lng");
								
								oldDistance = distanceTillNow;
								distanceTillNow += maneuverDistance;
							}
							
							JSONObject lastManeuver = new JSONObject();
							if(index == maneuvers.length()) {
								lastManeuver = (JSONObject) maneuvers.get(index-1);
							} else {
								lastManeuver = (JSONObject) maneuvers.get(index);
							}
							JSONObject endPoint = lastManeuver.getJSONObject("startPoint");
							endLat = endPoint.getDouble("lat");
							endLng = endPoint.getDouble("lng");

							if (distanceTillNow < mid) {return null;}
							
							double m = (mid-oldDistance)/(distanceTillNow-oldDistance);
							double midLat = startLat + (endLat - startLat)*m;
							double midLng = startLng + (endLng - startLng)*m;
							
							midpoint.put("lat", midLat);
							midpoint.put("lng", midLng);
						}
						result.put("midway", midpoint);
					}
				} catch (Exception e) {
					JSONObject error = new JSONObject();
					error.put("message", "Error processing request. Try again after some time");
					result.put("error", error);
				}
			}
		}
		return result;
	}
}